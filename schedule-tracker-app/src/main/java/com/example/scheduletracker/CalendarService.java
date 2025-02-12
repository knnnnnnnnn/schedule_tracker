package com.example.scheduletracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

@Service
public class CalendarService {

	private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
	private static final String CREDENTIALES_FILE_PATH = "/credentials.json";
	
	/**
	 * 
	 * @param year 年
	 * @param month 月
	 */
	public void connect(int year, int month) {
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME)
					.build();
			
			//取得期間
			LocalDate startLocalDate = LocalDate.of(year, month, 1);
			LocalDate endLocalDate = LocalDate.of(year, month, startLocalDate.getDayOfMonth());
			Date startDate = java.sql.Date.valueOf(startLocalDate);
			Date endDate = java.sql.Date.valueOf(endLocalDate);

			Events events = service.events().list("anakiarukas2@gmail.com")
					.setTimeMin(new DateTime(startDate))
					.setTimeMax(new DateTime(endDate))
					.execute();
			
			List<Event> items = events.getItems();
			if (items.isEmpty()) {
				System.out.println("events found");
			} else {
				for (Event event : items) {
					if (event.getStart() == null) return;
					DateTime start = event.getStart().getDateTime();
					if (start == null) {
						start = event.getStart().getDate();
					}
					System.out.println(event.getSummary());
				}
			}
			
		} catch (GeneralSecurityException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}	
	}
	
	/**
	 * OAuth認証ファイルが存在するかどうか
	 * @return 結果
	 */
	public boolean isFileExsit() {
		String filePath = TOKENS_DIRECTORY_PATH + "/StoredCredential";
		File file = new File(filePath);
		return file.exists();
	}
	
	/**
	 * OAuth認証
	 * @param HTTP_TRANSPORT
	 * @return
	 * @throws IOException
	 */
	private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		
		InputStream in = CalendarService.class.getResourceAsStream(CREDENTIALES_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found : " + CREDENTIALES_FILE_PATH);
		}
		
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		
		Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
		return credential;
	}
}
