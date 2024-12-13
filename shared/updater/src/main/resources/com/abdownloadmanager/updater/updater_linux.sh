APP_NAME="ABDownloadManager"

awaitTermination(){
  local processName="${1:?}"
  while pgrep -f "$processName" > /dev/null; do
    echo "waiting for process $processName to terminate"
    sleep 1
  done
}
stopApp(){
  echo "executing: pkill \"${APP_NAME}\""
  pkill -f "$APP_NAME"
  awaitTermination "$APP_NAME"
  echo "process $APP_NAME stopped"
}
removeCurrentInstallation(){
  local installationFolder="${1:?}"
  filesToRemove=(
    "bin"
    "libs"
  )
  echo "removing current installation"
  for filesToRemove in "${filesToRemove[@]}" ; do
      echo "executing rm -rf \"$installationFolder/$filesToRemove\""
      rm -rf "$installationFolder/$filesToRemove"
  done
}
copyUpdateToInstallationFolder(){
    local updateFile="$1"
    local installationFolder="${2:?"installationFolder not passed"}"
    echo "copying update files to installation folder"
    echo "executing: cp \"$installationFolder/*\" $installationFolder"
    cp "$installationFolder/*" "$installationFolder"
}

removeUpdateFiles(){
    local updateFile="$1"
    echo "removing update folder"
    echo "executing: rm -rf \"$updateFile\""
    rm -rf "$updateFile"
}

executeProgram(){
  local installationFolder=$1
  echo "starting $APP_NAME..."
  echo "executing: \"$installationFolder\"/$APP_NAME"
  "$installationFolder"/$APP_NAME
}
main(){
  local updateFile="$1"
  local installationFolder="$2"

  stopApp "$installationFolder"
  removeCurrentInstallation "$installationFolder"
  copyUpdateToInstallationFolder "$updateFile" "$installationFolder"
  removeUpdateFiles "$updateFile"
  executeProgram "$installationFolder"
}

main "$@"