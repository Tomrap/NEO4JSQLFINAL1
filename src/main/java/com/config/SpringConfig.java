package com.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.springframework.context.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


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

    //SQL to NEO4J - path to NEO4J database
    @Lazy
    @Bean
    public BatchInserter batchInserter() throws IOException {
        return BatchInserters.inserter(new File( "C:\\Users\\John\\Documents\\Neo4j\\sakila.db" ));
    }

    //path to SQL database
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost:3306/");
        ds.setUsername("root");
        ds.setPassword("0000");
        return ds;
    }


    @Lazy
    @Bean
    public ResourceDatabasePopulator resourceDatabasePopulator() {
        String rootPath = System.getProperty("user.dir");
        File schemaSQL = new File(StringUtils.join(rootPath, "/src/" , "schema.sql"));
        File dataSQL = new File(StringUtils.join(rootPath, "/src/" , "data.sql"));
        File alterSchemaSQL = new File(StringUtils.join(rootPath, "/src/" , "alterSchema.sql"));
        FileSystemResource schemaSQLFile = new FileSystemResource(schemaSQL);
        FileSystemResource dataSQLFile = new FileSystemResource(dataSQL);
        FileSystemResource alterSchemaSQLFile = new FileSystemResource(alterSchemaSQL);
        return new ResourceDatabasePopulator(schemaSQLFile,dataSQLFile,alterSchemaSQLFile);
    }

    @Lazy
    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource, ResourceDatabasePopulator resourceDatabasePopulator) {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
        return dataSourceInitializer;
    }

    //NEO4J to SQL - path to NEO4J database
    @Lazy
    @Bean
    public GraphDatabaseService graphDatabaseService() throws IOException {
        File DB_PATH = new File( "C:\\Users\\John\\Documents\\Neo4j\\sakila.db" );
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder( DB_PATH )
                .setConfig( GraphDatabaseSettings.read_only, "true" )
                .newGraphDatabase();
        registerShutdownHook(graphDb);
        return graphDb;
    }

    private void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }


    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}