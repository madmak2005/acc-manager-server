package ACC.saving;

import ACC.model.PageFileStatistics;

public interface ACCDataSaveService {
	public String saveToXLS(PageFileStatistics pageFileStatistics);
	public boolean saveToGoogle(PageFileStatistics pageFileStatistics);
}