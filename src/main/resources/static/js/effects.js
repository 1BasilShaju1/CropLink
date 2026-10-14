/**
 * CropLink Farm2Door - Premium Visual Effects & UI Utilities
 */

// ==========================================================================
// 1. Toast Notification System
// ==========================================================================
const Toast = {
    init: () => {
        if (!document.getElementById('toast-container')) {
            const container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }
    },

    show: (message, type = 'info', title = null, duration = 3500) => {
        Toast.init();
        const container = document.getElementById('toast-container');

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        const icons = {
            success: 'fa-circle-check',
            error: 'fa-circle-exclamation',
            warning: 'fa-triangle-exclamation',
            info: 'fa-circle-info'
        };

        const defaultTitles = {
            success: 'Success',
            error: 'Error',
            warning: 'Attention',
            info: 'Notice'
        };

        toast.innerHTML = `
            <div class="toast-icon">
                <i class="fa-solid ${icons[type] || icons.info}"></i>
            </div>
            <div class="toast-content">
                <div class="toast-title">${title || defaultTitles[type]}</div>
                <div class="toast-msg">${message}</div>
            </div>
            <button class="toast-close" aria-label="Close">
                <i class="fa-solid fa-xmark"></i>
            </button>
        `;

        toast.querySelector('.toast-close').addEventListener('click', () => {
            Toast.remove(toast);
        });

        container.appendChild(toast);

        const timer = setTimeout(() => {
            Toast.remove(toast);
        }, duration);

        toast._timer = timer;
    },

    remove: (toast) => {
        if (!toast) return;
        clearTimeout(toast._timer);
        toast.classList.add('toast-fade-out');
        setTimeout(() => {
            if (toast.parentElement) toast.parentElement.removeChild(toast);
        }, 300);
    },

    success: (msg, title) => Toast.show(msg, 'success', title),
    error: (msg, title) => Toast.show(msg, 'error', title),
    warning: (msg, title) => Toast.show(msg, 'warning', title),
    info: (msg, title) => Toast.show(msg, 'info', title)
};
window.Toast = Toast;

// ==========================================================================
// 2. Loading Spinner System
// ==========================================================================
const Loading = {
    init: () => {
        if (!document.getElementById('global-spinner-overlay')) {
            const overlay = document.createElement('div');
            overlay.id = 'global-spinner-overlay';
            overlay.className = 'spinner-overlay';
            overlay.innerHTML = `
                <div class="spinner"></div>
                <div class="spinner-text" id="global-spinnerText">Loading, please wait...</div>
            `;
            document.body.appendChild(overlay);
        }
    },

    show: (text = 'Loading, please wait...') => {
        Loading.init();
        const overlay = document.getElementById('global-spinner-overlay');
        const textEl = document.getElementById('global-spinnerText');
        if (textEl) textEl.textContent = text;
        overlay.classList.add('active');
    },

    hide: () => {
        const overlay = document.getElementById('global-spinner-overlay');
        if (overlay) overlay.classList.remove('active');
    }
};
window.Loading = Loading;

// ==========================================================================
// 3. Cursor Trail Effect
// ==========================================================================
function initCursorTrail() {
    // Only enabled for desktop devices with fine pointer
    if (window.matchMedia('(pointer: coarse)').matches) return;

    const dot = document.createElement('div');
    dot.className = 'cursor-dot';

    const trail = document.createElement('div');
    trail.className = 'cursor-trail';

    document.body.appendChild(dot);
    document.body.appendChild(trail);

    let mouseX = -100, mouseY = -100;
    let trailX = -100, trailY = -100;

    window.addEventListener('mousemove', (e) => {
        mouseX = e.clientX;
        mouseY = e.clientY;
        dot.style.left = `${mouseX}px`;
        dot.style.top = `${mouseY}px`;
    });

    function animateTrail() {
        trailX += (mouseX - trailX) * 0.15;
        trailY += (mouseY - trailY) * 0.15;
        trail.style.left = `${trailX}px`;
        trail.style.top = `${trailY}px`;
        requestAnimationFrame(animateTrail);
    }
    requestAnimationFrame(animateTrail);

    // Hover expand on interactive elements
    const interactiveSelector = 'a, button, input, select, textarea, .card-hover-lift, .product-card';
    document.addEventListener('mouseover', (e) => {
        if (e.target.closest(interactiveSelector)) {
            document.body.classList.add('cursor-hover');
        }
    });
    document.addEventListener('mouseout', (e) => {
        if (e.target.closest(interactiveSelector)) {
            document.body.classList.remove('cursor-hover');
        }
    });
}

