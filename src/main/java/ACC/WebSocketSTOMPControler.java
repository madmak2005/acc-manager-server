package ACC;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		List<String> fieldsPhysics = new ArrayList<String>();
	    OutputMessage om = new OutputMessage(sh.getPageFilePhysics(), fieldsPhysics);
	    this.template.convertAndSend("/acc/physics", om);
	}
    
    @Scheduled(fixedRate = 10000)
	@MessageMapping("/static")
	@SendTo("/acc/static")
	public void sendStatic() throws Exception {
		ACCSharedMemory sh = new ACCSharedMemory();
		List<String> fieldsStatic = new ArrayList<String>();
	    OutputMessage om = new OutputMessage(sh.getPageFileStatic(), fieldsStatic);
	    this.template.convertAndSend("/acc/static", om);
	}
    
    @Scheduled(fixedRate = 500)
	@MessageMapping("/graphics")
	@SendTo("/acc/graphics")
	public void sendGraphics() throws Exception {
		ACCSharedMemory sh = new ACCSharedMemory();
		List<String> fieldsGraphics = new ArrayList<String>();
	    OutputMessage om = new OutputMessage(sh.getPageFileGraphics(), fieldsGraphics);
	    this.template.convertAndSend("/acc/graphics", om);
	}
    
}
