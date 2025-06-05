package app.bola.taskforge.common.service;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.service.dto.OrganizationRequest;
import app.bola.taskforge.service.dto.OrganizationResponse;

public interface BaseService<REQ, ENT extends BaseEntity, RES> {
	
	
	RES createNew(REQ req);
}
