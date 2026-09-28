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
        showAlert("alertMsg", "Please provide valid product details, price, and quantity.", false);
        return;
    }

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
            showAlert("alertMsg", "Product added successfully.", true);
            setTimeout(() => {
                window.location.href = "my-products.html";
            }, 1200);
        } else {
            showAlert("alertMsg", data.message || "Failed to add product.", false);
        }
    })
    .catch(err => {
        showAlert("alertMsg", "Error connecting to server.", false);
    });
}

// Load Farmer Products
function loadFarmerProducts() {
    const user = checkAccess("FARMER");
    if (!user) return;

    fetch(`/api/products/farmer?farmerId=${user.id}`)
    .then(res => res.json())
    .then(data => {
        const container = document.getElementById("farmerProductsContainer");
        if (!container) return;

        if (!data.success || !data.data || data.data.length === 0) {
            container.innerHTML = `<p style="text-align: center; font-size: 1.1rem; color: #616161;">No products found. Add your first product!</p>`;
            return;
        }

        let html = `<div class="product-grid">`;
        data.data.forEach(p => {
            let badgeClass = "badge-available";
            if (p.availability === "Limited Stock") badgeClass = "badge-limited";
            if (p.availability === "Out of Stock") badgeClass = "badge-out";

            html += `
                <div class="product-card">
                    <div>
                        <span class="category-badge">${escapeHtml(p.productCategory)}</span>
                        <h3>${escapeHtml(p.productName)}</h3>
                        <div class="price-tag">₹${p.price.toFixed(2)} / ${escapeHtml(p.unit)}</div>
                        <ul class="details-list">
                            <li><strong>Stock:</strong> ${p.quantity} ${escapeHtml(p.unit)}</li>
                            <li><strong>Quality:</strong> ${escapeHtml(p.quality)}</li>
                            <li><strong>Status:</strong> <span class="badge ${badgeClass}">${escapeHtml(p.availability)}</span></li>
                            <li><strong>Delivery:</strong> ${escapeHtml(p.deliveryOption)}</li>
                            <li><strong>Payment:</strong> ${escapeHtml(p.paymentMethod)}</li>
                        </ul>
                        <p style="font-size: 0.9rem; color: #616161; margin-bottom: 1rem;">${escapeHtml(p.description)}</p>
                    </div>
                    <div style="display: flex; gap: 10px;">
                        <button class="btn btn-outline" style="flex: 1; padding: 8px;" onclick="openEditMode(${p.id})">Edit</button>
                        <button class="btn btn-danger" style="flex: 1; padding: 8px;" onclick="confirmDeleteProduct(${p.id})">Delete</button>
                    </div>
                </div>
            `;
        });
        html += `</div>`;
        container.innerHTML = html;
    })
    .catch(err => console.error(err));
}

// Delete Product with confirmation
function confirmDeleteProduct(productId) {
    const user = checkAccess("FARMER");
    if (!user) return;

    if (confirm("Are you sure you want to delete this product?")) {
        fetch(`/api/products?id=${productId}&farmerId=${user.id}`, {
            method: "DELETE"
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert("Product deleted successfully.");
                loadFarmerProducts();
            } else {
                alert(data.message || "Failed to delete product.");
            }
        })
        .catch(err => alert("Error deleting product."));
    }
}

// Open Edit Form
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

            document.getElementById("editFormCard").style.display = "block";
            window.scrollTo({ top: document.getElementById("editFormCard").offsetTop - 20, behavior: 'smooth' });
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
            showAlert("editAlertMsg", "Product updated successfully.", true);
            setTimeout(() => {
                document.getElementById("editFormCard").style.display = "none";
                loadFarmerProducts();
            }, 1000);
        } else {
            showAlert("editAlertMsg", data.message || "Failed to update product.", false);
        }
    })
    .catch(err => showAlert("editAlertMsg", "Error updating product.", false));
}
