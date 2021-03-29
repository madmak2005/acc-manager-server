package ACC.saving;

import ACC.model.PageFileStatistics;

public interface ACCDataSaveService {
	public void saveToXLS(PageFileStatistics pageFileStatistics);
	public void saveToGoogle(PageFileStatistics pageFileStatistics, String spreadsheetId);
}