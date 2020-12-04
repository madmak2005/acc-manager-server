package ACC.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Message {
	public Message(String message) {
		this.message = message;
	}

	private String message;

	public List<String> getFildsToFilter() {
		this.message=this.message.replaceAll("\\s+","");
		List<String> filter = new ArrayList<String>();
		if (this.message != null && this.message.length() > 0) {
			CharSequence splitter = ",";
			if (this.message.contains(splitter)) {
				String str[] = this.message.split(splitter.toString());
				filter = Arrays.asList(str);
			} else {
				filter.add(this.message);
			}
		}
		return filter;
	}
}
