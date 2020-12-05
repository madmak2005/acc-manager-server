package ACC.acm;

import java.util.List;

public interface AutomaticCarManagementService {
	public abstract void addAction(MacroAction action);
	public abstract void deleteAction(MacroManagement action);
	public abstract void executeMacro();
	public abstract void activateMacro();
	public abstract void deactivateMacro();
	public abstract void addActionList(List<MacroAction> actionList);
}
