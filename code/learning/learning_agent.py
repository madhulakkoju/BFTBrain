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
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
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
protocol_pool = ["pbft", "zyzzyva", "cheapbft", "sbft", "hotstuff", "prime"]
blocksize_options = [1, 10, 20, 50, 100, 200, 500]
early_execution_options = [False, True]
reorder_options = [False, True]


def reward_engineering(reward: float) -> float:
    if reward < 1000:
        return (reward / 1000) ** 2 * 1000
    return reward


class AgentCommServicer(gbft_pb2_grpc.AgentCommServicer):

    def send_data(self, request, context):
        request_queue.put(request)
        return gbft_pb2.google_dot_protobuf_dot_empty__pb2.Empty()


def get_replay_buffer_length(experiences_y):
    if args.replay_buffer <= 0:
        return len(experiences_y)
    return min(args.replay_buffer, len(experiences_y))


def init_single_experience_buffer(experiences_X, experiences_y, actions_list):
    # Read all csv files in the checkpoint folder in sequence according to their timestamp
    folder_path = "checkpoint/"
    files = sorted([f for f in os.listdir(folder_path) if f.endswith(".csv")])

    # Initialize the experience buffer (the order of training data is preserved)
    for file in files:
        df = pd.read_csv(folder_path + file)
        df.rename(columns=str.strip, inplace=True)
        # Map protocol names to indices
        df['action_protocol'] = df['action_protocol'].apply(lambda a: protocol_pool.index(str.strip(a)))
        # Map early_execution and reorder to integers
        df['action_early_execution'] = df['action_early_execution'].astype(int)
        df['action_reorder'] = df['action_reorder'].astype(int)
        # States
        states = df.loc[:, [
                               'FAST_PATH_FREQUENCY', 'SLOWNESS_OF_PROPOSAL', 'REQUEST_SIZE',
                               'MESSAGE_PER_SLOT', 'HAS_FAST_PATH', 'HAS_LEADER_ROTATION',
                               'WRITE_RATIO', 'HOT_KEY_RATIO', 'TRANS_ARRIVAL_RATE', 'EXECUTION_DELAY'
                           ]].values
        # Actions
        actions = df.loc[:, ['action_protocol', 'action_blocksize', 'action_early_execution', 'action_reorder']].values
        # One-hot encode protocols
        protocol_one_hot = np.eye(len(protocol_pool))[actions[:, 0].astype(int)]
        # Combine action features
        action_features = np.hstack((protocol_one_hot, actions[:, 1:]))
        # Rewards
        rewards = df['throughput'].values
        # experiences_X = (state, action)
        states_actions = np.hstack((states, action_features))
        experiences_X.extend([states_actions[i, :] for i in range(states_actions.shape[0])])
        experiences_y.extend([reward_engineering(reward) for reward in rewards])


class SingleRF:

    def __init__(self):
        self.experiences_X = []
        self.experiences_y = []
        self.model = RandomForestRegressor(max_depth=20)
        # Generate all possible action combinations
        self.action_combinations = list(itertools.product(
            protocol_pool,
            blocksize_options,
            early_execution_options,
            reorder_options
        ))
        # Create action feature matrix
        self.actions_matrix = self.create_actions_matrix()
        # Initialize enumeration matrix (will be updated with current state later)
        self.enumeration_matrix = np.hstack((np.zeros((len(self.action_combinations), 10)), self.actions_matrix))
        # Init experience buffer
        init_single_experience_buffer(self.experiences_X, self.experiences_y, self.action_combinations)
        # Init model
        if len(self.experiences_y):
            self.train()

    def create_actions_matrix(self):
        action_features = []
        for action in self.action_combinations:
            protocol, blocksize, early_execution, reorder = action
            # One-hot encode protocol
            protocol_one_hot = np.eye(len(protocol_pool))[protocol_pool.index(protocol)]
            # Normalize blocksize (optional, can also use raw value)
            blocksize_value = blocksize  # Or normalize as needed
            early_execution_value = int(early_execution)
            reorder_value = int(reorder)
            # Combine all action features
            action_feature = np.concatenate((
                protocol_one_hot,
                [blocksize_value, early_execution_value, reorder_value]
            ))
            action_features.append(action_feature)
        return np.array(action_features)

    def record_reward(self, prev_action, action, reward):
        self.experiences_y.append(reward_engineering(reward))

    def record_state_and_action(self, current_action, best_action, state):
        # Get action features for best_action
        action_feature = self.get_action_feature(best_action)
        # Combine state and action
        state_action = np.concatenate((state, action_feature))
        self.experiences_X.append(state_action)

    def get_action_feature(self, action):
        protocol, blocksize, early_execution, reorder = action
        # One-hot encode protocol
        protocol_one_hot = np.eye(len(protocol_pool))[protocol_pool.index(protocol)]
        # Convert action features
        blocksize_value = blocksize  # Or normalize as needed
        early_execution_value = int(early_execution)
        reorder_value = int(reorder)
        # Combine all action features
        action_feature = np.concatenate((
            protocol_one_hot,
            [blocksize_value, early_execution_value, reorder_value]
        ))
        return action_feature

    def get_prev_state(self, prev_prev_action, prev_action):
        idx = len(self.experiences_y) - 1
        if idx >= 0:
            return self.experiences_X[idx][:10].tolist()
        else:
            return None

    def train(self):
        replay_length = get_replay_buffer_length(self.experiences_y)
        bootstrapped_idx = np.random.choice(replay_length, replay_length, replace=True)
        training_X = np.vstack(self.experiences_X)[-replay_length:][bootstrapped_idx, :]
        training_y = np.array(self.experiences_y)[-replay_length:][bootstrapped_idx]
        self.model.fit(training_X, training_y)

    def retrain_and_predict(self, state, prev_action):
        training_overhead = 0
        inference_overhead = 0
        # Update the state part of enumeration matrix
        self.enumeration_matrix[:, 0:10] = state
        if len(self.experiences_y):
            training_start = time.time()
            # Retrain the model if there is any training data
            self.train()
            training_overhead += round(time.time() - training_start, 6)
            # Inference
            inference_start = time.time()
            prediction = self.model.predict(self.enumeration_matrix)
            inference_overhead += round(time.time() - inference_start, 6)
            # Find the best action
            best_idx = np.random.choice(np.flatnonzero(np.isclose(prediction, prediction.max())), replace=True)
            best_action = self.action_combinations[best_idx]
        else:
            # Choose a random action if there is no training data
            best_action = random.choice(self.action_combinations)

        return best_action, training_overhead, inference_overhead


