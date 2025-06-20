package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.MemberService;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.MemberRequest;
import app.bola.taskforge.service.dto.MemberResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@AllArgsConstructor
public class MemberController implements BaseController<MemberRequest, MemberResponse> {
	
	
	private final MemberService memberService;
	
	@Override
	public ResponseEntity<MemberResponse> createNew(MemberRequest request) {
		return ResponseEntity.ok(memberService.createNew(request));
	}
	
	@PostMapping("/accept-invitation")
	public ResponseEntity<InvitationResponse> acceptInvitation(@RequestParam String token) {
		return ResponseEntity.ok(memberService.acceptInvitation(token));
	}
	
	
}
