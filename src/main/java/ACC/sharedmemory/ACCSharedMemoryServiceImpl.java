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
	
		Page page = null;
		switch(pageTyp) {
		case "physics" : 
			PageFilePhysics  p = sh.getPageFilePhysics();
			lastpp = p;
			page = p;
			break;
		
		case "graphics" : 
			PageFileGraphics g = sh.getPageFileGraphics();
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
		if (statPoint != null && statPoint.iCurrentTime > 0) {
			statPoint.car = getStatCar();
			statistics.addStatPoint(statPoint);
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
		statPoint.clock	=	g.clock;
		statPoint.packetIDG  = g.packetId;
		statPoint.mfdTyreSet =              g.mfdTyreSet;     
		statPoint.mfdFuelToAdd =            g.mfdFuelToAdd;   
	    statPoint.mfdTyrePressureLF =       g.mfdTyrePressureLF; 
	    statPoint.mfdTyrePressureRF =       g.mfdTyrePressureRF; 
	    statPoint.mfdTyrePressureLR =       g.mfdTyrePressureLR; 
	    statPoint.mfdTyrePressureRR =       g.mfdTyrePressureRR; 
		statPoint.rainIntensityIn10min =    g.rainIntensityIn10min;
		statPoint.rainIntensityIn30min =    g.rainIntensityIn30min;
		statPoint.currentTyreSet =          g.currentTyreSet;
		statPoint.strategyTyreSet =         g.strategyTyreSet;
		
		statPoint.airTemp = p.airTemp;
		statPoint.brakeTemp = p.brakeTemp;
		statPoint.carDamage = p.carDamage;
		statPoint.discLife = p.discLife;
		statPoint.padLife = p.padLife;
		statPoint.fuel = p.fuel;
		statPoint.roadTemp = p.roadTemp;
		statPoint.speedKmh = p.speedKmh;
		statPoint.tyreCoreTemperature = p.tyreCoreTemperature;
		statPoint.wheelsPressure = p.wheelsPressure;
		statPoint.packetIDP = p.packetId;
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
		car.playerSurname = s.playerSurname;
		car.playerNick = s.playerNick;
		car.track = s.track;
		car.sectorCount = s.sectorCount;
		return car;
	}
	
	
	

}
