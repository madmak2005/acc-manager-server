package ACC.model;

public class StatPoint {
	public int 		lapNo = 0;
	public int 		sessionIndex = 0;
	public int 		session = 0;
	public float 	normalizedCarPosition = 0;       //Current player position
	public int   	iCurrentTime = 0;   //Current lap time in milliseconds
	public int   	iLastTime = 0;   //Current lap time in milliseconds
	public int   	iSplit = 0;
	public int		flag = 0;
	
	public int 		isInPit = 0;		//Car is pitting
	public int 		isInPitLane = 0;	//Car is in pit lane
	
	public float 	fuel = 0;			//Amount of fuel remaining in kg
	public float 	usedFuel = 0;		//Used fuel since last time refueling
	public float	fuelXlap = 0;
	
	public float 	airTemp = 0;		//Air temperature
	public float 	roadTemp = 0;		//Road temperature
	
	public int 		currentSectorIndex = 0;
	public int 		lastSectorTime = 0;
	
	public float[] 	carDamage = new float[5];
	public float[] 	brakeTemp = new float[4]; 		   //Brake discs temperatures
	public float[] 	wheelsPressure = new float[4];
	
	public float[] 	tyreCoreTemperature = new float[4];	
	public float[] 	tyreTempI = new float[4];
	public float[] 	tyreTempM = new float[4];
	public float[] 	tyreTempO = new float[4];
	
	public float[] 	padLife = new float[4];
	public float[] 	discLife = new float[4];
	
	public float 	speedKmh = 0;
	
	public int 		isValidLap = 0;
	public float 	distanceTraveled = 0;
	
	public float 	sessionTimeLeft = 0;
	public int 		currentMap = 0;
	
	public StatCar  car = new StatCar();
	
}
