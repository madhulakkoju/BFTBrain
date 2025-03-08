import gbft_pb2_grpc
import gbft_pb2
import logging
import grpc
import threading
import time
import random
from concurrent import futures
from constants import *

class EntityCommServicer(gbft_pb2_grpc.EntityCommServicer):
    def __init__(self, agent_stub):
        self.agent_stub = agent_stub
        self.next_protocol = "pbft"  # Initial protocol
        self.next_blocksize = 10     # Initial blocksize
        self.next_early_execution = False  # Initial early execution flag
        self.next_reorder = False    # Initial reorder flag
        self.stop_event = threading.Event()
        self.thread = threading.Thread(target=self.run)

    def start(self):
        self.thread.start()

    def run(self):
        episode = 0
        while not self.stop_event.is_set() and episode < 100:  # Run for 100 episodes
            # Simulate collecting metrics
            report = {
                FAST_PATH_FREQUENCY: random.uniform(0, 1),
                SLOWNESS_OF_PROPOSAL: random.uniform(0, 1),
                REQUEST_SIZE: random.randint(100, 1000),
                RECEIVED_MESSAGE_PER_SLOT: random.randint(1, 10),
                HAS_FAST_PATH: random.choice([0, 1]),
                HAS_LEADER_ROTATION: random.choice([0, 1]),
                REWARD: random.uniform(500, 1500),
                WRITE_RATIO: random.uniform(0,1),
                HOT_KEY_RATIO: random.uniform(0,0.1),
                TRANS_ARRIVAL_RATE: random.uniform(0,2000),
                EXECUTION_DELAY: random.uniform(1000,1500)
            }
            # Create a LearningData message
            learning_data = gbft_pb2.LearningData(
                report=report,
                next_protocol=self.next_protocol,
                next_blocksize=self.next_blocksize,
                next_early_execution=self.next_early_execution,
                next_reorder=self.next_reorder
            )
            # Send metrics to the agent
            logging.info("Sending metrics to agent: %s", report)
            self.agent_stub.send_data(learning_data)

            # Wait for agent's decision (simulate processing time)
            time.sleep(1)

            # In a real scenario, the agent would call send_decision on this server
            # Since we are simulating, we wait for the agent's decision in the method below
            episode += 1

    def send_decision(self, request, context):
        # Receive the next action from the agent
        self.next_protocol = request.next_protocol
        self.next_blocksize = request.next_blocksize
        self.next_early_execution = request.next_early_execution
        self.next_reorder = request.next_reorder
        logging.info("Received next actions from agent: protocol=%s, blocksize=%s, early_execution=%s, reorder=%s",
                     self.next_protocol, self.next_blocksize, self.next_early_execution, self.next_reorder)
        return gbft_pb2.google_dot_protobuf_dot_empty__pb2.Empty()

    def stop(self):
        self.stop_event.set()
        self.thread.join()

def serve():
    logging.basicConfig(level=logging.DEBUG)
    # Connect to the agent's gRPC server
    import argparse

    parser = argparse.ArgumentParser(description='Start a dummy bedrock entity server.')
    parser.add_argument('--port', type=int, required=True, help='Base port number to match the agent\'s port')
    args = parser.parse_args()

    bedrockRPCPort = args.port + 10
    agentPort = args.port + 20

    # Connect to the agent's gRPC server
    agent_channel = grpc.insecure_channel('localhost:{}'.format(agentPort))
    agent_stub = gbft_pb2_grpc.AgentCommStub(agent_channel)

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    entity_servicer = EntityCommServicer(agent_stub)
    gbft_pb2_grpc.add_EntityCommServicer_to_server(entity_servicer, server)

    # Start the dummy bedrock entity server
    server_address = '[::]:{}'.format(bedrockRPCPort)

    server.add_insecure_port(server_address)
    server.start()
    logging.info('Dummy bedrock entity server running at %s.', server_address)

    # Start the entity's main loop
    entity_servicer.start()

    try:
        while True:
            time.sleep(86400)  # Keep the main thread alive
    except KeyboardInterrupt:
        logging.info("Stopping the server...")
        entity_servicer.stop()
        server.stop(0)

if __name__ == '__main__':
    serve()
