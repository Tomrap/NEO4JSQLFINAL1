package com.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jndi.JndiTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Created by John on 2017-01-08.
 */


@Configuration
@ComponentScan(basePackages="com"
        ,excludeFilters={
        @ComponentScan.Filter(type= FilterType.ANNOTATION, value=EnableWebMvc.class)
})
public class SpringConfig {

    //TODO JNDI LOOKUP - PROBLEMS WITH TESTING, JNDI RESOURCE NOT AVAILABLE,ALREADY ADDED DATABASE RESOURCE TO TOMCAT CONTEXT.XML AND RESOURCE-LINK TO WEB.XML
//    @Bean
//    public DataSource getDataSource() throws NamingException {
//        JndiTemplate jndiTemplate = new JndiTemplate();
//        DataSource dataSource
//                = (DataSource) jndiTemplate.lookup("java:comp/env/jdbc/sakila");
//        return dataSource;
//    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost:3306/sakila");
        ds.setUsername("root");
        ds.setPassword("0000");
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}