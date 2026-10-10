/**
 * NAMITHAMART — Global Application Helper
 */

function formatCurrency(amount) {
  return '₹' + Number(amount || 0).toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

function showToast(message, type = 'info') {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.innerHTML = `
    <span>${message}</span>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

async function updateGlobalCartBadge() {
  const badge = document.getElementById('navCartCount');
  if (!badge) return;

  try {
    const data = await API.getCart();
    let count = 0;
    if (data.items) {
      count = data.items.reduce((sum, item) => sum + item.quantity, 0);
    }
    badge.innerText = count;
  } catch (e) {
    // Fallback to local storage if offline
    badge.innerText = '0';
  }
}

function handleGlobalSearch(event) {
  if (event) event.preventDefault();
  const input = document.getElementById('navSearchInput');
  if (!input) return;
  const query = input.value.trim();
  if (query) {
    window.location.href = `/products.html?search=${encodeURIComponent(query)}`;
  } else {
    window.location.href = '/products.html';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  updateGlobalCartBadge();

  const searchForm = document.getElementById('navSearchForm');
  if (searchForm) {
    searchForm.addEventListener('submit', handleGlobalSearch);
  }
});
