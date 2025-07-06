package app.bola.taskforge.controller;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/log")
@AllArgsConstructor
public class AuditLogController {
	

	 @PostMapping("/create-new")
	 public ResponseEntity<String> createNew(@RequestBody LogRequest request) {
		 log.info("Creating new audit log entry: {}", request);
		 // Here you would typically call a service to handle the log creation
		 return ResponseEntity.ok("Audit log entry created successfully");

	 }
	 
	 static class LogRequest {
		 String message;
		 String level;
		 String meta;
	 }
}
