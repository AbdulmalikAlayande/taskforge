package app.bola.taskforge.notification.model;

import lombok.Getter;

@Getter
public enum ChannelType {
	
	WEBSOCKET("websocket"),
	EMAIL("email"),
	PUSH("push");

	private final String type;

	ChannelType(String type) {
		this.type = type;
	}
	
	public static ChannelType fromString(String type) {
		for (ChannelType channelType : ChannelType.values()) {
			if (channelType.type.equalsIgnoreCase(type)) {
				return channelType;
			}
		}
		throw new IllegalArgumentException("Unknown channel type: " + type);
	}
}
