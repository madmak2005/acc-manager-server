package ACC;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


@Configuration
@EnableWebSocket
@ComponentScan("ACC")
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
	
    @Bean
    public WebSocketControllerPage webSocketController() {
        return new WebSocketControllerPage();
    }

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		
	}

}
