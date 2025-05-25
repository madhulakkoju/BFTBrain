import itertools
import gbft_pb2_grpc
import gbft_pb2
import logging
import numpy as np
import pandas as pd
import sklearn
import argparse
import grpc
import queue
import random
import time
import csv
import os
from datetime import datetime
from concurrent import futures
from threading import Condition, Lock
from sklearn.ensemble import RandomForestRegressor
from constants import *

parser = argparse.ArgumentParser(description='Start a learning agent.')
parser.add_argument('--unit', '-u', type=int, required=True, help='Unit id of the corresponding bedrock entity')
parser.add_argument('--port', '-p', type=int, required=True, help='Port of the corresponding bedrock entity')
parser.add_argument('--episodes', '-e', type=int, default=10000, help="Number of episodes to be run [Optional]")
parser.add_argument('--model', '-m', type=str, default="random-forest",
                    help="Predictive model to use [Optional]: random-forest, or neural-network")
parser.add_argument('--num-models', '-n', type=str, default="quadratic",
                    help="Number of models to have [Optional]: multi, single, quadratic, adapt, adapt+, or heuristic")
parser.add_argument('--discard', '-d', type=int, default=5,
                    help="Number of warm up episodes to discard, in order to avoid polluting experience buffer [Optional]")
parser.add_argument('--replay-buffer', '-r', type=int, default=-1,
                    help="Limit of size of replay buffer, less than 1 will mean no limit. [Optional]: -1 as no limit")
parser.add_argument('--multi-onehot', '-o', type=bool, default=False,
                    help="Whether to use one-hot encoding for multi model [Optional]")
args = parser.parse_args()

request_queue = queue.Queue()
protocol_pool = ["pbft", "cheapbft", "sbft", "prime"]
architecture_pool = ['OX', 'XOV', 'OXII', "XOV++"]



def reward_engineering(reward: float) -> float:
    """
    Sample shaping function, can be modified as needed.
    """
    if reward < 1000:
        return (reward / 1000) ** 2 * 1000
    return reward


class AgentCommServicer(gbft_pb2_grpc.AgentCommServicer):
    """
    Receives data from the Bedrock entity (via gRPC) and puts it into request_queue.
    """
    def send_data(self, request, context):
        logging.info(f"Received data from BFTBrain UNIT {request}")
        request_queue.put(request)
        return gbft_pb2.google_dot_protobuf_dot_empty__pb2.Empty()


def get_replay_buffer_length(experiences_y):
    if args.replay_buffer <= 0:
        return len(experiences_y)
    return min(args.replay_buffer, len(experiences_y))


def init_single_experience_buffer(experiences_X, experiences_y, actions_list):
    """
    Loads any prior CSV checkpoints from the 'checkpoint/' folder,
    and populates experiences_X, experiences_y with them.
    """
    folder_path = "checkpoint/"
    if not os.path.exists(folder_path):
        return

    files = sorted([f for f in os.listdir(folder_path) if f.endswith(".csv")])

    for file in files:
        df = pd.read_csv(folder_path + file)
        df.rename(columns=str.strip, inplace=True)

        # Map protocol names to indices
        df['action_protocol'] = df['action_protocol'].apply(
            lambda a: protocol_pool.index(str.strip(a))
        )

        # Map architecture names to indices
        df['action_architecture'] = df['action_architecture'].apply(
            lambda a: architecture_pool.index(str.strip(a))
        )

        # States
        states = df.loc[:, [
            'FAST_PATH_FREQUENCY', 'SLOWNESS_OF_PROPOSAL', 'REQUEST_SIZE',
            'MESSAGE_PER_SLOT', 'HAS_FAST_PATH', 'HAS_LEADER_ROTATION',
            'WRITE_RATIO', 'EXECUTION_DELAY'
        ]].values

        # Actions: protocol (index), architecture (index)
        actions = df.loc[:, ['action_protocol', 'action_architecture']].values

        # One-hot encode protocol
        protocol_one_hot = np.eye(len(protocol_pool))[actions[:, 0].astype(int)]
        # Remaining columns: architecture
        action_features = np.hstack((protocol_one_hot, actions[:, 1:]))

        # Rewards
        rewards = df['throughput'].values

        # Concatenate states and action features
        states_actions = np.hstack((states, action_features))

        for i in range(states_actions.shape[0]):
            experiences_X.append(states_actions[i, :])
            experiences_y.append(reward_engineering(rewards[i]))
        logging.info(f" Checkpoint retrieved:  experiences length: {len(experiences_X)}")



