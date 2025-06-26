package app.bola.taskforge.notification.config;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.auth.oauth2.AuthenticationFactoryOAuth2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class PulsarConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(PulsarConfig.class);
	@Value("${pulsar.security.mode:OFF}")
	private String securityMode;
	@Value("${pulsar.service.url}")
	private String serviceUrl;
	@Value("${pulsar.oauth2.issuer-url}")
	private String issuerUrl;
	@Value("${pulsar.oauth2.credentials-url}")
	private String credentialsUrl;
	@Value("${pulsar.oauth2.audience}")
	private String audience;
	
	@Bean
	public PulsarClient pulsarClient() {
		
		if (securityMode.equalsIgnoreCase("OFF")) {
			try {
				return PulsarClient.builder()
					.serviceUrl(serviceUrl)
					.build();
			} catch (PulsarClientException exception) {
				logger.error(exception.getMessage(), exception);
				return null;
			}
		}
		else {
			try {
				return PulsarClient.builder()
					.serviceUrl(serviceUrl)
					.authentication(AuthenticationFactoryOAuth2.clientCredentials(
						(new URI(issuerUrl)).toURL(),
						(new URI(credentialsUrl)).toURL(),
						audience
					))
					.build();
			}catch (PulsarClientException | MalformedURLException | URISyntaxException exception) {
				logger.error(exception.getMessage(), exception);
				return null;
			}
		}
	}
}
