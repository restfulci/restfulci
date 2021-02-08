package restfulci.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import restfulci.pipeline.domain.ParameterBean;
import restfulci.pipeline.domain.ParameterMapBean;
import restfulci.pipeline.domain.ReferredJobBean;
import restfulci.pipeline.domain.RemoteJobBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PipelineServiceTest {

	@Autowired private PipelineService service;
	
	@MockBean private RemoteJobRepository remoteJobRepository;
	@MockBean private ReferredJobRepository referredJobRepository;
	
	private final String token = "foo";
	
	@Test
	public void testUpdateReferredJobParameters() throws Exception {
		
		ReferredJobBean referredJob = new ReferredJobBean();
		referredJob.setId(123);
		referredJob.setOriginalJobId(456);
		given(referredJobRepository.findById(123)).willReturn(Optional.of(referredJob));
		
		List<RemoteJobBean.Parameter> remoteParameters = new ArrayList<RemoteJobBean.Parameter>();
		
		RemoteJobBean.Parameter requiredRemoteParameter = new RemoteJobBean.Parameter();
		requiredRemoteParameter.setName("REQUIRED");
		remoteParameters.add(requiredRemoteParameter);
		
		RemoteJobBean.Parameter optionalRemoteParameter = new RemoteJobBean.Parameter();
		optionalRemoteParameter.setName("OPTIONAL");
		optionalRemoteParameter.setDefaultValue("default");
		remoteParameters.add(optionalRemoteParameter);
		
		RemoteJobBean remoteJob = new RemoteJobBean();
		remoteJob.setType("FREESTYLE");
		remoteJob.setParameters(remoteParameters);
		given(remoteJobRepository.getJob(456, token)).willReturn(remoteJob);
		
		service.updateReferredJobParameters(123, token);
		
		ArgumentCaptor<ReferredJobBean> referredJobCaptor = ArgumentCaptor.forClass(ReferredJobBean.class);
		verify(referredJobRepository, times(1)).saveAndFlush(referredJobCaptor.capture());
		assertEquals(referredJobCaptor.getValue().getParameterMaps().size(), 2);
		List<ParameterMapBean> parameterMaps = new ArrayList<ParameterMapBean>(referredJobCaptor.getValue().getParameterMaps());
		for (int i = 0; i < 2; ++i) {
			if (parameterMaps.get(i).getRemoteName().equals("REQUIRED")) {
				assertFalse(parameterMaps.get(i).getOptional());
			}
			else {
				assertEquals(parameterMaps.get(i).getRemoteName(), "OPTIONAL");
				assertTrue(parameterMaps.get(i).getOptional());
			}
		}
	}
	
	@Test
	public void testReferredJobParameterIsKeptAndUpdatedIfAlreadyExist() throws Exception {
		
		ParameterBean parameter = new ParameterBean();
		ReferredJobBean referredJob = new ReferredJobBean();
		referredJob.setId(123);
		referredJob.setOriginalJobId(456);
		ParameterMapBean parameterMap = new ParameterMapBean();
		parameterMap.setReferredJob(referredJob);
		parameterMap.setParameter(parameter);
		parameterMap.setRemoteName("EXIST");
		parameterMap.setOptional(false);
		referredJob.addParameterMap(parameterMap);
		given(referredJobRepository.findById(123)).willReturn(Optional.of(referredJob));
		
		RemoteJobBean.Parameter remoteParameter = new RemoteJobBean.Parameter();
		remoteParameter.setName("EXIST");
		remoteParameter.setDefaultValue("default");
		RemoteJobBean remoteJob = new RemoteJobBean();
		remoteJob.setType("FREESTYLE");
		List<RemoteJobBean.Parameter> remoteParameters = new ArrayList<RemoteJobBean.Parameter>();
		remoteParameters.add(remoteParameter);
		remoteJob.setParameters(remoteParameters);
		given(remoteJobRepository.getJob(456, token)).willReturn(remoteJob);
		
		service.updateReferredJobParameters(123, token);
		
		ArgumentCaptor<ReferredJobBean> referredJobCaptor = ArgumentCaptor.forClass(ReferredJobBean.class);
		verify(referredJobRepository, times(1)).saveAndFlush(referredJobCaptor.capture());
		assertEquals(referredJobCaptor.getValue().getParameterMaps().size(), 1);
		ParameterMapBean newParameterMap = new ArrayList<ParameterMapBean>(referredJobCaptor.getValue().getParameterMaps()).get(0);
		assertEquals(newParameterMap.getRemoteName(), "EXIST");
		assertEquals(newParameterMap.getParameter(), parameter);
		assertTrue(newParameterMap.getOptional());
	}
	
	@Test
	public void testReferredJobParameterIsDeletedIfNotInRemote() throws Exception {
		
		ParameterBean parameter = new ParameterBean();
		ReferredJobBean referredJob = new ReferredJobBean();
		referredJob.setId(123);
		referredJob.setOriginalJobId(456);
		ParameterMapBean parameterMap = new ParameterMapBean();
		parameterMap.setReferredJob(referredJob);
		parameterMap.setParameter(parameter);
		parameterMap.setRemoteName("TO_BE_DELETED");
		parameterMap.setOptional(false);
		referredJob.addParameterMap(parameterMap);
		given(referredJobRepository.findById(123)).willReturn(Optional.of(referredJob));
		
		RemoteJobBean remoteJob = new RemoteJobBean();
		remoteJob.setType("FREESTYLE");
		remoteJob.setParameters(new ArrayList<RemoteJobBean.Parameter>());
		given(remoteJobRepository.getJob(456, token)).willReturn(remoteJob);
		
		service.updateReferredJobParameters(123, token);
		
		ArgumentCaptor<ReferredJobBean> referredJobCaptor = ArgumentCaptor.forClass(ReferredJobBean.class);
		verify(referredJobRepository, times(1)).saveAndFlush(referredJobCaptor.capture());
		assertEquals(referredJobCaptor.getValue().getParameterMaps().size(), 0);
	}
	
	@Test
	public void testGitJobGetCorrespondingReferredJobParameters() throws Exception {
		
		ReferredJobBean referredJob = new ReferredJobBean();
		referredJob.setId(123);
		referredJob.setOriginalJobId(456);
		given(referredJobRepository.findById(123)).willReturn(Optional.of(referredJob));
		
		List<RemoteJobBean.Parameter> remoteParameters = new ArrayList<RemoteJobBean.Parameter>();
		
		RemoteJobBean remoteJob = new RemoteJobBean();
		remoteJob.setType("GIT");
		remoteJob.setParameters(remoteParameters);
		given(remoteJobRepository.getJob(456, token)).willReturn(remoteJob);
		
		service.updateReferredJobParameters(123, token);
		service.updateReferredJobParameters(123, token);
		
		ArgumentCaptor<ReferredJobBean> referredJobCaptor = ArgumentCaptor.forClass(ReferredJobBean.class);
		verify(referredJobRepository, times(2)).saveAndFlush(referredJobCaptor.capture());
		assertEquals(referredJobCaptor.getValue().getParameterMaps().size(), 2);
		List<ParameterMapBean> parameterMaps = new ArrayList<ParameterMapBean>(referredJobCaptor.getValue().getParameterMaps());
		for (int i = 0; i < 2; ++i) {
			String remoteName = parameterMaps.get(i).getRemoteName();
			assertTrue(remoteName.equals("branchName") || remoteName.equals("commitSha"));
			assertTrue(parameterMaps.get(i).getOptional());
		}
	}
}
