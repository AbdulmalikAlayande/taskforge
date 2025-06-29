package app.bola.taskforge.service;

import app.bola.taskforge.common.service.BaseService;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.MemberRequest;
import app.bola.taskforge.service.dto.MemberResponse;

import java.util.Set;

public interface MemberService extends BaseService<MemberRequest, Member, MemberResponse> {
	
	/**
	 * Accepts an invitation to join an organization.
	 *
	 * @param token the invitation token
	 * @return the response containing details of the accepted invitation
	 */
	InvitationResponse acceptInvitation(String token);
	
	@Override
	Set<MemberResponse> findAll();
}
