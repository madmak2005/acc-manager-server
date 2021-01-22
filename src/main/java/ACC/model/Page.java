package ACC.model;

import java.util.List;

public interface Page {
	String getPageName();
	void setPageName(String pageName);
	String toJSON();
	String toJSON(List<String> fields);
}
