package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class JDBConnectionManager {
    private JDBConnectionManager() {}

    private static final Logger logger = Logger.getLogger("JDBConnectionManager");

    private static final DBConfigProperties dbConfigProperties = new DBConfigProperties();

    static {
        PropertyLoaderUtil.loadDBProperties(dbConfigProperties);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                dbConfigProperties.getDBUrl(),
                dbConfigProperties.getDBUser(),
                dbConfigProperties.getDBPassword()
        );
    }


}