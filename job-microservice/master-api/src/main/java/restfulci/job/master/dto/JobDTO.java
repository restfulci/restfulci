package restfulci.job.master.dto;

import java.io.IOException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import restfulci.job.shared.domain.FreestyleJobBean;
import restfulci.job.shared.domain.GitJobBean;
import restfulci.job.shared.domain.JobBean;
import restfulci.job.shared.exception.JobApiDataException;

@Getter
@Setter
@ToString
public class JobDTO {

	@NotNull
	@Size(min=2, max=32)
	private String name;
	
	@Size(max=128)
	private String remoteOrigin;
	
	@Size(max=128)
	private String configFilepath;
	
	private String dockerImage;
	
	private String[] command;
	
	public JobBean toBean() throws IOException {
		
		if ((remoteOrigin != null) && (configFilepath != null)) {
			
			GitJobBean jobBean = new GitJobBean();
			jobBean.setName(name);
			jobBean.setRemoteOrigin(remoteOrigin);
			jobBean.setConfigFilepath(configFilepath);
			return jobBean;
		}
		
		if ((dockerImage != null) && (command != null)) {
			
			FreestyleJobBean jobBean = new FreestyleJobBean();
			jobBean.setName(name);
			jobBean.setDockerImage(dockerImage);
			jobBean.setCommand(command);
			return jobBean;
		}
		
		throw new JobApiDataException("The job definition doesn't fit any existing job types.");
	}
}
