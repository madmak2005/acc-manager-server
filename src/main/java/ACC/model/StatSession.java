package ACC.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.gson.Gson;

import app.Application;

public class StatSession {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatSession.class);
	
	public int session_TYPE = AC_SESSION_TYPE.AC_UNKNOWN; 
	public Map<Integer,StatLap> laps = new HashMap<>();

	public CircularFifoQueue<StatLap> last3Laps = new CircularFifoQueue<>(3);
	public CircularFifoQueue<StatLap> last5Laps = new CircularFifoQueue<>(5);
	
	public StatLap bestLap = new StatLap();
	public StatLap lastLap = new StatLap();
	public StatLap currentLap = new StatLap();
	
	public StatCar car = new StatCar();
	
	public int sessionIndex = 0;
	public int internalSessionIndex = 0;
	public int internalLapIndex = 0;
	
	public boolean wasGreenFlag = false; 
	
	public int bestTime = 999999999;
	
	public float sessionTimeLeft = 0;
	
	public float distanceTraveled = 0;
	public float fuelAVG3Laps = 0;
	public float fuelAVG5Laps = 0;
	
	public int avgLapTime3 = 0;
	public int avgLapTime5 = 0;
	
	public int packetDelta = 0;
	
	protected void addStatLap(StatLap lap) {
		internalLapIndex++;
		lap.internalLapIndex = internalLapIndex; 
		laps.put(lap.lapNo, lap);
		if (laps.size() > 1) {
			lastLap = laps.get(laps.size() - 2);
			if (lastLap != null && lastLap.statPoints != null && lastLap.statPoints.size() > 1) {
				StatPoint lastLapLastStatPoint = lastLap.statPoints.get(lastLap.statPoints.size() - 1);
				if (lastLapLastStatPoint.lapNo < lap.lapNo) {
					lastLap.lapTime = lastLapLastStatPoint.iLastTime;
					lastLap.splitTimes.put(lastLapLastStatPoint.currentSectorIndex, lastLapLastStatPoint.iLastTime);
				}
				if (lap.isValidLap) {
					last3Laps.add(lap);
					last5Laps.add(lap);
					bestTime = lap.lapTime;
					bestLap = lap;
					laps.forEach((i, l) -> {
						if (l.lapTime > 0 && l.distanceTraveled > 500) {
							bestTime = bestTime > l.lapTime ? l.lapTime : bestTime;
							bestLap = bestTime > l.lapTime ? l : bestLap;
						}
					});
				}
				LOGGER.info("Start pos " + lastLap.firstStatPoint.normalizedCarPosition);

				LOGGER.info("End pos " + lastLap.statPoints.get(currentLap.statPoints.size()-1).normalizedCarPosition);
				lastLap.calculateLapStats();
			}
		}
		
		LOGGER.info(String.valueOf(currentLap.lapNo));
		LOGGER.info(String.valueOf(currentLap.lapTime));
		LOGGER.info(String.valueOf("Fuel [l]:" + currentLap.fuelLeftOnEnd));
		LOGGER.info("Enough fuel for next:" + PageFileStatistics.mstoStr(Math.round(currentLap.fuelEstForNextMiliseconds)));
	}
	
	protected void addStatPoint(StatPoint currentStatPoint) {
		sessionIndex = currentStatPoint.sessionIndex;
		sessionTimeLeft = currentStatPoint.sessionTimeLeft;
		distanceTraveled = currentStatPoint.distanceTraveled;
	}
	
	protected void calculateSessionStats() {
		
		float lavg = 0;
		int avgMS = 0;
		int i = 0;
		Iterator<StatLap> i3laps = last3Laps.iterator();
		while (i3laps.hasNext()) {
			i++;
			StatLap l = i3laps.next();
			lavg += l.fuelUsed;
			avgMS += l.lapTime;
		}
		
		fuelAVG3Laps = i == 0? 0 : lavg / i;
		avgLapTime3 = i == 0 ? 0 : Math.round(avgMS / i);
		
		
		lavg = 0;
		avgMS = 0;
		i = 0;
		Iterator<StatLap> i5laps = last5Laps.iterator();
		while (i5laps.hasNext()) {
			i++;
			StatLap l = i5laps.next();
			lavg += l.fuelUsed;
			avgMS += l.lapTime;
		}
		fuelAVG5Laps = i == 0? 0 : lavg / i;
		avgLapTime5 = i == 0? 0 : Math.round(avgMS / i);
		currentLap.fuelAVGPerLap = currentLap.fuelXlap;
		
		float minutes = (float) avgLapTime5 / (1000 * 60);
		float perminutes = minutes == 0 ? 0 : (currentLap.fuelUsed) / minutes;
		if (currentLap.lapTime > 0)
			currentLap.fuelAVGPerMinute = perminutes;
		
		
		currentLap.fuelNTFOnEnd = currentLap.lapTime * currentLap.fuelUsed == 0 ? 0 : ((avgLapTime3 + sessionTimeLeft) / avgLapTime5) * fuelAVG5Laps;
		
		if ((minutes * perminutes) > 0)
			currentLap.fuelEFNLapsOnEnd = (float) (currentLap.fuelLeftOnEnd) / (minutes * perminutes);
		else 
			currentLap.fuelEFNLapsOnEnd = 0;
		
		currentLap.fuelEstForNextMiliseconds =  (currentLap.fuelEFNLapsOnEnd * avgLapTime5);
	
	}
	
	public String toJSON() {
		String response = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			response = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			Application.LOGGER.debug(e.toString());
		}
		return response;
	}
}
