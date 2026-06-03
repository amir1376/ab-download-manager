// Xeton Download Manager - Browser Extension Background Script

const NATIVE_HOST_NAME = "com.xeton.integration";

// Create context menu on installation
chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: "download-link-with-xeton",
    title: "Download link with Xeton",
    contexts: ["link", "video", "audio", "image"]
  });

  chrome.contextMenus.create({
    id: "download-page-with-xeton",
    title: "Download page with Xeton",
    contexts: ["page"]
  });

  chrome.contextMenus.create({
    id: "download-all-links-with-xeton",
    title: "Download all links with Xeton",
    contexts: ["page"]
  });
});

// Handle context menu clicks
chrome.contextMenus.onClicked.addListener((info, tab) => {
  if (info.menuItemId === "download-link-with-xeton") {
    let url = info.linkUrl || info.srcUrl || info.pageUrl;
    sendToXeton("add_download", url, tab.title);
  } else if (info.menuItemId === "download-page-with-xeton") {
    sendToXeton("add_download", info.pageUrl, tab.title);
  } else if (info.menuItemId === "download-all-links-with-xeton") {
    chrome.scripting.executeScript({
      target: { tabId: tab.id },
      func: () => {
        return Array.from(document.links).map(a => a.href).filter(href => href.startsWith('http'));
      }
    }, (results) => {
      if (chrome.runtime.lastError || !results || !results[0]) return;
      const links = [...new Set(results[0].result)]; // Remove duplicates
      
      // Send each link to Xeton
      // Ideally, the native host should support a batch add action
      // but for now we loop. To avoid overwhelming the host, we send them sequentially or with a slight delay
      links.forEach((url, i) => {
        setTimeout(() => {
          sendToXeton("add_download", url, tab.title + " (Link)");
        }, i * 50);
      });
    });
  }
});

// Handle extension icon click (downloads the current page URL)
chrome.action.onClicked.addListener((tab) => {
  if (tab && tab.url) {
    sendToXeton("add_download", tab.url, tab.title);
  }
});

// Stream Sniffing
chrome.webRequest.onBeforeRequest.addListener(
  function(details) {
    const url = details.url;
    if (url.includes('.m3u8') || url.includes('.mpd')) {
      chrome.tabs.get(details.tabId, (tab) => {
        if (chrome.runtime.lastError || !tab) return;
        const title = tab.title || "Unknown Video";
        chrome.storage.session.get(['detectedStreams'], function(result) {
          let streams = result.detectedStreams || [];
          if (!streams.find(s => s.url === url)) {
            streams.push({ url, title, type: url.includes('.m3u8') ? 'hls' : 'dash' });
            // keep only last 10
            if (streams.length > 10) streams.shift();
            chrome.storage.session.set({ detectedStreams: streams });
            
            // Highlight icon to indicate stream found
            chrome.action.setBadgeText({ text: "!", tabId: details.tabId });
            chrome.action.setBadgeBackgroundColor({ color: "#FF0000", tabId: details.tabId });
          }
        });
      });
    }
  },
  { urls: ["<all_urls>"] },
  []
);

// Listen for messages from popup
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "send_to_xeton") {
    sendToXeton(request.type === "hls" ? "add_hls_stream" : "add_download", request.url, request.title);
    sendResponse({ success: true });
  }
  return true;
});

// Send message to the Rust native host
function sendToXeton(action, url, title) {
  const message = {
    action: action,
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