// ==========================================================================
// 4. Button Ripple Effect
// ==========================================================================
function initRippleEffect() {
    document.addEventListener('click', (e) => {
        const targetBtn = e.target.closest('.btn');
        if (!targetBtn) return;

        const rect = targetBtn.getBoundingClientRect();
        const ripple = document.createElement('span');
        ripple.className = 'ripple';

        const size = Math.max(rect.width, rect.height);
        ripple.style.width = ripple.style.height = `${size}px`;

        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;

        ripple.style.left = `${x}px`;
        ripple.style.top = `${y}px`;

        // Remove previous ripple if any
        const existing = targetBtn.querySelector('.ripple');
        if (existing) existing.remove();

        targetBtn.appendChild(ripple);

        setTimeout(() => {
            ripple.remove();
        }, 600);
    });
}

// ==========================================================================
// 5. Scroll Animations (IntersectionObserver)
// ==========================================================================
function initScrollAnimations() {
    const animElements = document.querySelectorAll('.fade-in-up, .slide-in-left, .slide-in-right, .zoom-in');

    if (!('IntersectionObserver' in window)) {
        animElements.forEach(el => el.classList.add('visible'));
        return;
    }

    const observer = new IntersectionObserver((entries, obs) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                obs.unobserve(entry.target);
            }
        });
    }, {
        root: null,
        threshold: 0.1,
        rootMargin: '0px 0px -40px 0px'
    });

    animElements.forEach(el => observer.observe(el));
}

// ==========================================================================
// 6. Smooth Page Transition Overlay
// ==========================================================================
function initPageTransitions() {
    let overlay = document.querySelector('.page-transition-overlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.className = 'page-transition-overlay';
        document.body.appendChild(overlay);
    }

    // Fade in current page
    requestAnimationFrame(() => {
        overlay.classList.remove('active');
    });

    // Intercept internal link clicks for smooth fade-out
    document.addEventListener('click', (e) => {
        const link = e.target.closest('a');
        if (!link) return;

        const href = link.getAttribute('href');
        if (!href || href.startsWith('#') || href.startsWith('javascript:') || link.target === '_blank') return;
        if (href.startsWith('mailto:') || href.startsWith('tel:')) return;

        // Check if internal navigation
        if (link.hostname === window.location.hostname || !href.startsWith('http')) {
            e.preventDefault();
            overlay.classList.add('active');
            setTimeout(() => {
                window.location.href = href;
            }, 250);
        }
    });
}

// ==========================================================================
// 7. Navbar Cart & Auth Syncer
// ==========================================================================
function updateNavbarState() {
    // Update cart counts
    if (window.Api && window.Api.cart) {
        const count = Api.cart.getItemCount();
        document.querySelectorAll('.cart-count').forEach(el => {
            el.textContent = count;
            el.style.display = count > 0 ? 'inline-block' : 'none';
        });
    }

    // Update Auth buttons or user profile in navbar
    const authNav = document.getElementById('navbar-auth-section');
    if (authNav && window.Api) {
        if (Api.isLoggedIn()) {
            const user = Api.getUser() || { fullName: 'User', role: 'CUSTOMER' };
            const role = user.role || 'CUSTOMER';
            let dashboardHref = '/pages/products.html';
            if (role === 'FARMER') dashboardHref = '/pages/farmer-dashboard.html';
            else if (role === 'ADMIN') dashboardHref = '/pages/admin-dashboard.html';

            authNav.innerHTML = `
                <div class="flex items-center gap-2">
                    <a href="${dashboardHref}" class="btn btn-outline btn-sm">
                        <i class="fa-solid fa-chart-line"></i> Dashboard
                    </a>
                    <a href="/pages/profile.html" class="btn btn-secondary btn-sm" title="My Profile">
                        <i class="fa-solid fa-user"></i> ${user.fullName.split(' ')[0]}
                    </a>
                    <button onclick="Api.logout()" class="btn btn-sm" style="color:#DC2626; background:transparent;" title="Logout">
                        <i class="fa-solid fa-arrow-right-from-bracket"></i>
                    </button>
                </div>
            `;
        } else {
            authNav.innerHTML = `
                <div class="flex items-center gap-2">
                    <a href="/pages/login.html" class="btn btn-outline btn-sm">Sign In</a>
                    <a href="/pages/register.html" class="btn btn-primary btn-sm">Join CropLink</a>
                </div>
            `;
        }
    }
}

// Listen to cart and auth events
window.addEventListener('cart-updated', updateNavbarState);
window.addEventListener('auth-changed', updateNavbarState);

// ==========================================================================
// 8. Auto Initialization on DOM Ready
// ==========================================================================
document.addEventListener('DOMContentLoaded', () => {
    initCursorTrail();
    initRippleEffect();
    initScrollAnimations();
    initPageTransitions();
    updateNavbarState();
});
