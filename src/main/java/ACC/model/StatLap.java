package ACC.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatLap {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatLap.class);
	
	public int lapNo = 0;
	public boolean fromPit = false;
	public boolean toPit = false;
	public int lapTime = 0;
	public float distanceTraveled = 0;

	public Map<Integer, Integer> splitTimes = new HashMap<>();
	protected List<StatPoint> statPoints = new ArrayList<>();

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

	public float avgpFL, avgpFR, avgpRL, avgpRR = 0;
	public float avgtFL, avgtFR, avgtRL, avgtRR = 0;

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

	protected List<Float> pFL = new ArrayList<>(), pFR = new ArrayList<>(), pRL = new ArrayList<>(),
			pRR = new ArrayList<>();
	protected List<Float> tFL = new ArrayList<>(), tFR = new ArrayList<>(), tRL = new ArrayList<>(),
			tRR = new ArrayList<>();

	protected List<Integer> rainIntensity = new ArrayList<>();
	protected List<Integer> trackGripStatus = new ArrayList<>();
	protected List<Float> airTemp = new ArrayList<>(), roadTemp = new ArrayList<>();
	
	protected boolean backToPit = false;
	
	StatPoint firstStatPoint = new StatPoint();

	protected void addStatPoint(StatPoint currentStatPoint) {
		statPoints.add(currentStatPoint);
		lapTime = currentStatPoint.iCurrentTime;
		isValidLap = !(currentStatPoint.isValidLap == 0); // zero == false
		lapNo = currentStatPoint.lapNo;
		distanceTraveled = currentStatPoint.distanceTraveled;
		fuelLeftOnEnd = currentStatPoint.fuel;
		sessionTimeLeft = currentStatPoint.sessionTimeLeft;
		fuelXlap = currentStatPoint.fuelXlap;
		trackStatus = currentStatPoint.trackStatus;
		
		airTemp.add(currentStatPoint.airTemp);
		roadTemp.add(currentStatPoint.roadTemp);
		rainIntensity.add(currentStatPoint.rainIntensity);
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
		
		Integer currentMapSteps = maps.get(currentStatPoint.currentMap);
		if (currentMapSteps != null) {
			maps.put(currentStatPoint.currentMap, currentMapSteps+1);
		}else {
			maps.put(currentStatPoint.currentMap, 1);
		}
		
		calculateLapStats();
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
			
			if (currentStatPoint.fuel > prevStatPoint.fuel && currentStatPoint.clock > firstStatPoint.clock) {
				fuelBeforePit = prevStatPoint.fuel;
				fuelAfterPit = currentStatPoint.fuel;
				fuelAdded = fuelAfterPit - fuelBeforePit;
			}
			
			fuelUsed = firstStatPoint.fuel - (currentStatPoint.fuel - fuelAdded);
			clockAtStart = firstStatPoint.clock;
			
			float minutes = (float) lapTime / (1000 * 60);
			if (lapTime > 0)
				fuelAVGPerMinute = minutes == 0 ? 0 : fuelUsed / minutes;
			
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
