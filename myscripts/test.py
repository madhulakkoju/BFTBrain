import os
import shutil
import subprocess
import time

# Config
SCRIPT = "./BFTBrain/scripts/miyuki/main.py"
CONFIG_DIR = "./BFTBrain/test"
FRAMEWORK_CONFIG = "./BFTBrain/config/config.framework.yaml"
COPY_SCRIPT = "./BFTBrain/copy.sh"
DURATION = 3700  # in seconds
USER = "ksp20"
PWORD = "Sai@123"

# Loop through each .yaml file
for file in os.listdir(CONFIG_DIR):
    if file.endswith(".yaml"):
        config_path = os.path.join(CONFIG_DIR, file)
        print(f"Processing config file: {config_path}")

        # Step 1: Copy content into framework config
        shutil.copyfile(config_path, FRAMEWORK_CONFIG)
        print(f"Copied content to {FRAMEWORK_CONFIG}")

        # Step 2: Run the sync experiment (fire-and-wait)
        print("Running sync experiment...")
        sync_proc = subprocess.Popen(
            ["python3", SCRIPT, "-e", "test", "-p", "m510-f-1", "sync"],
            env={**os.environ, "USER": USER, "PWORD": PWORD},
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )
        sync_stdout, sync_stderr = sync_proc.communicate()
        print("Sync experiment finished.")

        # Step 3: Start the main pbft experiment
        print("Starting pbft experiment...")
        pbft_proc = subprocess.Popen(
            ["python3", SCRIPT, "-e", "test", "-p", "m510-f-1", "single", "pbft", "--config", FRAMEWORK_CONFIG, "--public"],
            env={**os.environ, "USER": USER, "PWORD": PWORD},
            stdin=subprocess.PIPE,
            text=True
        )

        # Step 4: Wait and send stop
        time.sleep(DURATION)
        print("Sending 'stop' to pbft experiment...")
        pbft_proc.stdin.write("stop\n")
        pbft_proc.stdin.flush()
        pbft_proc.wait()
        print("pbft experiment finished.")

        # Step 5: Rename result file
        output_filename = f"{file}.csv"
        print(f"Copying results as {output_filename}")
        subprocess.run(["bash", COPY_SCRIPT, output_filename])

print("All experiments completed.")
