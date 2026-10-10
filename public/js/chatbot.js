/**
 * NAMITHAMART — AI Assistant Chatbot Widget
 */

const Chatbot = {
  isOpen: false,

  init() {
    this.renderWidget();
    this.bindEvents();
  },

  renderWidget() {
    const launcher = document.createElement('div');
    launcher.className = 'chat-widget-launcher';
    launcher.id = 'chatLauncher';
    launcher.title = 'Chat with NAMITHAMART AI Assistant';
    launcher.innerHTML = `
      <svg viewBox="0 0 24 24">
        <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
      </svg>
    `;

    const windowEl = document.createElement('div');
    windowEl.className = 'chat-window';
    windowEl.id = 'chatWindow';
    windowEl.innerHTML = `
      <div class="chat-header">
        <div class="chat-header-info">
          <div class="chat-avatar">NM</div>
          <div class="chat-title-group">
            <h4>NAMITHAMART AI</h4>
            <div class="chat-status">
              <span class="chat-status-dot"></span> Online Assistant
            </div>
          </div>
        </div>
        <button class="chat-close-btn" id="chatCloseBtn">&times;</button>
      </div>

      <div class="chat-body" id="chatBody">
        <div class="chat-bubble bot">
          Hi! I'm NAMITHAMART AI Assistant. How can I help you?
        </div>
        <div class="chat-chips-container" id="chatChips">
          <button class="chat-chip" onclick="Chatbot.sendQuickMessage('How do I track my order?')">📦 Track Order</button>
          <button class="chat-chip" onclick="Chatbot.sendQuickMessage('How do I register as a seller?')">🏪 Sell on NAMITHAMART</button>
          <button class="chat-chip" onclick="Chatbot.sendQuickMessage('What is your return policy?')">🔄 Return Policy</button>
          <button class="chat-chip" onclick="Chatbot.sendQuickMessage('What payment methods are supported?')">💳 Payment Options</button>
          <button class="chat-chip" onclick="Chatbot.sendQuickMessage('Contact customer support')">📞 Support</button>
        </div>
      </div>

      <div class="chat-footer">
        <input type="text" id="chatInput" placeholder="Ask NAMITHAMART AI anything..." autocomplete="off" />
        <button class="chat-send-btn" id="chatSendBtn" title="Send message">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="white">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
          </svg>
        </button>
      </div>
    `;

    document.body.appendChild(launcher);
    document.body.appendChild(windowEl);
  },

  bindEvents() {
    const launcher = document.getElementById('chatLauncher');
    const closeBtn = document.getElementById('chatCloseBtn');
    const input = document.getElementById('chatInput');
    const sendBtn = document.getElementById('chatSendBtn');

    launcher.addEventListener('click', () => this.toggleChat());
    closeBtn.addEventListener('click', () => this.toggleChat());

    sendBtn.addEventListener('click', () => this.handleSend());
    input.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') this.handleSend();
    });
  },

  toggleChat() {
    this.isOpen = !this.isOpen;
    const win = document.getElementById('chatWindow');
    if (this.isOpen) {
      win.classList.add('open');
      document.getElementById('chatInput').focus();
    } else {
      win.classList.remove('open');
    }
  },

  async handleSend() {
    const input = document.getElementById('chatInput');
    const text = input.value.trim();
    if (!text) return;

    this.addBubble(text, 'user');
    input.value = '';

    // Typing indicator
    const typingId = this.addBubble('NAMITHAMART AI is typing...', 'bot');

    try {
      const data = await API.sendChatMessage(text);
      this.removeBubble(typingId);
      this.addBubble(data.reply || "I am here to assist with your NAMITHAMART shopping experience.", 'bot');
    } catch (err) {
      this.removeBubble(typingId);
      this.addBubble("I am having trouble connecting to the NAMITHAMART server. Please verify your connection.", 'bot');
    }
  },

  sendQuickMessage(text) {
    document.getElementById('chatInput').value = text;
    this.handleSend();
  },

  addBubble(text, sender) {
    const body = document.getElementById('chatBody');
    const bubble = document.createElement('div');
    const id = 'msg-' + Date.now() + Math.random().toString(36).substring(2, 5);
    bubble.id = id;
    bubble.className = `chat-bubble ${sender}`;
    bubble.innerText = text;
    body.appendChild(bubble);
    body.scrollTop = body.scrollHeight;
    return id;
  },

  removeBubble(id) {
    const el = document.getElementById(id);
    if (el) el.remove();
  }
};

document.addEventListener('DOMContentLoaded', () => {
  Chatbot.init();
});
