#source .venv/bin/activate
#
#cd data
#
#build-proto.sh
#apply-build.sh
#
#cd ..
#
#cd code
#
#mvn clean install
#
#mvn dependency:copy-dependencies
#
#cd ..
#
#cd scripts
#
#./local_exp.sh pbft XOV learning
#
#on ctrl c run
#  tmux kill-server


#!/bin/bash
# Exit immediately if a command exits with a non-zero status,
# treat unset variables as an error, and propagate errors through pipes.
set -euo pipefail

# Trap CTRL+C (SIGINT) to kill the tmux server and then exit.
trap 'echo "Interrupt received. Killing tmux server..."; tmux kill-server; exit 1' INT

# Activate the Python virtual environment.
if [ -f .venv/bin/activate ]; then
  source .venv/bin/activate
  echo "Activated virtual environment."
else
  echo "Virtual environment not found. Exiting." >&2
  exit 1
fi

# Build protocol and apply build from the 'data' directory.
if [ -d "data" ]; then
  pushd data > /dev/null
    if [ -x "./build-proto.sh" ]; then
      ./build-proto.sh
    else
      echo "build-proto.sh is not executable or not found." >&2
      exit 1
    fi

    if [ -x "./apply-build.sh" ]; then
      ./apply-build.sh
    else
      echo "apply-build.sh is not executable or not found." >&2
      exit 1
    fi
  popd > /dev/null
else
  echo "Data directory not found." >&2
  exit 1
fi

# Build code and copy dependencies in the 'code' directory using Maven.
if [ -d "code" ]; then
  pushd code > /dev/null
    mvn clean install
    mvn dependency:copy-dependencies
  popd > /dev/null
else
  echo "Code directory not found." >&2
  exit 1
fi

# Run local experiment script in the 'scripts' directory.
if [ -d "scripts" ]; then
  pushd scripts > /dev/null
    if [ -x "./local_exp.sh" ]; then
      ./local_exp.sh pbft OX learning
    else
      echo "local_exp.sh is not executable or not found." >&2
      exit 1
    fi
  popd > /dev/null
else
  echo "Scripts directory not found." >&2
  exit 1
fi

echo "Script completed successfully."
