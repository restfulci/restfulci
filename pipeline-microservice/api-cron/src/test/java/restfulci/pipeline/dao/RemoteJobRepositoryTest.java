package restfulci.pipeline.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.pipeline.domain.RemoteJobBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisabledIfEnvironmentVariable(named="CI", matches="CircleCI")
public class RemoteJobRepositoryTest {
	
	@Autowired private RemoteJobRepository remoteJobRepository;

	@Test
	public void testGetJob() throws Exception {
		
		RemoteJobBean queriedJob = remoteJobRepository.getJob(1);
		assertEquals(queriedJob.getId(), 1);
		assertEquals(queriedJob.getName(), "job_name");
		assertEquals(queriedJob.getType(), "FREESTYLE");
		assertEquals(queriedJob.getParameters().size(), 1);
		assertEquals(queriedJob.getParameters().get(0).getName(), "ENV");
	}
}
