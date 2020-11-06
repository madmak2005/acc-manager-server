package ACC;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketSTOMPControler {
	
    @Autowired
    private SimpMessagingTemplate template;
	
    @Scheduled(fixedRate = 500)
	@MessageMapping("/physics")
	@SendTo("/acc/physics")
	public void sendPhysics() throws Exception {
		ACCSharedMemory sh = new ACCSharedMemory();
	    String time = new SimpleDateFormat("HH:mm").format(new Date());
	    OutputMessage om = new OutputMessage(sh.getPageFilePhysics(), time);
	    this.template.convertAndSend("/acc/physics", om);
	}
    
    @Scheduled(fixedRate = 10000)
	@MessageMapping("/static")
	@SendTo("/acc/static")
	public void sendStatic() throws Exception {
		ACCSharedMemory sh = new ACCSharedMemory();
	    String time = new SimpleDateFormat("HH:mm").format(new Date());
	    OutputMessage om = new OutputMessage(sh.getPageFileStatic(), time);
	    this.template.convertAndSend("/acc/static", om);
	}
    
    @Scheduled(fixedRate = 500)
	@MessageMapping("/graphics")
	@SendTo("/acc/graphics")
	public void sendGraphics() throws Exception {
		ACCSharedMemory sh = new ACCSharedMemory();
	    String time = new SimpleDateFormat("HH:mm").format(new Date());
	    OutputMessage om = new OutputMessage(sh.getPageFileGraphics(), time);
	    this.template.convertAndSend("/acc/graphics", om);
	}
    
}
