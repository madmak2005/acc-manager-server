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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/acc/{page}")
public class WebSocketController {

	private static Map<String, Session> livingSessions = new ConcurrentHashMap<String, Session>();
	private static Session sessionGraphics, sessionPhysics, sessionStatic;

	@OnOpen
	public void openSession(@PathParam("page") String page, Session session) {
		System.out.println("openSession " + page);
		String sessionId = session.getId();
		livingSessions.put(sessionId, session);
		switch (page) {
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

	@OnMessage
	public void onMessage(@PathParam("page") String page, Session session, String message) {
		System.out.println("onMessage");

	}

	@Scheduled(fixedRate = 333)
	private void sendTextGraphics() {
		ACCSharedMemory sh = new ACCSharedMemory();
		String time = new SimpleDateFormat("HH:mm").format(new Date());
		PageFileGraphics p = sh.getPageFileGraphics();
		LocalDateTime now = new LocalDateTime();
		//for debugging if you don't have ACC but want to see some data changes 
		p.rainLights = p.packetId == 0 ? (Math.random() < 0.1 ? 0 : 1) : p.rainLights;
		p.packetId = p.packetId == 0 ? now.getMillisOfDay() : p.packetId;
		OutputMessage om = new OutputMessage(p, time);
		if (sessionGraphics != null && om != null)
			sendText(sessionGraphics, om.content);
	}

	@Scheduled(fixedRate = 333)
	private void sendTextPhysics() {
		ACCSharedMemory sh = new ACCSharedMemory();
		String time = new SimpleDateFormat("HH:mm").format(new Date());
		PageFilePhysics p = sh.getPageFilePhysics();
		LocalDateTime now = new LocalDateTime();
		p.packetId = p.packetId == 0 ? now.getMillisOfDay() : p.packetId;
		OutputMessage om = new OutputMessage(p, time);
		//System.out.println(p.toJSON());
		if (sessionPhysics != null && om != null)
			sendText(sessionPhysics, om.content);
	}

	@Scheduled(fixedRate = 2000)
	private void sendTextStatic() {
		ACCSharedMemory sh = new ACCSharedMemory();
		String time = new SimpleDateFormat("HH:mm").format(new Date());
		PageFileStatic p = sh.getPageFileStatic();
		OutputMessage om = new OutputMessage(p, time);
		if (sessionStatic != null && om != null)
			sendText(sessionStatic, om.content);
	}

	private void sendText(Session session, String message) {
		RemoteEndpoint.Basic basic = session.getBasicRemote();
		try {
			System.out.println(message);
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