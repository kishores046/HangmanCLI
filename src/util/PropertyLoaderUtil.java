package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyLoaderUtil {

    private static final Logger logger = Logger.getLogger("PropertyLoaderUtil");
    private static final String ACTIVE_PROFILE = System.getProperty("profile", "win");
    private static final String CONFIG_FILE    = "/db-" + ACTIVE_PROFILE + "-config.properties";

    private PropertyLoaderUtil() {}

    public static void loadDBProperties(DBConfigProperties dbConfigProperties) {


        try (InputStream is = PropertyLoaderUtil.class.getResourceAsStream(CONFIG_FILE)) {

            if (is == null) {
                throw new ExceptionInInitializerError(
                        "Config file not found: " + CONFIG_FILE +
                                " | active profile: '" + ACTIVE_PROFILE +
                                "' (set via -Dprofile=win|wsl, defaults to win)"
                );
            }


            Properties props = new Properties();
            props.load(is);

            dbConfigProperties.setDBUrl(require(props, "DB_URL"));
            dbConfigProperties.setDBUser(require(props, "DB_USER"));
            dbConfigProperties.setDBPassword(require(props, "DB_PASSWORD"));

            logger.log(Level.INFO, "DB config loaded — profile: {0} | URL: {1}",
                    new Object[]{ ACTIVE_PROFILE, dbConfigProperties.getDBUrl() });

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static String require(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ExceptionInInitializerError(
                    "Missing required property '" + key + "' in " + CONFIG_FILE
            );
        }
        return value.trim();
    }
}