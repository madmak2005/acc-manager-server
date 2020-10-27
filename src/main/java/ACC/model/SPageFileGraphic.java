package ACC.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import virtualKeyboard.VirtualKeyboardApplication;

public class SPageFileGraphic {

	private class AC_STATUS {
		public static final int AC_OFF = 0;
		public static final int AC_REPLAY = 1;
		public static final int AC_LIVE = 2;
		public static final int AC_PAUSE = 3;
	}

	private class AC_SESSION_TYPE {
		public static final int AC_UNKNOWN = -1;
		public static final int AC_PRACTICE = 0;
		public static final int AC_QUALIFY = 1;
		public static final int AC_RACE = 2;
		public static final int AC_HOTLAP = 3;
		public static final int AC_TIME_ATTACK = 4;
		public static final int AC_DRIFT = 5;
		public static final int AC_DRAG = 6;
		public static final int AC_HOTSTINT = 7;
		public static final int AC_HOTLAPSUPERPOLE = 8;
	}

	private class AC_FLAG_TYPE {
		public static final int AC_NO_FLAG = 0;
		public static final int AC_BLUE_FLAG = 1;
		public static final int AC_YELLOW_FLAG = 2;
		public static final int AC_BLACK_FLAG = 3;
		public static final int AC_WHITE_FLAG = 4;
		public static final int AC_CHECKERED_FLAG = 5;
		public static final int AC_PENALTY_FLAG = 6;
	}

	private enum PenaltyShortcut {
		None, 
		DriveThrough_Cutting, 
		StopAndGo_10_Cutting, 
		StopAndGo_20_Cutting, 
		StopAndGo_30_Cutting,
		Disqualified_Cutting, 
		RemoveBestLaptime_Cutting,
		DriveThrough_PitSpeeding, 
		StopAndGo_10_PitSpeeding, 
		StopAndGo_20_PitSpeeding, 
		StopAndGo_30_PitSpeeding,
		Disqualified_PitSpeeding, 
		RemoveBestLaptime_PitSpeeding,
		Disqualified_IgnoredMandatoryPit,
		PostRaceTime, 
		Disqualified_Trolling, 
		Disqualified_PitEntry, 
		Disqualified_PitExit, 
		Disqualified_WrongWay,
		DriveThrough_IgnoredDriverStint, 
		Disqualified_IgnoredDriverStint,
		Disqualified_ExceededDriverStintLimit,
	}

	int packetId = 0;
	int status = AC_STATUS.AC_OFF;
	int session = AC_SESSION_TYPE.AC_PRACTICE;
	String currentTime;
	String lastTime;
	String bestTime;
	String split;
	int completedLaps = 0;
	int position = 0;
	int iCurrentTime = 0;
	int iLastTime = 0;
	int iBestTime = 0;
	float sessionTimeLeft = 0;
	float distanceTraveled = 0;
	int isInPit = 0;
	int currentSectorIndex = 0;
	int lastSectorTime = 0;
	int numberOfLaps = 0;
	String tyreCompound;
	float replayTimeMultiplier = 0;
	float normalizedCarPosition = 0;

	int activeCars = 0;
	float[][] carCoordinates = new float[60][3];
	int[] carID = new int[60];
	int playerCarID = 0;
	float penaltyTime = 0;
	int flag = AC_FLAG_TYPE.AC_NO_FLAG;
	PenaltyShortcut penalty = PenaltyShortcut.None;
	int idealLineOn = 0;
	int isInPitLane = 0;

	float surfaceGrip = 0;
	int mandatoryPitDone = 0;

	float windSpeed = 0;
	float windDirection = 0;

	int isSetupMenuVisible = 0;

	int mainDisplayIndex = 0;
	int secondaryDisplayIndex = 0;
	int TC = 0;
	int TCCut = 0;
	int EngineMap = 0;
	int ABS = 0;
	int fuelXLap = 0;
	int rainLights = 0;
	int flashingLights = 0;
	int lightsStage = 0;
	float exhaustTemperature = 0.0f;
	int wiperLV = 0;
	int DriverStintTotalTimeLeft = 0;
	int DriverStintTimeLeft = 0;
	int rainTyres = 0;
	
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
