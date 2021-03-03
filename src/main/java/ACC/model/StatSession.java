package ACC.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class StatSession {
	public int session_TYPE = AC_SESSION_TYPE.AC_UNKNOWN; 
	public Map<Integer,StatLap> laps = new HashMap<>();

	public CircularFifoQueue<StatLap> last3Laps = new CircularFifoQueue<>(3);
	public CircularFifoQueue<StatLap> last5Laps = new CircularFifoQueue<>(5);
	
	public StatLap bestLap = new StatLap();
	public StatLap lastLap = new StatLap();
	public StatLap currentLap = new StatLap();
	
	public StatCar car = new StatCar();
	
	public int sessionIndex = 0;
	public int internalSessionIndex = 0;
	public boolean wasGreenFlag = false; 
	
	public int bestTime = 0;
	
	public float sessionTimeLeft = 0;
	
	public float distanceTraveled = 0;
	public float fuelAVG3Laps = 0;
	public float fuelAVG5Laps = 0;
	
	public int avgLapTime3 = 0;
	public int avgLapTime5 = 0;
	
	public int packetDelta = 0;
	
}
