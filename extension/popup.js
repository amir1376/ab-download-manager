document.addEventListener('DOMContentLoaded', () => {
  const downloadPageBtn = document.getElementById('download-page');
  const streamsList = document.getElementById('streams-list');

  // Download Current Page
  downloadPageBtn.addEventListener('click', () => {
    chrome.tabs.query({active: true, currentWindow: true}, (tabs) => {
      if (tabs.length > 0) {
        chrome.runtime.sendMessage({
          action: "send_to_xeton",
          type: "http",
          url: tabs[0].url,
          title: tabs[0].title
        });
        window.close();
      }
    });
  });

  // Render Detected Streams
  chrome.storage.session.get(['detectedStreams'], (result) => {
    const streams = result.detectedStreams || [];
    if (streams.length > 0) {
      streamsList.innerHTML = '';
      
      // Clear the badge since we're viewing them
      chrome.tabs.query({active: true, currentWindow: true}, (tabs) => {
        if (tabs.length > 0) {
          chrome.action.setBadgeText({ text: "", tabId: tabs[0].id });
        }
      });

      // Render each stream
      streams.slice().reverse().forEach((stream) => {
        const li = document.createElement('li');
        li.className = 'stream-item';
        
        const titleDiv = document.createElement('div');
        titleDiv.className = 'stream-title';
        titleDiv.textContent = stream.title;
        
        const typeDiv = document.createElement('div');
        typeDiv.className = 'stream-type';
        typeDiv.textContent = `${stream.type.toUpperCase()} - ${new URL(stream.url).hostname}`;
        
        const btn = document.createElement('button');
        btn.className = 'download-btn';
        btn.textContent = 'Send to Xeton';
        btn.addEventListener('click', () => {
          chrome.runtime.sendMessage({
            action: "send_to_xeton",
            type: stream.type,
            url: stream.url,
            title: stream.title
          });
          window.close();
        });

        li.appendChild(titleDiv);
        li.appendChild(typeDiv);
        li.appendChild(btn);
        streamsList.appendChild(li);
      });
    }
  });
});
