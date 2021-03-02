package ACC.model;

import java.util.List;

public interface Page {
	String getPageName();
	void setPageName(String pageName);
	boolean isACCConnected();
	String toJSON();
	String toJSON(List<String> fields);
}
