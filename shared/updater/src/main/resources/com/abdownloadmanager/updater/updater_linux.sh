APP_NAME="ABDownloadManager"
awaitTermination(){
  local processName="${1:?}"
  local count=0
  while true; do
    local pids=$(pidof "$processName")
    if [ -z "$pids" ]; then
      break
    fi
    if [ $count -eq 10 ]; then
      echo "timeout waiting for $processName to terminate"
      break
    fi
    echo "waiting for $processName to terminate"
    sleep 1
  done
}
stopApp(){
  echo "stopping the app"
  local pids=$(pidof "$APP_NAME")
  if [ -z "$pids" ]; then
    echo "no process found with name $APP_NAME"
    return
  fi
  kill -9 "$pids"
  awaitTermination "$APP_NAME"
  if [ $? -ne 0 ]; then
    echo "failed to stop $APP_NAME"
    return 1
  fi
  echo "process $APP_NAME stopped"
}
removeCurrentInstallation(){
  local installationFolder="${1:?}"
  filesToRemove=(
    "bin"
    "lib"
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
    echo "executing: cp -a \"$updateFile/.\" $installationFolder"
    cp -a "$updateFile/." "$installationFolder"
}

removeUpdateFiles(){
    local updateFile="$1"
    echo "removing update folder"
    echo "executing: rm -rf \"$updateFile\""
    rm -rf "$updateFile"
}
executablePath(){
  local installationFolder="${1:?}"
  echo "$installationFolder/bin/$APP_NAME"
}
executeProgram(){
  local installationFolder=$1
  local path=$(executablePath "$installationFolder")
  echo "starting $APP_NAME..."
  echo "executing: \"$path\""
  "$path"
}
main(){
  local updateFile="$1"
  local installationFolder="$2"

  stopApp "$installationFolder"
  if [ $? -ne 0 ]; then
      echo "returning back to program"
      executeProgram "$installationFolder"
      exit 1
  fi
  removeCurrentInstallation "$installationFolder"
  copyUpdateToInstallationFolder "$updateFile" "$installationFolder"
  removeUpdateFiles "$updateFile"
  executeProgram "$installationFolder"
}

main "$@"