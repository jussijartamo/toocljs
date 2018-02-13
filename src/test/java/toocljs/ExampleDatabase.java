package toocljs;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

public class ExampleDatabase {
    private static final int PORT = 5555;
    private static final String EXAMPLE_DATABASE = "example.database.sql";

    public static void main(String[] args) throws Throwable {
        final String sql = new String(Files.readAllBytes(Paths.get(EXAMPLE_DATABASE)));
        final EmbeddedPostgres postgres = EmbeddedPostgres.builder().setPort(PORT).start();
        final Thread gracefulShutdownThread = new Thread(() -> {
            System.out.println("Shutting down database gracefully!");
            try {
                postgres.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Runtime.getRuntime().addShutdownHook(gracefulShutdownThread);
        final String url = "jdbc:postgresql://localhost:"+PORT+"/postgres?user=postgres&password=postgres";
        System.out.println("Database started: " + url);
        try(Connection connection = DriverManager.getConnection(url)) {
            connection.createStatement().execute(sql);
        }
        Thread.currentThread().join();

    }
}
