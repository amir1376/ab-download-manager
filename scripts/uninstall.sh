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
    echo -e "${timestamp} -- "$0" [Error]: \033[0;31m$@\033[0m" | tee -a ${LOG_FILE}
  else
    # Default color for non-error messages
    echo -e "${timestamp} -- "$0" [Info]: $@" | tee -a ${LOG_FILE}
  fi
}

delete_app_config_dir() {

    local answer
    read -p "Do you want to continue? [Y/n]: " -r answer
    answer=${answer:-Y}  # Set default to 'Y' if no input is given

    case $answer in
        [Yy]* )
            rm -rf "$HOME/.abdm"
            logger "$APP_NAME settings and download lists directory: $HOME/.abdm removed."
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

    logger "Killing Any $APP_NAME Processes ..."
    pkill -f "$APP_NAME"

    logger "removing $APP_NAME desktop file ..."
    # --- Remove the .desktop file in ~/.local/share/applications
    rm "$HOME/.local/share/applications/abdownloadmanager.desktop"

    logger "unlinking $APP_NAME link ..."
    unlink "$HOME/.local/bin/$APP_NAME"

    logger "removing $APP_NAME binary ..."
    rm -rf "$HOME/.local/$APP_NAME"
    rm -rf "$HOME/.local/bin/$APP_NAME"

    logger "removing $APP_NAME autostart at boot file ..."
    rm -f "$HOME/.config/autostart/AB Download Manager.desktop"

    logger "removing $APP_NAME settings and download lists"
    delete_app_config_dir

    logger "AB Download Manager completely removed"
}

main() {
  delete_app
}

main "$@"