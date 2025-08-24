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

remove_if_exists() {
    local target="$1"

    if [ -z "$target" ]; then
        logger "No target specified in remove_if_exists function"
        return 1
    fi

    if [ -e "$target" ]; then
        logger "File \"$target\" Removed"
        rm -rf "$target"
    else
        logger "File \"$target\" does not exist"
    fi
}

delete_app_config_dir() {

    local answer
    read -p "Do you want to continue? [Y/n]: " -r answer
    answer=${answer:-Y}  # Set default to 'Y' if no input is given

    case $answer in
        [Yy]* )
            remove_if_exists "$HOME/.abdm"
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
            kill -9 $PIDS 2>/dev/null || echo "Force kill failed..."
        else
            echo "$APP_NAME terminated successfully."
        fi
    else
        echo "$APP_NAME is not running."
    fi

    logger "removing $APP_NAME desktop file ..."
    # --- Remove the .desktop file in ~/.local/share/applications (OLD VERSION)
    remove_if_exists "$HOME/.local/share/applications/com.abdownloadmanager.desktop"

    # --- Remove the .desktop file in ~/.local/share/applications
    remove_if_exists "$HOME/.local/share/applications/com.abdownloadmanager.abdownloadmanager.desktop"

    # --- Remove the .png file in ~/.local/share/icons/hicolor/512x512/apps
    remove_if_exists "$HOME/.local/share/icons/hicolor/512x512/apps/com.abdownloadmanager.abdownloadmanager.png"

    logger "removing $APP_NAME link ..."
    remove_if_exists "$HOME/.local/bin/$APP_NAME"

    logger "removing $APP_NAME binary ..."
    remove_if_exists "$HOME/.local/$APP_NAME"

    logger "removing $APP_NAME autostart at boot file ..." # OLD VERSION
    remove_if_exists "$HOME/.config/autostart/com.abdownloadmanager.desktop"

    logger "removing $APP_NAME autostart at boot file ..."
    remove_if_exists "$HOME/.config/autostart/com.abdownloadmanager.abdownloadmanager.desktop"

    if [ -e "$HOME/.abdm" ]; then
        logger "removing $APP_NAME settings and download lists $HOME/.abdm"
        delete_app_config_dir
    fi

    logger "AB Download Manager completely removed"
}

main() {
  delete_app
}

main "$@"
