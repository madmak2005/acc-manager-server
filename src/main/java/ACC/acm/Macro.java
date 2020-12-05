package ACC.acm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import ACC.model.PageFileGraphics;
import ACC.sharedmemory.ACCSharedMemoryService;
import virtualKeyboard.VirtualKeyboardAPI;

@Component
@ComponentScan("ACC")
public class Macro {

	@Autowired
	private ACCSharedMemoryService accSharedMemoryService;

	String track;
	final ArrayList<MacroAction> actionList = new ArrayList<MacroAction>();
	final VirtualKeyboardAPI api = new VirtualKeyboardAPI();
	final int DOWN = 0;
	final int UP = 2;

	private boolean active = false;

	protected boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	double lastPostion = 0;
	Set<Integer> executionABSList = new HashSet<Integer>();
	Set<Integer> executionTCList = new HashSet<Integer>();
	Set<Integer> executionMapList = new HashSet<Integer>();

	public void executeActions() {
		if (isActive()) {
			actionList.forEach(action -> {
				double value = -1.0;
				double actionPosition = -1.0;
				PageFileGraphics pageFileGraphics = (PageFileGraphics) accSharedMemoryService.getPageFile("graphics");
				double currentPosition = pageFileGraphics.normalizedCarPosition;
				if (action.pageFile.equals("PageFileGraphics")) {
					if (action.pageFileValueName.equals("ABS"))
						value = pageFileGraphics.ABS;
					if (action.pageFileValueName.equals("TC")) {
						value = pageFileGraphics.TC;
					}
					if (action.pageFileValueName.equals("MAP"))
						value = pageFileGraphics.EngineMap;

					if (actionPosition == -1.0)
						actionPosition = getRandomNumber(action.start1, action.start2);
					if (currentPosition >= action.start1 && currentPosition < action.start2
							&& currentPosition >= actionPosition) {
						int count = 0;
						while (action.parameterTargetValue < value && count <= 10) {
							decrease(action);
							count++;
						}
						while (action.parameterTargetValue > value && count <= 10) {
							increase(action);
							count++;
						}
						if (count >= 10)
							System.out.println("Something wrong. Skipping.");
					}

				}
			});
		}
	}

	private void decrease(MacroAction action) {
		Map<String, String> allParams = new HashMap<String, String>();
		allParams.put("key", action.keyToDecrease);
		allParams.put("time", String.valueOf(action.keyToIncreaseTime));
		api.execute(allParams);

	}

	private void increase(MacroAction action) {
		Map<String, String> allParams = new HashMap<String, String>();
		allParams.put("key", action.keyToIncrease);
		allParams.put("time", String.valueOf(action.keyToIncreaseTime));
		api.execute(allParams);
	}

	public double getRandomNumber(double min, double max) {
		return ((Math.random() * (max - min)) + min);
	}

	public void addMacroAction(MacroAction action) {
		if (!actionList.contains(action)) {
			Iterator<MacroAction> i = actionList.iterator();
			while (i.hasNext()) {
				MacroAction act = i.next();
				if (act.order == action.order) {
					actionList.remove(act);
				}
			}
			;
			actionList.add(action);
			System.out.println(actionList.size());
			Collections.sort(actionList, new SortbyOrder());
		}
	}

	public void deleteMacroAction(MacroManagement action) {
		MacroAction toDelete = null;
		Iterator<MacroAction> i = actionList.iterator();
		while (i.hasNext()) {
			MacroAction check = i.next();
			if (check.order == action.deleteOrderId) {
				toDelete = check;
			}
		}
		if (toDelete != null) {
			actionList.remove(toDelete);
		}
	}

}

class SortbyOrder implements Comparator<MacroAction> {
	@Override
	public int compare(MacroAction a, MacroAction b) {
		return a.order - b.order;
	}
}