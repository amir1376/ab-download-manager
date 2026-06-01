// Xeton Download Manager - Browser Extension Background Script

const NATIVE_HOST_NAME = "com.xeton.integration";

// Create context menu on installation
chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: "download-with-xeton",
    title: "Download with Xeton",
    contexts: ["link", "video", "audio", "image", "page"]
  });
});

// Handle context menu clicks
chrome.contextMenus.onClicked.addListener((info, tab) => {
  if (info.menuItemId === "download-with-xeton") {
    let url = info.linkUrl || info.srcUrl || info.pageUrl;
    sendToXeton(url, tab.title);
  }
});

// Handle extension icon click (downloads the current page URL)
chrome.action.onClicked.addListener((tab) => {
  if (tab && tab.url) {
    sendToXeton(tab.url, tab.title);
  }
});

// Send message to the Rust native host
function sendToXeton(url, title) {
  const message = {
    action: "add_download",
    url: url,
    title: title || "Unknown Title"
  };

  console.log("Sending to Xeton:", message);

  chrome.runtime.sendNativeMessage(NATIVE_HOST_NAME, message, (response) => {
    if (chrome.runtime.lastError) {
      console.error("Error communicating with native host:", chrome.runtime.lastError.message);
      // Optional: Fallback to regular browser download if Xeton is not running/installed
    } else {
      console.log("Xeton responded:", response);
    }
  });
}
