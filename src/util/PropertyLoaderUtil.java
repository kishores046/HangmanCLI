package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyLoaderUtil {

    private static final Logger logger =
            Logger.getLogger(PropertyLoaderUtil.class.getName());
    private static final String ACTIVE_PROFILE = System.getProperty("profile", "win");
    private static final String CONFIG_FILE    = "/db-" + ACTIVE_PROFILE + "-config.properties";

    private PropertyLoaderUtil() {}

    public static void loadDBProperties(DBConfigProperties dbConfigProperties) {

        dbConfigProperties.setDBUrl(requireEnv("DB_URL"));
        dbConfigProperties.setDBUser(requireEnv("DB_USER"));
        dbConfigProperties.setDBPassword(requireEnv("DB_PASSWORD"));

        logger.info("Database configuration loaded from environment variables.");

    }

    private static String requireEnv(String key) {
        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            throw new ExceptionInInitializerError(
                    "Required environment variable '" + key + "' is missing.");
        }

        return value.trim();
    }
}