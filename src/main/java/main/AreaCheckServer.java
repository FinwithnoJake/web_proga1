package main;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AreaCheckServer {
    private static final CopyOnWriteArrayList<RequestResult> history = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        int port = 5000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Обработчик для проверки области
        server.createContext("/check", new AreaCheckHandler());
        // Обработчик для статических файлов
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("✅ Сервер запущен на http://localhost:" + port);
        System.out.println("📁 Открой в браузере: http://localhost:" + port);
        System.out.println("⏹️  Для остановки нажми Ctrl+C");
    }

    // Обработчик для проверки попадания
    static class AreaCheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long startTime = System.currentTimeMillis();

            try {
                if (!"GET".equals(exchange.getRequestMethod())) {
                    sendError(exchange, "Method not allowed", 405);
                    return;
                }

                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                String xStr = params.get("x");
                String yStr = params.get("y");
                String rStr = params.get("r");

                if (xStr == null || yStr == null || rStr == null) {
                    sendError(exchange, "Missing parameters: x, y, r required", 400);
                    return;
                }

                // Используем твои классы для валидации и проверки
                try {
                    int x = Integer.parseInt(xStr);
                    float y = Float.parseFloat(yStr);
                    float r = Float.parseFloat(rStr);

                    // Валидация с помощью твоего Validator
                    if (Validator.validateX(x) && Validator.validateY(y) && Validator.validateR(r)) {
                        // Проверка попадания с помощью твоего Checker
                        boolean hit = Checker.hit(x, y, r);

                        // Сохраняем результат
                        RequestResult result = new RequestResult(x, y, r, hit, LocalDateTime.now());
                        history.add(result);

                        // Отправляем успешный ответ
                        sendSuccessResponse(exchange, hit, startTime);
                    } else {
                        sendError(exchange, "Invalid parameter values", 400);
                    }

                } catch (NumberFormatException e) {
                    sendError(exchange, "Parameters must be numbers", 400);
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, "Server error: " + e.getMessage(), 500);
            }
        }

        private void sendSuccessResponse(HttpExchange exchange, boolean hit, long startTime) throws IOException {
            long executionTime = System.currentTimeMillis() - startTime;

            // Формируем JSON ответ (без text blocks)
            String json = "{" +
                    "\"result\": " + hit + "," +
                    "\"current_time\": \"" + LocalDateTime.now() + "\"," +
                    "\"execution_time_ms\": " + executionTime + "," +
                    "\"history\": " + getHistoryJson() +
                    "}";

            sendJsonResponse(exchange, json, 200);
        }

        private String getHistoryJson() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < history.size(); i++) {
                RequestResult item = history.get(i);
                sb.append("{")
                        .append("\"x\":").append(item.x).append(",")
                        .append("\"y\":").append(String.format("%.2f", item.y)).append(",")
                        .append("\"r\":").append(item.r).append(",")
                        .append("\"hit\":").append(item.hit).append(",")
                        .append("\"time\":\"").append(item.timestamp).append("\"")
                        .append("}");
                if (i < history.size() - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    // Обработчик для статических файлов (HTML, CSS, JS, изображения)
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // Корневой путь -> index.html
            if (path.equals("/") || path.equals("")) {
                path = "/index.html";
            }

            // Пытаемся найти файл в ресурсах
            InputStream resourceStream = getClass().getResourceAsStream(path);

            if (resourceStream != null) {
                // Файл найден - отдаём его
                byte[] content = readAllBytes(resourceStream);
                String contentType = getContentType(path);

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);

                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
                resourceStream.close();
            } else {
                // Файл не найден
                if (path.equals("/index.html")) {
                    serveDefaultPage(exchange);
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            }
        }

        private byte[] readAllBytes(InputStream inputStream) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".ico")) return "image/x-icon";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            return "text/plain";
        }

        private void serveDefaultPage(HttpExchange exchange) throws IOException {
            // HTML без text blocks
            String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Area Check Server</title>\n" +
                    "    <style>\n" +
                    "        body { font-family: Arial; padding: 40px; text-align: center; }\n" +
                    "        .status { color: green; font-size: 24px; margin: 20px; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <h1>✅ Сервер работает!</h1>\n" +
                    "    <div class=\"status\">Backend API готов к работе</div>\n" +
                    "    <p>Но статические файлы не найдены. Проверь структуру папок.</p>\n" +
                    "    <p>Проверить API: <a href=\"/check?x=1&y=2&r=3\">/check?x=1&y=2&r=3</a></p>\n" +
                    "</body>\n" +
                    "</html>";

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, html.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(html.getBytes());
            os.close();
        }
    }

    // Вспомогательные методы
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    result.put(pair[0], pair[1]);
                }
            }
        }
        return result;
    }

    private static void sendJsonResponse(HttpExchange exchange, String json, int status) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }

    private static void sendError(HttpExchange exchange, String message, int status) throws IOException {
        String json = "{\"error\": \"" + message + "\"}";
        sendJsonResponse(exchange, json, status);
    }

    // Класс для хранения истории запросов
    static class RequestResult {
        int x;
        float y;
        float r;
        boolean hit;
        LocalDateTime timestamp;

        RequestResult(int x, float y, float r, boolean hit, LocalDateTime timestamp) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.hit = hit;
            this.timestamp = timestamp;
        }
    }
}