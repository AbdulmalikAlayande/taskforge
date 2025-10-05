package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.Task;

import java.util.List;

public interface TaskRepository extends TenantAwareRepository<Task, String> {
	
	List<Task> findByProject_Id(String projectId);
}
