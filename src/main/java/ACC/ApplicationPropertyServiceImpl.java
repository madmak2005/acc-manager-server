package ACC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import app.Application;
import lombok.Data;

@ComponentScan("ACC")
@Service
public class ApplicationPropertyServiceImpl implements ApplicationPropertyService{
	
	private String sheetID = ""; 
	
	@Autowired
	private ServerProperties serverProperties;
	
	@Override
    public int getApplicationProperty(){
        return (Application.useDebug && Application.debug ? 10 : 100);
    }
    
	@Override
    public int getStatisticsInterval(){
        return (Application.useDebug && Application.debug ? 33: 333);
    }
    
	@Override
    public int getApplicationPort(){
        return serverProperties.getPort();
    }

	@Override
	public String getSheetID() {
		return sheetID;
	}

	@Override
	public void setSheetID(String sheetID) {
		this.sheetID = sheetID;
		
	}
    
    
}
