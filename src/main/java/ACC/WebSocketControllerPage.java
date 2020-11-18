package ACC;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.joda.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;

import ACC.model.PageFileGraphics;
import ACC.model.PageFilePhysics;
import ACC.model.PageFileStatic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 * This class allows to open websocket session to receive data of
 * Assetto Corsa Competizione pages of type: SPageFilePhysics, SPageFileGraphic, SPageFileStatic
 *
 */

/**
 * @author tomasz.makowski
 *
 */
@ServerEndpoint("/acc/{page}")
public class WebSocketControllerPage {
	private static Map<String, Session> livingSessions = new ConcurrentHashMap<String, Session>();
	private static Session sessionGraphics, sessionPhysics, sessionStatic;
	private static List<String> fieldsGraphics;
	private static List<String> fieldsPhysics;
	private static List<String> fieldsStatic;
	
	/**
	 * @param pageName one of 'graphics', 'physics', 'static' values
	 * @param session
	 * 
	 */
	@OnOpen
	public void openSession(@PathParam("page") String pageName, Session session) {
		System.out.println("openSession " + pageName);
		String sessionId = session.getId();
		livingSessions.put(sessionId, session);
		switch (pageName) {
		case "graphics" -> {
			sessionGraphics = session;
			sendTextGraphics();
		}
		case "physics" -> {
			sessionPhysics = session;
			sendTextPhysics();
		}
		case "static" -> {
			sessionStatic = session;
			sendTextStatic();
		}
		}
	}
	

	/**
	 * @param page
	 * @param session
	 * @param message
	 * 
	 * Not used
	 */
	@OnMessage
	public void onMessage(@PathParam("page") String page, Session session, String message) {
		Message msg = new Message(message);
		if (sessionGraphics != null && sessionGraphics.getId() == session.getId()) {
			fieldsGraphics = msg.getFildsToFilter();
		}
		if (sessionPhysics != null && sessionPhysics.getId() == session.getId()) {
			fieldsPhysics = msg.getFildsToFilter();
		}
		if (sessionStatic != null && sessionStatic.getId() == session.getId()) {
			fieldsStatic = msg.getFildsToFilter();
		}
		System.out.println("onMessage");

	}

	@Scheduled(fixedRate = 200)
	private void sendTextGraphics() {
		ACCSharedMemory sh = new ACCSharedMemory();
		PageFileGraphics p = sh.getPageFileGraphics();
		LocalDateTime now = new LocalDateTime();
		//for debugging only,  if you don't have ACC but want to see some data changes 
		p.lightsStage = p.packetId == 0 ? (Math.random() < 0.5 ? (Math.random() < 0.5 ? 0 : 1) : 2) : p.rainLights;
		p.packetId = p.packetId == 0 ? now.getMillisOfDay() : p.packetId;
		OutputMessage om = new OutputMessage(p, fieldsGraphics);
		if (sessionGraphics != null && om != null)
			sendText(sessionGraphics, om.content);
	}

	@Scheduled(fixedRate = 200)
	private void sendTextPhysics() {
		ACCSharedMemory sh = new ACCSharedMemory();
		PageFilePhysics p = sh.getPageFilePhysics();
		LocalDateTime now = new LocalDateTime();
		p.packetId = p.packetId == 0 ? now.getMillisOfDay() : p.packetId;
		OutputMessage om = new OutputMessage(p, fieldsPhysics);
		if (sessionPhysics != null && om != null)
			sendText(sessionPhysics, om.content);
	}

	@Scheduled(fixedRate = 2000)
	private void sendTextStatic() {
		ACCSharedMemory sh = new ACCSharedMemory();
		PageFileStatic p = sh.getPageFileStatic();
		OutputMessage om = new OutputMessage(p, fieldsStatic);
		if (sessionStatic != null && om != null)
			sendText(sessionStatic, om.content);
	}

	private void sendText(Session session, String message) {
		RemoteEndpoint.Basic basic = session.getBasicRemote();
		try {
			//System.out.println(message);
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

		livingSessions.remove(sessionId);

	}

}
