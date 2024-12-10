#!/usr/bin/env bash

set -euo pipefail
# set -x


APP_NAME="ABDownloadManager"

LOG_FILE="/tmp/ab-dm-uninstaller.log"

# --- Custom Logger
logger() {
  timestamp=$(date +"%Y/%m/%d %H:%M:%S")

  if [[ "$1" == "error" ]]; then
    # Red color for errors
    echo -e "${timestamp} -- ABDM-Uninstaller [Error]: \033[0;31m$@\033[0m" | tee -a ${LOG_FILE}
  else
    # Default color for non-error messages
    echo -e "${timestamp} -- ABDM-Uninstaller [Info]: $@" | tee -a ${LOG_FILE}
  fi
}

delete_app_config_dir() {

    local answer
    read -p "Do you want to continue? [Y/n]: " -r answer
    answer=${answer:-Y}  # Set default to 'Y' if no input is given

    case $answer in
        [Yy]* )
            find "$HOME" -maxdepth 1 -type d -name ".abdm" -exec bash -c 'echo "File {} Removed"; rm -rf {}' \;
            ;;
        [Nn]* )
            logger "Remove The $HOME/.abdm directory manually."
            ;;
        * )
            logger error "Please answer yes or no."
            delete_app_config_dir
            ;;
    esac
}

delete_app() {

    # Find the PID(s) of the application
    PIDS=$(pidof "$APP_NAME") || true

    if [ -n "$PIDS" ]; then
        echo "Found $APP_NAME with PID(s): $PIDS. Attempting to kill..."

        # Attempt to terminate the process gracefully
        kill $PIDS 2>/dev/null || echo "Graceful kill failed..."

        # Wait for a short period to allow graceful shutdown
        sleep 2

        # Check if the process is still running
        PIDS=$(pidof "$APP_NAME") || true
        if [ -n "$PIDS" ]; then
            echo "Process still running. Force killing..."
            kill $PIDS 2>/dev/null || echo "Graceful kill failed..."
        else
            echo "$APP_NAME terminated successfully."
        fi
    else
        echo "$APP_NAME is not running."
    fi

    logger "removing $APP_NAME desktop file ..."
    # --- Remove the .desktop file in ~/.local/share/applications
    find "$HOME/.local/share/applications" -maxdepth 1 -type f -name "abdownloadmanager.desktop" -exec bash -c 'echo "File {} Removed"; rm -rf {}' \;

    logger "unlinking $APP_NAME link ..."
    find "$HOME/.local/bin/" -maxdepth 1 -type f -name "$APP_NAME" -exec bash -c 'echo "File {} Removed"; rm -rf {}' \;

    logger "removing $APP_NAME binary ..."
    find "$HOME/.local/bin" -maxdepth 1 -type l -name "$APP_NAME" -exec bash -c 'echo "File {} Removed"; rm -rf {}' \;
    find "$HOME/.local/bin" -maxdepth 1 -type d -name "$APP_NAME" -exec bash -c 'echo "File {} Removed"; rm -rf {}' \;

    logger "removing $APP_NAME autostart at boot file ..."
    find "$HOME/.config/autostart" -maxdepth 1 -type d -name "AB Download Manager.desktop" -exec bash -c 'echo "File {} Removed"; rm -rf {}' \;

    logger "removing $APP_NAME settings and download lists"
    delete_app_config_dir

    logger "AB Download Manager completely removed"
}

main() {
  delete_app
}

main "$@"