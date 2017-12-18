package com.pulan.dialogserver.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
/**
 * MySql dataSource
 */
@Configuration
public class DruidDBConfig {

    private Logger logger = LogManager.getLogger(DruidDBConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(env.getProperty("spring.datasource.url"));
        datasource.setUsername(env.getProperty("spring.datasource.username"));
        datasource.setPassword(env.getProperty("spring.datasource.password"));
        datasource.setDriverClassName(env.getProperty("spring.datasource.driverClassName"));
        //configuration
        datasource.setInitialSize(Integer.parseInt(env.getProperty("spring.datasource.initialSize")));
        datasource.setMinIdle(Integer.parseInt(env.getProperty("spring.datasource.minIdle")));
        datasource.setMaxActive(Integer.parseInt(env.getProperty("spring.datasource.maxActive")));
        datasource.setMaxWait(Long.parseLong(env.getProperty("spring.datasource.maxWait")));
        datasource.setTimeBetweenEvictionRunsMillis(Long.parseLong(env.getProperty("spring.datasource.timeBetweenEvictionRunsMillis")));
        datasource.setMinEvictableIdleTimeMillis(Long.parseLong(env.getProperty("spring.datasource.minEvictableIdleTimeMillis")));
        datasource.setValidationQuery(env.getProperty("spring.datasource.validationQuery"));
        datasource.setTestWhileIdle(true);
        datasource.setTestOnBorrow(false);
        datasource.setTestOnReturn(false);
        datasource.setPoolPreparedStatements(true);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(20);
        return datasource;
    }
}
