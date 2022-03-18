package ACC.sharedmemory;

import java.util.EnumMap;
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
	
	public enum CarModel {
		 amr_v12_vantage_gt3
		,audi_r8_lms
		,bentley_continental_gt3_2016
		,bentley_continental_gt3_2018
		,bmw_m6_gt3
		,jaguar_g3
		,ferrari_488_gt3
		,honda_nsx_gt3
		,lamborghini_gallardo_rex
		,lamborghini_huracan_gt3
		,lamborghini_huracan_st
		,lexus_rc_f_gt3
		,mclaren_650s_gt3
		,mercedes_amg_gt3
		,nissan_gt_r_gt3_2017
		,nissan_gt_r_gt3_2018
		,porsche_991_gt3_r
		,porsche_991ii_gt3_cup
		,amr_v8_vantage_gt3
		,audi_r8_lms_evo
		,honda_nsx_gt3_evo
		,lamborghini_huracan_gt3_evo
		,mclaren_720s_gt3
		,porsche_991ii_gt3_r
		,alpine_a110_gt4
		,amr_v8_vantage_gt4
		,audi_r8_gt4
		,bmw_m4_gt4
		,chevrolet_camaro_gt4r
		,ginetta_g55_gt4
		,ktm_xbow_gt4
		,maserati_mc_gt4
		,mclaren_570s_gt4
		,mercedes_amg_gt4
		,porsche_718_cayman_gt4_mr
		,ferrari_488_gt3_evo
		,mercedes_amg_gt3_evo
		,bmw_m4_gt3
		
	}
	
	EnumMap<CarModel, Integer> brakeBiasMap = new EnumMap<>(CarModel.class);
	
	
	@Override
	public OutputMessage getPageFileMessage(String pageTyp, List<String> fieldsFilter) {
		brakeBiasMap.put(CarModel.amr_v12_vantage_gt3, -7);
		brakeBiasMap.put(CarModel.audi_r8_lms, -14);
		brakeBiasMap.put(CarModel.bentley_continental_gt3_2016, -7);
		brakeBiasMap.put(CarModel.bentley_continental_gt3_2018, -7);
		brakeBiasMap.put(CarModel.bmw_m6_gt3, -15);
		brakeBiasMap.put(CarModel.jaguar_g3, -7);
		brakeBiasMap.put(CarModel.ferrari_488_gt3, -17);
		brakeBiasMap.put(CarModel.honda_nsx_gt3, -14);
		brakeBiasMap.put(CarModel.lamborghini_gallardo_rex, -14);
		brakeBiasMap.put(CarModel.lamborghini_huracan_gt3, -14);
		brakeBiasMap.put(CarModel.lamborghini_huracan_st, -14);
		brakeBiasMap.put(CarModel.lexus_rc_f_gt3, -14);
		brakeBiasMap.put(CarModel.mclaren_650s_gt3, -17);
		brakeBiasMap.put(CarModel.mercedes_amg_gt3, -14);
		brakeBiasMap.put(CarModel.nissan_gt_r_gt3_2017, -15);
		brakeBiasMap.put(CarModel.nissan_gt_r_gt3_2018, -15);
		brakeBiasMap.put(CarModel.porsche_991_gt3_r, -21);
		brakeBiasMap.put(CarModel.porsche_991ii_gt3_cup, -5);
		brakeBiasMap.put(CarModel.amr_v8_vantage_gt3, -7);
		brakeBiasMap.put(CarModel.audi_r8_lms_evo, -14);
		brakeBiasMap.put(CarModel.honda_nsx_gt3_evo, -14);
		brakeBiasMap.put(CarModel.lamborghini_huracan_gt3_evo, -14);
		brakeBiasMap.put(CarModel.mclaren_720s_gt3, -17);
		brakeBiasMap.put(CarModel.porsche_991ii_gt3_r, -21);
		brakeBiasMap.put(CarModel.alpine_a110_gt4, -15);
		brakeBiasMap.put(CarModel.amr_v8_vantage_gt4, -20);
		brakeBiasMap.put(CarModel.audi_r8_gt4, -15);
		brakeBiasMap.put(CarModel.bmw_m4_gt4, -22);
		brakeBiasMap.put(CarModel.chevrolet_camaro_gt4r, -18);
		brakeBiasMap.put(CarModel.ginetta_g55_gt4, -18);
		brakeBiasMap.put(CarModel.ktm_xbow_gt4, -20);
		brakeBiasMap.put(CarModel.maserati_mc_gt4, -15);
		brakeBiasMap.put(CarModel.mclaren_570s_gt4, -9);
		brakeBiasMap.put(CarModel.mercedes_amg_gt4, -20);
		brakeBiasMap.put(CarModel.porsche_718_cayman_gt4_mr, -20);
		brakeBiasMap.put(CarModel.ferrari_488_gt3_evo, -17);
		brakeBiasMap.put(CarModel.mercedes_amg_gt3_evo, -14);
		brakeBiasMap.put(CarModel.bmw_m4_gt3 , -14);
		
		Page page = getPageFile(pageTyp);
		return new OutputMessage(page, fieldsFilter);
	}

	@Override
	public Page getPageFile(String pageTyp) {
	
		Page page = null;
		switch(pageTyp) {
		case "physics" : 
			PageFilePhysics  p = sh.getPageFilePhysics();
			int x = recalculateBrakeBias(p);
			if (p.brakeBias > 0)
				p.brakeBias += Integer.valueOf(x).doubleValue()/100.00;
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

	private int recalculateBrakeBias(PageFilePhysics p) {
		int offset = 0;
		if (lastps != null && !lastps.carModel.isEmpty()) {
			if (lastps.carModel.equals("amr_v12_vantage_gt3"         )) return brakeBiasMap.get(CarModel.amr_v12_vantage_gt3);
			if (lastps.carModel.equals("audi_r8_lms"                 )) return brakeBiasMap.get(CarModel.audi_r8_lms);
			if (lastps.carModel.equals("bentley_continental_gt3_2016")) return brakeBiasMap.get(CarModel.bentley_continental_gt3_2016);
			if (lastps.carModel.equals("bentley_continental_gt3_2018")) return brakeBiasMap.get(CarModel.bentley_continental_gt3_2018);
			if (lastps.carModel.equals("bmw_m6_gt3"                  )) return brakeBiasMap.get(CarModel.bmw_m6_gt3);
			if (lastps.carModel.equals("jaguar_g3"                   )) return brakeBiasMap.get(CarModel.jaguar_g3);
			if (lastps.carModel.equals("ferrari_488_gt3"             )) return brakeBiasMap.get(CarModel.ferrari_488_gt3);
			if (lastps.carModel.equals("honda_nsx_gt3"               )) return brakeBiasMap.get(CarModel.honda_nsx_gt3);
			if (lastps.carModel.equals("lamborghini_gallardo_rex"    )) return brakeBiasMap.get(CarModel.lamborghini_gallardo_rex);
			if (lastps.carModel.equals("lamborghini_huracan_gt3"     )) return brakeBiasMap.get(CarModel.lamborghini_huracan_gt3);
			if (lastps.carModel.equals("lamborghini_huracan_st"      )) return brakeBiasMap.get(CarModel.lamborghini_huracan_st);
			if (lastps.carModel.equals("lexus_rc_f_gt3"              )) return brakeBiasMap.get(CarModel.lexus_rc_f_gt3);
			if (lastps.carModel.equals("mclaren_650s_gt3"            )) return brakeBiasMap.get(CarModel.mclaren_650s_gt3);
			if (lastps.carModel.equals("mercedes_amg_gt3"            )) return brakeBiasMap.get(CarModel.mercedes_amg_gt3);
			if (lastps.carModel.equals("nissan_gt_r_gt3_2017"        )) return brakeBiasMap.get(CarModel.nissan_gt_r_gt3_2017);
			if (lastps.carModel.equals("nissan_gt_r_gt3_2018"        )) return brakeBiasMap.get(CarModel.nissan_gt_r_gt3_2018);
			if (lastps.carModel.equals("porsche_991_gt3_r"           )) return brakeBiasMap.get(CarModel.porsche_991_gt3_r);
			if (lastps.carModel.equals("porsche_991ii_gt3_cup"       )) return brakeBiasMap.get(CarModel.porsche_991ii_gt3_cup);
			if (lastps.carModel.equals("amr_v8_vantage_gt3"          )) return brakeBiasMap.get(CarModel.amr_v8_vantage_gt3);
			if (lastps.carModel.equals("audi_r8_lms_evo"             )) return brakeBiasMap.get(CarModel.audi_r8_lms_evo);
			if (lastps.carModel.equals("honda_nsx_gt3_evo"           )) return brakeBiasMap.get(CarModel.honda_nsx_gt3_evo);
			if (lastps.carModel.equals("lamborghini_huracan_gt3_evo" )) return brakeBiasMap.get(CarModel.lamborghini_huracan_gt3_evo);
			if (lastps.carModel.equals("mclaren_720s_gt3"            )) return brakeBiasMap.get(CarModel.mclaren_720s_gt3);
			if (lastps.carModel.equals("porsche_991ii_gt3_r"         )) return brakeBiasMap.get(CarModel.porsche_991ii_gt3_r);
			if (lastps.carModel.equals("alpine_a110_gt4"             )) return brakeBiasMap.get(CarModel.alpine_a110_gt4);
			if (lastps.carModel.equals("amr_v8_vantage_gt4"          )) return brakeBiasMap.get(CarModel.amr_v8_vantage_gt4);
			if (lastps.carModel.equals("audi_r8_gt4"                 )) return brakeBiasMap.get(CarModel.audi_r8_gt4);
			if (lastps.carModel.equals("bmw_m4_gt4"                  )) return brakeBiasMap.get(CarModel.bmw_m4_gt4);
			if (lastps.carModel.equals("chevrolet_camaro_gt4r"       )) return brakeBiasMap.get(CarModel.chevrolet_camaro_gt4r);
			if (lastps.carModel.equals("ginetta_g55_gt4"             )) return brakeBiasMap.get(CarModel.ginetta_g55_gt4);
			if (lastps.carModel.equals("ktm_xbow_gt4"                )) return brakeBiasMap.get(CarModel.ktm_xbow_gt4);
			if (lastps.carModel.equals("maserati_mc_gt4"             )) return brakeBiasMap.get(CarModel.maserati_mc_gt4);
			if (lastps.carModel.equals("mclaren_570s_gt4"            )) return brakeBiasMap.get(CarModel.mclaren_570s_gt4);
			if (lastps.carModel.equals("mercedes_amg_gt4"            )) return brakeBiasMap.get(CarModel.mercedes_amg_gt4);
			if (lastps.carModel.equals("porsche_718_cayman_gt4_mr"   )) return brakeBiasMap.get(CarModel.porsche_718_cayman_gt4_mr);
			if (lastps.carModel.equals("ferrari_488_gt3_evo"         )) return brakeBiasMap.get(CarModel.ferrari_488_gt3_evo);
			if (lastps.carModel.equals("mercedes_amg_gt3_evo"        )) return brakeBiasMap.get(CarModel.mercedes_amg_gt3_evo);
			if (lastps.carModel.equals("bmw_m4_gt3 "                 )) return brakeBiasMap.get(CarModel.bmw_m4_gt3 );
		}
		return offset;
		
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
		statPoint.iBestTime = g.iBestTime;
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
		statPoint.position = g.position;
		statPoint.driverStintTimeLeft = g.driverStintTimeLeft;
		statPoint.driverStintTotalTimeLeft = g.driverStintTotalTimeLeft;
		
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
		statPoint.pitLimiterOn = p.pitLimiterOn;
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
