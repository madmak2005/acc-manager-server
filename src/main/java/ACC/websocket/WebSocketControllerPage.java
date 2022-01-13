package ACC.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ACC.ApplicationContextAwareImpl;
import ACC.ApplicationPropertyService;
import ACC.acm.AutomaticCarManagementService;
import ACC.acm.MacroAction;
import ACC.acm.MacroManagement;
import ACC.model.Message;
import ACC.model.OutputMessage;
import ACC.model.Page;
import ACC.model.PageFileStatistics;
import ACC.model.StatLap;
import ACC.model.StatPoint;
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

	@Autowired
	private ApplicationPropertyService applicationPropertyService = (ApplicationPropertyService) ApplicationContextAwareImpl
	.getApplicationContext().getBean("applicationPropertyService");;
	
	private static Map<String, Session> livingSessions = new ConcurrentHashMap<String, Session>();
	private static Session sessionGraphics, sessionPhysics, sessionStatic, sessionMacro, sessionStatistics;
	private static List<Session> sessionMobileStatsList = new ArrayList<Session>();
	
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
			sendTextStatistics();
			break;
		case "mobileStats":
			//sessionMobileStats = session;
			sessionMobileStatsList.add(session);
			sendAllTextMobileStats();
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
	private void runTextStatistics() {
		OutputMessage om = accSharedMemoryService.getPageFileMessage("statistics", fieldsStatistics);
		if (sessionStatistics != null && om != null) {
			PageFileStatistics stat = (PageFileStatistics) om.page;
			String response = "";
			try {
				Set<String> fieldsFilter = new HashSet<String>();
				FilterProvider filters = new SimpleFilterProvider().addFilter("filter1",
						SimpleBeanPropertyFilter.serializeAllExcept(fieldsFilter));
				ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
				mapper.setSerializationInclusion(Include.NON_NULL);
				response = mapper.writeValueAsString(stat.currentSession);
			} catch (JsonProcessingException e) {
				Application.LOGGER.error(e.toString());
			}
			sendText(sessionStatistics, response);
			
		}
			
	}
	
	public void sendTextStatistics() {
		OutputMessage om = accSharedMemoryService.getPageFileMessage("statistics", fieldsStatistics);
		if (sessionStatistics != null && om != null) {
			PageFileStatistics stat = (PageFileStatistics) om.page;
			String response = "";
			try {
				ObjectMapper mapper = new ObjectMapper();
				response = mapper.writeValueAsString(stat);
			} catch (JsonProcessingException e) {
				Application.LOGGER.error(e.toString());
			}
			
			sendText(sessionStatistics, response);
		}
	}
	
	public void sendTextMobileStats(StatLap prevLap) {
		if (!sessionMobileStatsList.isEmpty()) {
			Gson gson = new GsonBuilder()
			                //.setPrettyPrinting()
			                .serializeSpecialFloatingPointValues() // This is the key
			                .create();
			StatLap deepCopy = gson.fromJson(gson.toJson(prevLap), StatLap.class);
			deepCopy.clearStatData();
			String response = "";
			try {
				ObjectMapper mapper = new ObjectMapper();
				response = mapper.writeValueAsString(deepCopy);
				sendText(sessionMobileStatsList, response);
			} catch (JsonProcessingException e) {
				Application.LOGGER.debug(e.toString());
			}
			
		}
	}
	
	private void sendAllTextMobileStats() {
		/*
		 * Send all laps from current session onConnection
		 */
		// ApplicationPropertyService applicationPropertyService =
		// (ApplicationPropertyService) context
		// .getBean("applicationPropertyService");
		if (applicationPropertyService != null) {
			OutputMessage om = accSharedMemoryService.getPageFileMessage("statistics", fieldsStatistics);
			if (!sessionMobileStatsList.isEmpty() && om != null) {
				PageFileStatistics stat = (PageFileStatistics) om.page;
				if (stat != null && stat.currentSession != null) {
					int internalSessionIndex = stat.currentSession.internalSessionIndex;
					List<StatLap> sessionLaps = applicationPropertyService
							.getMobileSessionLapList(internalSessionIndex);
					sessionLaps.forEach(lap -> {
					
						String response = "";
						try {
							ObjectMapper mapper = new ObjectMapper();
							mapper.setSerializationInclusion(Include.NON_NULL);
							response = mapper.writeValueAsString(lap);
							sendText(sessionMobileStatsList, response);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (JsonProcessingException e) {
							Application.LOGGER.debug(e.toString());
						}
					});
				} else {
					StatLap emptyLap = new StatLap();
					Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
					StatLap deepCopy = gson.fromJson(gson.toJson(emptyLap), StatLap.class);
					deepCopy.clearStatData();
					String response = "";
					try {
						ObjectMapper mapper = new ObjectMapper();
						mapper.setSerializationInclusion(Include.NON_NULL);
						response = mapper.writeValueAsString(deepCopy);
					} catch (JsonProcessingException e) {
						Application.LOGGER.debug(e.toString());
					}
					sendText(sessionMobileStatsList, response);
				}
			}
		}
		/*
		 * OutputMessage om = accSharedMemoryService.getPageFileMessage("statistics",
		 * fieldsStatistics); if (sessionMobileStats != null && om != null) {
		 * PageFileStatistics stat = (PageFileStatistics) om.page; int currentLapNo = 0;
		 * if (stat.currentSession != null && stat.currentSession.laps != null) {
		 * Map<Integer, StatLap> lapsToSend = stat.currentSession.laps;
		 * Iterator<StatLap> it = lapsToSend.values().iterator(); if (it.hasNext()) {
		 * while (it.hasNext()) { StatLap lap = it.next(); //Gson gson = new
		 * GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create()
		 * ; //StatLap deepCopy = gson.fromJson(gson.toJson(lap), StatLap.class);
		 * StatLap deepCopy = null; synchronized(this){ deepCopy = (StatLap)
		 * SerializationUtils.clone(lap); } deepCopy.clearStatData(); String response =
		 * ""; try { ObjectMapper mapper = new ObjectMapper();
		 * mapper.setSerializationInclusion(Include.NON_NULL); response =
		 * mapper.writeValueAsString(deepCopy); } catch (JsonProcessingException e) {
		 * Application.LOGGER.debug(e.toString()); } if (lap.lapNo > currentLapNo) {
		 * currentLapNo = lap.lapNo; sendText(sessionMobileStats, response); try {
		 * Thread.sleep(50); } catch (InterruptedException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } } } } else { StatLap emptyLap = new
		 * StatLap(); Gson gson = new
		 * GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create()
		 * ; StatLap deepCopy = gson.fromJson(gson.toJson(emptyLap), StatLap.class);
		 * deepCopy.clearStatData(); String response = ""; try { ObjectMapper mapper =
		 * new ObjectMapper(); mapper.setSerializationInclusion(Include.NON_NULL);
		 * response = mapper.writeValueAsString(deepCopy); } catch
		 * (JsonProcessingException e) { Application.LOGGER.debug(e.toString()); }
		 * sendText(sessionMobileStats, response); } } }
		 */
	}
	

	private void sendText(Session session, String message) {
		if (session.isOpen()) {
			RemoteEndpoint.Basic basic = session.getBasicRemote();
			try {
				basic.sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendText(List<Session> sessions, String message) {
		for (Session session : sessions) {
			if (session.isOpen()) {
				RemoteEndpoint.Basic basic = session.getBasicRemote();
				try {
					basic.sendText(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

	@OnError
	public void onError(Session session, Throwable thr) {
		System.out.println("Connection terminated");
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
		
		
		sessionMobileStatsList.remove(session);
		

		livingSessions.remove(sessionId);

	}

}
