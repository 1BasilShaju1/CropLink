/* ====================================================
   CROPLINK BUYER MANAGEMENT LOGIC
   ==================================================== */

function initBuyerDashboard() {
    const user = checkAccess("BUYER");
    if (!user) return;

    const buyerNameEl = document.getElementById("buyerName");
    if (buyerNameEl) {
        buyerNameEl.textContent = user.name;
    }
}
