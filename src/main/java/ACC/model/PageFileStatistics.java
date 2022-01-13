package ACC.model;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ACC.ApplicationContextAwareImpl;
import ACC.ApplicationPropertyService;
import ACC.saving.ACCDataSaveService;
import ACC.websocket.WebSocketControllerPage;
import app.Application;

@JsonFilter("filter1")
public class PageFileStatistics implements Page {
	boolean googleSaving = false;
	
	public PageFileStatistics() {
		super();
		setPageName("statistics");
		previous = Instant.now();
	}

	@JsonIgnore
	private static final Logger LOGGER = LoggerFactory.getLogger(PageFileStatistics.class);
	@JsonIgnore
	public Map<Integer, StatSession> sessions = new HashMap<>();
	@JsonIgnore
	public List<StatSession> mobileSessions = new ArrayList<>();

	public StatSession currentSession;
	private StatSession currentMobileSession;
	
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
	protected Instant lastMFDtry = Instant.now();

	@JsonIgnore
	private boolean saved = false;

	@JsonIgnore
	public boolean googleSaved = false;

	public static String mstoStr(long durationInMillis) {
		long millis = durationInMillis % 1000;
		long second = (durationInMillis / 1000) % 60;
		long minute = (durationInMillis / (1000 * 60)) % 60;
		long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

		return String.format("%02d:%02d:%02d:%03d", hour, minute, second, millis);
	}
	
	private void statSessionUpdateMobileSession(StatSession ms, StatSession session) {
		ms.setSession_TYPE(session.getSession_TYPE()); 
		ms.car =                   session.car;                  
		ms.sessionIndex =          session.sessionIndex;         
		ms.internalSessionIndex =  session.internalSessionIndex; 
		ms.internalLapIndex =      session.internalLapIndex;
		ms.sessionTimeLeft =       session.sessionTimeLeft;
		ms.distanceTraveled =      session.distanceTraveled;
		ms.fuelAVG3Laps =          session.fuelAVG3Laps;
		ms.fuelAVG5Laps =          session.fuelAVG5Laps;
		ms.avgLapTime3 =           session.avgLapTime3;
		ms.avgLapTime5 =           session.avgLapTime5;
		ms.iBestTime =             session.iBestTime;
		ms.fuelXLap =              session.fuelXLap;
	}

