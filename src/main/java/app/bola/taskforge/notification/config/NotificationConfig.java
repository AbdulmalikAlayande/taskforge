package app.bola.taskforge.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class NotificationConfig {
	
	@Value("${scheduled.notification.thread-pool-size:10}")
	private String threadPoolSize;
	
	@Bean
	public ScheduledExecutorService scheduledExecutorService() {
		return Executors.newScheduledThreadPool(Integer.parseInt(threadPoolSize));
	}
}
