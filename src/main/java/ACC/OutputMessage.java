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
		if (fields == null || fields.size() == 0)
			this.content = page.toJSON();
		else
			this.content = page.toJSON(fields);
	}
	
	public OutputMessage(String content) {
		super();
		this.content = content;
	}
	
	public String content;
	public Page page;
	public List<String> fields;
	
}