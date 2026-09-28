# CROPLINK - FARMER PRODUCT MANAGEMENT SYSTEM

CropLink is a simple, database-free, pure Core Java web application built for college project demonstration. It enables farmers to add and manage agricultural produce while allowing buyers to browse, search, filter, and place orders directly.

---

## 🚀 Features

### 🌾 Farmer Role
- **Register & Login** as a Farmer.
- **Farmer Dashboard**: Direct links to manage products and orders.
- **Add Product**: Create listings with categories (Vegetables, Fruits, Grains, Spices, Other Products), pricing, quantity, unit, quality, availability, delivery, and payment options.
- **My Products**: View, edit, or delete self-owned agricultural products.
- **Order Management**: View received orders for listed products and update status (`Pending`, `Accepted`, `Rejected`, `Completed`).

### 🛒 Buyer Role
- **Register & Login** as a Buyer.
- **Buyer Dashboard**: Direct links to browse produce and view order history.
- **Browse Produce**: View available products with real-time stock indicators.
- **Search & Filter**: Search products by name and filter by category.
- **Product Details & Ordering**: View complete details, dynamically calculate total amount (`Total Amount = Price × Quantity`), and place orders.
- **My Orders**: View personal order history and current processing status.

---

## 🛠️ Technology Stack

- **Backend**: Core Java 17+, `com.sun.net.httpserver.HttpServer`, `ArrayList` In-Memory Storage.
- **Frontend**: HTML5, Vanilla CSS3 (Agricultural Design Theme: Green `#2E7D32`, Accent Yellow `#F9A825`), Vanilla JavaScript (Fetch API, `localStorage`).
- **Dependencies**: 0 external libraries or frameworks (No Spring Boot, MySQL, Maven, Gradle, Jackson, Gson, or React).

---

## 📁 Project Structure

```
CropLink/
│
├── src/
│   ├── Main.java
│   │
│   ├── model/
│   │   ├── User.java
│   │   ├── Product.java
│   │   └── Order.java
│   │
│   ├── service/
│   │   ├── UserService.java
│   │   ├── ProductService.java
│   │   └── OrderService.java
│   │
│   ├── handler/
│   │   ├── AuthHandler.java
│   │   ├── ProductHandler.java
│   │   ├── OrderHandler.java
│   │   └── StaticFileHandler.java
│   │
│   └── util/
│       ├── JsonUtil.java
│       └── ResponseUtil.java
│
├── public/
│   ├── index.html
│   ├── register.html
│   ├── login.html
│   ├── farmer-dashboard.html
│   ├── buyer-dashboard.html
│   ├── add-product.html
│   ├── my-products.html
│   ├── products.html
│   ├── product-details.html
│   ├── farmer-orders.html
│   ├── buyer-orders.html
│   │
│   ├── css/
│   │   └── style.css
│   │
│   └── js/
│       ├── auth.js
│       ├── farmer.js
│       ├── buyer.js
│       ├── products.js
│       └── orders.js
│
└── README.md
```

---

## ⚙️ How to Run in VS Code

1. Open the `CropLink` folder in Visual Studio Code.
2. Open the integrated terminal (PowerShell).

### Step 1: Compile the Java Project
```powershell
javac --add-modules jdk.httpserver -d out (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

### Step 2: Run the Application Server
```powershell
java --add-modules jdk.httpserver -cp out Main
```

### Step 3: Open in Browser
Open your browser and navigate to:
```
http://localhost:8080
```

---

## 🔑 Demo Test Accounts

- **Sample Farmer**:
  - Email: `farmer@test.com`
  - Password: `1234`
- **Sample Buyer**:
  - Email: `buyer@test.com`
  - Password: `1234`
