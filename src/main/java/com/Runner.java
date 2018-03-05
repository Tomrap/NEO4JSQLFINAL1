package com;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:config.properties")
public class Runner implements CommandLineRunner {

    @Value("${convertGraphToSql}")
    private boolean convertGraphToSql;

    @Autowired
    private MainService mainService;

    public static void main(String[] args) throws Exception {

        SpringApplication app = new SpringApplication(Runner.class);
        app.run(args);

    }

    @Override
    public void run(String... args) throws Exception {

        if (convertGraphToSql) {
            mainService.convertGraphToSQL();
        } else {
            mainService.convertSQLtoGraph();
        }
    }
}