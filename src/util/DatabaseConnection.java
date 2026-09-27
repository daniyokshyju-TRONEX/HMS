import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = System.getenv().getOrDefault("DB_USERNAME", "root");
    private static final String PASSWORD = System.getenv().containsKey("DB_PASSWORD")
            ? System.getenv("DB_PASSWORD")
            : "";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
