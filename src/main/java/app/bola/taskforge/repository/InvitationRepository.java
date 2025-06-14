package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.Invitation;
import app.bola.taskforge.domain.entity.Organization;

import java.util.Optional;

public interface InvitationRepository extends TenantAwareRepository<Invitation, String> {
	
	boolean existsByInviteeEmailAndOrganization(String inviteeEmail, Organization organization);
	
	Optional<Invitation> findByInviteeEmail(String inviteeEmail);
}