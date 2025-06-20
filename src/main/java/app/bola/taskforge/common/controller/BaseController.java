package app.bola.taskforge.common.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface BaseController<REQ, RES> {
	
	@PostMapping("/create-new")
	default ResponseEntity<RES> createNew(@Valid @RequestBody REQ req) {
		throw new UnsupportedOperationException("This method is not implemented");
	}
	
}
