package br.com.equilibra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import br.com.equilibra.attachment.application.AttachmentProperties;

@SpringBootApplication
@EnableConfigurationProperties(AttachmentProperties.class)
public class EquilibraApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EquilibraApiApplication.class, args);
    }
}