package restfulci.master.dto;

import java.io.IOException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.GitJobBean;
import restfulci.shared.domain.JobBean;

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
		
		throw new IOException();
		/* 
		 * TODO:
		 * Should input a type, rather than completely rely on input content negotiation?
		 */
	}
}
