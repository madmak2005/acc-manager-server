package ACC.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import virtualKeyboard.VirtualKeyboardApplication;

public class SPageFilePhysics {
	
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
   public float[] rideHeight  = new float[2];
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

   public int isAIControlled;

   public float[][] tyreContactPoint = new float[4][3];
   public float[][] tyreContactNormal = new float[4][3];
   public float[][] tyreContactHeading = new float[4][3];

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
   
	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		String response = "";
		try {
			response = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			VirtualKeyboardApplication.LOGGER.debug(e.toString());
		}
		return response;
	}
}
