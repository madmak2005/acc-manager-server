package ACC;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import ACC.model.PageFilePhysics;

@Controller
public class WebsocketControler {
	
    @Autowired
    private SimpMessagingTemplate template;
	
    @Scheduled(fixedRate = 500)
	@MessageMapping("/page")
	@SendTo("/acc/messages")
	public void send() throws Exception {
		ACCSharedMemory sh = new ACCSharedMemory();
	    String time = new SimpleDateFormat("HH:mm").format(new Date());
	    OutputMessage om = new OutputMessage(sh.getPageFilePhysics(), time);
	    this.template.convertAndSend("/acc/messages", om);
	}
    
}
