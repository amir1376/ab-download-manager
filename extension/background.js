// Xeton Download Manager - Browser Extension Background Service Worker
// MV3 Compliant

const NATIVE_HOST_NAME = "com.xeton.integration";

// Task 4.1: Asynchronous Message Queueing
let messageQueue = [];
let isProcessingQueue = false;

// Task 2.2: Domain-Aware Rules Engine (In-memory cache)
const DEFAULT_RULES = {
  "strict-site.com": "Strict"
};

async function getDomainRules() {
  const result = await chrome.storage.local.get(['domainRules']);
  return result.domainRules || DEFAULT_RULES;
}

chrome.runtime.onInstalled.addListener(async () => {
  // Task 3.1: Context Menu API Hooks
  chrome.contextMenus.create({
    id: "download-link-with-xeton",
    title: "Download link with Xeton",
    contexts: ["link"]
  });

  chrome.contextMenus.create({
    id: "download-video-with-xeton",
    title: "Download video with Xeton",
    contexts: ["video", "audio"]
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

  chrome.contextMenus.create({
    id: "download-selected-links-with-xeton",
    title: "Download selected links with Xeton",
    contexts: ["selection"]
  });

  // Setup DeclarativeNetRequest dynamic rules for stream interception
  await setupDynamicRules();
});

// Task 2.1: Network-Layer Filtering via DNR
async function setupDynamicRules() {
  const oldRules = await chrome.declarativeNetRequest.getDynamicRules();
  const oldRuleIds = oldRules.map(rule => rule.id);

  const rules = await getDomainRules();
  const newRules = [];
  let ruleId = 1;

  const strictDomains = Object.keys(rules).filter(d => rules[d] === "Strict");
  if (strictDomains.length > 0) {
    newRules.push({
      id: ruleId++,
      priority: 1,
      action: {
        type: "redirect",
        redirect: {
          regexSubstitution: chrome.runtime.getURL("intercept.html") + "?url=\\0"
        }
      },
      condition: {
        regexFilter: "^https?://.*\\.(mp4|mkv|avi|m3u8|mpd)($|\\?.*)",
        requestDomains: strictDomains,
        resourceTypes: ["main_frame", "sub_frame", "media", "xmlhttprequest"]
      }
    });
  }

  await chrome.declarativeNetRequest.updateDynamicRules({
    removeRuleIds: oldRuleIds,
    addRules: newRules
  });
}

// Task 4.2: Payload Minimization & Thin-Client
// Task 3.2: Contextual Payload Serialization
async function getContextPayload(url) {
  let cookies = null;
  try {
    const cookieStore = await chrome.cookies.getAll({ url: url });
    cookies = cookieStore.map(c => `${c.name}=${c.value}`).join('; ');
  } catch (e) {
    console.warn("Could not fetch cookies:", e);
  }

  return {
    cookies: cookies,
    userAgent: navigator.userAgent
  };
}

async function sendToXeton(action, url, title, referrer = null) {
  const context = await getContextPayload(url);
  
  const message = {
    action: action,
    url: url,
    title: title || "Unknown Title",
    referrer: referrer,
    cookies: context.cookies,
    user_agent: context.userAgent
  };

  messageQueue.push(message);
  processQueue();
}

async function processQueue() {
  if (isProcessingQueue || messageQueue.length === 0) return;
  isProcessingQueue = true;

  while (messageQueue.length > 0) {
    const message = messageQueue.shift();
    try {
      // Native messaging bridge is non-blocking in MV3
      const response = await chrome.runtime.sendNativeMessage(NATIVE_HOST_NAME, message);
      console.log("Xeton responded:", response);
    } catch (e) {
      console.error("Error communicating with native host:", e);
    }
  }

  isProcessingQueue = false;
}

chrome.contextMenus.onClicked.addListener((info, tab) => {
  const referrer = tab ? tab.url : null;

  if (info.menuItemId === "download-link-with-xeton") {
    let url = info.linkUrl || info.srcUrl || info.pageUrl;
    sendToXeton("add_download", url, tab.title, referrer);
  } else if (info.menuItemId === "download-video-with-xeton") {
    let url = info.srcUrl || info.linkUrl;
    sendToXeton("add_download", url, tab.title + " (Video)", referrer);
  } else if (info.menuItemId === "download-page-with-xeton") {
    sendToXeton("add_download", info.pageUrl, tab.title, referrer);
  } else if (info.menuItemId === "download-all-links-with-xeton") {
    chrome.scripting.executeScript({
      target: { tabId: tab.id },
      func: () => {
        return Array.from(document.links).map(a => a.href).filter(href => href.startsWith('http'));
      }
    }, (results) => {
      if (chrome.runtime.lastError || !results || !results[0]) return;
      const links = [...new Set(results[0].result)];
      
      links.forEach((url) => {
        sendToXeton("add_download", url, tab.title + " (Link)", referrer);
      });
    });
  } else if (info.menuItemId === "download-selected-links-with-xeton") {
    chrome.scripting.executeScript({
      target: { tabId: tab.id },
      func: () => {
        const selection = window.getSelection();
        if (!selection.rangeCount) return [];
        const range = selection.getRangeAt(0);
        const container = document.createElement("div");
        container.appendChild(range.cloneContents());
        return Array.from(container.querySelectorAll("a")).map(a => a.href).filter(href => href.startsWith('http'));
      }
    }, (results) => {
      if (chrome.runtime.lastError || !results || !results[0]) return;
      const links = [...new Set(results[0].result)];
      
      links.forEach((url) => {
        sendToXeton("add_download", url, tab.title + " (Selected Link)", referrer);
      });
    });
  }
});

chrome.action.onClicked.addListener((tab) => {
  if (tab && tab.url) {
    sendToXeton("add_download", tab.url, tab.title, tab.url);
  }
});

chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "send_to_xeton") {
    sendToXeton(
      request.type === "hls" ? "add_hls_stream" : "add_download", 
      request.url, 
      request.title, 
      sender.tab ? sender.tab.url : null
    );
    sendResponse({ success: true });
  }
  return true;
});

// Intercept browser downloads natively
chrome.downloads.onCreated.addListener(async (downloadItem) => {
  // If rules say strictly intercept, we cancel and send to Xeton
  // Let's implement a simple heuristic:
  if (downloadItem.state === "in_progress" && !downloadItem.url.startsWith("blob:")) {
    chrome.downloads.cancel(downloadItem.id);
    await sendToXeton("add_download", downloadItem.url, downloadItem.filename, downloadItem.referrer);
  }
});
