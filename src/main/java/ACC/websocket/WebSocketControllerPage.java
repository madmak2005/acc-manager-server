package ACC.websocket;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ACC.ApplicationContextAwareImpl;
import ACC.acm.AutomaticCarManagementService;
import ACC.acm.MacroAction;
import ACC.acm.MacroManagement;
import ACC.model.Message;
import ACC.model.OutputMessage;
import ACC.model.PageFileStatistics;
import ACC.saving.ACCDataSaveService;
import ACC.sharedmemory.ACCSharedMemoryService;
import app.Application;

/**
 * This class allows to open websocket session to receive data of
 * Assetto Corsa Competizione pages of type: SPageFilePhysics, SPageFileGraphic, SPageFileStatic
 *
 */

/**
 * @author tomasz.makowski
 *
 */
@Controller
@ServerEndpoint("/acc/{page}")

public class WebSocketControllerPage {

	private AutomaticCarManagementService automaticCarManagementService = (AutomaticCarManagementService) ApplicationContextAwareImpl
			.getApplicationContext().getBean("automaticCarManagementService");
	
	private ACCSharedMemoryService accSharedMemoryService = (ACCSharedMemoryService) ApplicationContextAwareImpl
			.getApplicationContext().getBean("accSharedMemoryService");
	
	private ACCDataSaveService accDataSaveService = (ACCDataSaveService) ApplicationContextAwareImpl
			.getApplicationContext().getBean("accDataSaveService");

	private static Map<String, Session> livingSessions = new ConcurrentHashMap<String, Session>();
	private static Session sessionGraphics, sessionPhysics, sessionStatic, sessionMacro, sessionStatistics;
	private static List<String> fieldsGraphics;
	private static List<String> fieldsPhysics;
	private static List<String> fieldsStatic;
	private static List<String> fieldsStatistics;
	private PageFileStatistics statistics = new PageFileStatistics();

	/**
	 * @param pageName one of 'graphics', 'physics', 'static' values
	 * @param session
	 * 
	 */
	@OnOpen
	public void openSession(@PathParam("page") String pageName, Session session) {
		System.out.println(automaticCarManagementService);
		System.out.println("openSession " + pageName);
		String sessionId = session.getId();
		livingSessions.put(sessionId, session);
		
		switch (pageName) {
		case "graphics":
			sessionGraphics = session;
			//sendTextGraphics();
			break;
		case "physics":
			sessionPhysics = session;
			//sendTextPhysics();
			break;
		case "static":
			sessionStatic = session;
			//sendTextStatic();
			break;
		case "macro":
			sessionMacro = session;
			sendTextMacro();
			break;
		case "statistics":
			sessionStatistics = session;
			//sendTextStatistics();
			break;
		}
	}


	/**
	 * @param page
	 * @param session
	 * @param message
	 * 
	 *                Not used
	 */
	@OnMessage
	public void onMessage(@PathParam("page") String page, Session session, String message) {
		Message msg = new Message(message);
		if (sessionMacro != null && sessionMacro.getId() == session.getId()) {
			if (message.startsWith("{")) {
				try {
					MacroAction action = new ObjectMapper().readValue(message, MacroAction.class);
					if (action.getOrder() > 0)
						automaticCarManagementService.addAction(action);
					if (action.isActive()) {
						automaticCarManagementService.activateMacro();
					} else {
						automaticCarManagementService.deactivateMacro();
					}
				} catch (JsonProcessingException e) {
				}
				try {
					MacroManagement action = new ObjectMapper().readValue(message, MacroManagement.class);
					automaticCarManagementService.deleteAction(action);
				} catch (JsonProcessingException e) {

				}

			} else if (message.startsWith("[")) {
				try {
					List<MacroAction> actionList = new ObjectMapper().readValue(message,
							new TypeReference<List<MacroAction>>() {
							});
					automaticCarManagementService.addActionList(actionList);
				} catch (JsonProcessingException e) {

				}
				try {
					MacroManagement action = new ObjectMapper().readValue(message, MacroManagement.class);
					automaticCarManagementService.deleteAction(action);
				} catch (JsonProcessingException e) {

				}
			}
		}
		if (sessionGraphics != null && sessionGraphics.getId() == session.getId()) {
			fieldsGraphics = msg.getFildsToFilter();
		}
		if (sessionPhysics != null && sessionPhysics.getId() == session.getId()) {
			fieldsPhysics = msg.getFildsToFilter();
		}
		if (sessionStatic != null && sessionStatic.getId() == session.getId()) {
			fieldsStatic = msg.getFildsToFilter();
		}
		if (sessionStatistics != null && sessionStatistics.getId() == session.getId()) {
			if (message.startsWith("{")) {
				if (message.equals("{\"action\": \"saveSessions\"}")) {
					OutputMessage om = accSharedMemoryService.getPageFileMessage("statistics", fieldsStatistics);
					statistics = (PageFileStatistics) om.page;
					accDataSaveService.saveToXLS(statistics);
				}
			} else {
				fieldsStatistics = msg.getFildsToFilter();
			}
		}
		System.out.println("onMessage");

	}

