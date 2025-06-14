package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.MemberService;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.MemberRequest;
import app.bola.taskforge.service.dto.UserResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@AllArgsConstructor
public class MemberController implements BaseController<MemberRequest, UserResponse> {
	
	
	private final MemberService memberService;
	
	@Override
	public ResponseEntity<UserResponse> createNew(MemberRequest request) {
		return ResponseEntity.ok(memberService.createNew(request));
	}
	
	@PostMapping("/accept-invitation")
	public ResponseEntity<InvitationResponse> acceptInvitation(String token) {
		return ResponseEntity.ok(memberService.acceptInvitation(token));
	}
	
	
}
