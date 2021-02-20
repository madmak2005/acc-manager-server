package app;


import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@SpringBootApplication
@EnableWebSocket
@ComponentScan(basePackages = { "ACC", "virtualKeyboard" })
public class Application {
	public static final Logger LOGGER=LoggerFactory.getLogger(Application.class);
	
	public static boolean debug = false;
	public static boolean useDebug = false;
		
	public static void main(String[] args) {
		
        
		for (String s: args) {
            if (s.toUpperCase().equals("DEBUG")) {
            	debug = true;
            }
            if (s.toUpperCase().equals("USEDEBUG")) {
            	LOGGER.debug("USE DEBUG");
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
			  
			  System.out.println("=========================");
			  System.out.println("My IP: " + ip);
			  System.out.println("=========================");

			} catch (SocketException | UnknownHostException e) {
				System.out.println(e);
			}
	}
	


}
