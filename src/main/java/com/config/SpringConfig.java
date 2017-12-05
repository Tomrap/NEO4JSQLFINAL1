package com.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;


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
    public BatchInserter batchInserter() throws IOException {
        return BatchInserters.inserter(new File( "C:\\Users\\John\\Documents\\Neo4j\\sakila.db" ));
    }

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