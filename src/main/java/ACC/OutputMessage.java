package ACC;

import java.util.List;


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
	public OutputMessage(Page page, List<String> fields) {
		super();
		this.page = page;
		this.fields = fields;
		this.content = page.toJSON();
	}
	public String content;
	public Page page;
	public List<String> fields;
	
}