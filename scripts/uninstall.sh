#!/usr/bin/env bash

set -euo pipefail


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


delete_app() {

    logger "Killing Any $APP_NAME Processes ..."
    pkill --echo -f "$APP_NAME"

    logger "removing $APP_NAME desktop file ..."
    # --- Remove the .desktop file in ~/.local/share/applications
    rm "$HOME/.local/share/applications/abdownloadmanager.desktop"

    logger "unlinking $APP_NAME link ..."
    unlink "$HOME/.local/bin/$APP_NAME"

    logger "removing $APP_NAME binary ..."
    rm -rf "$HOME/.local/$APP_NAME"
    rm -rf "$HOME/.local/bin/$APP_NAME"

    rm -rf $HOME/Downloads/ABDM
    logger "AB Download Manager completely removed"
}

main() {
  delete_app
}

main "$@"