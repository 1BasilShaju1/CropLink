/**
 * CropLink Farm2Door - API Client & Application State Utility
 */

const API_BASE = '';
const TOKEN_KEY = 'croplink_token';
const USER_KEY = 'croplink_user';
const CART_KEY = 'croplink_cart';

const Api = {
    // --- Auth Management ---
    getToken: () => localStorage.getItem(TOKEN_KEY),
    getUser: () => {
        try {
            return JSON.parse(localStorage.getItem(USER_KEY) || 'null');
        } catch (e) {
            return null;
        }
    },
    isTokenExpired: (token) => {
        if (!token) return true;
        try {
            const parts = token.split('.');
            if (parts.length !== 3) return true;
            const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
            if (!payload || !payload.exp) return false;
            return (payload.exp * 1000) < Date.now();
        } catch (e) {
            return true;
        }
    },
    setAuth: (token, user) => {
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        window.dispatchEvent(new CustomEvent('auth-changed', { detail: { token, user } }));
    },
    clearAuth: () => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        window.dispatchEvent(new CustomEvent('auth-changed', { detail: null }));
    },
    isLoggedIn: () => {
        const token = localStorage.getItem(TOKEN_KEY);
        if (!token) return false;
        if (Api.isTokenExpired(token)) {
            Api.clearAuth();
            return false;
        }
        return true;
    },
    getUserRole: () => {
        const user = Api.getUser();
        return user ? user.role : null;
    },
    logout: () => {
        Api.clearAuth();
        if (window.Toast) {
            Toast.info('Logged out successfully');
        }
        setTimeout(() => {
            window.location.href = '/pages/login.html';
        }, 500);
    },

    /**
     * Auth Guard: Ensures user is authenticated and has required role(s).
     * @param {Array<string>|string} allowedRoles - E.g. ['FARMER'] or 'CUSTOMER'
     */
    requireAuth: (allowedRoles = []) => {
        if (!Api.isLoggedIn()) {
            const currentPath = encodeURIComponent(window.location.pathname + window.location.search);
            window.location.href = `/pages/login.html?redirect=${currentPath}`;
            return false;
        }
        if (typeof allowedRoles === 'string') allowedRoles = [allowedRoles];
        if (allowedRoles.length > 0) {
            const role = Api.getUserRole();
            if (!allowedRoles.includes(role)) {
                if (window.Toast) Toast.error('Unauthorized access');
                // Redirect to respective dashboard
                if (role === 'FARMER') window.location.href = '/pages/farmer-dashboard.html';
                else if (role === 'ADMIN') window.location.href = '/pages/admin-dashboard.html';
                else if (role === 'DELIVERY_PARTNER') window.location.href = '/pages/delivery-dashboard.html';
                else if (role === 'CUSTOMER') window.location.href = '/pages/customer-dashboard.html';
                else window.location.href = '/pages/products.html';
                return false;
            }
        }
        return true;
    },

    // --- Core HTTP Request ---
    request: async (method, path, body = null, isMultipart = false) => {
        const headers = {};
        const token = Api.getToken();
        if (token) headers['Authorization'] = 'Bearer ' + token;
        if (!isMultipart) headers['Content-Type'] = 'application/json';

        const options = { method, headers };
        if (body) {
            options.body = isMultipart ? body : JSON.stringify(body);
        }

        try {
            const res = await fetch(API_BASE + path, options);
            if (res.status === 401) {
                Api.clearAuth();
                const isLoginPage = window.location.pathname.includes('login.html');
                if (!isLoginPage) {
                    window.location.href = '/pages/login.html?expired=true';
                }
                throw new Error('Session expired. Please log in again.');
            }

            let data;
            const contentType = res.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                data = await res.json();
            } else {
                const text = await res.text();
                data = { message: text || res.statusText };
            }

            if (!res.ok) {
                const errorMsg = data.message || (data.data && data.data.message) || 'Request failed with status ' + res.status;
                throw new Error(errorMsg);
            }
            return data;
        } catch (err) {
            console.warn(`[Api ${method} ${path}]`, err.message);
            throw err;
        }
    },

    get: (path) => Api.request('GET', path),
    post: (path, body) => Api.request('POST', path, body),
    put: (path, body) => Api.request('PUT', path, body),
    patch: (path, body) => Api.request('PATCH', path, body),
    delete: (path) => Api.request('DELETE', path),
    postForm: (path, formData) => Api.request('POST', path, formData, true),
    putForm: (path, formData) => Api.request('PUT', path, formData, true),

    // --- Formatting Helpers ---
    formatCurrency: (amount) => {
        const num = Number(amount) || 0;
        return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },

    formatDate: (dateStr) => {
        if (!dateStr) return 'N/A';
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return dateStr;
        return d.toLocaleDateString('en-IN', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    },

    // --- Cart Management ---
    cart: {
        getCart: () => {
            try {
                return JSON.parse(localStorage.getItem(CART_KEY) || '[]');
            } catch (e) {
                return [];
            }
        },
        saveCart: (items) => {
            localStorage.setItem(CART_KEY, JSON.stringify(items));
            window.dispatchEvent(new CustomEvent('cart-updated', { detail: items }));
        },
        addItem: (product, quantity = 1) => {
            const cart = Api.cart.getCart();
            const index = cart.findIndex(item => item.id === product.id);
            if (index > -1) {
                cart[index].quantity += Number(quantity);
            } else {
                cart.push({
                    id: product.id,
                    name: product.name,
                    price: Number(product.price) || 0,
                    unit: product.unit || 'kg',
                    farmerName: product.farmerName || 'Verified Farm',
                    imageUrl: product.imageUrl || (product.images && product.images[0]) || 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400',
                    quantity: Number(quantity)
                });
            }
            Api.cart.saveCart(cart);
            if (window.Toast) Toast.success(`Added ${product.name} to cart`);
        },
        updateQuantity: (productId, quantity) => {
            let cart = Api.cart.getCart();
            if (quantity <= 0) {
                cart = cart.filter(item => item.id !== productId);
            } else {
                const item = cart.find(i => i.id === productId);
                if (item) item.quantity = quantity;
            }
            Api.cart.saveCart(cart);
        },
        removeItem: (productId) => {
            const cart = Api.cart.getCart().filter(item => item.id !== productId);
            Api.cart.saveCart(cart);
            if (window.Toast) Toast.info('Item removed from cart');
        },
        clearCart: () => {
            Api.cart.saveCart([]);
        },
        getItemCount: () => {
            return Api.cart.getCart().reduce((sum, item) => sum + (item.quantity || 0), 0);
        },
        getSubtotal: () => {
            return Api.cart.getCart().reduce((sum, item) => sum + (item.price * item.quantity), 0);
        }
    }
};

window.Api = Api;
