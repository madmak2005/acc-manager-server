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
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import app.Application;

@JsonFilter("filter1")
public class PageFileStatic implements Page{
	@JsonIgnore
	private Object o;

	public String smVersion = "";
	public String acVersion = "";

	// session static info
	public int numberOfSessions = 0;
	public int numCars = 0;
	
	public String carModel = "";
	
	public String track = "";
	
	public String playerName = "";
	
	public String playerSurname = "";
	
	public String playerNick = "";
	
	public int sectorCount = 0;

	// car static info
	public float maxTorque = 0;
	public float maxPower = 0;
	public int maxRpm = 0;
	public float maxFuel = 0;
	
	public float[] suspensionMaxTravel = new float[4];
	public float[] tyreRadius = new float[4];
	@JsonIgnore
	public float maxTurboBoost = 0;
	@JsonIgnore
	public float deprecated_1 = 0;
	@JsonIgnore
	public float deprecated_2 = 0;

	public int penaltiesEnabled = 0;

	public float aidFuelRate = 0;
	public float aidTireRate = 0;
	public float aidMechanicalDamage = 0;
	public int aidAllowTyreBlankets = 0;
	public float aidStability = 0;
	public int aidAutoClutch = 0;
	public int aidAutoBlip = 0;
	@JsonIgnore
	public int hasDRS = 0;
	@JsonIgnore
	public int hasERS = 0;
	@JsonIgnore
	public int hasKERS = 0;
	public float kersMaxJ = 0;
	@JsonIgnore
	public int engineBrakeSettingsCount = 0;
	@JsonIgnore
	public int ersPowerControllerCount = 0;
	@JsonIgnore
	public float trackSPlineLength = 0;
	@JsonIgnore
	public String trackConfiguration = "";
	@JsonIgnore
	public float ersMaxJ = 0;
	@JsonIgnore
	public int isTimedRace = 0;
	@JsonIgnore
	public int hasExtraLap = 0;
	@JsonIgnore
	public String carSkin = "";
	@JsonIgnore
	public int reversedGridPositions = 0;
	public int PitWindowStart = 0;
	public int PitWindowEnd = 0;
	public int isOnline = 0;
	
	public PageFileStatic(SPageFileStatic o) {
		this.o = o;
		fillFieldsHelper(o);
	}

	public void fillFieldsHelper(Object source) {
		List<Field> sourceFields = Arrays.asList(source.getClass().getDeclaredFields());
		sourceFields.forEach(valueOne -> {
			try {
				Object value = valueOne.get(source);
				Field attrOne = source.getClass().getDeclaredField(valueOne.getName().replace("source",""));
				switch (attrOne.getType().getName()) {
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
	
}
