/**
 * 
 */
package rhelper;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import rhelper.dto.RosterHeaderInfo;
import rhelper.dto.RosterInfo;
import rhelper.dto.RosterRowInfo;
import lombok.Setter;

/**
 * @author akira
 *
 */
public class RosterExcelWriter {

	private static final DecimalFormat FORMAT_HOUR = new DecimalFormat("##");

	private static final DecimalFormat FORMAT_MINUTE = new DecimalFormat("00");

	private static final String WEEKDAY_STRING[] = { "日", "月", "火", "水", "木",
			"金", "土" };

	public double getDulationFromMinute(int onMinute) {
		return (double) onMinute / 60;
	}

	public String getTimeFromMinute(int onMinute) {
		int hour = onMinute / 60;
		int minute = onMinute % 60;

		return FORMAT_HOUR.format(hour) + ":" + FORMAT_MINUTE.format(minute);
	}

	public String getWeekDayStr(int date) {
		return WEEKDAY_STRING[date];
	}

	@Setter
	private RosterInfo info;

	@Setter
	private Sheet sheet;

	@Setter
	private String outputPath;

	// 曜日用スタイル保持
	private CellStyle[] cellStyleArray = new CellStyle[7];

	/**
	 * @throws Exception
	 */
	public void execute() throws Exception {

		checkTemplate();

		checkData();

		Calendar cal = Calendar.getInstance();
		RosterHeaderInfo header = info.getHeader();

		applyHeader(header);

		int rowIndex = 0;
		for (RosterRowInfo rowInfo : info.getList()) {
			applyRowInfo(header, rowInfo, rowIndex, cal);
			rowIndex++;
		}

		writeExcelFile();
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void writeExcelFile() throws Exception {
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(outputPath);
			sheet.getWorkbook().write(fileOut);
		} finally {
			if (fileOut != null) {
				fileOut.close();
			}
		}
	}

	/**
	 * @param header
	 * @param rowInfo
	 * @param rowIndex
	 * @param cal
	 */
	private void applyRowInfo(RosterHeaderInfo header, RosterRowInfo rowInfo,
			int rowIndex, Calendar cal) {
		Row row;
		Cell cell;

		cal.set(header.getYear(), header.getMonth() - 1, rowInfo.getDate());

		row = sheet.getRow(5 + rowIndex);

		// 月
		cell = row.getCell(1);
		cell.setCellFormula("$F$1");

		// 日付
		cell = row.getCell(2);
		cell.setCellValue(rowInfo.getDate());

		// 曜日
		int day = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
		cell = row.getCell(3);
		cell.setCellValue(getWeekDayStr(day));
		cell.setCellStyle(cellStyleArray[day]);

		// 作業内容
		cell = row.getCell(5);
		cell.setCellValue(rowInfo.getDetails());

		if (rowInfo.isPresent()) {
			// 開始時刻
			cell = row.getCell(7);
			cell.setCellValue(getTimeFromMinute(rowInfo.getStartOnMinute()));

			// 終了時刻
			cell = row.getCell(8);
			cell.setCellValue(getTimeFromMinute(rowInfo.getEndOnMinute()));

			// 休憩時間
			cell = row.getCell(9);
			cell.setCellValue(getDulationFromMinute(rowInfo
					.getIntervalMinutes()));

			// 実働時間
			cell = row.getCell(10);
			cell.setCellValue(getDulationFromMinute(rowInfo.getWorkMinutes()));
		}

		// 備考
		cell = row.getCell(11);
		cell.setCellValue(rowInfo.getRemarks());
	}

	/**
	 * 
	 */
	private void checkData() {

		int workMinutes = 0;
		int intervalMinutes = 0;
		int extraMinutes = 0;

		for (RosterRowInfo rowInfo : info.getList()) {
			workMinutes += rowInfo.getWorkMinutes();
			intervalMinutes += rowInfo.getIntervalMinutes();
			extraMinutes += rowInfo.getExtraMinutes();
		}

		RosterHeaderInfo header = info.getHeader();

		if (header.getSumOfWork() > 0 && header.getSumOfWork() != workMinutes) {
			System.out.println("総勤務時間が一致しません");
		}

		if (header.getSumOfInterval() > 0
				&& header.getSumOfInterval() != intervalMinutes) {
			System.out.println("総休憩時間が一致しません");
		}

		if (header.getSumOfExtra() > 0
				&& header.getSumOfExtra() != extraMinutes) {
			System.out.println("総残業時間が一致しません");
		}
	}

	/**
	 * @param header
	 */
	private void applyHeader(RosterHeaderInfo header) {

		Row row;
		Cell cell;

		// ヘッダデータ設定
		// 年月
		row = sheet.getRow(0);
		cell = row.getCell(1);
		cell.setCellValue(header.getYear());
		cell = row.getCell(5);
		cell.setCellValue(header.getMonth());

		// 名前
		row = sheet.getRow(2);
		cell = row.getCell(9);
		cell.setCellValue(header.getName());
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void checkTemplate() throws Exception {
		// テンプレートヘッダチェック
		Cell cell = sheet.getRow(1).getCell(9);
		if (!"㈲アークネット".equals(cell.getStringCellValue())) {
			throw new Exception("テンプレートが一致しません");
		}

		// 曜日のスタイル取得
		for (int idx = 0; idx < 7; idx++) {
			cell = sheet.getRow(5 + idx).getCell(3);
			cellStyleArray[idx] = cell.getCellStyle();
		}
	}
}
