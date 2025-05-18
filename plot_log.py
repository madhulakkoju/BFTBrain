import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import os
import time

# --- Configuration ---
csv_file_path = 'log/csv/test/8282.csv'  # IMPORTANT: Replace with the actual path to your CSV file
update_interval_ms = 1000  # Update plot every 1000ms (1 second)
annotation_fontsize = 7    # Font size for annotations
annotation_offset_y = 0.5  # Vertical offset for text from point (adjust as needed based on y-axis scale)
# ---------------------

# --- Plot Setup ---
fig, ax = plt.subplots(figsize=(12, 7)) # Slightly larger figure
line, = ax.plot([], [], 'r-o', markersize=4, label='Throughput') # Added label for legend

ax.set_xlabel("Episode")
ax.set_ylabel("Throughput")
ax.set_title("Real-time Blockchain Throughput vs. Episode (with Details)")
ax.grid(True)
# List to store matplotlib text annotation objects
annotations = []
# ------------------

# --- Global variable to track file modification time ---
last_mod_time = 0
# -----------------------------------------------------

# --- Animation Update Function ---
def update(frame):
    """This function is called periodically by FuncAnimation"""
    global last_mod_time, annotations # Use global variables

    required_columns = ['episode', 'throughput', 'protocol', 'architecture']

    try:
        # --- Efficiency Check: Only read if file has been modified ---
        if not os.path.exists(csv_file_path):
            # Return existing artists without changes if file doesn't exist yet
            return [line] + annotations # Return line and all current annotations

        current_mod_time = os.path.getmtime(csv_file_path)

        if current_mod_time == last_mod_time:
            # No change in file
            return [line] + annotations # Return line and all current annotations

        last_mod_time = current_mod_time
        # -------------------------------------------------------------

        # Read the CSV file
        data = pd.read_csv(csv_file_path)

        # --- Data Validation ---
        if not all(col in data.columns for col in required_columns):
            missing_cols = [col for col in required_columns if col not in data.columns]
            print(f"Error: CSV missing required columns: {missing_cols}. Skipping update.")
            # Optionally clear plot or just skip update
            # line.set_data([], [])
            # for ann in annotations:
            #     ann.set_visible(False)
            # ax.relim()
            # ax.autoscale_view()
            return [line] + annotations # Return existing artists

        if data.empty:
            print("Warning: CSV file is empty. Clearing plot.")
            line.set_data([], [])
            for ann in annotations:
                ann.set_visible(False) # Hide all annotations
            ax.relim()
            ax.autoscale_view()
            return [line] + annotations

        # --- Extract Data ---
        x = data['episode']
        y = data['throughput']
        protocols = data['protocol']
        architectures = data['architecture']
        num_points = len(x)
        # --------------------

        # --- Update Line Plot ---
        line.set_data(x, y)
        # ----------------------

        # --- Update Annotations ---
        # Calculate dynamic offset based on current y-axis range
        y_range = ax.get_ylim()
        dynamic_offset = (y_range[1] - y_range[0]) * 0.01 # Offset by 1% of y-range height

        for i in range(num_points):
            point_x = x.iloc[i]
            point_y = y.iloc[i]
            label_text = f"({point_x}, {protocols.iloc[i]}, {architectures.iloc[i]})"

            if i < len(annotations):
                # Update existing annotation
                ann = annotations[i]
                ann.set_position((point_x, point_y + dynamic_offset))
                ann.set_text(label_text)
                ann.set_visible(True) # Make sure it's visible
            else:
                # Create new annotation
                new_ann = ax.text(point_x, point_y + dynamic_offset, label_text,
                                  fontsize=annotation_fontsize,
                                  ha='center', # Horizontal alignment
                                  va='bottom') # Vertical alignment
                annotations.append(new_ann)

        # Hide any extra annotations if data size decreased
        for i in range(num_points, len(annotations)):
            annotations[i].set_visible(False)
        # ------------------------

        # Adjust plot limits automatically
        ax.relim()
        ax.autoscale_view(tight=False) # Allow some padding
        # Ensure y-axis starts at or below 0 if desired, otherwise autoscale handles it
        # current_ylim = ax.get_ylim()
        # ax.set_ylim(bottom=min(0, current_ylim[0]))


    except FileNotFoundError:
        print(f"Error: File not found at '{csv_file_path}'. Waiting...")
        time.sleep(1)
        pass
    except pd.errors.EmptyDataError:
        print(f"Warning: File '{csv_file_path}' is empty. Waiting for data...")
        line.set_data([], []) # Clear the plot if file is empty
        for ann in annotations:
            ann.set_visible(False) # Hide all annotations
        ax.relim()
        ax.autoscale_view()
        pass
    except Exception as e:
        print(f"An error occurred during update: {type(e).__name__} - {e}")
        # You might want to add more specific error handling here
        pass

    # Return *all* artists that might have been modified
    # This is important for blitting, though managing many text objects can still impact performance
    return [line] + annotations
# ---------------------------------

# --- Create and Start Animation ---
# Blit=True can significantly improve performance, but might have issues
# on some backends when adding/removing artists dynamically. If you experience
# rendering problems (like text not showing up correctly), try blit=False.
# However, blit=False will redraw the entire plot each frame, making it slower.
try:
    ani = animation.FuncAnimation(fig, update, interval=update_interval_ms,
                                  blit=True, # <<< TRY THIS FIRST
                                  cache_frame_data=False,
                                  save_count=0) # Helps prevent memory buildup
except Exception as e:
    print(f"Blitting failed ({e}), falling back to blit=False (slower).")
    ani = animation.FuncAnimation(fig, update, interval=update_interval_ms,
                                  blit=False, # <<< FALLBACK
                                  cache_frame_data=False)


ax.legend() # Add legend (shows 'Throughput' label for the line)
plt.tight_layout()
plt.show()
# ----------------------------------

print("Plot window closed. Script finished.")