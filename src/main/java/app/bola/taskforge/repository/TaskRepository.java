package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.Task;

public interface TaskRepository extends TenantAwareRepository<Task, String> {

}
