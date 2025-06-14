package app.bola.taskforge.config;

import app.bola.taskforge.interceptor.AuditLogInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@AllArgsConstructor
public class  WebMvcConfig implements WebMvcConfigurer {

	AuditLogInterceptor auditLogInterceptor;

	@Override
	public void addInterceptors(@NonNull InterceptorRegistry registry) {
		registry.addInterceptor(auditLogInterceptor)
				.addPathPatterns("/**");
	}

}
