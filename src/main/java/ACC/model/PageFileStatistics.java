package ACC.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.method.annotation.SessionAttributeMethodArgumentResolver;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.gson.Gson;

import ACC.ApplicationContextAwareImpl;
import ACC.saving.ACCDataSaveService;
import ACC.sharedmemory.ACCSharedMemoryService;
import app.Application;

@JsonFilter("filter1")
public class PageFileStatistics implements Page {
	@Autowired
	ACCDataSaveService accDataSaveService;

	public PageFileStatistics() {
		super();
		setPageName("statistics");
		previous = Instant.now();
	}
	@JsonIgnore
	private static final Logger LOGGER = LoggerFactory.getLogger(PageFileStatistics.class);
	@JsonIgnore
	public Map<Integer, StatSession> sessions = new HashMap<>();
	
	public StatSession currentSession = new StatSession();
	private String pageName;
	@JsonIgnore
	protected LocalDateTime currentDateAndTime = LocalDateTime.now();
	@JsonIgnore
	protected Instant previous, current;
	
	protected int raceStartAt = 0;
	@JsonIgnore
	protected Integer sessionCounter = 0;
	@JsonIgnore
	protected Instant lastChange = Instant.now();
	
	@JsonIgnore
	private boolean saved = false;
 
	public static String mstoStr(long durationInMillis) {
		long millis = durationInMillis % 1000;
		long second = (durationInMillis / 1000) % 60;
		long minute = (durationInMillis / (1000 * 60)) % 60;
		long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

		return String.format("%02d:%02d:%02d:%03d",hour, minute, second, millis);
	}
	
	public void addStatPoint(StatPoint statPoint) {
		StatPoint prevStatPoint = null;
		currentSession = sessions.get(sessionCounter);

		if (currentSession == null) {
			LOGGER.info("New session");
			currentSession = newSession(statPoint);
		}

		if (statPoint.iCurrentTime != 0) {
			if (currentSession.currentLap.statPoints.size() > 0) {
				prevStatPoint = currentSession.currentLap.statPoints
						.get(currentSession.currentLap.statPoints.size() - 1);
				currentSession.currentLap.addStatPoint(statPoint);
				if (statPoint.packetIDG != prevStatPoint.packetIDG && statPoint.packetIDP != prevStatPoint.packetIDP) {
					lastChange = Instant.now();
				}
				if (prevStatPoint.iCurrentTime != statPoint.iCurrentTime) {
					if (prevStatPoint.iCurrentTime > statPoint.iCurrentTime && prevStatPoint.lapNo == statPoint.lapNo) {
						// LOGGER.info("Kunos miracle. Skip this data");
					} else {
						currentSession.packetDelta = Math.abs(statPoint.packetIDG - prevStatPoint.packetIDP);
						if (Duration.between(lastChange, Instant.now()).getSeconds() > 5 && !saved) {
							LOGGER.info("Finished? Save sessions.");
							accDataSaveService.saveToXLS(this);
							saved = true;
						}

						if (newSessionStarted(prevStatPoint, statPoint))
							currentSession = newSession(statPoint);

						if (statPoint.flag == AC_FLAG_TYPE.ACC_GREEN_FLAG
								&& statPoint.session == AC_SESSION_TYPE.AC_RACE && currentSession != null
								&& !currentSession.wasGreenFlag) {
							// System.out.println(gson.toJson(currentSession));
							LOGGER.info("GREEN FLAG, GREEN FLAG, GREEN, GREEN, GREEN!!!!!!!");
							raceStartAt = statPoint.iCurrentTime;
							LOGGER.info("Race start at [ms]: " + String.valueOf(raceStartAt));
							currentSession = newSession(statPoint);
							currentSession.wasGreenFlag = true;
							currentSession.currentLap.first = true;
						}

						StatLap lap = currentSession.laps.get(statPoint.lapNo);

						if (lap == null) {
							if (statPoint.normalizedCarPosition <= 1) {
								LOGGER.info("New lap started: [" + statPoint.lapNo + "]");
								LOGGER.info("Car position: [" + statPoint.normalizedCarPosition + "]");
								StatLap prevLap = currentSession.laps.get(statPoint.lapNo - 1);
								if (prevLap != null) {
									prevLap.lapTime = statPoint.iLastTime;
									prevLap.splitTimes.put(statPoint.car.sectorCount - 1, statPoint.iLastTime);
									prevLap.lapTime = statPoint.iLastTime;
								}

								long duration = 0;
								current = Instant.now();
								if (previous != null) {
									duration = ChronoUnit.MILLIS.between(previous, current);
									long durationofLap = statPoint.iLastTime;
									if (durationofLap != 0) {
										DecimalFormat df = new DecimalFormat("#.##");
										LOGGER.info("Efficiency: " + df.format((float) duration / durationofLap * 100));
										previous = Instant.now();
									}
								}

								// init new lap, we don't have it in current session
								lap = new StatLap();
								lap.addStatPoint(statPoint);
								currentSession.addStatLap(lap);
								currentSession.addStatPoint(statPoint);
							}
						} else {
							if (statPoint.currentSectorIndex > 0)
								lap.splitTimes.put(statPoint.currentSectorIndex - 1, statPoint.lastSectorTime);
							if (lap.fuelLeftOnStart == 0 && statPoint.fuel != 0 && lap.lapNo == statPoint.lapNo)
								lap.fuelLeftOnStart = statPoint.fuel;

						}
						currentSession.currentLap = lap;
					}
				} else {
					currentSession.currentLap.addStatPoint(statPoint);
					currentSession.addStatPoint(statPoint);
					if (Duration.between(lastChange, Instant.now()).getSeconds() > 5 && !saved) {
						LOGGER.info("Finished? Save sessions.");
						accDataSaveService.saveToXLS(this);
						saved = true;
					}
				}
			} else {
				LOGGER.info("let's get the party started!");
				if (currentSession.currentLap.statPoints.size() == 0)
					currentSession.currentLap.addStatPoint(statPoint);
				else
					if (currentSession.currentLap.statPoints.get(currentSession.currentLap.statPoints.size()-1).normalizedCarPosition > statPoint.normalizedCarPosition )
						currentSession.currentLap.addStatPoint(statPoint);
			}
		}
	}

