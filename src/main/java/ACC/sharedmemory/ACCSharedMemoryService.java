package ACC.sharedmemory;

import java.util.List;

import ACC.model.OutputMessage;
import ACC.model.Page;

public interface ACCSharedMemoryService {
	public OutputMessage getPageFileMessage(String pageTyp, List<String> fieldsFilter);
	public Page getPageFile(String pageTyp);
}
