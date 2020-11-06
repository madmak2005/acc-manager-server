# acc-manager
This REST/ WebSocket server is used to download the data described in the "ACC Shared Memory" documentation. You need to have Assetto Corsa Competizione game to see the real data. 

## Java Version
Java 14 just because of new 'Switch' format usage

## How to start
1. Use Spring Tool Suite
2. Clone repository
3. Open project in Spring Tool Suite
4. Let maven download all dependencies
5. Lunch Assetto Corsa Competizione (the same computer)
6. Lunch project as Spring Boot in Spring Tool Suite

**Right now you need to first start ACC and enter the track then lunch the app**

## WebSockets
Just open in any websocket client. You can use online site http://www.websocket.org/echo.html (use http for ws:// because wss:// is not supported) or any websocket plugin avaiable for your web browser.
1. ws://x.x.x.x:8080/acc/graphics (refresh rate 333 ms)
2. ws://x.x.x.x:8080/acc/physics (refresh rate 333 ms)
3. ws://x.x.x.x:8080/acc/static (refresh rate 2000 ms)

## STOMP WebSockets
There is a built-in small html client using STOMP webockets to tranfer physics and graphics data every 500 ms. Static every 10 s. You can access it by lunching project and tring http://x.x.x.x:8080 in a web browser.
If you want to use STOMP in your client here you have some important information:
1. url: 'ws://x.x.x.x:8080/gs-websocket/websocket'
2. subscribe to: '/acc/graphics', '/acc/physics' or '/acc/static'

## REST
Just use GET method for the page you need:
1. http://x.x.x.x:8080/SPageFilePhysics 
2. http://x.x.x.x:8080/SPageFileGraphics 
3. http://x.x.x.x:8080/SPageFileStatis

Response example:
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

## Virtual Keyboard
Additionally, you can simulate sending a key pressed on the keyboard. This will be used to control the car settings:
http://x.x.x.x:8080/send?key=A
or
http://x.x.x.x:8080/send?string=VOLUME_UP

You need to use **/send?string=** for 'special' keys and **/send?key=** for single letters. You can use small and capital letters.

    CANCEL(3),
    BACK(8, "BACKSPACE"),
    TAB(9),
    CLEAR(12),
    RETURN(13, "ENTER"),
    SHIFT(16),
    CONTROL(17, "CTRL"),
    MENU(18, "ALT"),
    PAUSE(19),
    CAPITAL(20, "CAPSLOCK"),
    KANA(0x15),
    HANGEUL(0x15),
    HANGUL(0x15),
    JUNJA(0x17),
    FINAL(0x18),
    HANJA(0x19),
    KANJI(0x19),
    ESCAPE(0x1B, "ESC"),
    CONVERT(0x1C),
    NONCONVERT(0x1D),
    ACCEPT(0x1E),
    MODECHANGE(0x1F),
    SPACE(32, " "),
    PRIOR(33),
    NEXT(34),
    END(35),
    HOME(36),
    LEFT(37),
    UP(38),
    RIGHT(39),
    DOWN(40),
    SELECT(41),
    PRINT(42),
    EXECUTE(43),
    SNAPSHOT(44),
    INSERT(45),
    DELETE(46),
    HELP(47),

    NUM0(0x30, "0"),
    NUM1(0x31, "1"),
    NUM2(0x32, "2"),
    NUM3(0x33, "3"),
    NUM4(0x34, "4"),
    NUM5(0x35, "5"),
    NUM6(0x36, "6"),
    NUM7(0x37, "7"),
    NUM8(0x38, "8"),
    NUM9(0x39, "9"),

    A(0x41),
    B(0x42),
    C(0x43),
    D(0x44),
    E(0x45),
    F(0x46),
    G(0x47),
    H(0x48),
    I(0x49),
    J(0x4a),
    K(0x4b),
    L(0x4c),
    M(0x4d),
    N(0x4e),
    O(0x4f),
    P(0x50),
    Q(0x51),
    R(0x52),
    S(0x53),
    T(0x54),
    U(0x55),
    V(0x56),
    W(0x57),
    X(0x58),
    Y(0x59),
    Z(0x5a),

    LWIN(0x5B, "LEFT_WIN", "LEFT_WINDOWS"),
    RWIN(0x5C, "RIGHT_WIN", "RIGHT_WINDOWS"),
    APPS(0x5D),
    SLEEP(0x5F),
    NUMPAD0(0x60),
    NUMPAD1(0x61),
    NUMPAD2(0x62),
    NUMPAD3(0x63),
    NUMPAD4(0x64),
    NUMPAD5(0x65),
    NUMPAD6(0x66),
    NUMPAD7(0x67),
    NUMPAD8(0x68),
    NUMPAD9(0x69),
    MULTIPLY(0x6A),
    ADD(0x6B),
    SEPARATOR(0x6C),
    SUBTRACT(0x6D),
    DECIMAL(0x6E),
    DIVIDE(0x6F),
    F1(0x70),
    F2(0x71),
    F3(0x72),
    F4(0x73),
    F5(0x74),
    F6(0x75),
    F7(0x76),
    F8(0x77),
    F9(0x78),
    F10(0x79),
    F11(0x7A),
    F12(0x7B),
    F13(0x7C),
    F14(0x7D),
    F15(0x7E),
    F16(0x7F),
    F17(0x80),
    F18(0x81),
    F19(0x82),
    F20(0x83),
    F21(0x84),
    F22(0x85),
    F23(0x86),
    F24(0x87),
    NUMLOCK(0x90),
    SCROLL(0x91),
    LSHIFT(0xA0),
    RSHIFT(0xA1),
    LCONTROL(0xA2),
    RCONTROL(0xA3),
    LMENU(0xA4, "LEFT_ALT"),
    RMENU(0xA5, "RIGHT_ALT"),
    BROWSER_BACK(0xA6),
    BROWSER_FORWARD(0xA7),
    BROWSER_REFRESH(0xA8),
    BROWSER_STOP(0xA9),
    BROWSER_SEARCH(0xAA),
    BROWSER_FAVORITES(0xAB),
    BROWSER_HOME(0xAC),
    VOLUME_MUTE(0xAD),
    VOLUME_DOWN(0xAE),
    VOLUME_UP(0xAF),
    MEDIA_NEXT_TRACK(0xB0),
    MEDIA_PREV_TRACK(0xB1),
    MEDIA_STOP(0xB2),
    MEDIA_PLAY_PAUSE(0xB3),
    LAUNCH_MAIL(0xB4),
    LAUNCH_MEDIA_SELECT(0xB5),
    LAUNCH_APP1(0xB6),
    LAUNCH_APP2(0xB7),
    OEM_1(0xBA),
    OEM_PLUS(0xBB),
    OEM_COMMA(0xBC),
    OEM_MINUS(0xBD),
    OEM_PERIOD(0xBE),
    OEM_2(0xBF),
    OEM_3(0xC0),
    OEM_4(0xDB),
    OEM_5(0xDC),
    OEM_6(0xDD),
    OEM_7(0xDE),
    OEM_8(0xDF),
    OEM_102(0xE2),
    PROCESSKEY(0xE5),
    PACKET(0xE7),
    ATTN(0xF6),
    CRSEL(0xF7),
    EXSEL(0xF8),
    EREOF(0xF9),
    PLAY(0xFA),
    ZOOM(0xFB),
    NONAME(0xFC),
    PA1(0xFD),
    OEM_CLEAR(0xFE);
