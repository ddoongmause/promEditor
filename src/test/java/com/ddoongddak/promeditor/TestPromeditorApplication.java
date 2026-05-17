package com.ddoongddak.promeditor;

import org.springframework.boot.SpringApplication;

public class TestPromeditorApplication {

    public static void main(String[] args) {
        SpringApplication.from(PromeditorApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
