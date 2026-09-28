/* ====================================================
   CROPLINK ORDER MANAGEMENT LOGIC (BUYER & FARMER)
   ==================================================== */

// Load Buyer Orders
function loadBuyerOrders() {
    const user = checkAccess("BUYER");
    if (!user) return;

    const container = document.getElementById("buyerOrdersContainer");
    if (container) {
        container.innerHTML = `
            <div style="text-align:center; padding:3rem;">
                <div class="spinner"></div>
                <p style="color:#616161; margin-top:1rem;">Loading your orders...</p>
            </div>
        `;
    }

    fetch(`/api/orders/buyer?buyerId=${user.id}`)
        .then(res => res.json())
        .then(data => {
            if (!container) return;

            if (!data.success || !data.data || data.data.length === 0) {
                container.innerHTML = `
                    <div class="empty-state">
                        <div style="font-size: 3rem;">📦</div>
                        <h3>No orders yet</h3>
                        <p>Browse the market and place your first order!</p>
                        <a href="products.html" class="btn btn-primary" style="margin-top:1rem;">🛒 Browse Products</a>
                    </div>
                `;
                return;
            }

            let html = `
                <div class="table-container">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Order ID</th>
                                <th>Product Name</th>
                                <th>Quantity</th>
                                <th>Total Amount</th>
                                <th>Order Date</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
            `;

            data.data.forEach(o => {
                let badgeClass = "badge-pending";
                let statusIcon = "⏳";
                if (o.orderStatus === "Accepted") { badgeClass = "badge-accepted"; statusIcon = "✅"; }
                if (o.orderStatus === "Rejected") { badgeClass = "badge-rejected"; statusIcon = "❌"; }
                if (o.orderStatus === "Completed") { badgeClass = "badge-completed"; statusIcon = "🎉"; }

                html += `
                    <tr>
                        <td><strong>#${o.id}</strong></td>
                        <td><strong>${escapeHtml(toTitleCase(o.productName))}</strong></td>
                        <td>${o.quantity}</td>
                        <td><strong>₹${o.totalAmount.toFixed(2)}</strong></td>
                        <td style="font-size:0.9rem; color:#616161;">${escapeHtml(o.orderDate)}</td>
                        <td><span class="badge ${badgeClass}">${statusIcon} ${escapeHtml(o.orderStatus)}</span></td>
                    </tr>
                `;
            });

            html += `</tbody></table></div>`;
            container.innerHTML = html;
        })
        .catch(err => {
            if (container) container.innerHTML = `<p style="text-align:center; color:#B71C1C;">⚠️ Error loading orders.</p>`;
        });
}

// Load Farmer Received Orders
function loadFarmerOrders() {
    const user = checkAccess("FARMER");
    if (!user) return;

    const container = document.getElementById("farmerOrdersContainer");
    if (container) {
        container.innerHTML = `
            <div style="text-align:center; padding:3rem;">
                <div class="spinner"></div>
                <p style="color:#616161; margin-top:1rem;">Loading received orders...</p>
            </div>
        `;
    }

    fetch(`/api/orders/farmer?farmerId=${user.id}`)
        .then(res => res.json())
        .then(data => {
            if (!container) return;

            if (!data.success || !data.data || data.data.length === 0) {
                container.innerHTML = `
                    <div class="empty-state">
                        <div style="font-size: 3rem;">📋</div>
                        <h3>No orders received yet</h3>
                        <p>Your orders will appear here once buyers place them.</p>
                    </div>
                `;
                return;
            }

            let html = `
                <div class="table-container">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Order ID</th>
                                <th>Product Name</th>
                                <th>Buyer ID</th>
                                <th>Quantity</th>
                                <th>Total Amount</th>
                                <th>Current Status</th>
                                <th>Update Status</th>
                            </tr>
                        </thead>
                        <tbody>
            `;

            data.data.forEach(o => {
                let badgeClass = "badge-pending";
                let statusIcon = "⏳";
                if (o.orderStatus === "Accepted") { badgeClass = "badge-accepted"; statusIcon = "✅"; }
                if (o.orderStatus === "Rejected") { badgeClass = "badge-rejected"; statusIcon = "❌"; }
                if (o.orderStatus === "Completed") { badgeClass = "badge-completed"; statusIcon = "🎉"; }

                html += `
                    <tr>
                        <td><strong>#${o.id}</strong></td>
                        <td><strong>${escapeHtml(toTitleCase(o.productName))}</strong></td>
                        <td>Buyer #${o.buyerId}</td>
                        <td>${o.quantity}</td>
                        <td><strong>₹${o.totalAmount.toFixed(2)}</strong></td>
                        <td><span class="badge ${badgeClass}">${statusIcon} ${escapeHtml(o.orderStatus)}</span></td>
                        <td>
                            <div style="display: flex; gap: 8px; align-items:center;">
                                <select id="statusSelect_${o.id}" class="form-control" style="padding: 6px 10px; width: 145px;">
                                    <option value="Pending" ${o.orderStatus === 'Pending' ? 'selected' : ''}>⏳ Pending</option>
                                    <option value="Accepted" ${o.orderStatus === 'Accepted' ? 'selected' : ''}>✅ Accepted</option>
                                    <option value="Rejected" ${o.orderStatus === 'Rejected' ? 'selected' : ''}>❌ Rejected</option>
                                    <option value="Completed" ${o.orderStatus === 'Completed' ? 'selected' : ''}>🎉 Completed</option>
                                </select>
                                <button class="btn btn-primary" style="padding: 6px 14px; font-size: 0.9rem; white-space:nowrap;" onclick="updateOrderStatus(${o.id})">Update</button>
                            </div>
                        </td>
                    </tr>
                `;
            });

            html += `</tbody></table></div>`;
            container.innerHTML = html;
        })
        .catch(err => {
            if (container) container.innerHTML = `<p style="text-align:center; color:#B71C1C;">⚠️ Error loading orders.</p>`;
        });
}

// Update Order Status by Farmer
function updateOrderStatus(orderId) {
    const user = checkAccess("FARMER");
    if (!user) return;

    const selectEl = document.getElementById(`statusSelect_${orderId}`);
    if (!selectEl) return;

    // The option value= attributes are clean strings: "Pending", "Accepted", etc.
    const newStatus = selectEl.value;

    fetch(`/api/orders/status?orderId=${orderId}&farmerId=${user.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ orderStatus: newStatus })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            showToast("Order status updated successfully.");
            loadFarmerOrders();
        } else {
            alert(data.message || "Failed to update order status.");
        }
    })
    .catch(err => alert("Error updating order status."));
}
