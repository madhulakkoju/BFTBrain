import itertools
import gbft_pb2_grpc
import gbft_pb2
import logging
import numpy as np
import pandas as pd
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

""" Learning Agent for BFTBrain
This agent learns to select the best BFT protocol and architecture
based on the state of the system.
It uses a reinforcement learning approach with different predictive models. (Random Forest, Neural Network, etc.)
It communicates with the BFTBrain entity via gRPC.

reinforcement learning loop:
1. Wait for state and reward (throughput) from BFTBrain.
2. Update model with new experience (state, action, reward).
3. Decide next action based on current state.
4. Send decision back to BFTBrain.
5. Log data for offline analysis.

ML Model: 
INPUT: state (10 features) + action (protocol, architecture)
OUTPUT: predicted reward (throughput)

State features:
- FAST_PATH_FREQUENCY: Frequency of fast path proposals
- SLOWNESS_OF_PROPOSAL: Slowness of proposal in the system
- REQUEST_SIZE: Size of requests in the system
- RECEIVED_MESSAGE_PER_SLOT: Number of messages received per slot
- HAS_FAST_PATH: Whether the system has a fast path
- HAS_LEADER_ROTATION: Whether the system has leader rotation
- WRITE_RATIO: Ratio of write operations in the system
- EXECUTION_DELAY: Delay in execution of requests
- HOT_KEY_RATIO: Ratio of hot keys in the system
- TRANS_ARRIVAL_RATE: Transaction arrival rate in the system

Reward:
- throughput: Throughput of the system (reward for the agent) (Transactions per second)

Action features:
- action_protocol: Selected BFT protocol (one-hot encoded)
- action_architecture: Selected architecture (one-hot encoded)

Notes:

State Features:
    1. fast_path_frequency
    2. slowness_of_proposal, 
    3. request_size
    4. received_message_per_slot
    5. HAS_FAST_PATH
    6. HAS_LEADER_ROTATION 

will help to determine the best protocol.

State Features 
    1. write_ratio,
    2. execution_delay
    3. hot_key_ratio, 
    4. trans_arrival_rate 
will help to determine the best architecture.

Senerios:

For Workload A, which has high hot key ratio and high execution delay, 
the best performing architecture combination is XOV++ or XOV.

In Workload B, characterized by moderate trans arrival rate, 
high hot key ratio, and moderate write ratio, 
the OXII architecture performs the best.

Workload C, featuring low contention and very high execution delay, 
achieves the highest throughput with either XOV++ or XOV.

For Workload D, which has very high hot key ratio, a high write ratio,
 and low execution delay, the OXII architecture is the most effective choice.

"""


parser = argparse.ArgumentParser(description='Start a learning agent.')
parser.add_argument('--unit', '-u', type=int, required=True, help='Unit id of the corresponding bedrock entity')
parser.add_argument('--port', '-p', type=int, required=True, help='Port of the corresponding bedrock entity')
parser.add_argument('--episodes', '-e', type=int, default=10000, help="Number of episodes to be run [Optional]")
parser.add_argument('--model', '-m', type=str, default="random-forest",
                    help="Predictive model to use [Optional]: random-forest, or neural-network")
parser.add_argument('--num-models', '-n', type=str, default="quadratic",
                    help="Number of models to have [Optional]: multi, single, quadratic, adapt, adapt+, or heuristic")
parser.add_argument('--discard', '-d', type=int, default=1,
                    help="Number of warm up episodes to discard, in order to avoid polluting experience buffer [Optional]")
parser.add_argument('--replay-buffer', '-r', type=int, default=-1,
                    help="Limit of size of replay buffer, less than 1 will mean no limit. [Optional]: -1 as no limit")
parser.add_argument('--multi-onehot', '-o', type=bool, default=False,
                    help="Whether to use one-hot encoding for multi model [Optional]")
parser.add_argument('--epsilon', type=float, default=0.9, 
                    help="Exploration rate for epsilon-greedy strategy [Optional]")
parser.add_argument('--epsilon-decay', type=float, default=0.99, 
                    help="Rate at which epsilon decreases after each episode [Optional]")
parser.add_argument('--min-epsilon', type=float, default=0.2, 
                    help="Minimum value of epsilon [Optional]")
args = parser.parse_args()

request_queue = queue.Queue()
protocol_pool = ["pbft", "cheapbft", "sbft", "prime"]
architecture_pool = ['OX', 'XOV', 'OXII', "XOV++"]

NUM_STATE_FEATURES = 10


def reward_engineering(reward: float) -> float:
    """
    Applies reward engineering to the raw reward value.
    If the reward is less than 1000, it applies a quadratic scaling to amplify small rewards.
    
    """
    if reward < 1000:
        return (reward / 1000) ** 2 * 1000
    return reward


