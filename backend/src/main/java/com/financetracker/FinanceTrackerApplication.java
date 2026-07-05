package com.financetracker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinanceTrackerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(FinanceTrackerApplication.class, args);
    }

}
