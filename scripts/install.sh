#!/usr/bin/env bash

# Downloads the latest tarball from https://github.com/amir1376/ab-download-manager/releases and unpacks it into ~/.local/.
# Creates a .desktop entry for the app in ~/.local/share/applications based on FreeDesktop specifications.

set -euo pipefail

# --- Get the latest version of the app from the GitHub API

get_latest_version() {
    curl -fSs https://api.github.com/repos/amir1376/ab-download-manager/releases/latest | jq -r '.tag_name'
}

APP_NAME="ABDownloadManager"
PLATFORM="linux"
EXT="tar.gz"

LATEST_VERSION=$(get_latest_version)
ASSET_NAME=${APP_NAME}_${LATEST_VERSION:1}_${PLATFORM}.${EXT}
BINARY_PATH=$HOME/.local/$APP_NAME/bin/$APP_NAME

DOWNLOAD_URL="https://github.com/amir1376/ab-download-manager/releases/download/${LATEST_VERSION}/$ASSET_NAME"
DEPENDENCIES=(curl jq tar)

LOG_FILE="/tmp/ab-dm-installer.log"

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

# --- Detect OS and The Package Manager to use
detect_package_manager() {
    if [ -f /etc/os-release ]; then
        source /etc/os-release
        local OS=${NAME}
    elif type lsb_release >/dev/null 2>&1; then
        local OS=$(lsb_release -si)
    elif [ -f /etc/lsb-release ]; then
        source /etc/lsb-release
        local OS="${DISTRIB_ID}"
    elif [ -f /etc/debian_version ]; then
        local OS=Debian
    else
        logger error "Your Linux Distro is not Supperted."
        logger error "Please install ${DEPENDENCIES[@]} Manually."
        exit 1
    fi

    if `grep -E 'Debian|Ubuntu' <<< $OS > /dev/null` ; then
        systemPackage="apt"
    elif `grep -E 'Fedora|CentOS|Red Hat|AlmaLinux' <<< $OS > /dev/null`; then
        systemPackage="dnf"
    fi
}

# --- Install dependencies
install_dependencies() {
    detect_package_manager
    logger "installing dependencies: ${DEPENDENCIES[@]}"
    sudo ${systemPackage} install -y ${DEPENDENCIES[@]}
}

# --- Delete the old version Application if exists
delete_old_version() {
    # --- Killing Any Application Process 
    pkill -f "$APP_NAME"
    rm -rf "$HOME/.local/$APP_NAME"
    rm -rf "$HOME/.local/bin/$APP_NAME"
    logger "removed old version AB Download Manager"
}

# --- Generate a .desktop file for the app
generate_desktop_file() {
    cat <<EOF > "$HOME/.local/share/applications/abdownloadmanager.desktop"
[Desktop Entry]
Name=AB Download Manager
Comment=Manage and organize your download files better than before
GenericName=Downloader
Categories=Utility;Network;
Exec=$BINARY_PATH
Icon=$HOME/.local/$APP_NAME/lib/$APP_NAME.png
Terminal=false
Type=Application
StartupWMClass=com-abdownloadmanager-desktop-AppKt
EOF
}

# --- Download the latest version of the app
download_zip() {
    # Remove the app tarball if it exists in /tmp
    rm -f "/tmp/$ASSET_NAME"

    logger "downloading AB Download Manager ..."
    # Perform the download with curl
    if curl --progress-bar -fSL -o "/tmp/$ASSET_NAME" "${DOWNLOAD_URL}"; then
        logger "download finished successfully"
    else
        logger error "Download failed! Something Went Wrong"
        logger error "Hint: Check Your Internet Connectivity"
        # Optionally remove the partially downloaded file
        rm -f "/tmp/$ASSET_NAME"
    fi
}


# --- Install the app
install_app() {

    logger "Installing AB Download Manager ..."
    # --- Setup ~/.local directories
    mkdir -p "$HOME/.local/bin" "$HOME/.local/share/applications"
    tar -xzf "/tmp/$ASSET_NAME" -C "$HOME/.local"

    # --- remove tarball after installation
    rm "/tmp/$ASSET_NAME"

    # Link the binary to ~/.local/bin
    ln -s "$BINARY_PATH" "$HOME/.local/bin/$APP_NAME"

    # Create a .desktop file in ~/.local/share/applications
    generate_desktop_file

    logger "AB Download Manager installed successfully"
    logger "it can be found in Applications menu or run '$APP_NAME' in terminal"
    logger "Making sure $HOME/.local/bin exists in PATH"
    if ! $(echo "$PATH" | grep "$HOME/.local/bin" >/dev/null 2>&1); then
        logger "Adding $HOME/.local/bin to ${USER}'s PATH"
        echo "export $HOME/.local/bin:$PATH" >> $HOME/.bashrc
    fi
    logger "installation logs saved in: ${LOG_FILE}"
    
}

# --- Check if the app is installed
check_if_installed() {
    local installed_version
    installed_version=$($APP_NAME --version 2>/dev/null)
    if [ -n "$installed_version" ]; then
        echo "$installed_version"
    else
        echo ""
    fi
}

# --- Update the app
update_app() {
    logger "checking update"
    if [ "$1" != "${LATEST_VERSION:1}" ]; then
        logger "new version is available: v${LATEST_VERSION:1}. Updating..."
        download_zip
        delete_old_version
        install_app
    else
        logger "You have the latest version installed."
        exit 0
    fi
}

main() {
    echo "" > "$LOG_FILE"
    local installed_version
    install_dependencies
    installed_version=$(check_if_installed)
    if [ -n "$installed_version" ]; then
        logger "AB Download Manager v$installed_version is currently installed."
        update_app "$installed_version"
    else
        download_zip
        install_app
    fi
}

main "$@"