class AgentCommServicer(gbft_pb2_grpc.AgentCommServicer):
    """
    Receives data from the Bedrock entity (via gRPC) and puts it into request_queue.
    """
    def send_data(self, request, context):
        request_queue.put(request)
        return gbft_pb2.google_dot_protobuf_dot_empty__pb2.Empty()


def get_replay_buffer_length(experiences_y):
    if args.replay_buffer <= 0:
        return len(experiences_y)
    return min(args.replay_buffer, len(experiences_y))


def init_single_experience_buffer(experiences_X, experiences_y):
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

        # States: now include two new features
        states = df.loc[:, [
                               'FAST_PATH_FREQUENCY', 'SLOWNESS_OF_PROPOSAL', 'REQUEST_SIZE',
                               'MESSAGE_PER_SLOT', 'HAS_FAST_PATH', 'HAS_LEADER_ROTATION',
                               'WRITE_RATIO',  'EXECUTION_DELAY', 'HOT_KEY_RATIO', 'TRANS_ARRIVAL_RATE'
                           ]].values

        # Actions: protocol (index), architecture (index)
        actions = df.loc[:, ['action_protocol', 'action_architecture']].values

        # One-hot encode protocol and architecture
        protocol_one_hot = np.eye(len(protocol_pool))[actions[:, 0].astype(int)]
        architecture_one_hot = np.eye(len(architecture_pool))[actions[:, 1].astype(int)]
        # Combine both one-hot encodings
        action_features = np.hstack((protocol_one_hot, architecture_one_hot))

        # Rewards
        rewards = df['throughput'].values

        # Concatenate states and action features
        states_actions = np.hstack((states, action_features))

        for i in range(states_actions.shape[0]):
            experiences_X.append(states_actions[i, :])
            experiences_y.append(reward_engineering(rewards[i]))
        logging.debug(f"Checkpoint retrieved:  experiences length: {len(experiences_X)}")


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
        
        # Track predictions for comparison with actual rewards
        self.last_prediction = None
        
        # Add epsilon-greedy parameters (without epsilon_delay)
        self.epsilon = args.epsilon
        self.epsilon_decay = args.epsilon_decay
        self.min_epsilon = args.min_epsilon

        # Generate all possible action combinations: (protocol, architecture)
        self.action_combinations = list(
            itertools.product(protocol_pool, architecture_pool)
        )

        # Build a matrix for all possible actions
        self.actions_matrix = self.create_actions_matrix()

        # Enumeration matrix: first NUM_STATE_FEATURES zeros, then action features
        self.enumeration_matrix = np.hstack(
            (np.zeros((len(self.action_combinations), NUM_STATE_FEATURES)), self.actions_matrix)
        )

        # Initialize experience buffer from disk
        init_single_experience_buffer(self.experiences_X, self.experiences_y)

        # If we have any existing data, train immediately
        if len(self.experiences_y):
            self.train()

    def create_actions_matrix(self):
        """
        Creates a NumPy array containing the features for each possible (protocol, architecture).
        Both protocol and architecture are one-hot encoded.
        """
        action_features = []
        for action in self.action_combinations:
            protocol, architecture = action
            # One-hot protocol
            protocol_one_hot = np.eye(len(protocol_pool))[protocol_pool.index(protocol)]
            # One-hot architecture (changed from index)
            architecture_one_hot = np.eye(len(architecture_pool))[architecture_pool.index(architecture)]
            # Combine protocol + architecture one-hot vectors
            action_feature = np.concatenate((protocol_one_hot, architecture_one_hot))
            action_features.append(action_feature)
        return np.array(action_features)

    def record_reward(self, prev_action, action, reward):
        """
        Log the reward for the previous experience.
        """
        self.experiences_y.append(reward_engineering(reward))

    def record_state_and_action(self, current_action, best_action, state):
        """
        Store (state, best_action) for next reward.
        """
        action_feature = self.get_action_feature(best_action)
        state_action = np.concatenate((state, action_feature))
        self.experiences_X.append(state_action)

    def get_action_feature(self, action):
        """
        Convert action tuple to numeric feature vector.
        Both protocol and architecture are one-hot encoded.
        """
        protocol, architecture = action
        protocol_one_hot = np.eye(len(protocol_pool))[protocol_pool.index(protocol)]
        architecture_one_hot = np.eye(len(architecture_pool))[architecture_pool.index(architecture)]
        return np.concatenate((protocol_one_hot, architecture_one_hot))

    def get_prev_state(self, prev_prev_action, prev_action):
        """
        Returns the previous state portion from experiences_X.
        """
        idx = len(self.experiences_y) - 1
        if idx >= 0:
            return self.experiences_X[idx][:NUM_STATE_FEATURES].tolist()
        else:
            return None

    def train(self):
        """
        Re-trains the random forest on the replay-limited buffer.
        """
        replay_length = get_replay_buffer_length(self.experiences_y)
        bootstrapped_idx = np.random.choice(replay_length, replay_length, replace=True)
        training_X = np.vstack(self.experiences_X)[-replay_length:][bootstrapped_idx, :]
        training_y = np.array(self.experiences_y)[-replay_length:][bootstrapped_idx]
        self.model.fit(training_X, training_y)

    def retrain_and_predict(self, state, prev_action):
        """
        Retrain model, predict for all actions, return best action.
        includes epsilon-greedy exploration.
        """
        training_overhead = 0
        inference_overhead = 0
        self.last_prediction = None  # Reset last prediction

        # Update state portion of enumeration matrix
        self.enumeration_matrix[:, 0:NUM_STATE_FEATURES] = state
        
        # Apply epsilon decay 
        self.epsilon = max(self.min_epsilon, self.epsilon * self.epsilon_decay)
        
        # Epsilon-greedy strategy
        if random.random() < self.epsilon:
            # Exploration: choose random action
            best_action = random.choice(self.action_combinations)
            logging.debug(f"Exploring with epsilon={self.epsilon:.4f}, random action: {best_action}")
        else:
            # Exploitation: choose best action according to model
            if len(self.experiences_y):
                training_start = time.time()
                self.train()
                training_overhead = round(time.time() - training_start, 6)

                inference_start = time.time()
                prediction = self.model.predict(self.enumeration_matrix)
                inference_overhead = round(time.time() - inference_start, 6)

                # Create a list of (prediction, action_index) tuples
                prediction_action_pairs = [(pred, idx) for idx, pred in enumerate(prediction)]
                
                # Sort by prediction value (descending)
                prediction_action_pairs.sort(key=lambda x: x[0], reverse=True)
                
                # Log top 5 predictions
                logging.debug("Top 5 predicted throughput values:")
                for i in range(min(5, len(prediction_action_pairs))):
                    pred, action_idx = prediction_action_pairs[i]
                    action = self.action_combinations[action_idx]
                    logging.debug(f"  {i+1}. Action: {action}, Predicted throughput: {pred:.2f} transactions/sec")

                best_idx = np.random.choice(
                    np.flatnonzero(np.isclose(prediction, prediction.max())),
                    replace=True
                )
                best_action = self.action_combinations[best_idx]
                self.last_prediction = prediction[best_idx]  # Store the prediction for the chosen action
                logging.debug(f"Exploiting with epsilon={self.epsilon:.4f}, best action: {best_action}, predicted throughput: {self.last_prediction:.2f} transactions/sec")
            else:
                best_action = random.choice(self.action_combinations)
                logging.debug(f"No data yet, choosing random action: {best_action}")

        return best_action, training_overhead, inference_overhead


