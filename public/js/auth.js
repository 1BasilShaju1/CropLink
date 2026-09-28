/* ====================================================
   CROPLINK AUTH LOGIC AND NAVIGATION MANAGEMENT
   ==================================================== */

const API_BASE = "/api";

function getLoggedInUser() {
    const userStr = localStorage.getItem("loggedInUser");
    if (!userStr) return null;
    try {
        return JSON.parse(userStr);
    } catch (e) {
        return null;
    }
}

function setLoggedInUser(user) {
    localStorage.setItem("loggedInUser", JSON.stringify(user));
}

function logout() {
    localStorage.removeItem("loggedInUser");
    window.location.href = "index.html";
}

// Allow BUYER-only pages and shared pages (products, product-details)
function checkAccess(requiredRole) {
    const user = getLoggedInUser();
    if (!user) {
        window.location.href = "login.html";
        return null;
    }
    // products.html and product-details.html are open to both roles
    // Only strictly role-protected pages use requiredRole
    if (requiredRole && user.role !== requiredRole) {
        if (user.role === "FARMER") {
            window.location.href = "farmer-dashboard.html";
        } else {
            window.location.href = "buyer-dashboard.html";
        }
        return null;
    }
    return user;
}

// Check login only — allow any role (used on shared pages)
function checkLoggedIn() {
    const user = getLoggedInUser();
    if (!user) {
        window.location.href = "login.html";
        return null;
    }
    return user;
}

function updateNavbar() {
    const navLinks = document.getElementById("navLinks");
    if (!navLinks) return;

    const user = getLoggedInUser();
    if (user) {
        const dashboardLink = user.role === "FARMER" ? "farmer-dashboard.html" : "buyer-dashboard.html";
        navLinks.innerHTML = `
            <span class="user-info">👤 ${escapeHtml(toTitleCase(user.name))} <span class="role-badge">${user.role}</span></span>
            <li><a href="${dashboardLink}">Dashboard</a></li>
            ${user.role === "FARMER" ? `
                <li><a href="my-products.html">My Products</a></li>
                <li><a href="products.html">🌿 Market</a></li>
                <li><a href="farmer-orders.html">Orders</a></li>
            ` : `
                <li><a href="products.html">Browse Products</a></li>
                <li><a href="buyer-orders.html">My Orders</a></li>
            `}
            <li><a href="#" onclick="logout(); return false;" class="btn btn-secondary" style="padding: 5px 12px; color: #000;">Logout</a></li>
        `;
    } else {
        navLinks.innerHTML = `
            <li><a href="index.html">Home</a></li>
            <li><a href="login.html">Login</a></li>
            <li><a href="register.html" class="btn btn-secondary" style="padding: 5px 12px; color: #000;">Register</a></li>
        `;
    }
}

function showAlert(elementId, message, isSuccess) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.textContent = message;
    el.className = isSuccess ? "alert alert-success" : "alert alert-error";
    el.style.display = "block";
    setTimeout(() => {
        el.style.display = "none";
    }, 5000);
}

function escapeHtml(str) {
    if (!str) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

/**
 * Converts any string to Title Case for professional display.
 * e.g., "FRESH TOMATOES FROM FARM" => "Fresh Tomatoes From Farm"
 * e.g., "black pepper" => "Black Pepper"
 */
function toTitleCase(str) {
    if (!str) return "";
    return String(str)
        .toLowerCase()
        .split(" ")
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(" ");
}

/**
 * Sentence case: Only first letter of entire string uppercase.
 * e.g., "FRESH TOMATOES FROM THE LOCAL FARM." => "Fresh tomatoes from the local farm."
 */
function toSentenceCase(str) {
    if (!str) return "";
    const lower = String(str).toLowerCase();
    return lower.charAt(0).toUpperCase() + lower.slice(1);
}

document.addEventListener("DOMContentLoaded", () => {
    updateNavbar();
});
