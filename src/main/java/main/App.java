package main;

import com.fastcgi.FCGIInterface;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class App {
    private static final String TEMPLATE = """
            Content-Type: application/json
            Content-Length: %d

            %s""";
    public static void main(String[] args) {

            final var b = new FCGIInterface();
        while (b.FCGIaccept() >= 0) {
            try {
                Map<String, String> params = Params.parse(FCGIInterface.request.params.getProperty("QUERY_STRING"));
                int x  = Integer.parseInt(params.get("x"));
                float y  = Float.parseFloat(params.get("y"));
                int r  = Integer.parseInt(params.get("r"));

                if (Validator.validateX(x) && Validator.validateY(y) && Validator.validateR(r)) {
                    sendJson(String.format("{\"result\": %b}", Checker.hit(x,y,r)));
                } else {
                    sendJson("{\"error\": \"invalid data\"}");
                }
            } catch (NumberFormatException e) {
                sendJson("{\"error\": \"wrong param type\"}");
            } catch (NullPointerException e) {
                sendJson("{\"error\": \"missed necessary param\"}");
            } catch (Exception e) {
                sendJson(String.format("{\"error\": %s}", e));
            }
        }
    }
    private static void sendJson(String json) {
        System.out.println(json);
    }
}