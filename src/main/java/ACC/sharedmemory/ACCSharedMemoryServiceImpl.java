package ACC.sharedmemory;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import ACC.model.PageFileGraphics;
import ACC.model.PageFilePhysics;
import ACC.model.PageFileStatic;
import ACC.model.PageFileStatistics;
import ACC.model.StatCar;
import ACC.model.StatPoint;
import ACC.model.OutputMessage;
import ACC.model.Page;

@ComponentScan("ACC")
@Service
public class ACCSharedMemoryServiceImpl implements ACCSharedMemoryService {

	
	final ACCSharedMemory sh = new ACCSharedMemory();
	PageFileStatistics statistics = new PageFileStatistics();
	
	PageFilePhysics  lastpp;
	PageFileGraphics lastpg;
	PageFileStatic   lastps;
	
	@Override
	public OutputMessage getPageFileMessage(String pageTyp, List<String> fieldsFilter) {
		Page page = getPageFile(pageTyp);
		return new OutputMessage(page, fieldsFilter);
	}

	@Override
	public Page getPageFile(String pageTyp) {
		LocalDateTime now = new LocalDateTime();
		
		Page page = null;
		switch(pageTyp) {
		case "physics" : 
			PageFilePhysics  p = sh.getPageFilePhysics();
			p.brakeBias = p.packetId == 0 ? 56.90f : p.brakeBias;
			p.packetId = p.packetId == 0 ? now.getMillisOfDay() : p.packetId;

			lastpp = p;
			page = p;
			break;
		
		case "graphics" : 
			PageFileGraphics g = sh.getPageFileGraphics();
			Double sec = (double) now.getSecondOfMinute()/60;
			Double mili = (double) now.getMillisOfSecond()/100000;
			float position = (float) (sec+mili);
			g.normalizedCarPosition = g.packetId == 0 ? position : g.normalizedCarPosition;
			g.lightsStage = g.packetId == 0 ? (Math.random() < 0.5 ? (Math.random() < 0.5 ? 0 : 1) : 2) : g.rainLights;
			g.isInPit     = g.packetId == 0 ? now.getMinuteOfHour() % 2 : g.isInPit;
			g.packetId    = g.packetId == 0 ? now.getMillisOfDay() : g.packetId;

			lastpg = g;
			page = g;
			break;
		
		case "static" : 
			PageFileStatic   s = sh.getPageFileStatic();
			lastps = s;
			page = s;
			break;
		
		case "statistics" : 
			PageFileStatistics   ss = getPageFileStatistics();
			page = ss;
			break;
		};
		return page;
		
	}

	private PageFileStatistics getPageFileStatistics() {
		StatPoint statPoint = getStatPoint();
		if (statPoint != null) {
			statPoint.car = getStatCar();
			statistics.addStatPoint(statPoint);
			statistics.sessions.forEach( (i,action) -> {
			});
				
		}
		return statistics;
	}

	@Override
	public StatPoint getStatPoint() {
		StatPoint statPoint = new StatPoint();
		
		if (lastpg != null && lastpp != null) {
		
		PageFilePhysics  p = lastpp;
		PageFileGraphics g = lastpg;
		
		
		statPoint.normalizedCarPosition = g.normalizedCarPosition;
		statPoint.currentSectorIndex = g.currentSectorIndex;
		statPoint.iCurrentTime = g.iCurrentTime;
		statPoint.iLastTime = g.iLastTime;
		statPoint.iSplit = g.iSplit;
		statPoint.lastSectorTime = g.lastSectorTime;
		statPoint.isInPit = g.isInPit;
		statPoint.isInPitLane = g.isInPitLane;
		statPoint.lapNo = g.completedLaps;
		statPoint.isValidLap = g.isValidLap;
		statPoint.usedFuel = g.usedFuel;
		statPoint.fuelXlap = g.fuelXLap;
		statPoint.sessionIndex = g.sessionIndex;
		statPoint.session = g.session;
		statPoint.distanceTraveled = g.distanceTraveled;
		statPoint.sessionTimeLeft = g.sessionTimeLeft;
		statPoint.flag = g.flag;
		statPoint.currentMap = g.EngineMap;
		statPoint.rainIntensity = g.rainIntensity;
		statPoint.trackGripStatus = g.trackGripStatus;
		statPoint.trackStatus = g.trackStatus;
		
		statPoint.airTemp = p.airTemp;
		statPoint.brakeTemp = p.brakeTemp;
		statPoint.carDamage = p.carDamage;
		statPoint.discLife = p.discLife;
		statPoint.fuel = p.fuel;
		statPoint.padLife = p.padLife;
		statPoint.roadTemp = p.roadTemp;
		statPoint.speedKmh = p.speedKmh;
		statPoint.tyreCoreTemperature = p.tyreCoreTemperature;
		statPoint.wheelsPressure = p.wheelsPressure;
		}
		return statPoint;
	}

	@Override
	public StatCar getStatCar() {
		PageFileStatic   s = lastps;
		StatCar car = new StatCar();
		car.carModel = s.carModel;
		car.maxFuel = s.maxFuel;
		car.playerName = s.playerName;
		car.track = s.track;
		car.sectorCount = s.sectorCount;
		return car;
	}
	
	
	

}
