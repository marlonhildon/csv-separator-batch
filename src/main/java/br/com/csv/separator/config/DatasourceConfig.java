package br.com.csv.separator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean("h2Datasource")
    public DataSource getDataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

}
