#!/bin/bash

# Name of the tmux session
SESSION_NAME="run"

# Wait time in seconds (2.5 hours = 2 * 3600 + 1800 = 9000)
WAIT_TIME=14400

echo "Waiting for 2.5 hours before sending 'stop' to tmux session '$SESSION_NAME'..."
sleep "$WAIT_TIME"

# Check if session exists
if tmux has-session -t "$SESSION_NAME" 2>/dev/null; then
    tmux send-keys -t "$SESSION_NAME" "stop" C-m
    echo "'stop' command sent to tmux session '$SESSION_NAME'."
else
    echo "Session '$SESSION_NAME' not found. Nothing to send."
fi
