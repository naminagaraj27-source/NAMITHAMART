/**
 * NAMITHAMART — REST API Client
 * Interfaces with Java Backend
 */

const API = {
  baseUrl: '',

  getToken() {
    return localStorage.getItem('nm_token');
  },

  setAuth(token, user) {
    if (token) localStorage.setItem('nm_token', token);
    if (user) localStorage.setItem('nm_user', JSON.stringify(user));
  },

  getUser() {
    const raw = localStorage.getItem('nm_user');
    try {
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  },

  clearAuth() {
    localStorage.removeItem('nm_token');
    localStorage.removeItem('nm_user');
  },

  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers
      });

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(data.message || data.error || `HTTP ${response.status}`);
      }

      return data;
    } catch (err) {
      console.error(`[API Error] ${endpoint}:`, err);
      throw err;
    }
  },

  // Auth Endpoints
  async login(email, password) {
    const data = await this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });
    if (data.token && data.user) {
      this.setAuth(data.token, data.user);
    }
    return data;
  },

  async register(name, email, password, role = 'buyer', storeName = '') {
    const data = await this.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ name, email, password, role, storeName })
    });
    if (data.token && data.user) {
      this.setAuth(data.token, data.user);
    }
    return data;
  },

  async me() {
    const user = this.getUser();
    const query = user ? `?userId=${user.id}` : '';
    return this.request(`/api/auth/me${query}`);
  },

  async logout() {
    try {
      await this.request('/api/auth/logout', { method: 'POST' });
    } catch (e) {}
    this.clearAuth();
  },

  // Products Endpoints
  async getProducts(params = {}) {
    const query = new URLSearchParams();
    if (params.category && params.category !== 'All') query.append('category', params.category);
    if (params.search) query.append('search', params.search);
    if (params.sellerId) query.append('sellerId', params.sellerId);
    if (params.sort) query.append('sort', params.sort);
    const qs = query.toString() ? `?${query.toString()}` : '';
    return this.request(`/api/products${qs}`);
  },

  async getProduct(id) {
    return this.request(`/api/products/${id}`);
  },

  async getCategories() {
    return this.request('/api/products/categories');
  },

  async createProduct(productData) {
    return this.request('/api/products', {
      method: 'POST',
      body: JSON.stringify(productData)
    });
  },

  async updateProduct(id, productData) {
    return this.request(`/api/products/${id}`, {
      method: 'PUT',
      body: JSON.stringify(productData)
    });
  },

  async deleteProduct(id) {
    return this.request(`/api/products/${id}`, {
      method: 'DELETE'
    });
  },

  // Cart Endpoints
  async getCart(userId) {
    const uid = userId || (this.getUser() ? this.getUser().id : 4);
    return this.request(`/api/cart?userId=${uid}`);
  },

  async addToCart(productId, quantity = 1, userId) {
    const uid = userId || (this.getUser() ? this.getUser().id : 4);
    return this.request('/api/cart/add', {
      method: 'POST',
      body: JSON.stringify({ userId: uid, productId, quantity })
    });
  },

  async updateCartItem(cartId, quantity) {
    return this.request('/api/cart/update', {
      method: 'PUT',
      body: JSON.stringify({ cartId, quantity })
    });
  },

  async removeCartItem(cartId) {
    return this.request(`/api/cart/remove?id=${cartId}`, {
      method: 'DELETE'
    });
  },

  async clearCart(userId) {
    const uid = userId || (this.getUser() ? this.getUser().id : 4);
    return this.request(`/api/cart/clear?userId=${uid}`, {
      method: 'POST'
    });
  },

  // Orders Endpoints
  async checkout(orderPayload) {
    return this.request('/api/orders/checkout', {
      method: 'POST',
      body: JSON.stringify(orderPayload)
    });
  },

  async getOrders(params = {}) {
    const query = new URLSearchParams();
    if (params.userId) query.append('userId', params.userId);
    if (params.sellerId) query.append('sellerId', params.sellerId);
    const qs = query.toString() ? `?${query.toString()}` : '';
    return this.request(`/api/orders${qs}`);
  },

  async getOrder(id) {
    return this.request(`/api/orders/${id}`);
  },

  async updateOrderStatus(id, status) {
    return this.request(`/api/orders/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status })
    });
  },

  // Reviews Endpoints
  async getReviews(productId) {
    return this.request(`/api/reviews?productId=${productId}`);
  },

  async createReview(reviewData) {
    return this.request('/api/reviews', {
      method: 'POST',
      body: JSON.stringify(reviewData)
    });
  },

  // Seller Endpoints
  async getSellerStats(sellerId) {
    const sid = sellerId || (this.getUser() ? this.getUser().id : 2);
    return this.request(`/api/seller/stats?sellerId=${sid}`);
  },

  // Admin Endpoints
  async getAdminStats() {
    return this.request('/api/admin/stats');
  },

  async getAdminUsers() {
    return this.request('/api/admin/users');
  },

  async deleteUser(userId) {
    return this.request(`/api/admin/users/${userId}`, {
      method: 'DELETE'
    });
  },

  // Chatbot Endpoint
  async sendChatMessage(message) {
    return this.request('/api/chat', {
      method: 'POST',
      body: JSON.stringify({ message })
    });
  }
};
