package app.bola.taskforge.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfig {
	
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration()
				.setAmbiguityIgnored(true)
				.setFieldMatchingEnabled(true)
				.setMatchingStrategy(MatchingStrategies.STRICT)
				.setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
				.setMethodAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PUBLIC)
				.setSkipNullEnabled(true)
				.setSourceNamingConvention(org.modelmapper.convention.NamingConventions.JAVABEANS_ACCESSOR)
				.setDestinationNamingConvention(org.modelmapper.convention.NamingConventions.JAVABEANS_ACCESSOR);;
		return modelMapper;
	}
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
