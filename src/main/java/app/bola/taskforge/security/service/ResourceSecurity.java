package app.bola.taskforge.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceSecurity {
	
	
	public boolean isTaskOwner(String taskId, String authenticationName) {
		return true;
	}
}
