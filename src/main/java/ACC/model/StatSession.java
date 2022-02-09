package ACC.model;

import java.io.Serializable;
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

public class StatSession implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(StatSession.class);
	
	private int session_TYPE = AC_SESSION_TYPE.AC_UNKNOWN;
	
	public int getSession_TYPE() {
		return session_TYPE;
	}

	protected void setSession_TYPE(int session_TYPE) {
		this.session_TYPE = session_TYPE;
		switch (session_TYPE) {
			case AC_SESSION_TYPE.AC_QUALIFY:
				session_TYPENAME ="QUALIFY";
				break;
			case AC_SESSION_TYPE.AC_PRACTICE:
				session_TYPENAME ="PRACTICE";
				break;
			case AC_SESSION_TYPE.AC_RACE:
				session_TYPENAME ="RACE";
				break;
			case AC_SESSION_TYPE.AC_HOTLAP:
				session_TYPENAME ="HOTLAP";
				break;
			default: 
				session_TYPENAME ="UNKNOWN";
				break;
			}
	}
	
	public String teamCode = "";
	public String pin = "";

	public String session_TYPENAME = "UNKNOWN";
	public Map<Integer,StatLap> laps = new HashMap<>();

	public CircularFifoQueue<StatLap> last3Laps = new CircularFifoQueue<>(4);
	public CircularFifoQueue<StatLap> last5Laps = new CircularFifoQueue<>(6);
	
	public StatLap bestLap = new StatLap();
	public StatLap lastLap = new StatLap();
	public StatLap currentLap = new StatLap();
	
	public StatCar car = new StatCar();
	
	public int sessionIndex = 0;
	public int internalSessionIndex = 0;
	public int internalLapIndex = 0;
	
	public boolean wasGreenFlag = false; 
	
	public float sessionTimeLeft = 0;
	
	public float distanceTraveled = 0;
	public float fuelAVG3Laps = 0;
	public float fuelAVG5Laps = 0;
	public float fuelXLap = 0;
	
	public int avgLapTime3 = 0;
	public int avgLapTime5 = 0;
	
	public int packetDelta = 0;

	public int iBestTime;
	
	//public int size = 0;
	/*
	public boolean importStatLap(StatLap lap) {
		boolean lapExists = false;
		if (laps != null) {
			for (Map.Entry<Integer, StatLap> ml : laps.entrySet()) {
				StatLap l = ml.getValue();
				if (l.lapNo == lap.lapNo) lapExists = true;
			}
		
			if (!lapExists) {
				internalLapIndex++;
				lap.internalLapIndex = internalLapIndex; 
				laps.put(lap.lapNo, lap);
				
				if (internalLapIndex >= 2) {
						if (lap.isValidLap) {
							last3Laps.add(lap);
							last5Laps.add(lap);
							bestLap = lap;
							laps.forEach((i, l) -> {
									bestLap = bestLap.lapTime > l.lapTime ? l : bestLap;
							});
						}
				}
			}
		}
		return lapExists;
	}
	*/
	
	protected void addStatLap(StatLap lap) {
		internalLapIndex++;
		lap.internalLapIndex = internalLapIndex;
		laps.put(lap.lapNo, lap);
		LOGGER.info("ADD_STAT_LAP:" + lap.lapNo + ": " + PageFileStatistics.mstoStr(Math.round(lap.lapTime)));
		if (internalLapIndex >= 2) {
			lastLap = (StatLap) laps.values().toArray()[laps.size() - 2];
			if (lastLap != null) {
				if (lap.isValidLap) {
					LOGGER.info("ADDING LAP:" + lap.lapNo + ": " + PageFileStatistics.mstoStr(Math.round(lap.lapTime)));
					last3Laps.add(lap);
					last5Laps.add(lap);
					bestLap = lap;
					laps.forEach((i, l) -> {
						bestLap = bestLap.lapTime > l.lapTime ? l : bestLap;
					});
				}
				if (lastLap.statPoints != null) {
					fuelXLap = lastLap.statPoints.get(lastLap.statPoints.size() - 1).fuelXlap;
					if (lastLap.statPoints.size() > 1) {
						StatPoint lastLapLastStatPoint = lastLap.statPoints.get(lastLap.statPoints.size() - 1);
						if (lastLapLastStatPoint.lapNo < lap.lapNo) {
							lastLap.lapTime = lastLapLastStatPoint.iLastTime;
							lastLap.splitTimes.put(lastLapLastStatPoint.currentSectorIndex,
									lastLapLastStatPoint.iLastTime);
						}

						LOGGER.info("Start pos " + lastLap.firstStatPoint.normalizedCarPosition);
						// LOGGER.info("End pos " +
						// lastLap.statPoints.get(currentLap.statPoints.size()-1).normalizedCarPosition);
						lastLap.calculateLapStats();
					}
				}
			}
		}

		LOGGER.info(String.valueOf(currentLap.lapNo));
		LOGGER.info(String.valueOf("Lap time:" + PageFileStatistics.mstoStr(Math.round(currentLap.lapTime))));
		LOGGER.info(String.valueOf("Fuel [l]:" + currentLap.fuelLeftOnEnd));
		LOGGER.info(
				"Enough fuel for next:" + PageFileStatistics.mstoStr(Math.round(currentLap.fuelEstForNextMiliseconds)));
	}
	
	protected void addStatPoint(StatPoint currentStatPoint) {
		sessionIndex = currentStatPoint.sessionIndex;
		sessionTimeLeft = currentStatPoint.sessionTimeLeft;
		distanceTraveled = currentStatPoint.distanceTraveled;
	}
	
	public void calculateSessionStats() {
		
		float lavg = 0;
		int avgMS = 0;
		int i = 0;
		Iterator<StatLap> i3laps = last3Laps.iterator();
		while (i3laps.hasNext()) {
			i++;
			StatLap l = i3laps.next();
			lavg += l.fuelUsed;
			avgMS += l.lapTime;
			LOGGER.info("CALC AVG 3LAP:" + l.lapNo + ": " + PageFileStatistics.mstoStr(Math.round(l.lapTime)));
		}
		
		if (last3Laps.size() == 3) {
			fuelAVG3Laps = lavg / 3;
			avgLapTime3 = Math.round(avgMS / 3);
			//currentLap.fuelAVGPerLap = fuelAVG5Laps/3;
		}
		
		lavg = 0;
		avgMS = 0;
		i = 0;
		Iterator<StatLap> i5laps = last5Laps.iterator();
		while (i5laps.hasNext()) {
			i++;
			StatLap l = i5laps.next();
			lavg += l.fuelUsed;
			avgMS += l.lapTime;
			LOGGER.info("CALC AVG 5LAP:" + l.lapNo + ": " + PageFileStatistics.mstoStr(Math.round(l.lapTime)));
			
		}
		
		if (i>0)
			currentLap.fuelAVGPerLap = lavg/i; //next override if we have our calculations
		else
			currentLap.fuelAVGPerLap = currentLap.fuelUsed > 0 ? currentLap.fuelUsed : currentLap.fuelXlap;
		
		if (last5Laps.size() == 5) {
			fuelAVG5Laps = lavg / 5;
			avgLapTime5 = Math.round(avgMS / 5);
		}
		
		
		if (avgLapTime5 != 0) {
			float minutes = (float) avgLapTime5 / (1000 * 60);
			float perminutes = minutes == 0 ? 0 : (currentLap.fuelUsed) / minutes;
			if (currentLap.lapTime > 0)
				currentLap.fuelAVGPerMinute = perminutes;
			
			currentLap.fuelNTFOnEnd = currentLap.lapTime * currentLap.fuelUsed == 0 ? 0 : ((avgLapTime5 + sessionTimeLeft) / avgLapTime5) * fuelAVG5Laps;
			
			if ((minutes * perminutes) > 0)
				currentLap.fuelEFNLapsOnEnd = (float) (currentLap.fuelLeftOnEnd) / (minutes * perminutes);
			else 
				currentLap.fuelEFNLapsOnEnd = 0;
			
			currentLap.fuelEstForNextMiliseconds =  (currentLap.fuelEFNLapsOnEnd * avgLapTime5);
			//currentLap.fuelAVGPerLap = currentLap.fuelXlap;
			
		} else if (avgLapTime3 != 0){
			float minutes = (float) avgLapTime3 / (1000 * 60);
			float perminutes = minutes == 0 ? 0 : (currentLap.fuelUsed) / minutes;
			if (currentLap.lapTime > 0)
				currentLap.fuelAVGPerMinute = perminutes;
			
				currentLap.fuelNTFOnEnd = currentLap.lapTime * currentLap.fuelUsed == 0 ? 0 : ((avgLapTime3 + sessionTimeLeft) / avgLapTime3) * fuelAVG3Laps;
			
			if ((minutes * perminutes) > 0)
				currentLap.fuelEFNLapsOnEnd = (float) (currentLap.fuelLeftOnEnd) / (minutes * perminutes);
			else 
				currentLap.fuelEFNLapsOnEnd = 0;
			
			currentLap.fuelEstForNextMiliseconds =  (currentLap.fuelEFNLapsOnEnd * avgLapTime3);
		} else if (currentLap.lapTime > 0){
			float minutes = (float) currentLap.lapTime / (1000 * 60);
			float perminutes = minutes == 0 ? 0 : (currentLap.fuelUsed) / minutes;
			currentLap.fuelAVGPerMinute = perminutes;
			currentLap.fuelNTFOnEnd = currentLap.lapTime * currentLap.fuelUsed == 0 ? 0 : ((currentLap.lapTime + sessionTimeLeft) / currentLap.lapTime) * currentLap.fuelUsed;
			
			if ((minutes * perminutes) > 0)
				currentLap.fuelEFNLapsOnEnd = (float) (currentLap.fuelLeftOnEnd) / (minutes * perminutes);
			else 
				currentLap.fuelEFNLapsOnEnd = 0;
			
			currentLap.fuelEstForNextMiliseconds =  (currentLap.fuelEFNLapsOnEnd * currentLap.lapTime);
		} else {
			currentLap.fuelNTFOnEnd = 0;
			currentLap.fuelEFNLapsOnEnd = 0;
		}
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

	public void clearStatData() {
		this.bestLap = null;
		this.currentLap = null;
		this.laps = null;
		this.last3Laps = null;
		this.last5Laps = null;
		this.lastLap = null;
	}
}
