package app.bola.taskforge.service;

import app.bola.taskforge.common.service.BaseService;
import app.bola.taskforge.domain.entity.Organization;
import app.bola.taskforge.service.dto.*;

import java.util.Set;

public interface OrganizationService extends BaseService<OrganizationRequest, Organization, OrganizationResponse> {
	
	
	InvitationResponse inviteMember(InvitationRequest request);
	
	Set<MemberResponse> getMembers(String publicId);
}
