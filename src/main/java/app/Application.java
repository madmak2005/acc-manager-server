package app;


import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.socket.config.annotation.EnableWebSocket;



@SpringBootApplication
@EnableWebSocket
@ComponentScan(basePackages = { "ACC", "virtualKeyboard" })
public class Application {
    
	public static Logger LOGGER=LoggerFactory.getLogger(Application.class);
	public static boolean debug = false;
	public static boolean useDebug = false;

	public static void main(String[] args) {
		for (String s: args) {
            if (s.toUpperCase().equals("DEBUG")) {
            	LOGGER.info("DEBUG: save data to json files");
            	debug = true;
            }
            if (s.toUpperCase().equals("USEDEBUG")) {
            	LOGGER.info("USE DEBUG: ");
            	useDebug = true;
            }
            
        }
		SpringApplication.run(Application.class, args);
		showIP();
	}
	
	
	private static void showIP() {
		try(final DatagramSocket socket = new DatagramSocket()){
			  socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			  String ip = socket.getLocalAddress().getHostAddress();
			  String port = "8080";
			  LOGGER.info("=========================================");
			  LOGGER.info("In mobile application enter:");
			  LOGGER.info("IP: " + ip);
			  LOGGER.info("PORT: " + port);
			  LOGGER.info("=========================================");
			  LOGGER.info("In webbrowser: http://localhost:" + port);
			  LOGGER.info("=========================================");

			} catch (SocketException | UnknownHostException e) {
				LOGGER.error(e.toString());
			}
	}
}
