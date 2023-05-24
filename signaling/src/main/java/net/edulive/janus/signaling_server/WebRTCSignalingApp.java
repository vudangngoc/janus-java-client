package net.edulive.janus.signaling_server;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
public class WebRTCSignalingApp {
    private static final Map<WsContext, String> userUsernameMap = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(WebRTCSignalingApp.class);

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> config.staticFiles.add("/public", Location.CLASSPATH))
                .start(7070);
        MessageHandler messageHandler = new MessageHandler(System.getProperty("janus_address","localhost:8188"));

        app.ws("/signaling", ws -> {
            ws.onConnect(ctx -> {
                String username = randomString();
                userUsernameMap.put(ctx, username);
                messageHandler.createSession(username,ctx);
                ctx.send("{\"type\":\"status\",\"status\":\"connected\"}");
                logger.info("{} joined", username);
            });
            ws.onClose(ctx -> {
                String username = userUsernameMap.get(ctx);
                userUsernameMap.remove(ctx);
                messageHandler.destroySession(username);
                logger.info("{} left ", username);
            });
            ws.onMessage(ctx -> {
                String username = userUsernameMap.get(ctx);
                logger.info("{} send {}", username,ctx.message());
                messageHandler.handleMessage(userUsernameMap.get(ctx), ctx);
            });
        });
    }
        private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        private static final Random random = new Random();
        public static String randomString() {
            int length = 20; // the desired length of the random string
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int index = random.nextInt(CHARACTERS.length());
                sb.append(CHARACTERS.charAt(index));
            }
            return sb.toString();
        }
}
