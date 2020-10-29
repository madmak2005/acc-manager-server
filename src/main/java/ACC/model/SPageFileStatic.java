package ACC.model;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;


public class SPageFileStatic extends Structure {
	
	public SPageFileStatic(Pointer p) {
		super(p);
		read();
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList(
				"smVersion"
				,"acVersion"
				
				,"numberOfSessions"
				,"numCars"
				,"carModel"
				,"track"
				,"playerName"
				,"playerSurname"
				,"playerNick"
				,"sectorCount"

				,"maxTorque"
				,"maxPower"
				,"maxRpm"
				,"maxFuel"
				,"suspensionMaxTravel"
				,"tyreRadius"
				,"maxTurboBoost"

				,"deprecated_1"
				,"deprecated_2"

				,"penaltiesEnabled"

				,"aidFuelRate"
				,"aidTireRate"
				,"aidMechanicalDamage"
				,"aidAllowTyreBlankets"
				,"aidStability"
				,"aidAutoClutch"
				,"aidAutoBlip"

				,"hasDRS"
				,"hasERS"
				,"hasKERS"
				,"kersMaxJ"
				,"engineBrakeSettingsCount"
				,"ersPowerControllerCount"
				,"trackSPlineLength"
				,"trackConfiguration"
				,"ersMaxJ"

				,"isTimedRace"
				,"hasExtraLap"

				,"carSkin"
				,"reversedGridPositions"
				,"PitWindowStart"
				,"PitWindowEnd"
				,"isOnline"
				);
	}
	
	public byte[] smVersion = new byte[30];
	public byte[] acVersion = new byte[30];

	// session static info
	public int numberOfSessions = 0;
	public int numCars = 0;
	
	public byte[] carModel = new byte[66];
	
	public byte[] track = new byte[66];
	
	public byte[] playerName = new byte[66];
	
	public byte[] playerSurname = new byte[66];
	
	public byte[] playerNick = new byte[66];
	
	public int sectorCount = 0;

	// car static info
	public float maxTorque = 0;
	public float maxPower = 0;
	public int maxRpm = 0;
	public float maxFuel = 0;

	public float[] suspensionMaxTravel = new float[4];
	public float[] tyreRadius = new float[4];
	public float maxTurboBoost = 0;

	public float deprecated_1 = 0;
	public float deprecated_2 = 0;

	public int penaltiesEnabled = 0;

	public float aidFuelRate = 0;
	public float aidTireRate = 0;
	public float aidMechanicalDamage = 0;
	public int aidAllowTyreBlankets = 0;
	public float aidStability = 0;
	public int aidAutoClutch = 0;
	public int aidAutoBlip = 0;

	public int hasDRS = 0;
	public int hasERS = 0;
	public int hasKERS = 0;
	public float kersMaxJ = 0;
	public int engineBrakeSettingsCount = 0;
	public int ersPowerControllerCount = 0;
	public float trackSPlineLength = 0;
	
	public byte[] trackConfiguration = new byte[66];
	public float ersMaxJ = 0;

	public int isTimedRace = 0;
	public int hasExtraLap = 0;
	
	public byte[] carSkin = new byte[66];
	public int reversedGridPositions = 0;
	public int PitWindowStart = 0;
	public int PitWindowEnd = 0;
	public int isOnline = 0;

}



