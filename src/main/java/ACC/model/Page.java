package ACC.model;

import java.util.List;

public interface Page {
	String toJSON();
	String toJSON(List<String> fields);
}
