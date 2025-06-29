package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.MemberService;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.MemberRequest;
import app.bola.taskforge.service.dto.MemberResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/api/members")
@AllArgsConstructor
public class MemberController implements BaseController<MemberRequest, MemberResponse> {
	
	private final MemberService memberService;
	
	@Override
	@PostMapping
	public ResponseEntity<MemberResponse> createNew(@RequestBody MemberRequest request) {
		return ResponseEntity.ok(memberService.createNew(request));
	}
	
	@GetMapping("/{publicId}")
	@Override
	public ResponseEntity<MemberResponse> getById(@PathVariable String publicId) {
		return ResponseEntity.ok(memberService.findById(publicId));
	}
	
	@GetMapping
	public ResponseEntity<Collection<MemberResponse>> getAll() {
		return ResponseEntity.ok(memberService.findAll());
	}
	
	@PutMapping("/{publicId}")
	public ResponseEntity<MemberResponse> update(@PathVariable String publicId, @RequestBody MemberRequest request) {
		return ResponseEntity.ok(memberService.update(publicId, request));
	}
	
	@DeleteMapping("/{publicId}")
	public ResponseEntity<Void> delete(@PathVariable String publicId) {
		memberService.deleteById(publicId);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/accept-invitation")
	public ResponseEntity<InvitationResponse> acceptInvitation(@RequestParam String token) {
		return ResponseEntity.ok(memberService.acceptInvitation(token));
	}
}