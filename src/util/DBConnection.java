package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {

    private DBConnection() {}

    private static final Logger logger = Logger.getLogger("DBConnection");

    private static final DBConfigProperties dbConfigProperties = new DBConfigProperties();

    static {
        loadProperties();
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                dbConfigProperties.getDBUrl(),
                dbConfigProperties.getDBUser(),
                dbConfigProperties.getDBPassword()
        );
    }

    private static void loadProperties() {

        try (InputStream is = DBConnection.class.getResourceAsStream("/dbconfig.properties")) {

            if (is == null) {
                throw new ExceptionInInitializerError(
                        "dbconfig.properties not found on the classpath.\n" +
                                "Fix: move the file from resources/ into src/ (project root of the source tree),\n" +
                                "or right-click resources/ in IntelliJ → Mark Directory as → Resources Root."
                );
            }

            Properties props = new Properties();
            props.load(is);

            dbConfigProperties.setDBUrl(props.getProperty("DB_URL"));
            dbConfigProperties.setDBUser(props.getProperty("DB_USER"));
            dbConfigProperties.setDBPassword(props.getProperty("DB_PASSWORD"));

            logger.log(Level.INFO, "DB config loaded — URL: {0}", dbConfigProperties.getDBUrl());

        } catch (IOException e) {
             throw new ExceptionInInitializerError(e);
        }
    }
}