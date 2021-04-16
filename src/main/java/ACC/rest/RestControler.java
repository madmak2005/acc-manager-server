package ACC.rest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import ACC.ApplicationContextAwareImpl;
import ACC.ApplicationPropertyService;
import ACC.model.OutputMessage;
import ACC.model.PageFileStatistics;
import ACC.saving.ACCDataSaveService;
import ACC.saving.GoogleController;
import ACC.sharedmemory.ACCSharedMemoryService;

@RestController
public class RestControler {

	@Autowired
	ACCSharedMemoryService accSharedMemoryService;
	
	@Autowired
	ACCDataSaveService accDataSaveService;
	
	@Autowired
	ApplicationPropertyService applicationPropertyService;
	
	/*
	private ApplicationPropertyService applicationPropertyService = (ApplicationPropertyService) ApplicationContextAwareImpl
			.getApplicationContext().getBean("applicationPropertyService");
	 */
	
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
	public String saveSessions() {
		List<String> fieldsStatistics = new ArrayList<String>();
		OutputMessage om = accSharedMemoryService.getPageFileMessage("statistics", fieldsStatistics);
		PageFileStatistics statistics = (PageFileStatistics) om.page;
		return accDataSaveService.saveToXLS(statistics);
	}

	@GetMapping("/google")
	public String saveSessionsGoogle() {
		GoogleController dq = new GoogleController();
		try {
			dq.docsTest();
		} catch (IOException | GeneralSecurityException e) {
			// TODO Auto-generated catch block
			return e.toString();
		}
		return "ok";
	}
	
    @PostMapping(value = { "/setGoogleSheetID" })
	public String setGoogleSheetID(@RequestBody SheetForm sheet) throws Exception {
    	if (sheet != null){
    		applicationPropertyService.setSheetID(sheet.sheetID);
    	}
    	return "ok";
	}

	@GetMapping("/getSession")
	public String getSessions(@RequestParam Map<String, String> allParams) {
		List<String> fieldsStatistics = new ArrayList<String>();
		OutputMessage om = accSharedMemoryService.getPageFileMessage("statistics", fieldsStatistics);
		if (allParams.containsKey("range")) {
			Gson gson = new Gson();
			PageFileStatistics s = (PageFileStatistics) om.page;
			switch (allParams.get("range")) {
			case "all":
				return om.content;
			case "lastLap":
				return gson.toJson(s.currentSession.lastLap);
			case "currentSession":
				return gson.toJson(s.currentSession);
			default:
				return om.content;
			}
		} else {
			System.out.println(om.content);
			return om.content;
		}
	}

}

class SheetForm {
	public String sheetID;

	protected String getSheetID() {
		return sheetID;
	}

	protected void setSheetID(String sheetID) {
		this.sheetID = sheetID;
	}
}
