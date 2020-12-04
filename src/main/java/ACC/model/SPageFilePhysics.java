package ACC.model;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class SPageFilePhysics extends Structure {

	public SPageFilePhysics(Pointer p) {
		super(p);
		read();
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("packetId", "gas", "brake", "fuel", "gear", "rpms", "steerAngle", "speedKmh", "velocity",
				"accG", "wheelSlip", "wheelLoad", "wheelsPressure", "wheelAngularSpeed", "tyreWear", "tyreDirtyLevel",
				"tyreCoreTemperature", "camberRAD", "suspensionTravel", "drs", "tc", "heading", "pitch", "roll",
				"cgHeight", "carDamage", "numberOfTyresOut", "pitLimiterOn", "abs", "kersCharge", "kersInput",
				"autoShifterOn", "rideHeight", "turboBoost", "ballast", "airDensity", "airTemp", "roadTemp",
				"localAngularVel", "finalFF", "performanceMeter", "engineBrake", "ersRecoveryLevel", "ersPowerLevel",
				"ersHeatCharging", "ersIsCharging", "kersCurrentKJ", "drsAvailable", "drsEnabled", "brakeTemp",
				"clutch", "tyreTempI", "tyreTempM", "tyreTempO", "isAIControlled", "tyreContactPoint",
				"tyreContactNormal", "tyreContactHeading", "brakeBias", "localVelocity", "P2PActivations", "P2PStatus",
				"currentMaxRpm", "mz", "fx", "fy", "slipRatio", "slipAngle", "tcinAction", "absInAction",
				"suspensionDamage", "tyreTemp",
		"waterTemp",   
		"brakePressure",
		"frontBrakeCompound", 
		"rearBrakeCompound",
		"padLife",
		"discLife",
		"ignitionOn",    
		"starterEngineOn",
		"isEngineRunning",
		"kerbVibration",
		"slipVibrations",
		"gVibrations",
		"absVibrations"
				);
	}

	public int packetId = 0;
	public float gas = 0;
	public float brake = 0;
	public float fuel = 0;
	public int gear = 0;
	public int rpms = 0;
	public float steerAngle = 0;
	public float speedKmh = 0;
	public float[] velocity = new float[3];
	public float[] accG = new float[3];
	public float[] wheelSlip = new float[4];
	public float[] wheelLoad = new float[4];
	public float[] wheelsPressure = new float[4];
	public float[] wheelAngularSpeed = new float[4];
	public float[] tyreWear = new float[4];
	public float[] tyreDirtyLevel = new float[4];
	public float[] tyreCoreTemperature = new float[4];
	public float[] camberRAD = new float[4];
	public float[] suspensionTravel = new float[4];
	public float drs = 0;
	public float tc = 0;
	public float heading = 0;
	public float pitch = 0;
	public float roll = 0;
	public float cgHeight;
	public float[] carDamage = new float[5];
	public int numberOfTyresOut = 0;
	public int pitLimiterOn = 0;
	public float abs = 0;
	public float kersCharge = 0;
	public float kersInput = 0;
	public int autoShifterOn = 0;
	public float[] rideHeight = new float[2];
	public float turboBoost = 0;
	public float ballast = 0;
	public float airDensity = 0;
	public float airTemp = 0;
	public float roadTemp = 0;
	public float[] localAngularVel = new float[3];;
	public float finalFF = 0;
	public float performanceMeter = 0;

	public int engineBrake = 0;
	public int ersRecoveryLevel = 0;
	public int ersPowerLevel = 0;
	public int ersHeatCharging = 0;
	public int ersIsCharging = 0;
	public float kersCurrentKJ = 0;

	public int drsAvailable = 0;
	public int drsEnabled = 0;

	public float[] brakeTemp = new float[4];
	public float clutch = 0;

	public float[] tyreTempI = new float[4];
	public float[] tyreTempM = new float[4];
	public float[] tyreTempO = new float[4];

	public int isAIControlled = 0;

	public float[] tyreContactPoint = new float[12];
	public float[] tyreContactNormal = new float[12];
	public float[] tyreContactHeading = new float[12];

	public float brakeBias = 0;

	public float[] localVelocity = new float[3];

	public int P2PActivations = 0;
	public int P2PStatus = 0;

	public int currentMaxRpm = 0;

	public float[] mz = new float[4];
	public float[] fx = new float[4];
	public float[] fy = new float[4];
	public float[] slipRatio = new float[4];
	public float[] slipAngle = new float[4];

	public int tcinAction = 0;
	public int absInAction = 0;
	public float[] suspensionDamage = new float[4];
	public float[] tyreTemp = new float[4];

	public float waterTemp = 0;
	public float[] brakePressure = new float[4];
	public int frontBrakeCompound = 0;
	public int rearBrakeCompound = 0;
	public float[] padLife = new float[4];
	public float[] discLife = new float[4];
	public int ignitionOn = 0;
	public int starterEngineOn = 0;
	public int isEngineRunning = 0;
	public float kerbVibration = 0;
	public float slipVibrations = 0;
	public float gVibrations = 0;
	public float absVibrations = 0;

}
