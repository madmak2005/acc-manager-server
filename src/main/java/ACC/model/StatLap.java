package ACC.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ACC.ApplicationContextAwareImpl;
import ACC.ApplicationPropertyService;
import ACC.saving.ACCDataSaveService;
import virtualKeyboard.VirtualKeyboardAPI;

public class StatLap implements Serializable{
	/**
	 * 
	 */
	@Autowired
	ApplicationPropertyService applicationPropertyService;
	
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(StatLap.class);
	
	public String teamCode = "";
	public String pin = "";
	public int lapNo = 0;
	public boolean fromPit = false;
	public boolean toPit = false;
	public int lapTime = 0;
	public float distanceTraveled = 0;

	public Map<Integer, Integer> splitTimes = new HashMap<>();
	protected List<StatPoint> statPoints = new ArrayList<>();
	public int internalLapIndex = 0;

	public float fuelAdded = 0;
	public float fuelUsed = 0;
	public float fuelBeforePit = 0;
	public float fuelAfterPit = 0;
	
	public float fuelLeftOnStart = 0;
	public float fuelLeftOnEnd = 0;
	public float fuelAVGPerMinute = 0;
	public float fuelXlap = 0;
	public Map<Integer, Integer> maps = new HashMap<>();
	public int rainTyres; // Are rain tyres equipped
	public boolean isValidLap = true;
	public boolean first, last = false;
	public float sessionTimeLeft = 0;
	public String session_TYPE = "UNKNOWN"; 
	public int sessionIndex = 0;
	public int internalSessionIndex = 0;

	public float avgpFL, avgpFR, avgpRL, avgpRR = 0;
	public float avgtFL, avgtFR, avgtRL, avgtRR = 0;
	
	public float avgBPFL, avgBPFR, avgBPRL, avgBPRR = 0;
	public float avgBDFL, avgBDFR, avgBDRL, avgBDRR = 0;

	public float avgAirTemp = 0;
	public float avgRoadTemp = 0;

	public float fuelNTFOnEnd = 0;
	public float fuelEstForNextMiliseconds = 0;
	public float fuelEFNLapsOnEnd = 0;

	public float fuelAVGPerLap = 0;

	public float clockAtStart = 0;

	public float avgRainIntensity = 0;
	public float avgTrackGripStatus = 0;
	public String trackStatus = "";
	public String driverName = "";
	
	public boolean saved = false;

	protected List<Float> pFL = new ArrayList<>(), pFR = new ArrayList<>(), pRL = new ArrayList<>(),
			pRR = new ArrayList<>();
	protected List<Float> tFL = new ArrayList<>(), tFR = new ArrayList<>(), tRL = new ArrayList<>(),
			tRR = new ArrayList<>();
	protected List<Float> bpFL = new ArrayList<>(), bpFR = new ArrayList<>(), bpRL = new ArrayList<>(),
			bpRR = new ArrayList<>();
	protected List<Float> bdFL = new ArrayList<>(), bdFR = new ArrayList<>(), bdRL = new ArrayList<>(),
			bdRR = new ArrayList<>();

	protected List<Integer> rainIntensity = new ArrayList<>();
	protected List<Integer> trackGripStatus = new ArrayList<>();
	protected List<Float> airTemp = new ArrayList<>(), roadTemp = new ArrayList<>();
	
	protected boolean backToPit = false;
	
    public int mfdTyreSet = 0;
    public float mfdFuelToAdd = 0;
    public float mfdTyrePressureLF = 0;
    public float mfdTyrePressureRF = 0;
    public float mfdTyrePressureLR = 0;
    public float mfdTyrePressureRR = 0;
	public int rainIntensityIn10min = 0;
	public int rainIntensityIn30min = 0;
	public int currentTyreSet = 0;
	public int strategyTyreSet = 0;
	public String carModel ="";
	public String track ="";
	public int position = 0;
	public int driverStintTotalTimeLeft = 0;
	public int driverStintTimeLeft = 0;
	public long docId = 0;
	
	StatPoint firstStatPoint = new StatPoint();
	
	public void clearStatData() {
		airTemp = null;
		bdFL = null;
		bdFR = null;
		bdRL = null;
		bdRR = null;
		bpFL = null;
		bpFR = null;
		bpRL = null;
		bpRR = null;
		pFL = null;
		pFR = null;
		pRL = null;
		pRR = null;
		rainIntensity = null;
		roadTemp = null;
		statPoints = null;
		tFL = null;
		tFR = null;
		tRL = null;
		tRR = null;
		trackGripStatus =null;
	}

