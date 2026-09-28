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

function checkAccess(requiredRole) {
    const user = getLoggedInUser();
    if (!user) {
        window.location.href = "login.html";
        return null;
    }
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

function updateNavbar() {
    const navLinks = document.getElementById("navLinks");
    if (!navLinks) return;

    const user = getLoggedInUser();
    if (user) {
        const dashboardLink = user.role === "FARMER" ? "farmer-dashboard.html" : "buyer-dashboard.html";
        navLinks.innerHTML = `
            <span class="user-info">Welcome, ${escapeHtml(user.name)} (${user.role})</span>
            <li><a href="${dashboardLink}">Dashboard</a></li>
            ${user.role === "FARMER" ? `
                <li><a href="my-products.html">My Products</a></li>
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

document.addEventListener("DOMContentLoaded", () => {
    updateNavbar();
});
