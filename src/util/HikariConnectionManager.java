package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

import java.util.logging.Logger;


public class HikariConnectionManager {
    private static final HikariDataSource datasource;

    private static final Logger logger = Logger.getLogger("HikariConnectionManager");

    private static final DBConfigProperties dbConfigProperties = new DBConfigProperties();

    static {
        PropertyLoaderUtil.loadDBProperties(dbConfigProperties);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbConfigProperties.getDBUrl());
        config.setUsername(dbConfigProperties.getDBUser());
        config.setPassword(dbConfigProperties.getDBPassword());
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);
        config.setMaxLifetime(1800000);
        datasource = new HikariDataSource(config);
    }


    public static DataSource getDataSource() {
        return datasource;
    }

}
