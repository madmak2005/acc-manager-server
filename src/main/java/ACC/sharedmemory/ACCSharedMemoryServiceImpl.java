package ACC.sharedmemory;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import ACC.model.PageFileGraphics;
import ACC.model.PageFilePhysics;
import ACC.model.PageFileStatic;
import ACC.model.OutputMessage;
import ACC.model.Page;

@ComponentScan("ACC")
@Service
public class ACCSharedMemoryServiceImpl implements ACCSharedMemoryService {

	
	final ACCSharedMemory sh = new ACCSharedMemory();
	
	@Override
	public OutputMessage getPageFileMessage(String pageTyp, List<String> fieldsFilter) {
		Page page = getPageFile(pageTyp);
		return new OutputMessage(page, fieldsFilter);
	}

	@Override
	public Page getPageFile(String pageTyp) {
		LocalDateTime now = new LocalDateTime();
		
		Page page = switch(pageTyp) {
		case "physics" : {
			PageFilePhysics  p = sh.getPageFilePhysics();	
			p.packetId = p.packetId == 0 ? now.getMillisOfDay() : p.packetId;
			yield p;
		}
		case "graphics" : {
			PageFileGraphics g = sh.getPageFileGraphics();
			Double sec = (double) now.getSecondOfMinute()/60;
			Double mili = (double) now.getMillisOfSecond()/100000;
			float position = (float) (sec+mili);
			g.normalizedCarPosition = g.packetId == 0 ? position : g.normalizedCarPosition;
			g.lightsStage = g.packetId == 0 ? (Math.random() < 0.5 ? (Math.random() < 0.5 ? 0 : 1) : 2) : g.rainLights;
			g.isInPit     = g.packetId == 0 ? now.getMinuteOfHour() % 2 : g.isInPit;
			g.packetId    = g.packetId == 0 ? now.getMillisOfDay() : g.packetId;
			yield g;
		}
		case "static" : {
			PageFileStatic   s = sh.getPageFileStatic();
			yield s;
		}
		default : yield null;
		};
		return page;
		
	}
	
	

}
