# acc-manager (work in progress)
## Java Spring REST Server for Assetto Corsa Competizione ##
**Using Named Shared Memory - only Windows Version**

It is used to download the data described in the "ACC Shared Memory" documentation. At the moment, only the part related to SPageFileStatic is partly implemented. The rest is work in progress.
http://x.x.x.x:8080/SPageFileStatic
Response:
```json
{
  "smVersion": "",
  "acVersion": "",
  "numberOfSessions": 0,
  "numCars": 0,
  "carModel": "",
  "track": "",
  "playerName": null,
  "playerSurname": null,
  "playerNick": null,
  "sectorCount": 0,
  "maxTorque": 0.0,
  "maxPower": 0.0,
  "maxRpm": 0,
  "maxFuel": 0.0,
  "suspensionMaxTravel": null,
  "tyreRadius": null,
  "maxTurboBoost": 0.0,
  "deprecated_1": 0.0,
  "deprecated_2": 0.0,
  "penaltiesEnabled": 0,
  "aidFuelRate": 0.0,
  "aidTireRate": 0.0,
  "aidMechanicalDamage": 0.0,
  "aidAllowTyreBlankets": 0,
  "aidStability": 0.0,
  "aidAutoClutch": 0,
  "aidAutoBlip": 0,
  "hasDRS": 0,
  "hasERS": 0,
  "hasKERS": 0,
  "kersMaxJ": 0.0,
  "engineBrakeSettingsCount": 0,
  "ersPowerControllerCount": 0,
  "trackSPlineLength": 0.0,
  "trackConfiguration": null,
  "ersMaxJ": 0.0,
  "isTimedRace": 0,
  "hasExtraLap": 0,
  "carSkin": null,
  "reversedGridPositions": 0,
  "PitWindowStart": 0,
  "PitWindowEnd": 0,
  "isOnline": 111
}
```

Additionally, you can simulate sending a key press on the keyboard. This will be used to control the car settings:
http://x.x.x.x:8080/send?key=A
or
http://x.x.x.x:8080/send?string=VOLUME_UP