	protected synchronized void addStatPoint(StatPoint currentStatPoint) {
		if (statPoints.size() > 1) {
			StatPoint prevStatPoint = statPoints.get(statPoints.size() - 2);
			if (prevStatPoint.pitLimiterOn == 0 && currentStatPoint.pitLimiterOn == 1) {
				VirtualKeyboardAPI api = new VirtualKeyboardAPI();
				Map<String,String> keyMap = new HashMap<String, String>();
				ApplicationContext context = ApplicationContextAwareImpl.getApplicationContext();
				LOGGER.info("AUTOSAVING REPLAY");
				if (context != null) {
					ApplicationPropertyService applicationPropertyService = (ApplicationPropertyService) context
							.getBean("applicationPropertyService");
					String key = applicationPropertyService.getAutoSaveReplayKey();
					String activity = applicationPropertyService.getAutoSaveReplayActivity();
					LOGGER.info("USING KEY: " + (key != null ? key : "null"));
					LOGGER.info("AutoSaveReplayActivity: " + (activity != null ? activity : "null"));
					if (key != null && !key.isEmpty() && activity != null && activity.equals("Y")) {
						keyMap.put("key", key);
						api.execute(keyMap);
					}
				}
			}
		}
		
		if (currentStatPoint.wheelsPressure[0] > 0 && currentStatPoint.tyreCoreTemperature[0] > 0
				&& currentStatPoint.padLife[0] > 0 && currentStatPoint.discLife[0] > 0) {
			statPoints.add(currentStatPoint);
			lapTime = currentStatPoint.iCurrentTime;
			if (isValidLap) //change only from true to false
				if (currentStatPoint.isValidLap == 0) // zero == false
					isValidLap = false; 
			lapNo = currentStatPoint.lapNo;
			distanceTraveled = currentStatPoint.distanceTraveled;
			fuelLeftOnEnd = currentStatPoint.fuel;
			sessionTimeLeft = currentStatPoint.sessionTimeLeft;
			session_TYPE = "UNKNOWN";
			
			switch (currentStatPoint.session) {
				case AC_SESSION_TYPE.AC_QUALIFY:
					session_TYPE = "QUALIFY";
					break;
				case AC_SESSION_TYPE.AC_PRACTICE:
					session_TYPE = "PRACTICE";
					break;
				case AC_SESSION_TYPE.AC_RACE:
					session_TYPE = "RACE";
					break;
				case AC_SESSION_TYPE.AC_HOTLAP:
					session_TYPE = "HOTLAP";
					break;
				default: 
					session_TYPE = "UNKNOWN";
					break;
			};

			sessionIndex =currentStatPoint.sessionIndex;
			
			fuelXlap = currentStatPoint.fuelXlap;
			trackStatus = currentStatPoint.trackStatus;
			driverName = currentStatPoint.car.playerNick + " [" +currentStatPoint.car.playerName + " " + currentStatPoint.car.playerSurname + "]";
		    mfdTyreSet = currentStatPoint.mfdTyreSet;             
		    mfdFuelToAdd = currentStatPoint.mfdFuelToAdd;        
		    mfdTyrePressureLF = currentStatPoint.mfdTyrePressureLF;   
		    mfdTyrePressureRF = currentStatPoint. mfdTyrePressureRF;   
		    mfdTyrePressureLR = currentStatPoint.mfdTyrePressureLR;   
		    mfdTyrePressureRR = currentStatPoint.mfdTyrePressureRR;   
			rainIntensityIn10min = currentStatPoint.rainIntensityIn10min;
			rainIntensityIn30min = currentStatPoint.rainIntensityIn30min;
			currentTyreSet = currentStatPoint.currentTyreSet;      
			strategyTyreSet = currentStatPoint.strategyTyreSet;     
			track = currentStatPoint.car.track;
			carModel = currentStatPoint.car.carModel;
			position = currentStatPoint.position;
			driverStintTotalTimeLeft = currentStatPoint.driverStintTotalTimeLeft;
			driverStintTimeLeft = currentStatPoint.driverStintTimeLeft;
			
			if (currentStatPoint.airTemp > 0)
				airTemp.add(currentStatPoint.airTemp);
			if (currentStatPoint.roadTemp > 0)
				roadTemp.add(currentStatPoint.roadTemp);
			if (currentStatPoint.airTemp > 0)
				rainIntensity.add(currentStatPoint.rainIntensity);
			if (currentStatPoint.airTemp > 0)
				trackGripStatus.add(currentStatPoint.trackGripStatus);

			if (currentStatPoint.wheelsPressure != null) {
				pFL.add(currentStatPoint.wheelsPressure[0]);
				pFR.add(currentStatPoint.wheelsPressure[1]);
				pRL.add(currentStatPoint.wheelsPressure[2]);
				pRR.add(currentStatPoint.wheelsPressure[3]);
			}
			if (currentStatPoint.tyreCoreTemperature != null) {
				tFL.add(currentStatPoint.tyreCoreTemperature[0]);
				tFR.add(currentStatPoint.tyreCoreTemperature[1]);
				tRL.add(currentStatPoint.tyreCoreTemperature[2]);
				tRR.add(currentStatPoint.tyreCoreTemperature[3]);
			}

			if (currentStatPoint.tyreCoreTemperature != null) {
				bpFL.add(currentStatPoint.padLife[0]);
				bpFR.add(currentStatPoint.padLife[1]);
				bpRL.add(currentStatPoint.padLife[2]);
				bpRR.add(currentStatPoint.padLife[3]);
			}

			if (currentStatPoint.tyreCoreTemperature != null) {
				bdFL.add(currentStatPoint.discLife[0]);
				bdFR.add(currentStatPoint.discLife[1]);
				bdRL.add(currentStatPoint.discLife[2]);
				bdRR.add(currentStatPoint.discLife[3]);
			}

			Integer currentMapSteps = maps.get(currentStatPoint.currentMap);
			if (currentMapSteps != null) {
				maps.put(currentStatPoint.currentMap, currentMapSteps + 1);
			} else {
				maps.put(currentStatPoint.currentMap, 1);
			}

			calculateLapStats();
		}
	}
	
