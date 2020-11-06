package ACC;

import ACC.model.Page;

public class OutputMessage{
	
	@SuppressWarnings("unused")
	private String getContent() {
		return content;
	}
	@SuppressWarnings("unused")
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