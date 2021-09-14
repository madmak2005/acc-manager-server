package ACC.acm;

import lombok.Data;

@Data
public class MacroAction {
	
	public int order = 0;
	public String pageFile = "";
	public String pageFileValueName = "";
	public double start1 = 0.0;
	public double start2 = 0.0;
	public double parameterTargetValue = 0.0;
	public String keyToIncrease = "";
	public int keyToIncreaseTime = 0;
	public String keyToDecrease = "";
	public int keyToDecreaseTime = 0;
	public boolean active = false;
	
	public MacroAction() {
		super();
	}
	
	public MacroAction(int order, String pageFile, String pageFileValueName, double start1, double start2,
			int parameterTargetValue, String keyToIncrease,
			int keyToIncreaseTime, String keytoDecrease, int keyToDecreaseTime, boolean active) {
		this.order = order;
		this.pageFile = pageFile;
		this.pageFileValueName = pageFileValueName;
		this.start1 = start1;
		this.start2 = start2;
		this.parameterTargetValue = parameterTargetValue;
		this.keyToIncrease = keyToIncrease;
		this.keyToIncreaseTime = keyToIncreaseTime;
		this.keyToDecrease = keytoDecrease;
		this.keyToDecreaseTime = keyToDecreaseTime;
		this.active = active;
	}

	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}	
}


