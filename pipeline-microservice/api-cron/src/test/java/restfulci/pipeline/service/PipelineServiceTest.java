package restfulci.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.pipeline.dao.ReferredJobRepository;
import restfulci.pipeline.dao.RemoteJobRepository;
import restfulci.pipeline.domain.ParameterMapBean;
import restfulci.pipeline.domain.ReferredJobBean;
import restfulci.pipeline.domain.RemoteJobBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PipelineServiceTest {

	@Autowired private PipelineService service;
	
	@MockBean private RemoteJobRepository remoteJobRepository;
	@MockBean private ReferredJobRepository referredJobRepository;
	
	@Test
	public void testUpdateReferredJobParameters() throws Exception {
		
		ReferredJobBean referredJob = new ReferredJobBean();
		referredJob.setId(123);
		referredJob.setOriginalJobId(456);
		given(referredJobRepository.findById(123)).willReturn(Optional.of(referredJob));
		
		RemoteJobBean.Parameter remoteParameter = new RemoteJobBean.Parameter();
		remoteParameter.setName("ENV");
		RemoteJobBean remoteJob = new RemoteJobBean();
		List<RemoteJobBean.Parameter> remoteParameters = new ArrayList<RemoteJobBean.Parameter>();
		remoteParameters.add(remoteParameter);
		remoteJob.setParameters(remoteParameters);
		given(remoteJobRepository.getJob(456)).willReturn(remoteJob);
		
		service.updateReferredJobParameters(123);
		
		ArgumentCaptor<ReferredJobBean> referredJobCaptor = ArgumentCaptor.forClass(ReferredJobBean.class);
		verify(referredJobRepository, times(1)).saveAndFlush(referredJobCaptor.capture());
		assertEquals(referredJobCaptor.getValue().getParameterMaps().size(), 1);
		assertEquals(new ArrayList<ParameterMapBean>(referredJobCaptor.getValue().getParameterMaps()).get(0).getRemoteName(), "ENV");
	}
}
