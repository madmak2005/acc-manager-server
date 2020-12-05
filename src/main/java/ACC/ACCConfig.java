package ACC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import ACC.acm.AutomaticCarManagementService;
import ACC.sharedmemory.ACCSharedMemoryService;


@Configuration
@ComponentScan("ACC")
public class ACCConfig {

	@Autowired
	private ACCSharedMemoryService accSharedMemoryService;
	
	@Bean 
	public ACCSharedMemoryService accSharedMemoryService() {
		return accSharedMemoryService;
	}
	
	@Autowired
	private AutomaticCarManagementService automaticCarManagementService;
	
	@Bean 
	public AutomaticCarManagementService automaticCarManagementService() {
		return automaticCarManagementService;
	}
}
