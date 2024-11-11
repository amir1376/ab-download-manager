#!/usr/bin/env bash

# Downloads the latest tarball from https://github.com/amir1376/ab-download-manager/releases and unpacks it into ~/.local/.
# Creates a .desktop entry for the app in ~/.local/share/applications based on FreeDesktop specifications.

set -euo pipefail

# --- Get the latest version of the app from the GitHub API

get_latest_version() {
    curl -s https://api.github.com/repos/amir1376/ab-download-manager/releases/latest | jq -r '.tag_name'
}

APP_NAME="ABDownloadManager"
PLATFORM="linux"
EXT="tar.gz"

LATEST_VERSION=$(get_latest_version)
ASSET_NAME=${APP_NAME}_${LATEST_VERSION:1}_${PLATFORM}.${EXT}
BINARY_PATH=$HOME/.local/$APP_NAME/bin/$APP_NAME

DOWNLOAD_URL="https://github.com/amir1376/ab-download-manager/releases/download/${LATEST_VERSION}/$ASSET_NAME"
DEPENDENCIES=(curl jq tar)

# --- Custom Logger
logger() {
  timestamp=$(date +"[%Y/%m/%d %H:%M:%S]")

  if [[ "$1" == "error" ]]; then
    # Red color for errors
    echo -e "${timestamp} [Error]: \033[0;31m${timestamp} $@\033[0m"
  else
    # Default color for non-error messages
    echo -e "${timestamp} $@"
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
    clear
}

# --- Delete the old version Application if exists
delete_old_version() {
    # --- Killing Any Application Process 
    pkill -f "$APP_NAME"
    rm -rf "$HOME/.local/$APP_NAME"
    rm -rf "$HOME/.local/bin/$APP_NAME"
    logger "removed old version AB Download Manager"
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
    # --- Remove The App tarball if its not downloaded correctly
    # due to internet connectivity or sth else 
    sudo find  "/tmp" -type f -iname "$ASSET_NAME" -exec bash -c 'echo "File {} removed"; rm {}' \;
    logger "Downloading AB Download Manager ..."
    curl -Ls -o "/tmp/$ASSET_NAME" "${DOWNLOAD_URL}"
    logger "Download Finished"
}

# --- Install the app
install_app() {
    echo "Installing AB Download Manager ..."
    # --- Setup ~/.local directories
    mkdir -p "$HOME/.local/bin" "$HOME/.local/share/applications"
    tar -xzf "/tmp/$ASSET_NAME" -C "$HOME/.local"

    # --- remove tarball after installation
    rm "/tmp/$ASSET_NAME"

    # Link the binary to ~/.local/bin
    ln -s "$BINARY_PATH" "$HOME/.local/bin/$APP_NAME"

    # Create a .desktop file in ~/.local/share/applications
    generate_desktop_file

    echo "AB Download Manager installed successfully"
    echo "it can be found in Applications menu or run '$APP_NAME' in terminal"
    echo "Making sure $HOME/.local/bin is in your PATH"
    if ! $(echo "$PATH" | grep "$HOME/.local/bin" >/dev/null 2>&1); then
        echo "Adding $HOME/.local/bin to ${USER}'s PATH"
        echo "export $HOME/.local/bin:$PATH" >> $HOME/.bashrc
    fi
    
}

# Update the app
update_app() {
    echo "Checking update..."
    if [ "$1" != "${LATEST_VERSION:1}" ]; then
        echo "An update is available: v${LATEST_VERSION:1}. Updating..."
        download_zip
        delete_old_version
        install_app
    else
        echo "You have the latest version installed."
        exit 0
    fi
}

main() {
    local installed_version
    install_dependencies
    installed_version=$(check_if_installed)
    if [ -n "$installed_version" ]; then
        echo "AB Download Manager v$installed_version is currently installed"
        update_app "$installed_version"
    else
        download_zip
        install_app
    fi
}

main "$@"
