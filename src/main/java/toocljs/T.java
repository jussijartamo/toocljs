package toocljs;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static spark.Spark.*;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@WebSocket
public class T {
    private static final Path CLOJURE_TEMPLATE = Paths.get("cljs/template.clj");
    private static final IFn READ_STRING = Clojure.var("clojure.core", "read-string");
    private static final IFn EVAL = Clojure.var("clojure.core", "eval");
    private static final AtomicReference<String> TEMPLATE = new AtomicReference<>();
    private static final AtomicReference<Map<Object, Object>> PROPERTIES =
            new AtomicReference<>(Collections.emptyMap());
    private static final ThreadLocal<RemoteEndpoint> SESSION_USER = new ThreadLocal<>();

    public static void prn(String str) throws IOException {
        SESSION_USER.get().sendString(str);
    }

    public static String getProperty(String p) {
        Object o = PROPERTIES.get().get(p);
        if(o == null) {
            throw new RuntimeException("Keyword " + p + " is undefined!");
        }
        return o.toString();
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws IOException {
        try {
            SESSION_USER.set(user.getRemote());
            final String code = removeZeroWidthSpace(message);
            Object invoke = READ_STRING.invoke(String.format(TEMPLATE.get(), code));
            EVAL.invoke(invoke);
        } catch (Throwable t) {
            prn(String.format("[{\"error\":\"%s\"}]", t.getMessage().replaceAll("\"", "'")));
            t.printStackTrace();
        } finally {
            user.disconnect();
        }
    }

    public static void main(String[] args) {
        final Optional<Path> properties = Stream.of(args).map(Paths::get).filter(p -> p.toFile().exists()).findFirst();
        if (properties.isPresent()) {
            Path propertyPath = properties.get();
            System.out.println("Using property file: " + propertyPath.toFile().getAbsolutePath());
            Runnable update = () -> {
                try {
                    Properties p = new Properties();
                    p.load(Files.newBufferedReader(propertyPath));
                    PROPERTIES.set(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            watch(propertyPath, update);
            update.run();
        } else {
            System.out.println("Give property file path as an argument!");
            PROPERTIES.set(Map.of("test", "jdbc:postgresql://localhost:5555/postgres?user=postgres&password=postgres"));
        }
        port(8000);
        staticFiles.expireTime(1L);
        staticFiles.externalLocation("html");
        webSocket("/t", T.class);
        init();
    }

    public static void watch(Path relativeFile, Runnable onChangeEvent) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Path file = relativeFile.getFileName();
            Path absolutePath = relativeFile.toAbsolutePath();
            WatchService ws = FileSystems.getDefault().newWatchService();
            absolutePath.getParent().register(ws, ENTRY_MODIFY);
            executorService.submit(() -> {
                while (true) {
                    final WatchKey key = ws.take();
                    final boolean isScriptChanged = key.pollEvents().stream()
                            .map(WatchEvent::context)
                            .anyMatch(file::equals);
                    if (isScriptChanged) {
                        onChangeEvent.run();
                    }
                    key.reset();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String removeZeroWidthSpace(String s) {
        return s.replaceAll("\u200B", "");
    }

    static {
        Runnable update = () -> {
            try {
                TEMPLATE.set(new String(Files.readAllBytes(CLOJURE_TEMPLATE)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        watch(CLOJURE_TEMPLATE, update);
        update.run();
    }
}
