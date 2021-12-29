package ACC.model;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
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
public class PageFileGraphics implements Page {
	
	public	PageFileGraphics(SPageFileGraphics o){
		this.o = o;
		setPageName("graphics");
		fillFieldsHelper(o);
	}
	
	public	PageFileGraphics(String jsonString){
		setPageName("graphics");
		
	}
	
	@JsonIgnore
	private Object o;


	public int packetId = 0;
	public int status = 0;
	public int session = AC_SESSION_TYPE.AC_PRACTICE;
	public String currentTime = "";
	public String lastTime = "";
	public String bestTime = "";
	public String split = "";
	public int completedLaps = 0;
	public int position = 0;
	public int iCurrentTime = 0;
	public int iLastTime = 0;
	public int iBestTime = 0;
	public float sessionTimeLeft = 0;
	public float distanceTraveled = 0;
	public int isInPit = 0;
	public int currentSectorIndex = 0;
	public int lastSectorTime = 0;
	public int numberOfLaps = 0;
	public String tyreCompound;
	public float replayTimeMultiplier = 0;
	public float normalizedCarPosition = 0;

	public int activeCars = 0;
	public float[][] carCoordinates = new float[60][3];
	public int[] carID = new int[60];
	public int playerCarID = 0;
	public float penaltyTime = 0;
	public int flag = AC_FLAG_TYPE.ACC_NO_FLAG;
	public int penalty = AC_PENALTYSHOTCUT.None;
	public int idealLineOn = 0;
	public int isInPitLane = 0;

	public float surfaceGrip = 0;
	public int mandatoryPitDone = 0;

	public float windSpeed = 0;
	public float windDirection = 0;

	public int isSetupMenuVisible = 0;

	public int mainDisplayIndex = 0;
	public int secondaryDisplayIndex = 0;
	public int TC = 0;
	public int TCCut = 0;
	public int EngineMap = 0;
	public int ABS = 0;
	public float fuelXLap = 0;
	public int rainLights = 0;
	public int flashingLights = 0;
	public int lightsStage = 0;
	public float exhaustTemperature = 0.0f;
	public int wiperLV = 0;
	public int DriverStintTotalTimeLeft = 0;
	public int DriverStintTimeLeft = 0;
	public int rainTyres = 0;
	public int sessionIndex = 0;
	public float usedFuel = 0.0f;
    public String deltaLapTime = "";
    public int iDeltaLapTime = 0;
    public String estimatedLapTime = "";
    public int iEstimatedLapTime = 0;
    public int isDeltaPositive = 0;
    public int iSplit = 0;
    public int isValidLap = 0;
    public float fuelEstimatedLaps = 0.0f;
    public String trackStatus = "";
    public int missingMandatoryPits = 0;
    public float clock = 0;
    public int directionLightsLeft = 0;
    public int directionLightsRight = 0;
    public int GlobalYellow = 0;
    public int GlobalYellow1 = 0;
    public int GlobalYellow2 = 0;
    public int GlobalYellow3 = 0;
    public int GlobalWhite = 0;
    public int GlobalGreen = 0;
    public int GlobalChequered = 0;
    public int GlobalRed = 0;
    public int mfdTyreSet = 0;
    public float mfdFuelToAdd = 0;
    public float mfdTyrePressureLF = 0;
    public float mfdTyrePressureRF = 0;
    public float mfdTyrePressureLR = 0;
    public float mfdTyrePressureRR = 0;
    public int trackGripStatus = 0;
	public int rainIntensity = 0;
	public int rainIntensityIn10min = 0;
	public int rainIntensityIn30min = 0;
	public int currentTyreSet = 0;
	public int strategyTyreSet = 0;
	public int gapAhead = 0;
    public int gapBehind = 0;
    
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
					 if (valueOne.getName().equals("carCoordinates")) {
						 float[] carccords = (float[])value;
						 for (int car = 0; car <=59; car++) {
							 for(int coords = 0; coords <=2; coords++) {
								 this.carCoordinates[car][coords] = carccords[i];
								 i++;
							 }
						 }
					 }
				     break;
				 case "[B":
					 attrOne = this.getClass().getDeclaredField(valueOne.getName().replace("source",""));
					 byte[] byteArr = (byte[])value;
					 attrOne.set(this, new String(byteArr,Charset.forName("UTF-16le")).replaceAll("\\u0000", "") );
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
	
	public static PageFileGraphics fromJSON(String jsonString) {
		PageFileGraphics pg = null;
		try {
			pg = new ObjectMapper().readValue(jsonString, PageFileGraphics.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return pg;
		
	}

	@Override
	public boolean isACCConnected() {
		return packetId > 0;
	}


}

