package app.bola.taskforge.service;

import app.bola.taskforge.service.dto.ProjectRequest;
import app.bola.taskforge.service.dto.ProjectResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TaskForgeProjectService implements ProjectService{
	
	
	@Override
	public ProjectResponse createNew(ProjectRequest projectRequest) {
		return null;
	}
}
