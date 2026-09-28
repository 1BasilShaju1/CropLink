/* ====================================================
   CROPLINK ORDER MANAGEMENT LOGIC (BUYER & FARMER)
   ==================================================== */

// Load Buyer Orders
function loadBuyerOrders() {
    const user = checkAccess("BUYER");
    if (!user) return;

    fetch(`/api/orders/buyer?buyerId=${user.id}`)
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById("buyerOrdersContainer");
            if (!container) return;

            if (!data.success || !data.data || data.data.length === 0) {
                container.innerHTML = `<p style="text-align: center; font-size: 1.1rem; color: #616161;">No orders found.</p>`;
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
                if (o.orderStatus === "Accepted") badgeClass = "badge-accepted";
                if (o.orderStatus === "Rejected") badgeClass = "badge-rejected";
                if (o.orderStatus === "Completed") badgeClass = "badge-completed";

                html += `
                    <tr>
                        <td>#${o.id}</td>
                        <td><strong>${escapeHtml(o.productName)}</strong></td>
                        <td>${o.quantity}</td>
                        <td>₹${o.totalAmount.toFixed(2)}</td>
                        <td>${escapeHtml(o.orderDate)}</td>
                        <td><span class="badge ${badgeClass}">${escapeHtml(o.orderStatus)}</span></td>
                    </tr>
                `;
            });

            html += `
                        </tbody>
                    </table>
                </div>
            `;
            container.innerHTML = html;
        })
        .catch(err => console.error("Error loading buyer orders:", err));
}

// Load Farmer Received Orders
function loadFarmerOrders() {
    const user = checkAccess("FARMER");
    if (!user) return;

    fetch(`/api/orders/farmer?farmerId=${user.id}`)
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById("farmerOrdersContainer");
            if (!container) return;

            if (!data.success || !data.data || data.data.length === 0) {
                container.innerHTML = `<p style="text-align: center; font-size: 1.1rem; color: #616161;">No orders received yet.</p>`;
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
                if (o.orderStatus === "Accepted") badgeClass = "badge-accepted";
                if (o.orderStatus === "Rejected") badgeClass = "badge-rejected";
                if (o.orderStatus === "Completed") badgeClass = "badge-completed";

                html += `
                    <tr>
                        <td>#${o.id}</td>
                        <td><strong>${escapeHtml(o.productName)}</strong></td>
                        <td>Buyer #${o.buyerId}</td>
                        <td>${o.quantity}</td>
                        <td>₹${o.totalAmount.toFixed(2)}</td>
                        <td><span class="badge ${badgeClass}">${escapeHtml(o.orderStatus)}</span></td>
                        <td>
                            <div style="display: flex; gap: 8px;">
                                <select id="statusSelect_${o.id}" class="form-control" style="padding: 4px 8px; width: 130px;">
                                    <option value="Pending" ${o.orderStatus === 'Pending' ? 'selected' : ''}>Pending</option>
                                    <option value="Accepted" ${o.orderStatus === 'Accepted' ? 'selected' : ''}>Accepted</option>
                                    <option value="Rejected" ${o.orderStatus === 'Rejected' ? 'selected' : ''}>Rejected</option>
                                    <option value="Completed" ${o.orderStatus === 'Completed' ? 'selected' : ''}>Completed</option>
                                </select>
                                <button class="btn btn-primary" style="padding: 5px 12px; font-size: 0.9rem;" onclick="updateOrderStatus(${o.id})">Update Status</button>
                            </div>
                        </td>
                    </tr>
                `;
            });

            html += `
                        </tbody>
                    </table>
                </div>
            `;
            container.innerHTML = html;
        })
        .catch(err => console.error("Error loading farmer orders:", err));
}

// Update Order Status by Farmer
function updateOrderStatus(orderId) {
    const user = checkAccess("FARMER");
    if (!user) return;

    const selectEl = document.getElementById(`statusSelect_${orderId}`);
    if (!selectEl) return;

    const newStatus = selectEl.value;

    fetch(`/api/orders/status?orderId=${orderId}&farmerId=${user.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ orderStatus: newStatus })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            alert("Order status updated successfully.");
            loadFarmerOrders();
        } else {
            alert(data.message || "Failed to update order status.");
        }
    })
    .catch(err => alert("Error updating order status."));
}