class SingleRF:
    """
    A simple single-model approach using RandomForestRegressor.
    It enumerates all (protocol, architecture) combos,
    picks the best predicted reward each time.
    """

    def __init__(self):
        self.experiences_X = []
        self.experiences_y = []
        self.model = RandomForestRegressor()

        # Generate all possible action combinations: (protocol, architecture)

        self.action_combinations = list(
            itertools.product(protocol_pool, architecture_pool)
        )

        # Build a matrix for all possible actions
        self.actions_matrix = self.create_actions_matrix()

        # We'll keep a big enumeration matrix for inference: shape(#actions, 10 + #action_features)
        # The first 10 columns will be replaced each time by the current state.
        self.enumeration_matrix = np.hstack(
            (np.zeros((len(self.action_combinations), 8)), self.actions_matrix)
        )

        # Initialize experience buffer from disk
        init_single_experience_buffer(self.experiences_X, self.experiences_y, self.action_combinations)

        # If we have any existing data, train immediately
        if len(self.experiences_y):
            self.train()

    def create_actions_matrix(self):
        """
        Creates a NumPy array containing the features for each possible (protocol, architecture).
        Protocol is one-hot, architecture is an index.
        """
        action_features = []
        for action in self.action_combinations:
            protocol, architecture = action
            # One-hot protocol
            protocol_one_hot = np.eye(len(protocol_pool))[protocol_pool.index(protocol)]
            # Architecture index
            architecture_idx = architecture_pool.index(architecture)
            # Combine protocol + architecture
            action_feature = np.concatenate((protocol_one_hot, [architecture_idx]))
            action_features.append(action_feature)
        return np.array(action_features)

    def record_reward(self, prev_action, action, reward):
        """
        Log the reward for the previous experience. The actual (state+action) was appended
        in record_state_and_action(...). We only need to append the reward in the same order.
        """
        self.experiences_y.append(reward_engineering(reward))

    def record_state_and_action(self, current_action, best_action, state):
        """
        The agent has observed `state`, was *currently* using `current_action`
        and has decided `best_action`. We store (state, best_action) for the
        next time we see the result (reward).
        """
        action_feature = self.get_action_feature(best_action)
        state_action = np.concatenate((state, action_feature))
        self.experiences_X.append(state_action)

    def get_action_feature(self, action):
        """
        Convert a triple (protocol, architecture) into the numeric feature vector
        (one-hot for protocol, index for architecture).
        """
        protocol, architecture = action
        protocol_one_hot = np.eye(len(protocol_pool))[protocol_pool.index(protocol)]
        arch_idx = architecture_pool.index(architecture)
        return np.concatenate((protocol_one_hot, [arch_idx]))

    def get_prev_state(self, prev_prev_action, prev_action):
        """
        Returns just the state portion [0..9] from the last experience if it exists.
        """
        idx = len(self.experiences_y) - 1
        if idx >= 0:
            # The first 10 columns in experiences_X are the state features
            return self.experiences_X[idx][:8].tolist()
        else:
            return None

    def train(self):
        """
        Re-trains the random forest on the entire (or replay-limited) buffer.
        Uses bootstrap sampling from experiences_X, experiences_y.
        """
        replay_length = get_replay_buffer_length(self.experiences_y)
        bootstrapped_idx = np.random.choice(replay_length, replay_length, replace=True)
        training_X = np.vstack(self.experiences_X)[-replay_length:][bootstrapped_idx, :]
        training_y = np.array(self.experiences_y)[-replay_length:][bootstrapped_idx]
        self.model.fit(training_X, training_y)

    def retrain_and_predict(self, state, prev_action):
        """
        1) Retrain if we have any experience data
        2) Predict reward for each possible action in self.action_combinations
           by filling in the `state` portion of self.enumeration_matrix
        3) Pick the best action
        4) Return best_action, training_overhead, inference_overhead
        """
        training_overhead = 0
        inference_overhead = 0

        # Update the state part of enumeration matrix (10 features of state)
        self.enumeration_matrix[:, 0:8] = state

        if len(self.experiences_y):
            # Retrain
            training_start = time.time()
            self.train()
            training_overhead = round(time.time() - training_start, 6)

            # Inference
            inference_start = time.time()
            prediction = self.model.predict(self.enumeration_matrix)
            inference_overhead = round(time.time() - inference_start, 6)

            # Pick best
            best_idx = np.random.choice(
                np.flatnonzero(np.isclose(prediction, prediction.max())),
                replace=True
            )
            best_action = self.action_combinations[best_idx]
        else:
            # No data yet => pick random
            best_action = random.choice(self.action_combinations)

        return best_action, training_overhead, inference_overhead


