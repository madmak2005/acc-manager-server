package virtualKeyboard;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class VirtualKeyboardApplication {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	public static final Logger LOGGER=LoggerFactory.getLogger(VirtualKeyboardApplication.class);
	
	public static void main(String[] args) {
		
		
		SpringApplication.run(VirtualKeyboardApplication.class, args);
		showIP();
	}
	
	private static void showIP() {
		try(final DatagramSocket socket = new DatagramSocket()){
			  socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			  String ip = socket.getLocalAddress().getHostAddress();
			  System.out.println(ANSI_YELLOW + "========================="  + ANSI_RESET);
			  System.out.println("My IP: " + ANSI_GREEN + ip  + ANSI_RESET);
			  System.out.println(ANSI_YELLOW + "========================="  + ANSI_RESET);
			} catch (SocketException | UnknownHostException e) {
				System.out.println(e);
			}
	}

}
