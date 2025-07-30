#!/usr/bin/env bash

# Downloads the latest tarball from https://github.com/amir1376/ab-download-manager/releases and unpacks it into ~/.local/.
# Creates a .desktop entry for the app in ~/.local/share/applications based on FreeDesktop specifications.

set -euo pipefail

DEPENDENCIES=(curl tar)
LOG_FILE="/tmp/ab-dm-installer.log"

# --- Custom Logger
logger() {
  timestamp=$(date +"%Y/%m/%d %H:%M:%S")

  if [[ "$1" == "error" ]]; then
    # Red color for errors
    echo -e "${timestamp} -- ABDM-Installer [Error]: \033[0;31m$@\033[0m" | tee -a ${LOG_FILE}
  else
    # Default color for non-error messages
    echo -e "${timestamp} -- ABDM-Installer [Info]: $@" | tee -a ${LOG_FILE}
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

detect_package_manager

# --- Install dependencies
install_dependencies() {

    local answer
    read -p "Do you want to install $1? [Y/n]: " -r answer
    answer=${answer:-Y}  # Set default to 'Y' if no input is given

    case $answer in
        [Yy]* )
            sudo ${systemPackage} update -y
            logger "installing $1 package ..."
            sudo ${systemPackage} install -y $1
            ;;
        [Nn]* )
            logger "Skipping the installation of $1."
            ;;
        * )
            logger error "Please answer yes or no."
            install_dependencies "$1"  # re-prompt for the same package
            ;;
    esac
}

# Check dependencies and install if missing
check_dependencies() {
    for pkg in "${DEPENDENCIES[@]}"; do
        if ! command -v "$pkg" >/dev/null 2>&1; then
            logger "$pkg is not installed. Installing..."
            install_dependencies "$pkg"
        else
            logger "$pkg is already installed."
        fi
    done
}

APP_NAME="ABDownloadManager"
PLATFORM="linux"
ARCH="x64"
EXT="tar.gz"

RELEASE_URL="https://api.github.com/repos/amir1376/ab-download-manager/releases/latest"
GITHUB_RELEASE_DOWNLOAD="https://github.com/amir1376/ab-download-manager/releases/download"

LATEST_VERSION=$(curl -fSs "${RELEASE_URL}" | grep '"tag_name":' | sed -E 's/.*"tag_name": ?"([^"]+)".*/\1/')

ASSET_NAME="${APP_NAME}_${LATEST_VERSION:1}_${PLATFORM}_${ARCH}.${EXT}"
DOWNLOAD_URL="$GITHUB_RELEASE_DOWNLOAD/${LATEST_VERSION}/$ASSET_NAME"

APP_PATH="$HOME/.local/$APP_NAME"
BINARY_PATH="$APP_PATH/bin/$APP_NAME"
ICON_PATH="$APP_PATH/lib/$APP_NAME.png"


# --- Delete the old version Application if exists
delete_old_version() {
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

    # Remove old version directories
    # First Remove link to "$HOME/.local/$APP_NAME"
    remove_if_exists "$HOME/.local/bin/$APP_NAME"
    # then Remove the main binary files directory
    remove_if_exists "$HOME/.local/$APP_NAME"


    # Log the removal action
    logger "Removed old version of $APP_NAME"
}

# --- Generate a .desktop file for the app
generate_desktop_file() {
    cat <<EOF > "$HOME/.local/share/applications/com.abdownloadmanager.desktop"
[Desktop Entry]
Name=AB Download Manager
Comment=Manage and organize your download files better than before
GenericName=Downloader
Categories=Utility;Network;
Exec="$BINARY_PATH"
Icon=$ICON_PATH
Terminal=false
Type=Application
StartupWMClass=com-abdownloadmanager-desktop-AppKt
EOF
}

# --- Download the latest version of the app
download_zip() {
    # Remove the app tarball if it exists in /tmp
    remove_if_exists "/tmp/$ASSET_NAME"

    logger "downloading AB Download Manager ..."
    # Perform the download with curl
    if curl --progress-bar -fSL -o "/tmp/$ASSET_NAME" "${DOWNLOAD_URL}"; then
        logger "download finished successfully"
    else
        logger error "Download failed! Something Went Wrong"
        logger error "Check Your Internet Connectivity"
        # Optionally remove the partially downloaded file
        remove_if_exists "/tmp/$ASSET_NAME"
    fi
}


# --- Install the app
install_app() {

    logger "Installing AB Download Manager ..."
    # --- Setup ~/.local directories
    mkdir -p "$HOME/.local/bin" "$HOME/.local/share/applications"
    tar -xzf "/tmp/$ASSET_NAME" -C "$HOME/.local"

    # --- remove tarball after installation
    remove_if_exists "/tmp/$ASSET_NAME"

    # Link the binary to ~/.local/bin
    ln -sf "$BINARY_PATH" "$HOME/.local/bin/$APP_NAME"

    # Create a .desktop file in ~/.local/share/applications
    generate_desktop_file

    logger "AB Download Manager installed successfully"
    logger "it can be found in Applications menu or run '$APP_NAME' in terminal"
    logger "Make sure $HOME/.local/bin exists in PATH"
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
    check_dependencies
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
