package ACC.sharedmemory;

import java.util.List;

import ACC.model.OutputMessage;
import ACC.model.Page;
import ACC.model.StatCar;
import ACC.model.StatLap;
import ACC.model.StatPoint;

public interface ACCSharedMemoryService {
	public OutputMessage getPageFileMessage(String pageTyp, List<String> fieldsFilter);
	public Page getPageFile(String pageTyp);
	public StatPoint getStatPoint();
	public StatCar getStatCar();
	
}
