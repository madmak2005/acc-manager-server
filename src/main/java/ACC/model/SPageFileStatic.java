package ACC.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import virtualKeyboard.VirtualKeyboardApplication;

public class SPageFileStatic {
	public String smVersion;
	public String acVersion;

	// session static info
	public int numberOfSessions;
	public int numCars;
	public String carModel;
	public String track;
	public String playerName;
	public String playerSurname;
	public String playerNick;
	public int sectorCount;

	// car static info
	public float maxTorque;
	public float maxPower;
	public int maxRpm;
	public float maxFuel;
	public float[] suspensionMaxTravel;
	public float[] tyreRadius;
	public float maxTurboBoost;

	public float deprecated_1;
	public float deprecated_2;

	public int penaltiesEnabled;

	public float aidFuelRate;
	public float aidTireRate;
	public float aidMechanicalDamage;
	public int aidAllowTyreBlankets;
	public float aidStability;
	public int aidAutoClutch;
	public int aidAutoBlip;

	public int hasDRS;
	public int hasERS;
	public int hasKERS;
	public float kersMaxJ;
	public int engineBrakeSettingsCount;
	public int ersPowerControllerCount;
	public float trackSPlineLength;
	public String trackConfiguration;
	public float ersMaxJ;

	public int isTimedRace;
	public int hasExtraLap;

	public String carSkin;
	public int reversedGridPositions;
	public int PitWindowStart;
	public int PitWindowEnd;
	public int isOnline;

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
