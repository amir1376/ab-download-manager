APP_NAME="ABDownloadManager"

awaitTermination(){
  local processName="${1:?}"
  local count=0
  while true; do
    local pids=$(pgrep -x "$processName")
    if [ -z "$pids" ]; then
      break
    fi
    if [ $count -eq 10 ]; then
      echo "timeout waiting for $processName to terminate"
      break
    fi
    echo "waiting for $processName to terminate"
    sleep 1
    count=$((count + 1))
  done
}

stopApp(){
  echo "stopping the app"
  local pids=$(pgrep -x "$APP_NAME")
  if [ -z "$pids" ]; then
    echo "no process found with name $APP_NAME"
    return
  fi
  kill -9 $pids
  awaitTermination "$APP_NAME"
  if [ $? -ne 0 ]; then
    echo "failed to stop $APP_NAME"
    return 1
  fi
  echo "process $APP_NAME stopped"
}

removeCurrentInstallation(){
  local installationFolder="${1:?}"
  echo "removing current installation"
  echo "executing rm -rf \"$installationFolder\""
  rm -rf "$installationFolder"
}

copyUpdateToInstallationFolder(){
  local updateFile="$1"
  local installationFolder="${2:?}"
  echo "copying update files to installation folder"
  echo "executing: cp -Rp \"$updateFile\" \"$installationFolder\""
  cp -Rp "$updateFile" "$installationFolder"
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
  echo "executing: open \"$installationFolder\""
  open "$installationFolder"
}

main(){
  local updateFile="$1"
  local installationFolder="$2"

  stopApp "$APP_NAME"
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