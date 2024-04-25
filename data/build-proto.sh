# The following command requires protoc-gen-grpc-java plugin to be installed
mkdir -p /Users/sai/Desktop/Project_P/BFTBrain/data/target/java && protoc --plugin=protoc-gen-grpc-java=/usr/local/bin/protoc-gen-grpc-java \
    --grpc-java_out=target/java --proto_path=include/ --proto_path=proto/ proto/gbft.proto
protoc --proto_path=include/ --proto_path=proto/ --java_out=/Users/sai/Desktop/Project_P/BFTBrain/data/target/java proto/gbft.proto
# The following command requires grpcio-tools installed for python
python3 -m grpc_tools.protoc -I proto/ --python_out=/Users/sai/Desktop/Project_P/BFTBrain/code/learning/ --grpc_python_out=/Users/sai/Desktop/Project_P/BFTBrain/code/learning/ gbft.proto