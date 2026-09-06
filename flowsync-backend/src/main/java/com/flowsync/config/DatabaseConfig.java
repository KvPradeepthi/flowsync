package com.flowsync.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DatabaseConfig implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) bean;
            log.info("[DatabaseConfig] Configuring HikariDataSource for PgBouncer / Supabase compatibility");
            hikari.addDataSourceProperty("prepareThreshold", "0");
            hikari.addDataSourceProperty("preparedStatementCacheQueries", "0");

            String url = hikari.getJdbcUrl();
            if (url != null && url.contains("postgresql") && !url.contains("prepareThreshold")) {
                String separator = url.contains("?") ? "&" : "?";
                hikari.setJdbcUrl(url + separator + "prepareThreshold=0&preparedStatementCacheQueries=0");
            }
        }
        return bean;
    }
}