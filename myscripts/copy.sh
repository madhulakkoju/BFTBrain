#!/bin/bash

# Check for argument
if [ $# -ne 1 ]; then
  echo "Usage: $0 <new_filename.csv>"
  exit 1
fi

# Argument: new filename
RENAMED_FILE="$1"

# Variables
REMOTE_USER="ksp20"
REMOTE_HOST="node-5.testw.adapttnx24blockc-PG0.utah.cloudlab.us"
KEY_PATH=~/BFTBrain/scripts/miyuki/id_cloudlab
REMOTE_DIR="/users/ksp20/BFTBrain/log/csv/test"
ORIG_FILE="8000.csv"
ORIG_FILE2="8282.csv"
LOCAL_DEST="/users/ksp20/BFTBrain/results"

# Step 1: Copy original file from node-5 to local machine
echo "Copying $ORIG_FILE from $REMOTE_HOST to $LOCAL_DEST..."
scp -i "$KEY_PATH" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/$ORIG_FILE" "$LOCAL_DEST"
scp -i "$KEY_PATH" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/$ORIG_FILE2" "$LOCAL_DEST"

# Step 2: Rename the file locally
echo "Renaming downloaded file to $RENAMED_FILE locally..."
mv "$LOCAL_DEST/$ORIG_FILE" "$LOCAL_DEST/t_${RENAMED_FILE}"
mv "$LOCAL_DEST/$ORIG_FILE2" "$LOCAL_DEST/$RENAMED_FILE"

echo "Done."
