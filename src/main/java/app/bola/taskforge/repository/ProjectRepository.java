package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.domain.entity.Project;

import java.util.List;

public interface ProjectRepository extends TenantAwareRepository<Project, String> {
	
	List<Project> findAllByOrganization(Organization organization);
}
