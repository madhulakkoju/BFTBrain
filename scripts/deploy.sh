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
  tmux send-keys -t cloudlab:"$count" "ssh $line -p 22 -o \"StrictHostKeyChecking no\" \"wget -O - https://gist.githubusercontent.com/anon-osdi-24/e4d388dd56a63dcd7100662855b50d71/raw/c9575bbfc51f296e716d7634a4594c52267b7535/BFTBrain-deploy.sh > setup.sh \
    && chmod +x setup.sh && source setup.sh\"" C-m
  ((count++))
done < servers.txt