def run_agent(agent_stub):
    """
    Main agent loop that handles the RL workflow:
      - Wait for state & reward from BFTBrain
      - Update model with new experience
      - Decide next action based on current state
      - Send decision back to BFTBrain
      - Log data for analysis
      
    Args:
        agent_stub: gRPC stub for communicating with the BFTBrain entity
    """
    # Store history of actions for reward assignment
    actions = []
    # Store performance metrics for model training/inference
    time_records = []
    # Store prediction history
    prediction_history = []

    # Initialize the appropriate model based on command line arguments
    if args.model == "random-forest":
        logging.debug("Random Forest Model Initialized")
        model = SingleRF()
    elif args.model == "neural-network":
        logging.error('Neural network model not implemented.')
        return

    # Set up CSV logging for offline analysis
    folder_name = "data/"
    os.makedirs(folder_name, exist_ok=True)
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    filename = f"{folder_name}{timestamp}_u{args.unit}.csv"
    data_store = open(filename, 'w', newline='')
    csv_writer = csv.writer(data_store)
    
    # CSV header contains state features, actions, and performance metrics
    csv_writer.writerow([
        'FAST_PATH_FREQUENCY', 'SLOWNESS_OF_PROPOSAL', 'REQUEST_SIZE', 'MESSAGE_PER_SLOT',
        'HAS_FAST_PATH', 'HAS_LEADER_ROTATION', 'WRITE_RATIO', 'EXECUTION_DELAY',
        'HOT_KEY_RATIO', 'TRANS_ARRIVAL_RATE',
        'previous_action_protocol', 'previous_action_architecture',
        'action_protocol', 'action_architecture',
        'throughput', 'training_overhead(s)', 'inference_overhead(s)',
        'predicted_throughput', 'prediction_error'
    ])
    logging.debug('Learning agent has been initialized.')

    # Main learning loop
    for episode in range(args.episodes):
        # Wait for state and reward from BFTBrain
        request = request_queue.get()
        data = request.report

        # Extract current state with all 10 features from environment data
        state = np.array([
            data[FAST_PATH_FREQUENCY], data[SLOWNESS_OF_PROPOSAL], data[REQUEST_SIZE],
            data[RECEIVED_MESSAGE_PER_SLOT], data[HAS_FAST_PATH], data[HAS_LEADER_ROTATION],
            data[WRITE_RATIO], data[EXECUTION_DELAY], data[HOT_KEY_RATIO], data[TRANS_ARRIVAL_RATE]
        ])

        current_action = (request.next_protocol, request.next_architecture)

        # logging.debug(f"Episode {episode}: Got state={state}, current_action={current_action}")
        logging.debug(f"Episode {episode}: current_action={current_action}")

        # Record reward for previous step
        if len(actions) > 1:
            prev_action, action = actions.pop(0)
            time_record = time_records.pop(0)
            predicted_reward = prediction_history.pop(0) if prediction_history else None
            
            # Log the actual reward and compare with prediction if available
            actual_reward = data[REWARD]
            model.record_reward(prev_action, action, actual_reward)
            
            if predicted_reward is not None:
                prediction_error = actual_reward - predicted_reward
                error_percentage = (prediction_error / actual_reward * 100) if actual_reward > 0 else float('inf')
                logging.info(f"Reward comparison - Predicted: {predicted_reward:.2f}, Actual: {actual_reward:.2f}, " 
                             f"Error: {prediction_error:.2f} ({error_percentage:.2f}%)")
            else:
                prediction_error = None
                logging.debug(f"Actual reward: {actual_reward:.2f} (no prediction available)")

            # Log to CSV
            prev_row = model.get_prev_state(prev_action, action)
            if prev_row is not None:
                formatted = [round(val,4) if isinstance(val, float) else val for val in prev_row]
                row = (formatted + list(prev_action) + list(action) +
                       [round(actual_reward,4), round(time_record[0],6), round(time_record[1],6)])
                
                # Add prediction and error data if available
                if predicted_reward is not None:
                    row.extend([round(predicted_reward,4), round(prediction_error,4)])
                else:
                    row.extend([None, None])
                    
                csv_writer.writerow(row)

        # Warm-up discard episodes
        if episode < args.discard - 1:
            learningdata = gbft_pb2.LearningData(next_protocol="repeat", next_architecture="repeat")
            logging.debug("Warmup episode, repeating current bedrock action.")
            agent_stub.send_decision(learningdata)
            continue

        # Retrain & predict best action
        best_action, training_overhead, inference_overhead = model.retrain_and_predict(state, current_action)
        
        # Store prediction for future comparison
        prediction_history.append(model.last_prediction)

        actions.append((current_action, best_action))
        time_records.append([training_overhead, inference_overhead])
        model.record_state_and_action(current_action, best_action, state)

        learningdata = gbft_pb2.LearningData(next_protocol=best_action[0], next_architecture=best_action[1])
        logging.debug("Sending best_action decision back: %s", learningdata)
        agent_stub.send_decision(learningdata)
        data_store.flush()


