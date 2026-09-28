/* ====================================================
   CROPLINK FARMER MANAGEMENT LOGIC
   ==================================================== */

// Add Product Handler
function handleAddProduct(event) {
    event.preventDefault();
    const user = checkAccess("FARMER");
    if (!user) return;

    const productName = document.getElementById("productName").value.trim();
    const productCategory = document.getElementById("productCategory").value;
    const price = parseFloat(document.getElementById("price").value);
    const quantity = parseInt(document.getElementById("quantity").value);
    const unit = document.getElementById("unit").value;
    const quality = document.getElementById("quality").value;
    const availability = document.getElementById("availability").value;
    const deliveryOption = document.getElementById("deliveryOption").value;
    const paymentMethod = document.getElementById("paymentMethod").value;
    const description = document.getElementById("description").value.trim();

    if (!productName || price <= 0 || quantity <= 0) {
        showAlert("alertMsg", "Please provide a valid product name, price, and quantity.", false);
        return;
    }

    const btn = document.querySelector("#addProductForm button[type=submit]");
    if (btn) { btn.disabled = true; btn.textContent = "Saving..."; }

    const payload = {
        farmerId: user.id,
        productName,
        productCategory,
        price,
        quantity,
        unit,
        quality,
        availability,
        deliveryOption,
        paymentMethod,
        description
    };

    fetch("/api/products", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            showAlert("alertMsg", "✅ Product added successfully.", true);
            setTimeout(() => {
                window.location.href = "my-products.html";
            }, 1200);
        } else {
            showAlert("alertMsg", data.message || "Failed to add product.", false);
            if (btn) { btn.disabled = false; btn.textContent = "Save Product"; }
        }
    })
    .catch(err => {
        showAlert("alertMsg", "Error connecting to server.", false);
        if (btn) { btn.disabled = false; btn.textContent = "Save Product"; }
    });
}

