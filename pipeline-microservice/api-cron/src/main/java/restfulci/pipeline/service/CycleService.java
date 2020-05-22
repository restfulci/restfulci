package restfulci.pipeline.service;

import java.io.IOException;

import restfulci.pipeline.domain.CycleBean;

public interface CycleService {

	public CycleBean getCycle(Integer cycleId) throws IOException;
	public CycleBean triggerCycle(Integer pipelineId) throws IOException;
	public void updateCycle(Integer cycleId) throws IOException;
	public void deleteCycle(Integer cycleId) throws IOException;
}
