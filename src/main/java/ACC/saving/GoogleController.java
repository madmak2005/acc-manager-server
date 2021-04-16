package ACC.saving;
// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.FormatSchema;
// [START sheets_quickstart]
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import ACC.ApplicationContextAwareImpl;
import ACC.ApplicationPropertyService;
import ACC.ApplicationPropertyServiceImpl;
import ACC.model.OutputMessage;
import ACC.model.PageFileStatistics;
import ACC.sharedmemory.ACCSharedMemoryService;
import lombok.Data;

@Controller
public class GoogleController {
	
	private ACCSharedMemoryService accSharedMemoryService = (ACCSharedMemoryService) ApplicationContextAwareImpl
			.getApplicationContext().getBean("accSharedMemoryService");
	
	private ACCDataSaveService accDataSaveService = (ACCDataSaveService) ApplicationContextAwareImpl
			.getApplicationContext().getBean("accDataSaveService");
	
    static final String APPLICATION_NAME = "ACC Server Manager";
    static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials/credentials.json";
    private static final String CREDENTIALS_FOLDER_PATH = "credentialsFolder";
    static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";

    String spreadsheetId = "";
    
    static GoogleAuthorizationCodeFlow flow;
    
	@Value("${google.oauth.callback.uri}")
	private String CALLBACK_URI;
   
    @PostConstruct
	public void init() throws Exception {
        Resource resource = new ClassPathResource(CREDENTIALS_FILE_PATH);
        InputStream in = resource.getInputStream();
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        File credentialsFolder = new File(CREDENTIALS_FOLDER_PATH);
        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(credentialsFolder)).build();
	}
    
    @GetMapping(value = { "/saveGoogle" })
	public String showHomePage() throws Exception {
    	boolean isUserAuthenticated = false;
		Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
		if (credential != null) {
			boolean tokenValid = credential.refreshToken();
			if (tokenValid) {
				isUserAuthenticated = true;
			}
			OutputMessage om = accSharedMemoryService.getPageFileMessage("statistics", new ArrayList<String>());
			PageFileStatistics statistics = (PageFileStatistics) om.page;
			accDataSaveService.saveToGoogle(statistics);
		}
		return isUserAuthenticated ? "dashboard.html" : "index.html";
	}
    
   
	@GetMapping(value = { "/googlesignin" })
	public void doGoogleSignIn(HttpServletResponse response) throws Exception {
		GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
		String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
		response.sendRedirect(redirectURL);
	}

	@GetMapping(value = { "/oauth" })
	public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
		String code = request.getParameter("code");
		if (code != null) {
			saveToken(code);
			return "dashboard.html";
		}

		return "index.html";
	}
	
	private void saveToken(String code) throws Exception {
		GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
		flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);

	}

    public void docsTest() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        final String range = "Class Data!A2:E";
        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            System.out.println("Name, Major");
            for (List row : values) {
                System.out.printf("%s, %s\n", row.get(0), row.get(4));
            }
        }
    }
    
}


