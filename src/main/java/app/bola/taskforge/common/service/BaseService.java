package app.bola.taskforge.common.service;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.exception.InvalidRequestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BaseService<REQ, ENT extends BaseEntity, RES> {
	
	
	RES createNew(@NonNull REQ req);
	
	RES update(@NotBlank String publicId, @NonNull REQ req);
	
	
	default RES toResponse(ENT entity) {
		return null;
	}
	
	
	default Collection<RES> toResponse(Collection<ENT> entities) {
		return entities.stream().map(this::toResponse).collect(Collectors.toList());
	}
	
	
	default  <T> void performValidation(Validator validator, T request){
		Set<ConstraintViolation<T>> violations = validator.validate(request);
		Stream<String> errorMessageStream = violations.stream()
                    .map(violation -> violation.getPropertyPath()+": "+violation.getMessage());
		
		if (!violations.isEmpty()) {
			throw new InvalidRequestException(
					errorMessageStream.reduce((message1, message2) -> message1 + ", " + message2).orElse("Invalid request")
			);
		}
	}
}
