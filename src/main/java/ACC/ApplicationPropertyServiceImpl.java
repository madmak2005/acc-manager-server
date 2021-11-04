package ACC;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ACC.model.StatLap;
import ACC.model.StatSession;
import app.Application;
import lombok.Data;

@ComponentScan("ACC")
@Service
public class ApplicationPropertyServiceImpl implements ApplicationPropertyService{
	
	private String sheetID = "";
	List<StatLap> allLaps = new ArrayList<StatLap>();
	List<StatSession> sessions = new ArrayList<StatSession>(); 
	
	@Autowired
	private ServerProperties serverProperties;
	
	@Override
    public int getApplicationProperty(){
        return (Application.useDebug ? 10 : 100);
    }
    
	@Override
    public int getStatisticsInterval(){
        return (Application.useDebug ? 33: 333);
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
    
    
}
