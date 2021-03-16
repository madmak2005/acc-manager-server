package ACC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Service;

import app.Application;

@Service
public class ApplicationPropertyService {
	
	@Autowired
	private ServerProperties serverProperties;
	
    public int getApplicationProperty(){
        return (Application.useDebug && Application.debug ? 50 : 100);
    }
    
    public int getStatisticsInterval(){
        return (Application.useDebug && Application.debug ? 50: 333);
    }
    
    public int getApplicationPort(){
        return serverProperties.getPort();
    }
}
