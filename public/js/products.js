/* ====================================================
   CROPLINK PRODUCT BROWSING AND DETAILS LOGIC
   ==================================================== */

function loadAvailableProducts() {
    fetch("/api/products")
        .then(res => res.json())
        .then(data => {
            renderProductGrid(data.data || []);
        })
        .catch(err => console.error("Error loading products:", err));
}

function searchProducts() {
    const query = document.getElementById("searchInput").value.trim();
    if (!query) {
        loadAvailableProducts();
        return;
    }
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
    fetch(`/api/products/category?category=${encodeURIComponent(category)}`)
        .then(res => res.json())
        .then(data => {
            renderProductGrid(data.data || []);
        })
        .catch(err => console.error("Error filtering products:", err));
}

function renderProductGrid(products) {
    const container = document.getElementById("productGridContainer");
    if (!container) return;

    if (!products || products.length === 0) {
        container.innerHTML = `<p style="text-align: center; font-size: 1.1rem; color: #616161; grid-column: 1/-1;">No products found.</p>`;
        return;
    }

    let html = "";
    products.forEach(p => {
        let badgeClass = "badge-available";
        if (p.availability === "Limited Stock") badgeClass = "badge-limited";

        html += `
            <div class="product-card">
                <div>
                    <span class="category-badge">${escapeHtml(p.productCategory)}</span>
                    <h3>${escapeHtml(p.productName)}</h3>
                    <div class="price-tag">₹${p.price.toFixed(2)} / ${escapeHtml(p.unit)}</div>
                    <ul class="details-list">
                        <li><strong>Available Stock:</strong> ${p.quantity} ${escapeHtml(p.unit)}</li>
                        <li><strong>Quality:</strong> ${escapeHtml(p.quality)}</li>
                        <li><strong>Status:</strong> <span class="badge ${badgeClass}">${escapeHtml(p.availability)}</span></li>
                        <li><strong>Delivery:</strong> ${escapeHtml(p.deliveryOption)}</li>
                        <li><strong>Payment:</strong> ${escapeHtml(p.paymentMethod)}</li>
                    </ul>
                </div>
                <a href="product-details.html?id=${p.id}" class="btn btn-primary" style="width: 100%;">View Details</a>
            </div>
        `;
    });
    container.innerHTML = html;
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
                document.getElementById("productDetailsContainer").innerHTML = `<p>Product not found.</p>`;
            }
        })
        .catch(err => console.error(err));
}

function renderProductDetails(p) {
    const container = document.getElementById("productDetailsContainer");
    if (!container) return;

    let badgeClass = "badge-available";
    if (p.availability === "Limited Stock") badgeClass = "badge-limited";
    if (p.availability === "Out of Stock") badgeClass = "badge-out";

    container.innerHTML = `
        <div class="form-card" style="max-width: 700px;">
            <span class="category-badge">${escapeHtml(p.productCategory)}</span>
            <h2 style="text-align: left; margin-top: 10px;">${escapeHtml(p.productName)}</h2>
            <p style="font-size: 1.5rem; font-weight: 700; color: #2E7D32; margin-bottom: 15px;">₹${p.price.toFixed(2)} per ${escapeHtml(p.unit)}</p>
            
            <div style="background: #F1F8E9; padding: 15px; border-radius: 8px; margin-bottom: 20px;">
                <p><strong>Description:</strong> ${escapeHtml(p.description)}</p>
                <p style="margin-top: 8px;"><strong>Quality:</strong> ${escapeHtml(p.quality)} | <strong>Availability:</strong> <span class="badge ${badgeClass}">${escapeHtml(p.availability)}</span></p>
                <p style="margin-top: 8px;"><strong>Stock Left:</strong> ${p.quantity} ${escapeHtml(p.unit)}</p>
                <p style="margin-top: 8px;"><strong>Delivery Option:</strong> ${escapeHtml(p.deliveryOption)} | <strong>Payment Method:</strong> ${escapeHtml(p.paymentMethod)}</p>
            </div>

            <div id="orderAlertMsg" class="alert"></div>

            <form id="placeOrderForm" onsubmit="handlePlaceOrder(event)">
                <div class="form-group">
                    <label for="orderQuantity">Quantity Needed (${escapeHtml(p.unit)}):</label>
                    <input type="number" id="orderQuantity" class="form-control" min="1" max="${p.quantity}" value="1" oninput="calculateTotalAmount()" required>
                </div>

                <div class="form-group" style="background: #FFF9C4; padding: 12px; border-radius: 6px; text-align: center;">
                    <label style="margin:0; font-size: 1.2rem; color: #F57F17;">Total Amount: <span id="totalAmountDisplay">₹${p.price.toFixed(2)}</span></label>
                </div>

                <button type="submit" class="btn btn-secondary" style="width: 100%; font-size: 1.1rem; padding: 12px;">Place Order</button>
            </form>
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
    const user = checkAccess("BUYER");
    if (!user) return;

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
            showAlert("orderAlertMsg", "Order placed successfully.", true);
            setTimeout(() => {
                window.location.href = "buyer-orders.html";
            }, 1200);
        } else {
            showAlert("orderAlertMsg", data.message || "Failed to place order.", false);
        }
    })
    .catch(err => showAlert("orderAlertMsg", "Error connecting to server.", false));
}
