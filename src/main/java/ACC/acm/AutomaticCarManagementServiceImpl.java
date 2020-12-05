package ACC.acm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutomaticCarManagementServiceImpl implements AutomaticCarManagementService{
	

	@Autowired
	private Macro macro;
	//private Macro macro = new Macro("service");
	
	@Override
	public void addAction(MacroAction action) {
		macro.addMacroAction(action);
	}

	@Override
	public void executeMacro() {
		macro.executeActions();
		
	}

	@Override
	public void deleteAction(MacroManagement action) {
		macro.deleteMacroAction(action);
		
	}

	@Override
	public void addActionList(List<MacroAction> actionList) {
		actionList.forEach(action -> {
			macro.addMacroAction(action);
		});
	}

	@Override
	public void activateMacro() {
		macro.setActive(true);
		
	}

	@Override
	public void deactivateMacro() {
		macro.setActive(false);
		
	}

}
