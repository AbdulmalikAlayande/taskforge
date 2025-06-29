package app.bola.taskforge.common.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

public interface BaseController<REQ, RES> {
	
	@PostMapping("create-new")
	default ResponseEntity<RES> createNew(@Valid @RequestBody REQ req) {
		throw new UnsupportedOperationException("This method is not implemented");
	}
	
	@GetMapping("{publicId}")
	default ResponseEntity<RES> getById(@PathVariable String publicId) {
		throw new UnsupportedOperationException("This method is not implemented");
	}
	
	@GetMapping("all")
	default ResponseEntity<Collection<RES>> getAll() {
		throw new UnsupportedOperationException("This method is not implemented");
	}
	
	@DeleteMapping("/{publicId}")
	default ResponseEntity<Void> delete(@PathVariable String publicId) {
		throw new UnsupportedOperationException("This method is not implemented");
	}
}
