package rhelper;

import static rhelper.StringUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;

import lombok.Setter;
import rhelper.dto.RosterHeaderInfo;
import rhelper.dto.RosterInfo;
import rhelper.dto.RosterRowInfo;
import rhelper.util.ExcelUtility;
import rhelper.util.FormatUtility;

public class HhsXlsReader {

	private static final int START_ROW = 27;

	// 開始列
	private static final int COLUMN_1ST = 0;
	private static final int COLUMN_2ND = 34;

	// 日数
	private static final int DAYS_OF_1ST = 15;
	private static final int DAYS_OF_2ND = 16;

	// 1日あたりの行数
	private static final int NUM_ROWS_PER_DAY = 5;

	@Setter
	private InputStream is;

	private ExcelUtility excelUtil;

	private FormatUtility formatUtil;

	public RosterInfo acquireInfo() throws InvalidFormatException, IOException,
			ParseException {

		assert is != null;

		RosterInfo info = new RosterInfo();

		Sheet sheet = excelUtil.acquireSheet(is);

		// ヘッダ情報取得
		acquireHeaderInfo(info.getHeader(), sheet);

		// データ情報取得（1列目）
		acquireDataColumn(info.getRowList(), info.getHeader(), sheet,
				COLUMN_1ST, START_ROW, DAYS_OF_1ST);

		// データ情報取得（2列目）
		acquireDataColumn(info.getRowList(), info.getHeader(), sheet,
				COLUMN_2ND, START_ROW, DAYS_OF_2ND);

		// データ情報からヘッダ情報更新
		updateHeaderInfo(info.getHeader(), info.getRowList());

		return info;
	}

	private void acquireHeaderInfo(RosterHeaderInfo header, Sheet sheet)
			throws InvalidFormatException, IOException, ParseException {

		assert sheet != null;

		// 名前
		header.setName(excelUtil.acquireStringValue(sheet, "I12"));

		// 年
		header.setYear(Integer.parseInt(excelUtil.acquireStringValue(sheet,
				"Q3")));

		// 月
		header.setMonth(Integer.parseInt(excelUtil.acquireStringValue(sheet,
				"X3")));

		// 通常勤務時間（分）
		header.setNormalWorks(formatUtil.getMinutes(excelUtil
				.acquireStringValue(sheet, "AF9")));

		// 通常勤務開始時間（分）
		header.setNormalStart(formatUtil.getMinutes(excelUtil
				.acquireStringValue(sheet, "AH9")));

		// 通常勤務終了時間（分）
		header.setNormalEnd(formatUtil.getMinutes(excelUtil.acquireStringValue(
				sheet, "AK9")));

		// 残業時間合計（分）
		header.setSumOfExtra(excelUtil.acquireMinutesFromHours(sheet, "AE21"));

		// 勤務時間合計（分）
		header.setSumOfWork(excelUtil.acquireMinutesFromHours(sheet, "AY21"));

		// 休憩時間合計（分）
		// header.setSumOfInterval(0);
	}

	private void updateHeaderInfo(RosterHeaderInfo header,
			List<RosterRowInfo> list) {

		int interval = 0;
		for (RosterRowInfo row : list) {
			interval += row.getIntervalMinutes();
		}

		// 休憩時間合計（分）
		header.setSumOfInterval(interval);
	}

	private void acquireDataColumn(List<RosterRowInfo> list,
			RosterHeaderInfo header, Sheet sheet, int column, int startRow,
			int num) throws ParseException {

		assert sheet != null;

		int row = startRow;
		for (int idx = 0; idx < num; idx++) {
			RosterRowInfo e = acquireDataInfo(header, sheet, column, row);
			if (e != null) {
				list.add(e);
			}
			row += NUM_ROWS_PER_DAY;
		}
	}

