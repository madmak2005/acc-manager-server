package ACC.rest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ACC.ApplicationContextAwareImpl;
import ACC.ApplicationPropertyService;
import ACC.model.ApplicationInfo;
import ACC.model.OutputMessage;
import ACC.model.PageFileStatistics;
import ACC.model.StatLap;
import ACC.model.StatSession;
import ACC.saving.ACCDataSaveService;
import ACC.saving.GoogleController;
import ACC.sharedmemory.ACCSharedMemoryService;
import ACC.websocket.WebSocketControllerPage;

@RestController
public class RestControler {

	@Autowired
	ACCSharedMemoryService accSharedMemoryService;
	
	@Autowired
	ACCDataSaveService accDataSaveService;
	
	@Autowired
	ApplicationPropertyService applicationPropertyService;
	
	@Autowired
	ApplicationContext context;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RestControler.class);
	
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
	
    @Value("${application.name}")
    private String applicationName;

    @Value("${build.version}")
    private String buildVersion;

    @Value("${build.timestamp}")
    private String buildTimestamp;
	
    @GetMapping(value = {"/info"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getInfo() {
    	ApplicationInfo ai = new ApplicationInfo(applicationName,buildVersion,buildTimestamp);
    	Gson gson = new Gson();
        return gson.toJson(ai);
    }
	
    @PostMapping(value = { "/setGoogleSheetID" })
	public String setGoogleSheetID(@RequestBody SheetForm sheet) throws Exception {
    	if (sheet != null){
    		applicationPropertyService.setSheetID(sheet.sheetID);
    	}
    	return "ok";
	}
    
	@PostMapping("/setAutoSaveKey")
	public void setAutoSaveKey(@RequestParam Map<String, String> allParams) {
		if (allParams.containsKey("key")) {
			applicationPropertyService.setAutoSaveReplayKey(allParams.get("key"));
		}
	}
	
	@PostMapping("/setAutoSaveActivity")
	public void ssetAutoSaveActivity(@RequestParam Map<String, String> allParams) {
		LOGGER.info("key");
		LOGGER.info(allParams.get("key"));
		if (allParams.containsKey("key")) {
			applicationPropertyService.setAutoSaveReplayActivity(allParams.get("key"));
		}
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
	
	@PutMapping("/importTeamLap")
	public void importTeamLap(@RequestBody String lap) {
		//System.out.println("RECEIVED DATA");
		//System.out.println(lap);
		if (lap!=null && !lap.isEmpty()) {
				Gson gson = new Gson();
				StatLap statlap = gson.fromJson(lap, StatLap.class);
				applicationPropertyService.importLap(statlap);
				/*
				boolean lapExists = stat.currentSession.importStatLap(statlap);
				if (!lapExists) {
					WebSocketControllerPage webSocketControllerPage = (WebSocketControllerPage) context
							.getBean("webSocketControllerPage");
					if (webSocketControllerPage != null) {
						webSocketControllerPage.sendTextMobileStats(statlap);
					}
					applicationPropertyService.importLap(statlap);
				}
				*/
		}
	}
	
	@GetMapping("/getMobileSession")
	public String getMobileSession(@RequestParam Map<String, String> allParams) {
		if (allParams.containsKey("internalSessionIndex")) {
			Gson gson = new Gson();
			ApplicationPropertyService applicationPropertyService = (ApplicationPropertyService) context
					.getBean("applicationPropertyService");
			if (applicationPropertyService != null) {
				int internalSessionIndex = Integer.valueOf(allParams.get("internalSessionIndex"));
				List<StatLap> sessionLaps = applicationPropertyService.getMobileSessionLapList(internalSessionIndex);
				return gson.toJson(sessionLaps);
			} else {
				return gson.toJson(new ArrayList<StatLap>());
			}
		} else {
			Gson gson = new Gson();
			return gson.toJson(new ArrayList<StatLap>());
		}
	}
	@GetMapping("/getMobileSessionList")
	public synchronized String getMobileSessionList(@RequestParam Map<String, String> allParams) {
		Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .create();
			ApplicationPropertyService applicationPropertyService = (ApplicationPropertyService) context
					.getBean("applicationPropertyService");
			if (applicationPropertyService != null) {
				List<StatSession> sessions = applicationPropertyService.getMobileSessionList();
				//List<StatSession> sessionsCleared = new ArrayList<StatSession>();
				//for (StatSession session : sessions) {
				//	StatSession deepCopy = gson.fromJson(gson.toJson(session), StatSession.class);
				//	deepCopy.clearStatData();
				//	sessionsCleared.add(deepCopy);
				//}
				//return gson.toJson(sessionsCleared);
				return gson.toJson(sessions);
			} else {
				return gson.toJson(new ArrayList<StatSession>());
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