	public void addStatPoint(StatPoint statPoint) {
		StatPoint prevStatPoint = null;
		currentSession = sessions.get(sessionCounter);

		if (currentSession == null) {
			LOGGER.info("New session");
			currentSession = newSession(statPoint);
			currentMobileSession = new StatSession();
			currentMobileSession.clearStatData();
			statSessionUpdateMobileSession(currentMobileSession, currentSession);
		}

		if (statPoint.iCurrentTime != 0) {
			if (currentSession != null && currentSession.currentLap != null)
				if (currentSession.currentLap.statPoints.size() > 0) {
					prevStatPoint = currentSession.currentLap.statPoints
							.get(currentSession.currentLap.statPoints.size() - 1);
					currentSession.currentLap.addStatPoint(statPoint);
					if (statPoint.packetIDG != prevStatPoint.packetIDG
							&& statPoint.packetIDP != prevStatPoint.packetIDP) {
						lastChange = Instant.now();
					}
					if (prevStatPoint.iCurrentTime != statPoint.iCurrentTime) {
						if (prevStatPoint.iCurrentTime > statPoint.iCurrentTime
								&& prevStatPoint.lapNo == statPoint.lapNo) {
							// LOGGER.info("Kunos miracle. Skip this data");
						} else {
							currentSession.packetDelta = Math.abs(statPoint.packetIDG - prevStatPoint.packetIDP);
							if (Duration.between(lastChange, Instant.now()).getSeconds() > 5 && !saved) {
								LOGGER.info("Finished? Save sessions.");
								ApplicationContext context = ApplicationContextAwareImpl.getApplicationContext();
								if (context != null) {
									ACCDataSaveService accDataSaveService = (ACCDataSaveService) context
											.getBean("accDataSaveService");
									accDataSaveService.saveToXLS(this);
									saved = true;
								}
							}

							if (newSessionStarted(prevStatPoint, statPoint)) {
								statSessionUpdateMobileSession(currentMobileSession, currentSession);
								currentSession = newSession(statPoint);
							}

							saveMFD(prevStatPoint, statPoint);

							if (statPoint.flag == AC_FLAG_TYPE.ACC_GREEN_FLAG
									&& statPoint.session == AC_SESSION_TYPE.AC_RACE && currentSession != null
									&& !currentSession.wasGreenFlag) {
								LOGGER.info("GREEN FLAG, GREEN FLAG, GREEN, GREEN, GREEN!!!!!!!");
								raceStartAt = statPoint.iCurrentTime;
								LOGGER.info("Race start at [ms]: " + String.valueOf(raceStartAt));
								currentSession = newSession(statPoint);
								currentSession.wasGreenFlag = true;
								currentSession.currentLap.first = true;
							}

							StatLap lap = currentSession.laps.get(statPoint.lapNo);
							ApplicationContext context = ApplicationContextAwareImpl.getApplicationContext();
							if (lap == null) {
								if (statPoint.normalizedCarPosition < 1) {
									LOGGER.info("New lap started: [" + statPoint.lapNo + "]");
									LOGGER.info("Car position: [" + statPoint.normalizedCarPosition + "]");
									StatLap prevLap = currentSession.laps.get(statPoint.lapNo - 1);
									if (prevLap != null) {
										prevLap.lapTime = statPoint.iLastTime;
										prevLap.splitTimes.put(prevLap.splitTimes.size(), statPoint.iLastTime);
										prevLap.splitTimes.remove(0);
										currentSession.calculateSessionStats();
										statSessionUpdateMobileSession(currentMobileSession, currentSession);
										ApplicationPropertyService applicationPropertyService = (ApplicationPropertyService) context
												.getBean("applicationPropertyService");
										if (applicationPropertyService != null) {
											applicationPropertyService.addMobileSessionLap(prevLap);
										}
										LOGGER.info("send websocket");
										WebSocketControllerPage webSocketControllerPage = (WebSocketControllerPage) context
												.getBean("webSocketControllerPage");
										if (webSocketControllerPage != null) {
											LOGGER.info("send statistics");
											webSocketControllerPage.sendTextMobileStats(prevLap);

										}
									} else {
										/* first lap of session was finished */
										ApplicationPropertyService applicationPropertyService = (ApplicationPropertyService) context
												.getBean("applicationPropertyService");
										if (applicationPropertyService != null) {
											if (applicationPropertyService.getMobileSessionList().size() == 0) {
												// currentMobileSession = new StatSession();
												statSessionUpdateMobileSession(currentMobileSession, currentSession);
												currentMobileSession.clearStatData();
												applicationPropertyService.addMobileSession(currentMobileSession);
											}
										}
									}

									long duration = 0;
									current = Instant.now();
									if (previous != null) {
										duration = ChronoUnit.MILLIS.between(previous, current);
										long durationofLap = statPoint.iLastTime;
										if (durationofLap != 0) {
											DecimalFormat df = new DecimalFormat("#.##");
											LOGGER.info(
													"Efficiency: " + df.format((float) duration / durationofLap * 100));
											previous = Instant.now();
										}

										if (context != null) {
											ACCDataSaveService accDataSaveService = (ACCDataSaveService) context
													.getBean("accDataSaveService");

											if (accDataSaveService != null && !googleSaving) {
												int iCount = 1, iDelay = 1_000;
												ScheduledExecutorService scheduler = Executors
														.newSingleThreadScheduledExecutor();
												LOGGER.info("Start ...");
												List<Future<Integer>> futures = new ArrayList<>(iCount);

												for (int i = 0; i < iCount; i++) {
													int j = i;
													futures.add(
															scheduler.schedule(() -> j, iDelay, TimeUnit.MILLISECONDS));
												}
												for (Future<Integer> e : futures) {
													googleSaving = true;
													if (accDataSaveService.saveToGoogle(this)) {
														sessions.forEach((lp, session) -> {
															session.laps.forEach((id, l) -> {
																l.saved = true;
															});
														});
														googleSaved = true;
													}
													googleSaving = false;
													try {
														e.get();
													} catch (InterruptedException | ExecutionException e1) {
														// TODO Auto-generated catch block
														e1.printStackTrace();
													}
												}
												LOGGER.info("Complete");

											}
										}
									}

									// init new lap, we don't have it in current session
									lap = new StatLap();

									lap.addStatPoint(statPoint);
									lap.clockAtStart = statPoint.clock;
									currentSession.addStatLap(lap);
									currentSession.addStatPoint(statPoint);

								}
							} else {

								lap.splitTimes.put(statPoint.currentSectorIndex, statPoint.lastSectorTime);
								if (lap.fuelLeftOnStart == 0 && statPoint.fuel != 0 && lap.lapNo == statPoint.lapNo)
									lap.fuelLeftOnStart = statPoint.fuel;
								lap.internalSessionIndex = currentSession.internalSessionIndex;

								ApplicationPropertyService applicationPropertyService = (ApplicationPropertyService) context
										.getBean("applicationPropertyService");
								if (applicationPropertyService != null) {
									if (applicationPropertyService.getMobileSessionList().size() == 0) {
										currentMobileSession = new StatSession();
										statSessionUpdateMobileSession(currentMobileSession, currentSession);
										currentMobileSession.clearStatData();
										applicationPropertyService.addMobileSession(currentMobileSession);
									}
								}
							}
							currentSession.currentLap = lap;
							currentSession.iBestTime = statPoint.iBestTime;
							currentSession.fuelXLap = statPoint.fuelXlap;

							ApplicationPropertyService applicationPropertyService = (ApplicationPropertyService) context
									.getBean("applicationPropertyService");
							if (applicationPropertyService != null) {
								StatSession x = applicationPropertyService.getMobileSessionList()
										.get(applicationPropertyService.getMobileSessionList().size() - 1);
								statSessionUpdateMobileSession(x, currentSession);
							}
						}
					} else {
						if (currentSession != null) {
							currentSession.currentLap.addStatPoint(statPoint);
							currentSession.addStatPoint(statPoint);
						}
						if (Duration.between(lastChange, Instant.now()).getSeconds() > 5 && !saved) {
							LOGGER.info("Finished? Save sessions.");
							ApplicationContext context = ApplicationContextAwareImpl.getApplicationContext();
							if (context != null) {
								ACCDataSaveService accDataSaveService = (ACCDataSaveService) context
										.getBean("accDataSaveService");
								if (accDataSaveService != null)
									accDataSaveService.saveToXLS(this);
								saved = true;
							}
						}
					}
				} else {
					LOGGER.info("let's get the party started!");
					if (currentSession.currentLap.statPoints.size() == 0)
						currentSession.currentLap.addStatPoint(statPoint);
					else if (currentSession.currentLap.statPoints.get(currentSession.currentLap.statPoints.size()
							- 1).normalizedCarPosition > statPoint.normalizedCarPosition)
						currentSession.currentLap.addStatPoint(statPoint);
				}
		}
	}

