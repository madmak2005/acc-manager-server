package ACC;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestControler {
	ACCSharedMemory sh = new ACCSharedMemory();
	
	@GetMapping("/SPageFileStatic")
	public String getStaticJson() {
		return sh.getPageFileStatic().toJSON();
	}
	
	@GetMapping("/SPageFilePhysics")
	public String getPhysicsJson() {
		return sh.getPageFilePhysics().toJSON();
	}
}
