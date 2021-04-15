package ACC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ACC.acm.AutomaticCarManagementService;
import ACC.saving.ACCDataSaveService;
import ACC.sharedmemory.ACCSharedMemoryService;
import ACC.websocket.WebSocketControllerPage;


@Configuration
@ComponentScan("ACC")
@ComponentScan("ACC.saving")
@ComponentScan("ACC.websocket")
@EnableWebSocket
public class ACCConfig implements WebSocketConfigurer{
	
	@Autowired
	private ApplicationPropertyService applicationPropertyService;
	
	@Bean("applicationPropertyService") 
	public ApplicationPropertyService applicationPropertyService() {
		return applicationPropertyService;
	}	
	
	@Autowired
	private ACCDataSaveService accDataSaveService;
	
	@Bean("accDataSaveService")
	@DependsOn({"applicationPropertyService"})
	public ACCDataSaveService accDataSaveService() {
		return accDataSaveService;
	}
	
	@Autowired
	private ACCSharedMemoryService accSharedMemoryService;
	
	@Bean("accSharedMemoryService")
	public ACCSharedMemoryService accSharedMemoryService() {
		return accSharedMemoryService;
	}
	
	@Autowired
	private AutomaticCarManagementService automaticCarManagementService;
	
	@Bean("automaticCarManagementService") 
	public AutomaticCarManagementService automaticCarManagementService() {
		return automaticCarManagementService;
	}

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

	@Bean
    public WebSocketControllerPage webSocketController() throws JsonMappingException, JsonProcessingException {
       return new WebSocketControllerPage();
    }

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		
	}
}
