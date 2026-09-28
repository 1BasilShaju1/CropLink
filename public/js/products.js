/* ====================================================
   CROPLINK PRODUCT BROWSING AND DETAILS LOGIC
   ==================================================== */

function loadAvailableProducts() {
    showLoadingSpinner("productGridContainer");
    fetch("/api/products")
        .then(res => res.json())
        .then(data => {
            renderProductGrid(data.data || []);
        })
        .catch(err => {
            document.getElementById("productGridContainer").innerHTML =
                `<p class="empty-msg" style="grid-column:1/-1;">⚠️ Could not load products. Please try again.</p>`;
        });
}

function searchProducts() {
    const query = document.getElementById("searchInput").value.trim();
    if (!query) {
        loadAvailableProducts();
        return;
    }
    showLoadingSpinner("productGridContainer");
    fetch(`/api/products/search?name=${encodeURIComponent(query)}`)
        .then(res => res.json())
        .then(data => {
            renderProductGrid(data.data || []);
        })
        .catch(err => console.error("Error searching products:", err));
}

function filterProductsByCategory() {
    const category = document.getElementById("categorySelect").value;
    if (!category || category === "All") {
        loadAvailableProducts();
        return;
    }
    showLoadingSpinner("productGridContainer");
    fetch(`/api/products/category?category=${encodeURIComponent(category)}`)
        .then(res => res.json())
        .then(data => {
            renderProductGrid(data.data || []);
        })
        .catch(err => console.error("Error filtering products:", err));
}

function showLoadingSpinner(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = `
        <div class="loading-spinner" style="grid-column:1/-1; text-align:center; padding: 3rem;">
            <div class="spinner"></div>
            <p style="color:#616161; margin-top: 1rem;">Loading products...</p>
        </div>
    `;
}

