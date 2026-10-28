package ru.acs.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
public class RagSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagSpringApplication.class, args);
    }
}
