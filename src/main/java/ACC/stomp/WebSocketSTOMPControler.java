package ACC.stomp;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import ACC.ApplicationContextAwareImpl;
import ACC.model.OutputMessage;
import ACC.sharedmemory.ACCSharedMemory;
import ACC.sharedmemory.ACCSharedMemoryService;

@Controller
public class WebSocketSTOMPControler {
	
    @Autowired
    private SimpMessagingTemplate template;
	
    private ACCSharedMemoryService accSharedMemoryService = (ACCSharedMemoryService) ApplicationContextAwareImpl
			.getApplicationContext().getBean("accSharedMemoryService");
	
    @Scheduled(fixedRate = 500)
	@MessageMapping("/physics")
	@SendTo("/acc/physics")
	public void sendPhysics() throws Exception {
		List<String> fields = new ArrayList<String>();
	    FilterProvider filters = new SimpleFilterProvider()  
			      .addFilter("filter1", SimpleBeanPropertyFilter.serializeAllExcept(""));
		ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
		String res = mapper.writeValueAsString(accSharedMemoryService.getPageFile("physics"));
		OutputMessage om = new OutputMessage(res);
	    this.template.convertAndSend("/acc/physics", om);
	}
    
    @Scheduled(fixedRate = 10000)
	@MessageMapping("/static")
	@SendTo("/acc/static")
	public void sendStatic() throws Exception {
		List<String> fields = new ArrayList<String>();
	    FilterProvider filters = new SimpleFilterProvider()  
			      .addFilter("filter1", SimpleBeanPropertyFilter.serializeAllExcept(""));
		ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
		String res = mapper.writeValueAsString(accSharedMemoryService.getPageFile("static"));
		OutputMessage om = new OutputMessage(res);
	    this.template.convertAndSend("/acc/static", om);
	}
    
    @Scheduled(fixedRate = 500)
	@MessageMapping("/graphics")
	@SendTo("/acc/graphics")
	public void sendGraphics() throws Exception {
		List<String> fields = new ArrayList<String>();
	    FilterProvider filters = new SimpleFilterProvider()  
			      .addFilter("filter1", SimpleBeanPropertyFilter.serializeAllExcept(""));
		ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
		String res = mapper.writeValueAsString(accSharedMemoryService.getPageFile("graphics"));
		OutputMessage om = new OutputMessage(res);
		this.template.convertAndSend("/acc/graphics",om);
	}
    
}
