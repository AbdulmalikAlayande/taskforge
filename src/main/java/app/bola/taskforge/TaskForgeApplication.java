package app.bola.taskforge;

import app.bola.taskforge.repository.TenantAwareRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@SpringBootApplication
@EnableJpaRepositories(
		repositoryFactoryBeanClass = TenantAwareRepositoryFactoryBean.class,
		basePackages = "app.bola.taskforge.repository"
)
public class TaskForgeApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TaskForgeApplication.class, args);
	}
	
}
