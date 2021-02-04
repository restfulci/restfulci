package restfulci.job.shared.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.job.shared.dao.MinioRepository;
import restfulci.job.shared.domain.FreestyleRunBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MinioRepositoryTest {
	
	@Autowired private MinioRepository repository;
	
	@Test
	public void testPutAndGetRunOutputWithDefaultObjectReferral() throws Exception {
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(123);
		
		InputStream contentStream = new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
		String objectId = repository.putRunOutputAndReturnObjectName(contentStream, run.getDefaultRunOutputObjectReferral());
		assertEquals(objectId, new Integer(123).toString());
		run.setRunOutputObjectReferral(objectId);
		
		InputStream stream = repository.getRunOutput(run);
		String outputText = IOUtils.toString(stream, StandardCharsets.UTF_8.name());
		assertEquals(outputText, "hello");
		
		repository.removeRunOutput(run);
	}
	
	@Test
	public void testPutAndGetRunOutputWithoutDefaultObjectReferral() throws Exception {
		
		FreestyleRunBean run = new FreestyleRunBean();
		
		InputStream contentStream = new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
		String objectId = repository.putRunOutputAndReturnObjectName(contentStream, run.getDefaultRunOutputObjectReferral());
		assertEquals(objectId.length(), 10);
		run.setRunOutputObjectReferral(objectId);
		
		InputStream stream = repository.getRunOutput(run);
		String outputText = IOUtils.toString(stream, StandardCharsets.UTF_8.name());
		assertEquals(outputText, "hello");
		
		repository.removeRunOutput(run);
	}
}