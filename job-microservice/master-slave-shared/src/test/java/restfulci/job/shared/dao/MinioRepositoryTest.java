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
	public void testPutAndGetRunOutput() throws Exception {
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(123);
		
		InputStream contentStream = new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
		repository.putRunOutputAndUpdateRunBean(run, contentStream);
		assertEquals(run.getRunOutputObjectReferral(), run.getId().toString());
		
		InputStream stream = repository.getRunOutput(run);
		String outputText = IOUtils.toString(stream, StandardCharsets.UTF_8.name());
		assertEquals(outputText, "hello");
		
		repository.removeRunOutput(run);
	}
	
	@Test
	public void testPutAndGetRunOutputWhenRunIdIsNull() throws Exception {
		
		FreestyleRunBean run = new FreestyleRunBean();
		
		InputStream contentStream = new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
		repository.putRunOutputAndUpdateRunBean(run, contentStream);
		
		InputStream stream = repository.getRunOutput(run);
		String outputText = IOUtils.toString(stream, StandardCharsets.UTF_8.name());
		assertEquals(outputText, "hello");
		
		repository.removeRunOutput(run);
	}
}