function renderProductGrid(products) {
    const container = document.getElementById("productGridContainer");
    if (!container) return;

    const loggedInUser = getLoggedInUser();
    const isFarmer = loggedInUser && loggedInUser.role === "FARMER";

    if (!products || products.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column:1/-1;">
                <div style="font-size: 3rem;">🌾</div>
                <h3>No products found</h3>
                <p>Try adjusting your search or category filter.</p>
            </div>
        `;
        return;
    }

    let html = "";
    products.forEach(p => {
        let badgeClass = "badge-available";
        let badgeIcon = "✅";
        if (p.availability === "Limited Stock") { badgeClass = "badge-limited"; badgeIcon = "⚠️"; }

        // Title case for display - professional look
        const displayName = toTitleCase(p.productName);
        const displayCategory = toTitleCase(p.productCategory);
        const displayDesc = toSentenceCase(p.description);
        const displayQuality = toTitleCase(p.quality);
        const displayDelivery = toTitleCase(p.deliveryOption);
        const displayPayment = toTitleCase(p.paymentMethod);

        // Show "My Product" tag if farmer is viewing their own product
        const isOwnProduct = isFarmer && loggedInUser.id === p.farmerId;
        const ownerTag = isOwnProduct
            ? `<span class="own-product-tag">🌾 My Product</span>`
            : "";

        const actionBtn = isFarmer
            ? `<a href="product-details.html?id=${p.id}" class="btn btn-outline" style="width:100%;">👁 View Details</a>`
            : `<a href="product-details.html?id=${p.id}" class="btn btn-primary" style="width:100%;">🛒 View & Order</a>`;

        html += `
            <div class="product-card" data-id="${p.id}">
                <div>
                    <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:6px; margin-bottom:0.6rem;">
                        <span class="category-badge">${escapeHtml(displayCategory)}</span>
                        ${ownerTag}
                    </div>
                    <h3>${escapeHtml(displayName)}</h3>
                    <div class="price-tag">₹${p.price.toFixed(2)} <span style="font-size:0.9rem; font-weight:400; color:#616161;">/ ${escapeHtml(p.unit)}</span></div>
                    <ul class="details-list">
                        <li><strong>📦 Stock:</strong> ${p.quantity} ${escapeHtml(p.unit)}</li>
                        <li><strong>⭐ Quality:</strong> ${escapeHtml(displayQuality)}</li>
                        <li><strong>🔖 Status:</strong> <span class="badge ${badgeClass}">${badgeIcon} ${escapeHtml(p.availability)}</span></li>
                        <li><strong>🚚 Delivery:</strong> ${escapeHtml(displayDelivery)}</li>
                        <li><strong>💳 Payment:</strong> ${escapeHtml(displayPayment)}</li>
                    </ul>
                    <p class="product-desc">${escapeHtml(displayDesc)}</p>
                </div>
                ${actionBtn}
            </div>
        `;
    });
    container.innerHTML = html;

    // Animate cards in
    container.querySelectorAll(".product-card").forEach((card, i) => {
        card.style.opacity = "0";
        card.style.transform = "translateY(20px)";
        setTimeout(() => {
            card.style.transition = "opacity 0.3s ease, transform 0.3s ease";
            card.style.opacity = "1";
            card.style.transform = "translateY(0)";
        }, i * 60);
    });
}

// Product Details Page Logic
let currentProduct = null;

function loadProductDetails() {
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get("id");

    if (!productId) {
        window.location.href = "products.html";
        return;
    }

    fetch(`/api/products?id=${productId}`)
        .then(res => res.json())
        .then(data => {
            if (data.success && data.data) {
                currentProduct = data.data;
                renderProductDetails(currentProduct);
            } else {
                document.getElementById("productDetailsContainer").innerHTML =
                    `<div class="empty-state"><div style="font-size:3rem;">❌</div><h3>Product not found</h3><p>This product may have been removed.</p></div>`;
            }
        })
        .catch(err => console.error(err));
}

function renderProductDetails(p) {
    const container = document.getElementById("productDetailsContainer");
    if (!container) return;

    const loggedInUser = getLoggedInUser();
    const isBuyer = loggedInUser && loggedInUser.role === "BUYER";
    const isFarmer = loggedInUser && loggedInUser.role === "FARMER";
    const isOwnProduct = isFarmer && loggedInUser.id === p.farmerId;

    let badgeClass = "badge-available";
    if (p.availability === "Limited Stock") badgeClass = "badge-limited";
    if (p.availability === "Out of Stock") badgeClass = "badge-out";

    const displayName = toTitleCase(p.productName);
    const displayCategory = toTitleCase(p.productCategory);
    const displayDesc = toSentenceCase(p.description);
    const displayQuality = toTitleCase(p.quality);
    const displayDelivery = toTitleCase(p.deliveryOption);
    const displayPayment = toTitleCase(p.paymentMethod);

    const isOutOfStock = p.availability === "Out of Stock" || p.quantity <= 0;

    // Build order section conditionally
    let orderSection = "";
    if (isBuyer && !isOutOfStock) {
        orderSection = `
            <div id="orderAlertMsg" class="alert"></div>
            <form id="placeOrderForm" onsubmit="handlePlaceOrder(event)">
                <div class="form-group">
                    <label for="orderQuantity">Quantity Needed (${escapeHtml(p.unit)}):</label>
                    <input type="number" id="orderQuantity" class="form-control" min="1" max="${p.quantity}" value="1" oninput="calculateTotalAmount()" required>
                </div>
                <div class="total-amount-box">
                    <span>Total Amount:</span>
                    <span id="totalAmountDisplay" class="total-value">₹${p.price.toFixed(2)}</span>
                </div>
                <button type="submit" class="btn btn-secondary" style="width: 100%; font-size: 1.1rem; padding: 14px; margin-top: 12px;">
                    🛒 Place Order
                </button>
            </form>
        `;
    } else if (isBuyer && isOutOfStock) {
        orderSection = `<div class="alert alert-error" style="display:block;">❌ This product is currently out of stock.</div>`;
    } else if (isFarmer && isOwnProduct) {
        orderSection = `
            <div class="info-notice">
                🌾 This is your product listing. You can manage it from 
                <a href="my-products.html" style="color:#2E7D32; font-weight:600;">My Products</a>.
            </div>
        `;
    } else if (isFarmer && !isOwnProduct) {
        orderSection = `
            <div class="info-notice">
                👁 You are viewing this as a Farmer. Only Buyers can place orders.
            </div>
        `;
    }

    container.innerHTML = `
        <div class="product-detail-card">
            <div class="detail-header">
                <div>
                    <span class="category-badge" style="font-size:0.9rem;">${escapeHtml(displayCategory)}</span>
                    ${isOwnProduct ? `<span class="own-product-tag" style="margin-left:8px;">🌾 My Product</span>` : ""}
                </div>
                <h2 style="margin-top:12px; color:#1B5E20; font-size:2rem;">${escapeHtml(displayName)}</h2>
                <p class="detail-price">₹${p.price.toFixed(2)} <span style="font-size:1rem; font-weight:400; color:#616161;">per ${escapeHtml(p.unit)}</span></p>
            </div>

            <div class="detail-info-grid">
                <div class="info-item">
                    <span class="info-label">📦 Stock Left</span>
                    <span class="info-value">${p.quantity} ${escapeHtml(p.unit)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">⭐ Quality</span>
                    <span class="info-value">${escapeHtml(displayQuality)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">🔖 Availability</span>
                    <span class="info-value"><span class="badge ${badgeClass}">${escapeHtml(p.availability)}</span></span>
                </div>
                <div class="info-item">
                    <span class="info-label">🚚 Delivery</span>
                    <span class="info-value">${escapeHtml(displayDelivery)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">💳 Payment</span>
                    <span class="info-value">${escapeHtml(displayPayment)}</span>
                </div>
            </div>

            <div class="detail-description">
                <strong>📝 Description</strong>
                <p>${escapeHtml(displayDesc)}</p>
            </div>

            ${orderSection}
        </div>
    `;
}

function calculateTotalAmount() {
    if (!currentProduct) return;
    const qtyInput = document.getElementById("orderQuantity");
    const totalDisplay = document.getElementById("totalAmountDisplay");
    const qty = parseInt(qtyInput.value) || 0;
    const total = currentProduct.price * qty;
    totalDisplay.textContent = `₹${total.toFixed(2)}`;
}

function handlePlaceOrder(event) {
    event.preventDefault();
    const user = getLoggedInUser();
    if (!user || user.role !== "BUYER") {
        showAlert("orderAlertMsg", "Only buyers can place orders.", false);
        return;
    }

    if (!currentProduct) return;

    const quantity = parseInt(document.getElementById("orderQuantity").value);

    if (quantity <= 0) {
        showAlert("orderAlertMsg", "Ordered quantity must be greater than zero.", false);
        return;
    }

    if (quantity > currentProduct.quantity) {
        showAlert("orderAlertMsg", `Ordered quantity cannot exceed available quantity (${currentProduct.quantity}).`, false);
        return;
    }

    const btn = document.querySelector("#placeOrderForm button[type=submit]");
    if (btn) { btn.disabled = true; btn.textContent = "Placing Order..."; }

    const payload = {
        buyerId: user.id,
        productId: currentProduct.id,
        quantity: quantity
    };

    fetch("/api/orders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            showAlert("orderAlertMsg", "✅ Order placed successfully!", true);
            setTimeout(() => {
                window.location.href = "buyer-orders.html";
            }, 1200);
        } else {
            showAlert("orderAlertMsg", data.message || "Failed to place order.", false);
            if (btn) { btn.disabled = false; btn.textContent = "🛒 Place Order"; }
        }
    })
    .catch(err => {
        showAlert("orderAlertMsg", "Error connecting to server.", false);
        if (btn) { btn.disabled = false; btn.textContent = "🛒 Place Order"; }
    });
}