def run_agent(agent_stub):
    """
    Main loop:
      - Wait for state & reward from bedrock entity
      - Update the model with the old reward
      - Decide on the next action
      - Send decision back to entity
      - Log data to CSV
    """
    actions = []
    time_records = []

    if args.model == "random-forest":
        logging.info("Random Forest Model Initialized")
        model = SingleRF()
    elif args.model == "neural-network":
        logging.error('Neural network model not implemented.')
        return

    # Create the csv file for offline training/analysis
    folder_name = "data/"
    if not os.path.exists(folder_name):
        os.makedirs(folder_name)

    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    data_store = open(folder_name + timestamp + "_u" + str(args.unit) + ".csv", 'w', newline='')
    csv_writer = csv.writer(data_store)
    csv_writer.writerow([
        'FAST_PATH_FREQUENCY', 'SLOWNESS_OF_PROPOSAL', 'REQUEST_SIZE', 'MESSAGE_PER_SLOT', 'HAS_FAST_PATH',
        'HAS_LEADER_ROTATION', 'WRITE_RATIO',  'EXECUTION_DELAY',
        'previous_action_protocol', 'previous_action_architecture',
        'action_protocol', 'action_architecture',
        'throughput', 'training_overhead(s)', 'inference_overhead(s)'
    ])
    logging.info('Learning agent has been initialized.')

    for episode in range(args.episodes):
        # Wait for data from bedrock entity
        request = request_queue.get()
        data = request.report

        # Current state (8 features)
        state = np.array([
            data[FAST_PATH_FREQUENCY],
            data[SLOWNESS_OF_PROPOSAL],
            data[REQUEST_SIZE],
            data[RECEIVED_MESSAGE_PER_SLOT],
            data[HAS_FAST_PATH],
            data[HAS_LEADER_ROTATION],
            data[WRITE_RATIO],
            data[EXECUTION_DELAY]
        ])

        # Current action used by the bedrock entity
        current_action = (
            request.next_protocol,
            request.next_architecture
        )

        logging.info(f"Episode {episode}: Got state= {state}, current_action={current_action}")

        # Record the reward from the previous step
        if len(actions) > 1:
            prev_action, action = actions.pop(0)
            time_record = time_records.pop(0)
            model.record_reward(prev_action, action, data[REWARD])

            # Also log to CSV
            prev_row = model.get_prev_state(prev_action, action)
            if prev_row is not None:
                # Format floating point values to 4 decimal places to avoid excessive precision
                formatted_row = []
                for val in prev_row:
                    if isinstance(val, float):
                        formatted_row.append(round(val, 4))
                    else:
                        formatted_row.append(val)
                
                # Append [prev_action_protocol, ..., action_protocol, ..., throughput, overheads...]
                row = (formatted_row
                       + list(prev_action)
                       + list(action)
                       + [round(data[REWARD], 4), round(time_record[0], 6), round(time_record[1], 6)])
                csv_writer.writerow(row)

        # Discard warm-up episodes
        if episode < args.discard - 1:
            # Tell bedrock entity to repeat its default action during warm-up
            learningdata = gbft_pb2.LearningData(
                next_protocol="repeat",
                next_architecture="repeat"
            )
            logging.info("Warmup episode, repeating current bedrock action.")
            agent_stub.send_decision(learningdata)
            continue

        # Retrain & pick best action
        best_action, training_overhead, inference_overhead = model.retrain_and_predict(
            state, current_action
        )

        # Prepare for next iteration (the model will need to see how the *best_action* performs)
        actions.append((current_action, best_action))
        time_records.append([training_overhead, inference_overhead])
        model.record_state_and_action(current_action, best_action, state)

        # The chosen best_action is a triple: (protocol, architecture)
        learningdata = gbft_pb2.LearningData(
            next_protocol=best_action[0],
            next_architecture=best_action[1]
        )

        logging.info("Sending best_action decision back: %s", learningdata)
        agent_stub.send_decision(learningdata)
        data_store.flush()


if __name__ == '__main__':
    LOG_FORMAT = '%(asctime)s - %(levelname)s - %(funcName)s:%(lineno)d - %(message)s'
    logging.basicConfig(level=logging.DEBUG, format=LOG_FORMAT)

    # Set the same random seed for each peer
    random.seed(0)
    np.random.seed(0)

    # Start gRPC server
    bedrockRPCPort = args.port + 10
    agentPort = args.port + 20
    server_address = '[::]:{}'.format(agentPort)
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    gbft_pb2_grpc.add_AgentCommServicer_to_server(AgentCommServicer(), server)
    server.add_insecure_port(server_address)
    server.start()
    logging.info('gRPC server running at %s.', server_address)

    try:
        # Connect to the bedrock entity
        entity_channel = grpc.insecure_channel('localhost:{}'.format(bedrockRPCPort))
        agent_stub = gbft_pb2_grpc.EntityCommStub(entity_channel)

        # Main agent loop
        run_agent(agent_stub)
    finally:
        entity_channel.close()
