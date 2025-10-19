cask "ab-download-manager" do
  version "1.7.1"

  arch arm: "arm64", intel: "x64"

  sha256 arm:   "b19c223e90e3504e9b97e6078475c09199f6ca81d65f1fda1836e0e71a494718",
         intel: "00d28871cb5cb6f4c9d822df4bd3475900f953916a2664d2074ddaf66ad86004"

  url "https://github.com/amir1376/ab-download-manager/releases/download/v#{version}/ABDownloadManager_#{version}_mac_#{arch}.dmg",
      verified: "github.com/amir1376/ab-download-manager/"

  name "AB Download Manager"
  desc "Download manager that speeds up your downloads"
  homepage "https://abdownloadmanager.com/"

  livecheck do
    url :url
    strategy :github_latest
  end

  app "ABDownloadManager.app"
end
