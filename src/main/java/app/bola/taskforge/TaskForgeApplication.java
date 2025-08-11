package app.bola.taskforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;

@EnableJpaAuditing
@SpringBootApplication
public class TaskForgeApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TaskForgeApplication.class, args);
	}
	
}