// Load Farmer's Own Products
function loadFarmerProducts() {
    const user = checkAccess("FARMER");
    if (!user) return;

    const container = document.getElementById("farmerProductsContainer");
    if (container) {
        container.innerHTML = `
            <div style="text-align:center; padding:3rem;">
                <div class="spinner"></div>
                <p style="color:#616161; margin-top:1rem;">Loading your products...</p>
            </div>
        `;
    }

    fetch(`/api/products/farmer?farmerId=${user.id}`)
    .then(res => res.json())
    .then(data => {
        if (!container) return;

        if (!data.success || !data.data || data.data.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <div style="font-size: 3rem;">🌾</div>
                    <h3>No products yet</h3>
                    <p>Start adding your agricultural products to the market!</p>
                    <a href="add-product.html" class="btn btn-primary" style="margin-top:1rem;">➕ Add Your First Product</a>
                </div>
            `;
            return;
        }

        let html = `<div class="product-grid">`;
        data.data.forEach(p => {
            let badgeClass = "badge-available";
            if (p.availability === "Limited Stock") badgeClass = "badge-limited";
            if (p.availability === "Out of Stock") badgeClass = "badge-out";

            // Apply title case for professional display
            const displayName = toTitleCase(p.productName);
            const displayCategory = toTitleCase(p.productCategory);
            const displayDesc = toSentenceCase(p.description);
            const displayQuality = toTitleCase(p.quality);
            const displayDelivery = toTitleCase(p.deliveryOption);
            const displayPayment = toTitleCase(p.paymentMethod);

            html += `
                <div class="product-card">
                    <div>
                        <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:6px; margin-bottom:0.6rem;">
                            <span class="category-badge">${escapeHtml(displayCategory)}</span>
                            <span class="own-product-tag">🌾 My Product</span>
                        </div>
                        <h3>${escapeHtml(displayName)}</h3>
                        <div class="price-tag">₹${p.price.toFixed(2)} <span style="font-size:0.9rem; font-weight:400; color:#616161;">/ ${escapeHtml(p.unit)}</span></div>
                        <ul class="details-list">
                            <li><strong>📦 Stock:</strong> ${p.quantity} ${escapeHtml(p.unit)}</li>
                            <li><strong>⭐ Quality:</strong> ${escapeHtml(displayQuality)}</li>
                            <li><strong>🔖 Status:</strong> <span class="badge ${badgeClass}">${escapeHtml(p.availability)}</span></li>
                            <li><strong>🚚 Delivery:</strong> ${escapeHtml(displayDelivery)}</li>
                            <li><strong>💳 Payment:</strong> ${escapeHtml(displayPayment)}</li>
                        </ul>
                        <p class="product-desc">${escapeHtml(displayDesc)}</p>
                    </div>
                    <div style="display: flex; gap: 10px; margin-top: 1rem;">
                        <button class="btn btn-outline" style="flex:1;" onclick="openEditMode(${p.id})">✏️ Edit</button>
                        <button class="btn btn-danger" style="flex:1;" onclick="confirmDeleteProduct(${p.id})">🗑️ Delete</button>
                    </div>
                </div>
            `;
        });
        html += `</div>`;
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
    })
    .catch(err => {
        if (container) container.innerHTML = `<p style="text-align:center; color:#B71C1C;">⚠️ Error loading products.</p>`;
    });
}

// Delete Product with confirmation
function confirmDeleteProduct(productId) {
    const user = checkAccess("FARMER");
    if (!user) return;

    if (confirm("Are you sure you want to delete this product? This action cannot be undone.")) {
        fetch(`/api/products?id=${productId}&farmerId=${user.id}`, {
            method: "DELETE"
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                showToast("Product deleted successfully.");
                loadFarmerProducts();
            } else {
                alert(data.message || "Failed to delete product.");
            }
        })
        .catch(err => alert("Error deleting product."));
    }
}

// Toast notification
function showToast(message) {
    let toast = document.getElementById("toastMsg");
    if (!toast) {
        toast = document.createElement("div");
        toast.id = "toastMsg";
        document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.className = "toast show";
    setTimeout(() => { toast.className = "toast"; }, 3000);
}

// Open Edit Form and pre-fill values
function openEditMode(productId) {
    fetch(`/api/products?id=${productId}`)
    .then(res => res.json())
    .then(data => {
        if (data.success && data.data) {
            const p = data.data;
            document.getElementById("editProductId").value = p.id;
            document.getElementById("editProductName").value = p.productName;
            document.getElementById("editProductCategory").value = p.productCategory;
            document.getElementById("editPrice").value = p.price;
            document.getElementById("editQuantity").value = p.quantity;
            document.getElementById("editUnit").value = p.unit;
            document.getElementById("editQuality").value = p.quality;
            document.getElementById("editAvailability").value = p.availability;
            document.getElementById("editDeliveryOption").value = p.deliveryOption;
            document.getElementById("editPaymentMethod").value = p.paymentMethod;
            document.getElementById("editDescription").value = p.description;

            const editCard = document.getElementById("editFormCard");
            editCard.style.display = "block";
            editCard.scrollIntoView({ behavior: "smooth", block: "start" });
        }
    });
}

function handleUpdateProduct(event) {
    event.preventDefault();
    const user = checkAccess("FARMER");
    if (!user) return;

    const productId = parseInt(document.getElementById("editProductId").value);
    const productName = document.getElementById("editProductName").value.trim();
    const productCategory = document.getElementById("editProductCategory").value;
    const price = parseFloat(document.getElementById("editPrice").value);
    const quantity = parseInt(document.getElementById("editQuantity").value);
    const unit = document.getElementById("editUnit").value;
    const quality = document.getElementById("editQuality").value;
    const availability = document.getElementById("editAvailability").value;
    const deliveryOption = document.getElementById("editDeliveryOption").value;
    const paymentMethod = document.getElementById("editPaymentMethod").value;
    const description = document.getElementById("editDescription").value.trim();

    if (price <= 0 || quantity <= 0) {
        showAlert("editAlertMsg", "Price and quantity must be greater than zero.", false);
        return;
    }

    const btn = document.querySelector("#editProductForm button[type=submit]");
    if (btn) { btn.disabled = true; btn.textContent = "Updating..."; }

    const payload = {
        id: productId,
        farmerId: user.id,
        productName,
        productCategory,
        price,
        quantity,
        unit,
        quality,
        availability,
        deliveryOption,
        paymentMethod,
        description
    };

    fetch(`/api/products?id=${productId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            showAlert("editAlertMsg", "✅ Product updated successfully.", true);
            setTimeout(() => {
                document.getElementById("editFormCard").style.display = "none";
                loadFarmerProducts();
            }, 1000);
        } else {
            showAlert("editAlertMsg", data.message || "Failed to update product.", false);
            if (btn) { btn.disabled = false; btn.textContent = "Update Product"; }
        }
    })
    .catch(err => {
        showAlert("editAlertMsg", "Error updating product.", false);
        if (btn) { btn.disabled = false; btn.textContent = "Update Product"; }
    });
}
