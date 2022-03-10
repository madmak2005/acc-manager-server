package ACC.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ApplicationInfo {
	@Getter @Setter private String applicationName;
	@Getter @Setter private String buildVersion;
	@Getter @Setter private String buildTimestamp;
	
	public ApplicationInfo(String applicationName, String buildVersion, String buildTimestamp) {
		this.applicationName = applicationName;
		this.buildVersion = buildVersion;
		this.buildTimestamp = buildTimestamp;
	}
}
