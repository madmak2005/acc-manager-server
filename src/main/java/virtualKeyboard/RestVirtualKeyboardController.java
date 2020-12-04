package virtualKeyboard;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestVirtualKeyboardController {

	
	@GetMapping("/send")
	public String pressKey(@RequestParam Map<String, String> allParams) {
		VirtualKeyboardAPI api = new VirtualKeyboardAPI();
		return api.execute(allParams);
	}

	public String key(@RequestParam(value = "string") String string) {
		System.out.println("String");
		VirtualKeyboardAPI api = new VirtualKeyboardAPI();
		return "sent";
	}
}