	private void saveMFD(StatPoint prevStatPoint, StatPoint statPoint) {
		ApplicationContext context = ApplicationContextAwareImpl.getApplicationContext();
		if (context != null) {
			ACCDataSaveService accDataSaveService = (ACCDataSaveService) context.getBean("accDataSaveService");
			if (accDataSaveService != null) {
				if (!googleSaved) {
					if (Duration.between(lastMFDtry, Instant.now()).getSeconds() > 5) {
						lastMFDtry = Instant.now();
						if (accDataSaveService.saveHeaderToGoogle(this)) {
							googleSaved = true;
						}
					}
				} else {
					if (prevStatPoint.mfdFuelToAdd != statPoint.mfdFuelToAdd
							|| prevStatPoint.mfdTyrePressureLF != statPoint.mfdTyrePressureLF
							|| prevStatPoint.mfdTyrePressureLR != statPoint.mfdTyrePressureLR
							|| prevStatPoint.mfdTyrePressureRF != statPoint.mfdTyrePressureRF
							|| prevStatPoint.mfdTyrePressureRR != statPoint.mfdTyrePressureRR
							|| prevStatPoint.mfdTyreSet != prevStatPoint.mfdTyreSet) {
						accDataSaveService.saveHeaderToGoogle(this);
					}
				}
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

		if (prevStatPoint.distanceTraveled > currentStatPoint.distanceTraveled
				&& prevStatPoint.lapNo > currentStatPoint.lapNo) {
			LOGGER.info("Distance traveled is lower: IT IS A NEW SESSION");
			newSession = true;
		}

		return newSession;
	}
	
	public void clearStatData() {
		currentSession.currentLap.airTemp = null;
		currentSession.currentLap.bdFL = null;
		currentSession.currentLap.bdFR = null;
		currentSession.currentLap.bdRL = null;
		currentSession.currentLap.bdRR = null;
		currentSession.currentLap.bpFL = null;
		currentSession.currentLap.bpFR = null;
		currentSession.currentLap.bpRL = null;
		currentSession.currentLap.bpRR = null;
		currentSession.currentLap.pFL = null;
		currentSession.currentLap.pFR = null;
		currentSession.currentLap.pRL = null;
		currentSession.currentLap.pRR = null;
		currentSession.currentLap.rainIntensity = null;
		currentSession.currentLap.roadTemp = null;
		currentSession.currentLap.statPoints = null;
		currentSession.currentLap.tFL = null;
		currentSession.currentLap.tFR = null;
		currentSession.currentLap.tRL = null;
		currentSession.currentLap.tRR = null;
		currentSession.currentLap.trackGripStatus =null;
	}

	private StatSession newSession(StatPoint statPoint) {
		saved = false;
		// Gson gson = new Gson();
		// System.out.println(gson.toJson(sessions));
		sessionCounter++;
		currentSession = new StatSession();
		currentSession.internalSessionIndex = sessionCounter;
		currentSession.sessionIndex = statPoint.sessionIndex;
		currentSession.setSession_TYPE(statPoint.session);
		currentSession.sessionTimeLeft = statPoint.sessionTimeLeft;


		currentSession.car = statPoint.car;
		LOGGER.info(currentSession.car.carModel + " max tank: [" + currentSession.car.maxFuel + "]");
		LOGGER.info(currentSession.car.track);
		LOGGER.info(currentSession.car.playerName);

		sessions.put(sessionCounter, currentSession);
		
		ApplicationContext context = ApplicationContextAwareImpl.getApplicationContext();
		ApplicationPropertyService applicationPropertyService = (ApplicationPropertyService) context.getBean("applicationPropertyService");
		
		currentMobileSession = new StatSession();
		statSessionUpdateMobileSession(currentMobileSession,currentSession);
		currentMobileSession.clearStatData();
		if (applicationPropertyService != null) {
			applicationPropertyService.addMobileSession(currentMobileSession);
		}
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
			Set<String> fieldsFilter = new HashSet<String>();
			FilterProvider filters = new SimpleFilterProvider().addFilter("filter1",
					SimpleBeanPropertyFilter.serializeAllExcept(fieldsFilter));
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
