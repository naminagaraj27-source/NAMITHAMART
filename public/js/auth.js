/**
 * NAMITHAMART — Auth & Session Handler
 */

const Auth = {
  init() {
    this.updateNavUser();
  },

  getCurrentUser() {
    return API.getUser();
  },

  isLoggedIn() {
    return !!API.getUser();
  },

  requireAuth(redirectUrl = '/login.html') {
    if (!this.isLoggedIn()) {
      window.location.href = redirectUrl;
      return false;
    }
    return true;
  },

  requireRole(allowedRoles = [], redirectUrl = '/login.html') {
    const user = this.getCurrentUser();
    if (!user || !allowedRoles.includes(user.role.toLowerCase())) {
      window.location.href = redirectUrl;
      return false;
    }
    return true;
  },

  updateNavUser() {
    const userContainer = document.getElementById('navUserContainer');
    if (!userContainer) return;

    const user = this.getCurrentUser();
    if (user) {
      let dashLink = '/buyer-dashboard.html';
      if (user.role === 'seller') dashLink = '/seller-dashboard.html';
      if (user.role === 'admin') dashLink = '/admin-dashboard.html';

      userContainer.innerHTML = `
        <div style="display: flex; align-items: center; gap: 12px;">
          <a href="${dashLink}" class="user-menu-btn" title="Dashboard">
            ${user.name} (${user.role.toUpperCase()})
          </a>
          <button onclick="Auth.logout()" class="btn btn-sm btn-light" style="padding: 6px 12px; font-size: 0.82rem;" title="Logout">
            Logout
          </button>
        </div>
      `;
    } else {
      userContainer.innerHTML = `
        <a href="/login.html" class="user-menu-btn">
          Sign In
        </a>
      `;
    }
  },

  async logout() {
    await API.logout();
    if (typeof showToast === 'function') {
      showToast('Logged out of NAMITHAMART successfully');
    }
    setTimeout(() => {
      window.location.href = '/login.html';
    }, 600);
  }
};

document.addEventListener('DOMContentLoaded', () => {
  Auth.init();
});
