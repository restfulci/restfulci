package restfulci.pipeline.service;

import java.io.IOException;

import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.dto.CycleDTO;

public interface CycleService {

	public CycleBean getCycle(Integer cycleId) throws IOException;
	public CycleBean triggerCycle(Integer pipelineId, CycleDTO cycleDTO) throws IOException;
	public CycleBean updateCycle(CycleBean cycle) throws IOException;
	public void deleteCycle(Integer cycleId) throws IOException;
}
