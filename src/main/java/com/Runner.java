package com;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Runner implements CommandLineRunner {

    @Autowired
    private MainService mainService;

    public static void main(String[] args) throws Exception {

        SpringApplication app = new SpringApplication(Runner.class);
        app.run(args);

    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println("Im running");
        mainService.convertNEO4JtoSQL();

    }
}