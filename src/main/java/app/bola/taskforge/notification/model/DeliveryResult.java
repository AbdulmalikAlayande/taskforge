package app.bola.taskforge.notification.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class DeliveryResult {
	
	private int retryCount;
	private String bundleId;
	private String externalId;
	private String errorMessage;
	private Instant deliveredAt;
	private ChannelType channel;
	private DeliveryStatus status;
}
