package ACC.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ACC.model.OutputMessage;
import ACC.model.PageFileStatistics;
import ACC.sharedmemory.ACCSharedMemoryService;

@RestController
public class RestControler {
	
	@Autowired
	ACCSharedMemoryService accSharedMemoryService; 
	
	@GetMapping("/SPageFileStatic")
	public String getStaticJson() {
		return accSharedMemoryService.getPageFile("static").toJSON();
	}
	
	@GetMapping("/SPageFilePhysics")
	public String getPhysicsJson() {
		return accSharedMemoryService.getPageFile("physics").toJSON();
	}
	
	@GetMapping("/SPageFileGraphics")
	public String getGraphicsJson() {
		return accSharedMemoryService.getPageFile("graphics").toJSON();
	}
	
	@GetMapping("/save")
	public String getSaveSessions() {
		List<String> fieldsStatistics = new ArrayList<String>();
		OutputMessage om = accSharedMemoryService.getPageFileMessage("statistics", fieldsStatistics);
		PageFileStatistics statistics = (PageFileStatistics) om.page;
		statistics.saveToXLSX();
		return statistics.toJSON();
	}
	

}