	private boolean newSessionStarted(StatPoint prevStatPoint, StatPoint currentStatPoint) {
		boolean newSession = false;
		if (currentStatPoint.lapNo < prevStatPoint.lapNo) {
			LOGGER.info("LAP number is lower");
			newSession = true;
		}

		if (currentStatPoint.lapNo < prevStatPoint.lapNo) {
			LOGGER.info("LAP number is lower");
			newSession = true;
		}

		if (currentStatPoint.packetIDG < prevStatPoint.packetIDG) {
			LOGGER.info("Data from another session");
			newSession = true;
		}

		if (currentStatPoint.packetIDP < prevStatPoint.packetIDP) {
			LOGGER.info("Data from another session");
			newSession = true;
		}

		if (currentStatPoint.session != prevStatPoint.session) {
			LOGGER.info("New session");
			newSession = true;
		}
		
		if (prevStatPoint.distanceTraveled > currentStatPoint.distanceTraveled && prevStatPoint.lapNo > currentStatPoint.lapNo) {
			LOGGER.info("Distance traveled is lower: IT IS A NEW SESSION");
			newSession = true;
		}
		
		return newSession;
	}
	
	private StatSession newSession(StatPoint statPoint) {
		List<Integer> sessionsToRemove = new ArrayList<>();
		Iterator<Map.Entry<Integer, StatSession>> iterator = sessions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, StatSession> entry = iterator.next();
			if (entry.getValue().laps != null) {
				Iterator<Map.Entry<Integer, StatLap>> iteratorLap = entry.getValue().laps.entrySet().iterator();
				int i = 0;
				while (iteratorLap.hasNext()) {
					Map.Entry<Integer, StatLap> lap = iteratorLap.next();
					if (lap.getValue().splitTimes.size() != entry.getValue().car.sectorCount) {
						entry.getValue().laps.remove(lap.getKey());
					}
				}
			}
			if (entry.getValue().laps.size() == 0) {
				sessionsToRemove.add(entry.getKey());
			}
		}
		
		sessionsToRemove.forEach( key -> {
			sessions.remove(key);
		});

		//saveToXLSX();
		saved = false;
		Gson gson = new Gson();
		//System.out.println(gson.toJson(sessions));
		sessionCounter++;
		currentSession = new StatSession();
		currentSession.internalSessionIndex = sessionCounter;
		currentSession.sessionIndex = statPoint.sessionIndex;
		currentSession.session_TYPE = statPoint.session;

		switch (statPoint.session) {
		case AC_SESSION_TYPE.AC_QUALIFY:
			LOGGER.info("Session type: QUALIFY");
			break;
		case AC_SESSION_TYPE.AC_PRACTICE:
			LOGGER.info("Session type: PRACTICE");
			break;
		case AC_SESSION_TYPE.AC_RACE:
			LOGGER.info("Session type: RACE");
			break;
		case AC_SESSION_TYPE.AC_HOTLAP:
			LOGGER.info("Session type: HOTLAP");
			break;
		}

		currentSession.car = statPoint.car;
		LOGGER.info(currentSession.car.carModel + " max tank: [" + currentSession.car.maxFuel + "]");
		LOGGER.info(currentSession.car.track);
		LOGGER.info(currentSession.car.playerName);

		sessions.put(sessionCounter, currentSession);
		return currentSession;
	}

	


	@Override
	public String getPageName() {
		return pageName;
	}

	@Override
	public void setPageName(String pageName) {
		this.pageName = pageName;

	}

	@Override
	public String toJSON() {

		String response = "";
		Page page = this;
		try {
			FilterProvider filters = new SimpleFilterProvider().addFilter("filter1",
					SimpleBeanPropertyFilter.serializeAllExcept(""));
			ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
			response = mapper.writeValueAsString(page);
		} catch (JsonProcessingException e) {
			Application.LOGGER.debug(e.toString());
		}
		return response;
	}


	
	@Override
	public String toJSON(List<String> fields) {
		String response = "";
		PageFileStatistics page = this;
		try {
			Set<String> fieldsFilter = new HashSet<String>(fields);
			FilterProvider filters = new SimpleFilterProvider().addFilter("filter1",
					SimpleBeanPropertyFilter.filterOutAllExcept(fieldsFilter));
			ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
			response = mapper.writeValueAsString(page);
		} catch (JsonProcessingException e) {
			Application.LOGGER.debug(e.toString());
		}
		return response;
	}

	@Override
	public boolean isACCConnected() {
		return true;
	}
}
