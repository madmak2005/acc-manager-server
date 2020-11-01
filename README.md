# acc-manager (work in progress)
## Getting data from Assetto Corsa Competizione using Java Spring REST Server##
**ACC Named Shared Memory - only Windows Version**

It is used to download the data described in the "ACC Shared Memory" documentation. At the moment, only the part related to SPageFileStatic is partly implemented. The rest is work in progress.
http://x.x.x.x:8080/SPageFilePhysics [real data]
Response:
```json
{
  "packetId": 55300,
  "gas": 0,
  "brake": 0,
  "fuel": 10,
  "gear": 1,
  "rpms": 1315,
  "steerAngle": 0,
  "speedKmh": 0.001170993,
  "velocity": [
    -0.00024166811,
    0.000017340517,
    0.0003201305
  ],
  "accG": [
    0,
    0,
    0
  ],
  "wheelSlip": [
    0.05579049,
    0.045005016,
    0.04958255,
    0.036383506
  ],
  "wheelsPressure": [
    21.20558,
    21.20551,
    21.899912,
    21.900398
  ],
  "wheelAngularSpeed": [
    0,
    0,
    0,
    0
  ],
  "tyreCoreTemperature": [
    39.960087,
    39.95936,
    39.901043,
    39.906097
  ],
  "suspensionTravel": [
    0.016595084,
    0.01615412,
    0.019634157,
    0.019075776
  ],
  "tc": 0,
  "heading": 1.1479217,
  "pitch": -0.019962506,
  "roll": 0.033418566,
  "carDamage": [
    0,
    0,
    0,
    0,
    0
  ],
  "pitLimiterOn": 0,
  "abs": 0,
  "autoShifterOn": 0,
  "turboBoost": 0,
  "airTemp": 15.897041,
  "roadTemp": 21.816765,
  "localAngularVel": [
    -0.000027334961,
    0.000061373205,
    0.00014267705
  ],
  "finalFF": 0.004926979,
  "brakeTemp": [
    307.40875,
    307.40875,
    285.42386,
    285.42313
  ],
  "clutch": 0,
  "isAIControlled": 0,
  "tyreContactPoint": [
    [
      2051.675,
      476.65167,
      -1456.0654
    ],
    [
      2050.9983,
      476.70593,
      -1457.5658
    ],
    [
      2054.2393,
      476.6718,
      -1457.2177
    ],
    [
      2053.5605,
      476.72623,
      -1458.7233
    ]
  ],
  "tyreContactNormal": [
    [
      0.0071609626,
      0.99944085,
      0.032660615
    ],
    [
      0.0066271615,
      0.99940425,
      0.033871476
    ],
    [
      0.0071533187,
      0.99941796,
      0.033355545
    ],
    [
      0.0060230726,
      0.9995034,
      0.030930305
    ]
  ],
  "tyreContactHeading": [
    [
      0.91015184,
      0.0070148883,
      -0.41421542
    ],
    [
      0.9132388,
      0.0077500143,
      -0.40735096
    ],
    [
      0.91266114,
      0.007106499,
      -0.40865526
    ],
    [
      0.91079044,
      0.0072860355,
      -0.41280466
    ]
  ],
  "brakeBias": 0.79099995,
  "localVelocity": [
    0.00019183304,
    0.00003078532,
    0.00035134036
  ],
  "mz": [
    0,
    0,
    0,
    0
  ],
  "fx": [
    0,
    0,
    0,
    0
  ],
  "fy": [
    0,
    0,
    0,
    0
  ]
}
```

Additionally, you can simulate sending a key press on the keyboard. This will be used to control the car settings:
http://x.x.x.x:8080/send?key=A
or
http://x.x.x.x:8080/send?string=VOLUME_UP
