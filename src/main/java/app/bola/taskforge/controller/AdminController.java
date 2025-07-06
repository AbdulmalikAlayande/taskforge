package app.bola.taskforge.controller;

import app.bola.taskforge.service.AdminService;
import app.bola.taskforge.service.dto.CreateAdminRequest;
import app.bola.taskforge.service.dto.MemberResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminController {
	
	final AdminService adminService;
	
	@PostMapping("/create-new")
	public ResponseEntity<MemberResponse> createOrgAdmin(@Valid @RequestBody CreateAdminRequest request) {
		return ResponseEntity.ok(adminService.createOrgAdmin(request));
	}
}
