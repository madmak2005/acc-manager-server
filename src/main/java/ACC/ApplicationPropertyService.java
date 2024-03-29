package ACC;

import java.util.List;

import ACC.model.StatLap;
import ACC.model.StatSession;

public interface ApplicationPropertyService {
    public int getApplicationProperty();
    public int getStatisticsInterval();
    public int getApplicationPort();
    public String getSheetID();
    public void setSheetID(String sheetID);
    
    public List<StatLap> getMobileSessionLapList(int internalSessionIndex);
    public void addMobileSessionLap(StatLap statLap);
    
    public List<StatSession> getMobileSessionList();
    public void addMobileSession(StatSession session);
    public StatSession getEnduSession();
    public void importLap(StatLap lap);
    public void setAutoSaveReplayKey(String key);
    public String getAutoSaveReplayKey();
	public void setAutoSaveReplayActivity(String key);
	public String getAutoSaveReplayActivity();
}
