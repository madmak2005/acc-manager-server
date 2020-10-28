package virtualKeyboard;

import java.util.Map;

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
		
		if (key != null) {
			if (key.length() == 1) {
				KeyboardKey keyboardKey = KeyboardKey.getByCodename(key);
				if (Character.isUpperCase(key.charAt(0))) {
					api.sendText(KeyboardKey.RSHIFT, DOWN);
					api.sendText(keyboardKey, DOWN);
					api.sendText(keyboardKey, UP);
					api.sendText(KeyboardKey.RSHIFT, UP);
				} else {
					api.sendText(keyboardKey, DOWN);
					api.sendText(keyboardKey, UP);
				}
			}
		} else {
			String string = allParams.get("string");
			if (string != null) {
				if (string.equals("MEDIA_NEXT_TRACK")) {
					api.sendText(KeyboardKey.MEDIA_NEXT_TRACK, DOWN);
					api.sendText(KeyboardKey.MEDIA_NEXT_TRACK, UP);
				}
				if (string.equals("MEDIA_PREV_TRACK")) {
					api.sendText(KeyboardKey.MEDIA_PREV_TRACK, DOWN);
					api.sendText(KeyboardKey.MEDIA_PREV_TRACK, UP);
				}
				if (string.equals("MEDIA_STOP")) {
					api.sendText(KeyboardKey.MEDIA_STOP, DOWN);
					api.sendText(KeyboardKey.MEDIA_STOP, UP);
				}
				if (string.equals("MEDIA_PLAY_PAUSE")) {
					api.sendText(KeyboardKey.MEDIA_PLAY_PAUSE, DOWN);
					api.sendText(KeyboardKey.MEDIA_PLAY_PAUSE, UP);
				}
				if (string.equals("VOLUME_UP")) {
					api.sendText(KeyboardKey.VOLUME_UP, DOWN);
					api.sendText(KeyboardKey.VOLUME_UP, UP);
				}
				if (string.equals("VOLUME_DOWN")) {
					api.sendText(KeyboardKey.VOLUME_DOWN, DOWN);
					api.sendText(KeyboardKey.VOLUME_DOWN, UP);
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
