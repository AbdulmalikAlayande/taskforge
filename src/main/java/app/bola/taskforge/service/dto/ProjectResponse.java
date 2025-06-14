package app.bola.taskforge.service.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class ProjectResponse implements Serializable {
	
	String name;
	String category;
	String publicId;
	boolean archived;
	String createdBy;
	String modifiedBy;
	String description;
	LocalDate endDate;
	LocalDate startDate;
	LocalDateTime createdAt;
	LocalDateTime lastModifiedAt;
	Set<UserResponse> members;
	OrganizationResponse organization;
	
	
}