	private RosterRowInfo acquireDataInfo(RosterHeaderInfo header, Sheet sheet,
			int column, int row) throws ParseException {

		assert sheet != null;

		RosterRowInfo rowInfo = new RosterRowInfo();
		// 日
		String dateStr = excelUtil.acquireStringValue(sheet, row + 1, column);
		if (isEmpty(dateStr)) {
			// データなし
			return null;
		}
		rowInfo.setDate(Integer.parseInt(dateStr));

		// 出勤フラグ
		String sign = excelUtil.acquireStringValue(sheet, row, column + 2);
		if (isEmpty(sign)) {
			// 出勤無、理由以降のデータ取得不要
			rowInfo.setDetails(excelUtil.acquireStringValue(sheet, row,
					column + 7));
			return rowInfo;
		}
		rowInfo.setPresent(true);

		// デフォルトをヘッダから取得
		String details = "";
		int start = header.getNormalStart();
		int end = header.getNormalEnd();
		int workMinutes = header.getNormalWorks();
		int extraMinutes = 0;

		// 勤休情報等取得
		for (int idx = 0; idx < NUM_ROWS_PER_DAY; idx++) {
			String kin = excelUtil.acquireStringValue(sheet, row + idx,
					column + 3);

			switch (kin) {
			case "欠勤":
			case "特休":
			case "シフト休":
				// 出勤無、以降のデータ取得不要
				details = excelUtil.acquireStringValue(sheet, row + idx,
						column + 7);
				return rowInfo;

			case "休出":
				// 休日出勤は理由・開始・終了時間をそのまま差し替え
				details = excelUtil.acquireStringValue(sheet, row + idx,
						column + 7);

				// 開始時間（分）
				start = formatUtil.getMinutes(excelUtil.acquireStringValue(
						sheet, row + idx, column + 15));

				// 終了時間（分）
				end = formatUtil.getMinutes(excelUtil.acquireStringValue(sheet,
						row + idx, column + 18));
				break;

			case "半欠勤":
			case "遅刻":
			case "早退":
			case "私外出":
			case "＜交＞遅刻":
			case "＜交＞早退":
				// 理由差し替え
				details = excelUtil.acquireStringValue(sheet, row + idx,
						column + 7);

				// 開始時間
				int workStart = formatUtil.getMinutes(excelUtil
						.acquireStringValue(sheet, row + idx, column + 20));
				// 終了時間
				int workEnd = formatUtil.getMinutes(excelUtil
						.acquireStringValue(sheet, row + idx, column + 23));
				if (workStart == start) {
					start = workEnd;
				}
				if (workEnd == end) {
					end = workStart;
				}
				break;

			case "":
			case "出張":
			case "直行":
			case "直帰":
				// 理由なしの場合は定時内勤務
				String workDetails = excelUtil.acquireStringValue(sheet, row
						+ idx, column + 7);
				if (isEmpty(workDetails)) {
					continue;
				}

				// 勤休なし＋所定外時間あり＝残業
				// 終了時間
				String endValue = excelUtil.acquireStringValue(sheet,
						row + idx, column + 18);
				if (isEmpty(endValue)) {
					continue;
				}
				end = formatUtil.getMinutes(endValue);
				break;

			default:
				break;
			}
		}

		// 内容
		rowInfo.setDetails(details);

		// 通常勤務開始時間（分）
		rowInfo.setStartOnMinute(start);

		// 通常勤務終了時間（分）
		rowInfo.setEndOnMinute(end);
		// 午前の場合は翌日の時刻
		if (end <= 9 * 60) {
			end += 24 * 60;
		}

		// 稼働時間（分）
		String work = excelUtil.acquireStringValue(sheet, row, column + 25);
		if (work != null && work.length() > 0) {
			workMinutes = formatUtil.getMinutes(work);
			rowInfo.setWorkMinutes(workMinutes);
		}

		// 残業時間（分）
		String extra = excelUtil.acquireStringValue(sheet, row, column + 28);
		if (extra != null && extra.length() > 0) {
			extraMinutes = formatUtil.getMinutes(extra);
			rowInfo.setExtraMinutes(extraMinutes);
		}

		// 休憩時間（分）
		int intervalMinutes = end - start - workMinutes;
		rowInfo.setIntervalMinutes(intervalMinutes);

		if (end < start || extraMinutes > workMinutes || intervalMinutes < 0) {
			throw new ParseException(dateStr + "日に入力された時間が異常です", 0);
		}

		rowInfo.setRemarks("");

		return rowInfo;
	}

	public HhsXlsReader() {

		super();

		// DI(=Autowired)対象
		excelUtil = new ExcelUtility();
		formatUtil = new FormatUtility();
	}
}