	protected void calculateLapStats() {
		if (statPoints.size() > 1) {
			StatPoint prevStatPoint = statPoints.get(statPoints.size() - 2);
			StatPoint currentStatPoint = statPoints.get(statPoints.size()-1);
			if (prevStatPoint.fuel == 0 && currentStatPoint.fuel > 0)
				firstStatPoint = currentStatPoint;
			
			if (statPoints.get(0).fuel > 0)
				firstStatPoint = statPoints.get(0);

			if (prevStatPoint.usedFuel > currentStatPoint.usedFuel
					&& prevStatPoint.distanceTraveled > currentStatPoint.distanceTraveled
					&& prevStatPoint.lapNo == currentStatPoint.lapNo && currentStatPoint.isInPitLane == 1) {
				backToPit = true;
				LOGGER.info("BACK TO PIT??");
			}
			
			if (currentStatPoint.fuel > prevStatPoint.fuel && ( (currentStatPoint.clock > firstStatPoint.clock) || (currentStatPoint.clock == firstStatPoint.clock) )) {
				fuelBeforePit = prevStatPoint.fuel;
				fuelAfterPit = currentStatPoint.fuel;
				fuelAdded = (float) Math.ceil(fuelAfterPit - fuelBeforePit);
			}
			
			fuelUsed = firstStatPoint.fuel - (currentStatPoint.fuel - fuelAdded);
			clockAtStart = firstStatPoint.clock;
			
			float minutes = (float) lapTime / (1000 * 60);
			if (lapTime > 0 && fuelAdded == 0)
				fuelAVGPerMinute = minutes == 0 ? 0 : fuelUsed / minutes;
			else 
				fuelAVGPerMinute = 0;
			
			// we enter the pit
			if (prevStatPoint.isInPitLane == 0 && currentStatPoint.isInPitLane == 1) {
				toPit = true;
			}

			// we left the pit
			if (prevStatPoint.isInPitLane == 1 && currentStatPoint.isInPitLane == 0) {
				fromPit = true;
			}
			
			
			
			if (currentStatPoint.flag == AC_FLAG_TYPE.ACC_CHECKERED_FLAG) {
				last = true;
			}
			
			OptionalDouble average;
			
			if (pFL != null && pFR != null && pRL != null && pRR != null && tFL != null && tFR != null && tRL != null && tRR != null) {
			average = pFL.stream().mapToDouble(a -> a).average();
			avgpFL = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = pFR.stream().mapToDouble(a -> a).average();
			avgpFR = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = pRL.stream().mapToDouble(a -> a).average();
			avgpRL = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = pRR.stream().mapToDouble(a -> a).average();
			avgpRR = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = tFL.stream().mapToDouble(a -> a).average();
			avgtFL = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = tFR.stream().mapToDouble(a -> a).average();
			avgtFR = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = tRL.stream().mapToDouble(a -> a).average();
			avgtRL = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = tRR.stream().mapToDouble(a -> a).average();
			avgtRR = (float) (average.isPresent() ? average.getAsDouble() : 0);
			
			average = bdFL.stream().mapToDouble(a -> a).average();
			avgBDFL = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = bdFR.stream().mapToDouble(a -> a).average();
			avgBDFR = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = bdRL.stream().mapToDouble(a -> a).average();
			avgBDRL = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = bdRR.stream().mapToDouble(a -> a).average();
			avgBDRR = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = bpFL.stream().mapToDouble(a -> a).average();
			avgBPFL = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = bpFR.stream().mapToDouble(a -> a).average();
			avgBPFR = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = bpRL.stream().mapToDouble(a -> a).average();
			avgBPRL = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = bpRR.stream().mapToDouble(a -> a).average();
			avgBPRR = (float) (average.isPresent() ? average.getAsDouble() : 0);
			
			average = rainIntensity.stream().mapToDouble(a -> a).average();
			avgRainIntensity = (float) (average.isPresent() ? average.getAsDouble() : 0);

			average = trackGripStatus.stream().mapToDouble(a -> a).average();
			avgTrackGripStatus = (float) (average.isPresent() ? average.getAsDouble() : 0);
			}
			if (airTemp != null) {
				average = airTemp.stream().mapToDouble(a -> a).average();
				avgAirTemp = (float) (average.isPresent() ? average.getAsDouble() : 0);
			}

			average = roadTemp.stream().mapToDouble(a -> a).average();
			avgRoadTemp = (float) (average.isPresent() ? average.getAsDouble() : 0);
		}
	}
}
