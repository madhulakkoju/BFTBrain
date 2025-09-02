# check if session exists
if tmux has-session -t cloudlab 2>/dev/null; then
  # session exists - kill it
  tmux kill-session -t cloudlab
fi
# create new session
tmux new-session -d -s cloudlab

# create one window for each server
tail -n +2 servers.txt | while IFS= read -r line || [[ -n "$line" ]]; do
  tmux new-window -t cloudlab
done

count=0
while IFS= read -r line || [[ -n "$line" ]]; do
  tmux send-keys -t cloudlab:"$count" "ssh $line -p 22 -o \"StrictHostKeyChecking no\" \"wget -O - https://gist.githubusercontent.com/msiddhu/632c7b6b7420afd1d8002a5054253ad0/raw/7c58d70454d554bcdf3e53f71b6cb112b8aee0ea/BFTBrain-deploy.sh > setup.sh \
    && chmod +x setup.sh && source setup.sh\"" C-m
  ((count++))
done < servers.txt