def run_agent(agent_stub):
    # Init
    actions = []
    time_records = []
    if args.model == "random-forest":
        model = SingleRF()
    elif args.model == "neural-network":
        logging.error('neural network is not implemented yet')
        return

    # Create the csv file for debugging and offline training
    folder_name = "data/"
    if not os.path.exists(folder_name):
        os.makedirs(folder_name)
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    data_store = open(folder_name + timestamp + " u" + str(args.unit) + ".csv", 'w')
    csv_writer = csv.writer(data_store)
    csv_writer.writerow(
        ['FAST_PATH_FREQUENCY', 'SLOWNESS_OF_PROPOSAL', 'REQUEST_SIZE', 'MESSAGE_PER_SLOT', 'HAS_FAST_PATH',
         'HAS_LEADER_ROTATION',
         'WRITE_RATIO', 'HOT_KEY_RATIO', 'TRANS_ARRIVAL_RATE', 'EXECUTION_DELAY',
         'previous_action_protocol', 'previous_action_blocksize', 'previous_action_early_execution',
         'previous_action_reorder',
         'action_protocol', 'action_blocksize', 'action_early_execution', 'action_reorder',
         'throughput', 'training_overhead(s)', 'inference_overhead(s)'])
    logging.info('learning agent has been initialized.')

    for episode in range(args.episodes):
        # Wait for the notification from entity
        request = request_queue.get()
        data = request.report
        state = np.array([
            data[FAST_PATH_FREQUENCY],
            data[SLOWNESS_OF_PROPOSAL],
            data[REQUEST_SIZE],
            data[RECEIVED_MESSAGE_PER_SLOT],
            data[HAS_FAST_PATH],
            data[HAS_LEADER_ROTATION],
            data[WRITE_RATIO],
            data[HOT_KEY_RATIO],
            data[TRANS_ARRIVAL_RATE],
            data[EXECUTION_DELAY]
        ])
        current_action = (
            request.next_protocol,
            request.next_blocksize,
            request.next_early_execution,
            request.next_reorder
        )
        logging.info('episode %d: received learning request from bedrock entity, state=%s, current_action=%s', episode,
                     state, current_action)

        # Record the reward for the previous episodes
        if len(actions) > 1:
            prev_action, action = actions.pop(0)
            time_record = time_records.pop(0)
            model.record_reward(prev_action, action, data[REWARD])
            prev_row = model.get_prev_state(prev_action, action)
            if prev_row is not None:
                prev_row = prev_row + list(prev_action) + list(action) + [data[REWARD]] + time_record
                csv_writer.writerow(prev_row)

        # Discard warm up episodes
        if episode < args.discard - 1:
            # Notify the entity to repeat current default action
            agent_stub.send_decision(gbft_pb2.LearningData(next_protocol="repeat"))
            continue

        # Retrain and inference
        best_action, training_overhead, inference_overhead = model.retrain_and_predict(state, current_action)

        # Record the state and action for the next episode
        actions.append((current_action, best_action))
        time_records.append([training_overhead, inference_overhead])
        model.record_state_and_action(current_action, best_action, state)

        # Send back decision to the entity
        agent_stub.send_decision(gbft_pb2.LearningData(
            next_protocol=best_action[0],
            next_blocksize=best_action[1],
            next_early_execution=best_action[2],
            next_reorder=best_action[3]
        ))

        data_store.flush()


if __name__ == '__main__':
    LOG_FORMAT = '%(asctime)s - %(levelname)s - %(funcName)s:%(lineno)d - %(message)s'
    logging.basicConfig(level=logging.DEBUG, format=LOG_FORMAT)

    # Set the same random seed for each peer
    random.seed(0)
    np.random.seed(0)

    # Start the grpc server and client
    bedrockRPCPort = args.port + 10
    agentPort = args.port + 20
    server_address = '[::]:{}'.format(agentPort)
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    gbft_pb2_grpc.add_AgentCommServicer_to_server(AgentCommServicer(), server)
    server.add_insecure_port(server_address)
    server.start()
    logging.info('grpc server running at %s.', server_address)

    try:
        entity_channel = grpc.insecure_channel('localhost:9031')
        agent_stub = gbft_pb2_grpc.EntityCommStub(entity_channel)
        run_agent(agent_stub)
    finally:
        entity_channel.close()
