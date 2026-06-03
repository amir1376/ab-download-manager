const urlParams = new URLSearchParams(window.location.search);
const targetUrl = urlParams.get('url');

if (targetUrl) {
  // Check if it's an HLS or DASH stream
  const isStream = targetUrl.includes('.m3u8') || targetUrl.includes('.mpd');
  
  chrome.runtime.sendMessage({
    action: "send_to_xeton",
    type: isStream ? "hls" : "http",
    url: targetUrl,
    title: "Intercepted Media"
  }, (response) => {
    // Optionally close the tab or go back
    if (window.history.length > 1) {
      window.history.back();
    } else {
      window.close();
    }
  });
}
