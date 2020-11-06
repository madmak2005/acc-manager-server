package virtualKeyboard;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import virtualKeyboard.model.KeyboardKey;

@RestController
public class RestVirtualKeyboardController {
	final int DOWN = 0;
	final int UP = 2;
	
	@GetMapping("/send")
	public String key(@RequestParam Map<String, String> allParams) {
		VirtualKeyboardAPI api = new VirtualKeyboardAPI();
		String key = allParams.get("key");
		int time = 0;
		if (key != null) {
			if (key.length() == 1) {
				if (allParams.get("time") != null) 
					time = Integer.valueOf(allParams.get("time"));
				KeyboardKey keyboardKey = KeyboardKey.getByCodename(key);
				if (Character.isUpperCase(key.charAt(0))) {
					api.sendText(KeyboardKey.RSHIFT, DOWN, 0);
					api.sendText(keyboardKey, DOWN, time);
					if (time > 0)
						try {
							TimeUnit.MILLISECONDS.sleep(time);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					api.sendText(keyboardKey, UP, 0);
					api.sendText(KeyboardKey.RSHIFT, UP, 0);
				} else {
					api.sendText(keyboardKey, DOWN, time);
					if (time > 0)
						try {
							TimeUnit.MILLISECONDS.sleep(time);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					api.sendText(keyboardKey, UP, 0);
				}
			}
		} else {
			String string = allParams.get("string");
			if (string != null) {
				if (string.equals("MEDIA_NEXT_TRACK")) {
					api.sendText(KeyboardKey.MEDIA_NEXT_TRACK, DOWN, 0);
					api.sendText(KeyboardKey.MEDIA_NEXT_TRACK, UP, 0);
				}
				if (string.equals("MEDIA_PREV_TRACK")) {
					api.sendText(KeyboardKey.MEDIA_PREV_TRACK, DOWN, 0);
					api.sendText(KeyboardKey.MEDIA_PREV_TRACK, UP, 0);
				}
				if (string.equals("MEDIA_STOP")) {
					api.sendText(KeyboardKey.MEDIA_STOP, DOWN, 0);
					api.sendText(KeyboardKey.MEDIA_STOP, UP, 0);
				}
				if (string.equals("MEDIA_PLAY_PAUSE")) {
					api.sendText(KeyboardKey.MEDIA_PLAY_PAUSE, DOWN, 0);
					api.sendText(KeyboardKey.MEDIA_PLAY_PAUSE, UP, 0);
				}
				if (string.equals("VOLUME_UP")) {
					api.sendText(KeyboardKey.VOLUME_UP, DOWN, 0);
					api.sendText(KeyboardKey.VOLUME_UP, UP, 0);
				}
				if (string.equals("VOLUME_DOWN")) {
					api.sendText(KeyboardKey.VOLUME_DOWN, DOWN, 0);
					api.sendText(KeyboardKey.VOLUME_DOWN, UP, 0);
				}
			}
		}
		return "OK";		
	}

	public String key(@RequestParam(value = "string") String string) {
		System.out.println("String");
		VirtualKeyboardAPI api = new VirtualKeyboardAPI();


		return "sent";
	}
}
