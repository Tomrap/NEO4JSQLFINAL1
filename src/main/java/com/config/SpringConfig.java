package com.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;


/**
 * Created by John on 2017-01-08.
 */
@Configuration
@ComponentScan(basePackages="com")
public class SpringConfig {

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