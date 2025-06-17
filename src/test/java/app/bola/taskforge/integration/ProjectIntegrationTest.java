package app.bola.taskforge.integration;

import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ProjectIntegrationTest {
	
	@Nested
	class CreateProjectTests {
	
	}
	
	@Nested
	class AddMemberToProjectTests {
	
	}

	@Nested
	class RemoverMemberFromProjectTests {
	
	}
	
	@Nested
	class ChangeProjectStatusTests {
	
	}
	
	
}
