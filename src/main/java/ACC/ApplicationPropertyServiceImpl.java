package ACC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ACC.model.AC_SESSION_TYPE;
import ACC.model.StatLap;
import ACC.model.StatSession;
import app.Application;
import ch.qos.logback.classic.Logger;
import lombok.Data;

@ComponentScan("ACC")
@Service
public class ApplicationPropertyServiceImpl implements ApplicationPropertyService{
	
	private String sheetID = "";
	List<StatLap> allLaps = new ArrayList<StatLap>();
	List<StatSession> sessions = new ArrayList<StatSession>();
	List<StatSession> enduSessions = new ArrayList<StatSession>(); 
	StatSession enduSession = new StatSession(); 
	String autoSaveReplay = ""; 
	String autoSaveReplayActivity = ""; 
	
	@Autowired
	private ServerProperties serverProperties;
	
	@Override
    public int getApplicationProperty(){
        return (Application.useDebug ? 50 : 100);
    }
    
	@Override
    public int getStatisticsInterval(){
        return (Application.useDebug ? 67: 333);
    }
    
	@Override
    public int getApplicationPort(){
        return serverProperties.getPort();
    }

	@Override
	public String getSheetID() {
		return sheetID;
	}

	@Override
	public void setSheetID(String sheetID) {
		this.sheetID = sheetID;
		
	}

	@Override
	public List<StatLap> getMobileSessionLapList(int internalSessionIndex) {
		List<StatLap> sessionLaps = new ArrayList<StatLap>();
		for (StatLap statLap : allLaps) {
			if (statLap.internalSessionIndex == internalSessionIndex) sessionLaps.add(statLap);
		}
		return sessionLaps;
	}

	@Override
	public void addMobileSessionLap(StatLap statLap) {
		Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .create();
		StatLap deepCopy = gson.fromJson(gson.toJson(statLap), StatLap.class);
		deepCopy.clearStatData();
		allLaps.add(deepCopy);
	}

	@Override
	public List<StatSession> getMobileSessionList() {
		return sessions;
	}

	@Override
	public void addMobileSession(StatSession session) {
		sessions.add(session);
		
	}

	@Override
	public void importLap(StatLap lap) {
		boolean lapExists = false;
		if (enduSessions.isEmpty())
			enduSessions.add(enduSession);
		
		
		enduSession.teamCode = lap.teamCode;
		enduSession.pin = lap.pin;
		enduSession.session_TYPENAME = lap.session_TYPE;
		switch (enduSession.session_TYPENAME) {
		case "QUALIFY":
			enduSession.setSession_TYPE(1);
	          break;
	        case "PRACTICE":
	        	enduSession.setSession_TYPE(AC_SESSION_TYPE.AC_PRACTICE);
	          break;
	        case "RACE":
	        	enduSession.setSession_TYPE(AC_SESSION_TYPE.AC_RACE);
	          break;
	        case "HOTLAP":
	        	enduSession.setSession_TYPE(AC_SESSION_TYPE.AC_HOTLAP);
	          break;
	        case "TIME_ATTACK":
	        	enduSession.setSession_TYPE(AC_SESSION_TYPE.AC_TIME_ATTACK);
	          break;
	        case "HOTSTINT":
	        	enduSession.setSession_TYPE(AC_SESSION_TYPE.AC_HOTSTINT);
	          break;
	        case "HOTLAPSUPERPOLE":
	        	enduSession.setSession_TYPE(AC_SESSION_TYPE.AC_HOTLAPSUPERPOLE);
	          break;
	      }
		
		int sum = 0;
		if (!lap.splitTimes.isEmpty()) {
			for ( Entry<Integer, Integer> split : lap.splitTimes.entrySet()) {
				sum += split.getValue();
				split.setValue(sum);
			}
		}
		
		/*
		if (enduSession.laps.size() > 0) {
			for (Entry<Integer,StatLap> lapEntry : enduSession.laps.entrySet()) {
				StatLap tmpLap =  lapEntry.getValue();
				if (tmpLap.lapNo == lap.lapNo && tmpLap.teamCode == lap.teamCode && tmpLap.pin == lap.pin) lapExists = true;
			}
		} 
		*/
		
		if (!lapExists) {
			//System.out.println ("importig: " + lap.teamCode); 
			enduSession.laps.put(enduSession.laps.size(), lap); // importStatLap(lap);
		}
	}

	@Override
	public StatSession getEnduSession() {
		return enduSession;
	}

	@Override
	public void setAutoSaveReplayKey(String key) {
		autoSaveReplay = key;
	}

	@Override
	public String getAutoSaveReplayKey() {
		return autoSaveReplay;
	}

	@Override
	public void setAutoSaveReplayActivity(String key) {
		System.out.println("autoSaveReplayActivity:" + key);
		autoSaveReplayActivity = key;
		
	}

	@Override
	public String getAutoSaveReplayActivity() {
		return autoSaveReplayActivity;
	}
    
    
}
