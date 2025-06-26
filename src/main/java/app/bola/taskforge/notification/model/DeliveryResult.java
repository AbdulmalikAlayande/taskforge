package app.bola.taskforge.notification.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class DeliveryResult {
	
	private int retryCount;
	private String bundleId;
	private String externalId;
	private String errorMessage;
	private Instant deliveredAt;
	private ChannelType channel;
	private DeliveryStatus status;
	
	public static DeliveryResult success(String bundleId, ChannelType channelType, String messageId) {
		return DeliveryResult.builder()
				.bundleId(bundleId)
				.channel(channelType)
				.status(DeliveryStatus.SUCCESS)
				.externalId(messageId)
				.deliveredAt(Instant.now())
				.retryCount(0)
				.build();
	}
	
	public static DeliveryResult failure(String bundleId, ChannelType channelType, String errorMessage) {
		return DeliveryResult.builder()
				.bundleId(bundleId)
				.channel(channelType)
				.status(DeliveryStatus.FAILED)
				.errorMessage(errorMessage)
				.deliveredAt(Instant.now())
				.retryCount(0)
				.build();
	}
}
