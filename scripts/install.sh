#!/usr/bin/env bash

# Downloads the latest tarball from https://github.com/amir1376/ab-download-manager/releases and unpacks it into ~/.local/.
# Creates a .desktop entry for the app in ~/.local/share/applications based on FreeDesktop specifications.

set -euo pipefail

# Get the latest version of the app from the GitHub API
get_latest_version() {
    curl -s https://api.github.com/repos/amir1376/ab-download-manager/releases/latest | jq -r '.tag_name'
}

APP_NAME="ABDownloadManager"
PLATFORM="linux"
EXT="tar.gz"
LATEST_VERSION=$(get_latest_version)
ASSET_NAME=${APP_NAME}_${LATEST_VERSION:1}_${PLATFORM}.${EXT}
BINARY_PATH=$HOME/.local/$APP_NAME/bin/$APP_NAME

# Check if curl, jq, and tar are installed
check_dependencies() {
    for cmd in curl jq tar; do
        if ! command -v "$cmd" &>/dev/null; then
            print_message "Error: $cmd is not installed. Please install it and try again."
            exit 1
        fi
    done
}

# Get the download URL for the latest version of the app
get_download_url() {
    echo "https://github.com/amir1376/ab-download-manager/releases/download/${LATEST_VERSION}/$ASSET_NAME"
}

close_if_running() {
    pid=$(pidof -s "$APP_NAME")

    if [ -n "$pid" ]; then
        kill -9 "$pid"
        echo "Closed running instance of AB Download Manager before updating."
    fi
}

# Delete the old version of the app if it exists
delete_old_version() {
    close_if_running
    rm -rf "$HOME/.local/$APP_NAME"
    rm -rf "$HOME/.local/bin/$APP_NAME"
    echo "Old version of AB Download Manager deleted"
}

# Check if the app is installed
check_if_installed() {
    local installed_version
    installed_version=$($APP_NAME --version 2>/dev/null)
    if [ -n "$installed_version" ]; then
        echo "$installed_version"
    else
        echo ""
    fi
}

# Generate a .desktop file for the app
generate_desktop_file() {
    desktop_file_content="[Desktop Entry]
Name=AB Download Manager
Comment=Manage and organize your download files better than before
GenericName=Downloader
Categories=Utility;Network;
Exec=$BINARY_PATH
Icon=$HOME/.local/$APP_NAME/lib/$APP_NAME.png
Terminal=false
Type=Application
StartupWMClass=com-abdownloadmanager-desktop-AppKt"

    echo "$desktop_file_content" >"$HOME/.local/share/applications/abdownloadmanager.desktop"
}

# Download the latest version of the app
download_zip() {
    echo "Downloading AB Download Manager ..."
    url=$(get_download_url)
    curl -L -o "/tmp/$ASSET_NAME" "$url"
    echo "Download Finished"
}

# Install the app
install_app() {
    echo "Installing AB Download Manager ..."
    mkdir -p "$HOME/.local"
    tar -xzf "/tmp/$ASSET_NAME" -C "$HOME/.local"

    # Setup ~/.local directories
    mkdir -p "$HOME/.local/bin" "$HOME/.local/share/applications"

    # Link the binary to ~/.local/bin
    ln -s "$BINARY_PATH" "$HOME/.local/bin/$APP_NAME"

    # Create a .desktop file in ~/.local/share/applications
    generate_desktop_file

    echo "AB Download Manager installed successfully"
    echo "Open it from your Applications menu or run '$APP_NAME' from the terminal"
    echo "Make sure $HOME/.local/bin is in your PATH"
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
    check_dependencies
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