	@Scheduled(fixedRateString = "#{@applicationPropertyService.getApplicationProperty()}")
	private void sendTextGraphics() {
		OutputMessage om = accSharedMemoryService.getPageFileMessage("graphics", fieldsGraphics);
		if (sessionGraphics != null && om != null) {
			sendText(sessionGraphics, om.content);
		}
	}

	@Scheduled(fixedRateString = "#{@applicationPropertyService.getApplicationProperty()}")
	private void sendTextPhysics() {
		OutputMessage om = accSharedMemoryService.getPageFileMessage("physics", fieldsPhysics);
		if (sessionPhysics != null && om != null)
			sendText(sessionPhysics, om.content);
	}

	@Scheduled(fixedRate = 2000)
	private void sendTextStatic() {
		OutputMessage om = accSharedMemoryService.getPageFileMessage("static", fieldsStatic);
		if (sessionStatic != null && om != null)
			sendText(sessionStatic, om.content);
	}

	@Scheduled(fixedRate = 500)
	private void sendTextMacro() {
		if (sessionMacro != null)
			automaticCarManagementService.executeMacro();
	}
	
	
	@Scheduled(fixedRateString = "#{@applicationPropertyService.getStatisticsInterval()}")
	private void sendTextStatistics() {
		OutputMessage om = accSharedMemoryService.getPageFileMessage("statistics", fieldsStatistics);
		if (sessionStatistics != null && om != null) {
			PageFileStatistics stat = (PageFileStatistics) om.page;
			//stat.currentSession.last3Laps = new CircularFifoQueue<>(3);
			//stat.currentSession.last5Laps = new CircularFifoQueue<>(5);
			/*
			Gson gson = new GsonBuilder()
	                .setPrettyPrinting()
	                .serializeSpecialFloatingPointValues() // This is the key
	                .create();
	        */
			sendText(sessionStatistics, stat.currentSession.toJSON());
		}
			
	}
	
	//private void saveStatistics() {
//		statistics.saveToXLSX();
//	}

	private void sendText(Session session, String message) {
		RemoteEndpoint.Basic basic = session.getBasicRemote();
		try {
			// System.out.println(message);
			basic.sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	


	@OnClose
	public void onClose(@PathParam("page") String page, Session session) {

		String sessionId = session.getId();

		if (sessionGraphics != null && sessionGraphics.getId() == session.getId()) {
			System.out.println("onClose sessionGraphics");
			sessionGraphics = null;
		}
		if (sessionPhysics != null && sessionPhysics.getId() == session.getId()) {
			System.out.println("onClose sessionPhysics");
			sessionPhysics = null;
		}
		if (sessionStatic != null && sessionStatic.getId() == session.getId()) {
			System.out.println("onClose sessionStatic");
			sessionStatic = null;
		}
		if (sessionStatistics != null && sessionStatistics.getId() == session.getId()) {
			System.out.println("onClose sessionStatistics");
			sessionStatistics = null;
		}

		livingSessions.remove(sessionId);

	}

}
