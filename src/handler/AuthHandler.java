package handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.UserService;
import model.User;
import util.JsonUtil;
import util.ResponseUtil;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AuthHandler implements HttpHandler {
    private final UserService userService;

    public AuthHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (ResponseUtil.handleCorsPreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("POST".equalsIgnoreCase(method)) {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            if (path.endsWith("/register")) {
                handleRegister(exchange, body);
            } else if (path.endsWith("/login")) {
                handleLogin(exchange, body);
            } else {
                ResponseUtil.sendJsonResponse(exchange, 404, JsonUtil.createResponse(false, "Endpoint not found", null));
            }
        } else {
            ResponseUtil.sendJsonResponse(exchange, 405, JsonUtil.createResponse(false, "Method not allowed", null));
        }
    }

    private void handleRegister(HttpExchange exchange, String body) throws IOException {
        String name = JsonUtil.getString(body, "name");
        String email = JsonUtil.getString(body, "email");
        String password = JsonUtil.getString(body, "password");
        String role = JsonUtil.getString(body, "role");

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "All fields are required.", null));
            return;
        }

        if (!"FARMER".equalsIgnoreCase(role) && !"BUYER".equalsIgnoreCase(role)) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Role must be FARMER or BUYER.", null));
            return;
        }

        if (userService.findByEmail(email) != null) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Email already registered.", null));
            return;
        }

        User user = userService.registerUser(name, email, password, role);
        if (user != null) {
            String userJson = JsonUtil.userToJson(user);
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Registration successful.", userJson));
        } else {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Registration failed.", null));
        }
    }

    private void handleLogin(HttpExchange exchange, String body) throws IOException {
        String email = JsonUtil.getString(body, "email");
        String password = JsonUtil.getString(body, "password");

        if (email.isEmpty() || password.isEmpty()) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Email and password are required.", null));
            return;
        }

        User user = userService.loginUser(email, password);
        if (user != null) {
            String userJson = JsonUtil.userToJson(user);
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Login successful.", userJson));
        } else {
            ResponseUtil.sendJsonResponse(exchange, 401, JsonUtil.createResponse(false, "Invalid email or password.", null));
        }
    }
}
