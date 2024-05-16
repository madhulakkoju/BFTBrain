# Usage: ./local_exp.sh [protocol] [learning (optional)]
# Examples:
#        ./local_exp.sh pbft # run pbft without learning agents
#        ./local_exp.sh pbft # run bedrock with learning agents, using pbft as the default protocol
# [protocol]: protocol name to run
# [learning]: if *learning* provided, each bedrock entity will be paired with a local learning agent

protocol=$1

#Counts of coordination units: ( 1 Coordination server + 4 coordination units + 1 client )
count=6 # 3f+3 # Count of servers without learning agent
agent_count=0 # Count of servers with learning agent
# if learning, agent_count=count-2
# $# is num of args in cmd line..... -gt : greater than
if [ $# -gt 1 ]; then
  if [ "$2" == "learning" ]; then
    agent_count=$((count-2))
  fi
fi

# check if session exists
if tmux has-session -t cloudlab 2>/dev/null; then
  # session exists - kill it
  tmux kill-session -t cloudlab
fi
if tmux has-session -t cloudlab-learning 2>/dev/null; then
  # session exists - kill it
  tmux kill-session -t cloudlab-learning
fi

# create new session for cloudlab / server without learning node
tmux new-session -d -s cloudlab

# create new session when learning is enabled by checking the argument 2 while executing this file
if [ $agent_count -gt 0 ]; then
  tmux new-session -d -s cloudlab-learning
fi

# create one window for each server
for (( i=0; i<$count-1; i++ ))
do
  tmux new-window -t cloudlab # create a new window for each server / node
done
for (( i=0; i<$agent_count-1; i++ ))
do
  tmux new-window -t cloudlab-learning # create a new window for each learning node
done


sleep 5

echo "\033[4;34mStart running $protocol with $count servers\033[m"

echo "Protocol $protocol : [0/6] Killing previous processes if needed ..."
# Kill all processes if active
for (( i=0; i<$count; i++ ))
do
  echo "Killing Bedrock and learning agent on machine $i ..."
  # Kill_process_port from script : kill_process_port.sh ________ (port number)
  # tmux send-keys -t {session}:{window}.{pane}  "KEY/VAL" C -m        =====> To send keys to a specific session window
  tmux send-keys -t cloudlab:"$i" "cd ~/BFTBrain/code && ../scripts/kill_process_port.sh $((9020+$count)) && ../scripts/kill_process_port.sh $((9020+$count+20))" C-m
done

echo "Protocol $protocol : [1/6] Starting Coordination Server"
# start server on the first server - Cordination Server & send the output to the created coordination server session
# this run.sh runs the actual protocols and log all the server logs
tmux send-keys -t cloudlab:0 "./run.sh CoordinatorServer -p 9020 -r $protocol" C-m

echo "Protocol $protocol : [2/6] Waiting for 10 seconds for coordination server to set up ..."
sleep 5

# start units
for (( i=1; i<$count-1; i++ ))
do
  echo "Protocol $protocol : [3/6] Starting Coordination Unit $(($i-1))"
  # this run.sh creates the Coordinator unit and links / sends it to the cloudlab window
  tmux send-keys -t cloudlab:"$i" "./run.sh CoordinatorUnit -u $(($i-1)) -p $((9020+$i)) -n 1 -s 127.0.0.1:9020" C-m
  sleep 1
done

# start learning agents
for (( i=0; i<$agent_count; i++ ))
do
  echo "Protocol $protocol : [3/6] Starting Learning Agent for Coordination Unit $i"
  # this run.sh creates the Learning Agent unit and links / sends it to the cloudlab-learning window
  tmux send-keys -t cloudlab-learning:"$i" "cd ~/BFTBrain/code/learning/ && python3 learning_agent.py -u $i -p $((9021+$i)) -n single" C-m
done
sleep 5

echo "Protocol $protocol : [4/6] Starting Client"
# start client y calling run.sh as Coordinator Unit also sending inputs: u: count of agents ; p: port number ; -c    ; -s   C -m
tmux send-keys -t cloudlab:"$(($count-1))" "./run.sh CoordinatorUnit -u $(($count-2)) -p $((9020+$count-1)) -c 1 -s 127.0.0.1:9020" C-m

echo "Protocol $protocol : [5/6] Waiting for 10 seconds for connection to set up ..."
sleep 5

# coordination server start
# by sending empty key to Coordination server at cloudlab[0]
tmux send-keys -t cloudlab:0 C-m

echo "Protocol $protocol : [6/6] Executing ..."

echo "WAITING FOR INPUT TO KILL THE INSTANCE"
cat
# this cat command is just to stall the terminal.
# the actual execution os already running behind the scenes on other sessions: cloudlab & cloudlab-learning.

for (( i=0; i<$count; i++ ))
do
  echo "Stopping Bedrock on machine $i ..."
  tmux send-keys -t cloudlab:"$i" C-c
done
for (( i=0; i<$agent_count; i++ ))
do
  tmux send-keys -t cloudlab-learning:"$i" C-c
done

echo "Protocol $protocol : [Finish]"
