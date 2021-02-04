package restfulci.job.shared.dao;

import lombok.Getter;

@Getter
enum MinioBucket {

	RUN_OUTPUT("run-output"),
	RUN_CONFIGURATION("run-configuration"),
	RUN_RESULT("run-result");
	
	private String bucketName;
	
	private MinioBucket(String bucketName) {
		this.bucketName = bucketName;
	}
}
