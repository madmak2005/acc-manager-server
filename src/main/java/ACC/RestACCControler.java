package ACC;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestACCControler {
	ACCSharedMemory sh = new ACCSharedMemory();
	
	@GetMapping("/SPageFileStatic")
	public String getJson() {
		return sh.getSPageFileStatic().toJSON();
	}
}
