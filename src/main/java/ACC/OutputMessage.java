package ACC;

import ACC.model.PageFilePhysics;

public class OutputMessage{
	
	private String getContent() {
		return content;
	}
	private void setContent(String content) {
		this.content = content;
	}
	public OutputMessage(PageFilePhysics physics, String time) {
		super();
		this.physics = physics;
		this.content = physics.toJSON();
		this.time = time;
	}
	public String content;
	public PageFilePhysics physics;
	public String time;
	
}