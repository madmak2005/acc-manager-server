package app;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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
