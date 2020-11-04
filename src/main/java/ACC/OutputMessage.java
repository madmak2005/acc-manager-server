package ACC;

import ACC.model.Page;
import ACC.model.PageFilePhysics;

public class OutputMessage{
	
	private String getContent() {
		return content;
	}
	private void setContent(String content) {
		this.content = content;
	}
	public OutputMessage(Page page, String time) {
		super();
		this.page = page;
		this.content = page.toJSON();
		this.time = time;
	}
	public String content;
	public Page page;
	public String time;
	
}