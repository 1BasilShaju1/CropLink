package handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import util.ResponseUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {
    private final String publicDir;

    public StaticFileHandler(String publicDir) {
        this.publicDir = publicDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (ResponseUtil.handleCorsPreflight(exchange)) return;

        String requestPath = exchange.getRequestURI().getPath();
        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }

        // Prevent directory traversal attacks
        Path filePath = Paths.get(publicDir, requestPath).normalize();
        Path basePath = Paths.get(publicDir).toAbsolutePath().normalize();

        if (!filePath.toAbsolutePath().normalize().startsWith(basePath)) {
            ResponseUtil.sendTextResponse(exchange, 403, "Access Denied", "text/plain");
            return;
        }

        File file = filePath.toFile();
        if (!file.exists() || file.isDirectory()) {
            ResponseUtil.sendTextResponse(exchange, 404, "404 Not Found", "text/plain");
            return;
        }

        String contentType = getContentType(file.getName());
        byte[] bytes = Files.readAllBytes(filePath);
        ResponseUtil.sendTextResponse(exchange, 200, new String(bytes, java.nio.charset.StandardCharsets.UTF_8), contentType);
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html; charset=UTF-8";
        if (fileName.endsWith(".css")) return "text/css; charset=UTF-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".ico")) return "image/x-icon";
        return "text/plain; charset=UTF-8";
    }
}
