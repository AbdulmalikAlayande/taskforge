package app.bola.taskforge.service;

import app.bola.taskforge.common.service.BaseService;
import app.bola.taskforge.domain.entity.User;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.MemberRequest;
import app.bola.taskforge.service.dto.UserResponse;

public interface MemberService extends BaseService<MemberRequest, User, UserResponse> {
	
	InvitationResponse acceptInvitation(String token);
}
