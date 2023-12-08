package ru.shop.backend.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SearchApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext cont = SpringApplication.run(SearchApplication.class, args);
    }
}