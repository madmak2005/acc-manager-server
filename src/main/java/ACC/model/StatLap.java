package ACC.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatLap {
	public int 		lapNo = 0;
	public boolean 	fromPit = false;
	public boolean 	toPit = false;
	public int 		lapTime = 0;
	public float 	distanceTraveled = 0;
	
	public Map<Integer,Integer> splitTimes = new HashMap<>();
	
	public float fuelAdded = 0;
	public float fuelUsed = 0;
	public float fuelLeftOnStart = 0;
	public float fuelLeftOnEnd = 0;
	public float fuelAVGPerMinute = 0;
	public float fuelXlap = 0;
	public Map<Integer,Integer> maps = new HashMap<>();
	public int rainTyres;	//Are rain tyres equipped
	public boolean isValidLap = true;
	public boolean first, last = false;
	public float sessionTimeLeft = 0;
	
	
	public float pFL, pFR, pRL, pRR = 0;
	public float tFL, tFR, tRL, tRR = 0;
	
	public float airTemp = 0;
	public float roadTemp = 0;
	
	public float fuelNTFOnEnd = 0;
	public float fuelEstForNextMiliseconds = 0;
	public float fuelEFNLapsOnEnd = 0;
	
	public float fuelAVGPerLap = 0;
	
	public float clockAtStart = 0;
	
	public float rainIntensity = 0;
	public float trackGripStatus = 0;
	public String trackStatus = "";
}
