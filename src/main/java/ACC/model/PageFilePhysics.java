package ACC.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import app.Application;

@JsonFilter("filter1")
public class PageFilePhysics implements Page {
	
	public	PageFilePhysics(SPageFilePhysics o){
		this.o = o;
		setPageName("physics");
		fillFieldsHelper(o);
	}
	
	   @JsonIgnore
	   private Object o;
	
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
	   @JsonIgnore
	   public float[] wheelLoad = new float[4];
	   public float[] wheelsPressure = new float[4];
	   public float[] wheelAngularSpeed = new float[4];
	   @JsonIgnore
	   public float[] tyreWear = new float[4];
	   @JsonIgnore
	   public float[] tyreDirtyLevel = new float[4];
	   public float[] tyreCoreTemperature = new float[4];
	   @JsonIgnore
	   public float[] camberRAD = new float[4];
	   public float[] suspensionTravel = new float[4];
	   @JsonIgnore
	   public float drs = 0;
	   public float tc = 0;
	   public float heading = 0;
	   public float pitch = 0;
	   public float roll = 0;
	   @JsonIgnore
	   public float cgHeight;
	   public float[] carDamage = new float[5];
	   @JsonIgnore
	   public int numberOfTyresOut = 0;
	   public int pitLimiterOn = 0;
	   public float abs = 0;
	   @JsonIgnore
	   public float kersCharge = 0;
	   @JsonIgnore
	   public float kersInput = 0;
	   public int autoShifterOn = 0;
	   @JsonIgnore
	   public float[] rideHeight  = new float[2];
	   public float turboBoost = 0;
	   @JsonIgnore
	   public float ballast = 0;
	   @JsonIgnore
	   public float airDensity = 0;
	   public float airTemp = 0;
	   public float roadTemp = 0;
	   public float[] localAngularVel = new float[3];;
	   public float finalFF = 0;
	   @JsonIgnore
	   public float performanceMeter = 0;
	   @JsonIgnore
	   public int engineBrake = 0;
	   @JsonIgnore
	   public int ersRecoveryLevel = 0;
	   @JsonIgnore
	   public int ersPowerLevel = 0;
	   @JsonIgnore
	   public int ersHeatCharging = 0;
	   @JsonIgnore
	   public int ersIsCharging = 0;
	   @JsonIgnore
	   public float kersCurrentKJ = 0;
	   @JsonIgnore
	   public int drsAvailable = 0;
	   @JsonIgnore
	   public int drsEnabled = 0;

	   public float[] brakeTemp = new float[4];
	   public float clutch = 0;
	   @JsonIgnore
	   public float[] tyreTempI = new float[4];
	   @JsonIgnore
	   public float[] tyreTempM = new float[4];
	   @JsonIgnore
	   public float[] tyreTempO = new float[4];

	   public int isAIControlled = 0;

	   public float[][] tyreContactPoint = new float[4][3];
	   public float[][] tyreContactNormal = new float[4][3];
	   public float[][] tyreContactHeading = new float[4][3];

	   public float brakeBias = 0;

	   public float[] localVelocity = new float[3];
	   @JsonIgnore
	   public int P2PActivations = 0;
	   @JsonIgnore
	   public int P2PStatus = 0;
	   @JsonIgnore
	   public float currentMaxRpm = 0;

	   public float[] mz = new float[4];
	   public float[] fx = new float[4];
	   public float[] fy = new float[4];
	   @JsonIgnore
	   public float[] slipRatio = new float[4];
	   @JsonIgnore
	   public float[] slipAngle = new float[4];
	   @JsonIgnore
	   public int tcinAction = 0;
	   @JsonIgnore
	   public int absInAction = 0;
	   @JsonIgnore
	   public float[] suspensionDamage = new float[4];
	   @JsonIgnore
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
	   
		public void fillFieldsHelper(Object source) {
			List<Field> sourceFields = Arrays.asList(source.getClass().getDeclaredFields());
			sourceFields.forEach(valueOne -> {
				try {
					Object value = valueOne.get(source);
					Field attrOne = source.getClass().getDeclaredField(valueOne.getName().replace("source",""));
					switch (attrOne.getType().getName()) {
					 case "[F":
						 attrOne = source.getClass().getDeclaredField(valueOne.getName().replace("source",""));
						 int i = 0;
						 if (valueOne.getName().equals("tyreContactPoint")) {
							 float[] tyreCP = (float[])value;
							 for (int tyre = 0; tyre <=3; tyre++) {
								 for(int contact = 0; contact <=2; contact++) {
									 this.tyreContactPoint[tyre][contact] = tyreCP[i];
									 i++;
								 }
							 }
						 } else 
							 if (valueOne.getName().equals("tyreContactNormal")) {
								 float[] tyreCN = (float[])value;
								 for (int tyre = 0; tyre <=3; tyre++) {
									 for(int contact = 0; contact <=2; contact++) {
										 this.tyreContactNormal[tyre][contact] = tyreCN[i];
										 i++;
									 }
								 }
							 } else 
								 if (valueOne.getName().equals("tyreContactHeading")) {
									 float[] tyreCH = (float[])value;
									 for (int tyre = 0; tyre <=3; tyre++) {
										 for(int contact = 0; contact <=2; contact++) {
											 this.tyreContactHeading[tyre][contact] = tyreCH[i];
											 i++;
										 }
									 }
								 } else {
									 attrOne = this.getClass().getDeclaredField(valueOne.getName().replace("source",""));
									 attrOne.set(this, value);
								 }
					  break;
					 default: 
						 attrOne = this.getClass().getDeclaredField(valueOne.getName().replace("source",""));
						 attrOne.set(this, value);
						 break;
					}
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
					e.printStackTrace();
				}
				
			});
			
		}
		
		@Override
		public String toJSON() {
			
			String response = "";
			Page page = this;
			try {
				FilterProvider filters = new SimpleFilterProvider()  
					      .addFilter("filter1",   
					          SimpleBeanPropertyFilter.serializeAllExcept(""));
				ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
				response = mapper.writeValueAsString(page);
			} catch (JsonProcessingException e) {
				Application.LOGGER.debug(e.toString());
			}
			return response;
		}
		
		@Override
		public String toJSON(List<String> fields) {
			String response = "";
			Page page = this;
			try {
				Set<String> fieldsFilter = new HashSet<String>(fields);
				 FilterProvider filters = new SimpleFilterProvider()  
					      .addFilter("filter1",   
					          SimpleBeanPropertyFilter.filterOutAllExcept(fieldsFilter));
				ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
				response = mapper.writeValueAsString(page);
			} catch (JsonProcessingException e) {
				Application.LOGGER.debug(e.toString());
			}
			return response;
		}
		

		@JsonIgnore
		private String pageName;
		
		@Override
		public String getPageName() {
			return pageName;
		}

		@Override
		public void setPageName(String pageName) {
			this.pageName = pageName;
		}
}