if __name__ == '__main__':
    LOG_FORMAT = '%(asctime)s - %(levelname)s - %(funcName)s:%(lineno)d - %(message)s'
    
    # Create a logger
    logger = logging.getLogger()
    logger.setLevel(logging.DEBUG)
    
    # Create logs directory if it doesn't exist
    logs_dir = "logs/"
    os.makedirs(logs_dir, exist_ok=True)
    
    # Create timestamped log filename
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    log_filename = f"{logs_dir}{timestamp}_u{args.unit}.log"
    
    # Create file handler for logging to file
    file_handler = logging.FileHandler(log_filename)
    file_handler.setLevel(logging.INFO)
    file_handler.setFormatter(logging.Formatter(LOG_FORMAT))
    
    # Create console handler for logging to stdout
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    console_handler.setFormatter(logging.Formatter(LOG_FORMAT))
    
    # Add both handlers to the logger
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)
    
    # Set up root logger
    logging.basicConfig(level=logging.INFO, format=LOG_FORMAT, handlers=[])

    random.seed(0)
    np.random.seed(0)

    bedrockRPCPort = args.port + 10
    agentPort = args.port + 20
    server_address = f'[::]:{agentPort}'
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    gbft_pb2_grpc.add_AgentCommServicer_to_server(AgentCommServicer(), server)
    server.add_insecure_port(server_address)
    server.start()
    logging.debug('gRPC server running at %s.', server_address)

    try:
        entity_channel = grpc.insecure_channel(f'localhost:{bedrockRPCPort}')
        agent_stub = gbft_pb2_grpc.EntityCommStub(entity_channel)
        run_agent(agent_stub)
    finally:
        entity_channel.close()
