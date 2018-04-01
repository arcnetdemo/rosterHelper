package rhelper;

import static rhelper.StringUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;

import lombok.Setter;
import rhelper.dto.RosterHeaderInfo;
import rhelper.dto.RosterInfo;
import rhelper.dto.RosterRowInfo;
import rhelper.util.ExcelUtility;
import rhelper.util.FormatUtility;

public class LysitheaXlsReader {

	private static final int NUM_ROWS_PER_DAY = 1;

	private static final int START_ROW = 15;

	private static final int ROWS_OF_PROCESS = 124;

	@Setter
	private InputStream is;

	private ExcelUtility excelUtil;

	private FormatUtility formatUtil;

	private RosterRowInfo ankerInfo = new RosterRowInfo();

	private BigDecimal workMinutes = BigDecimal.ZERO;

	private BigDecimal outOfWorkMinutes = BigDecimal.ZERO;

	public RosterInfo acquireInfo() throws InvalidFormatException, IOException, ParseException {

		assert is != null;

		RosterInfo info = new RosterInfo();

		Sheet sheet = excelUtil.acquireSheet(is);

		// ヘッダ情報取得
		acquireHeaderInfo(info.getHeader(), sheet);

		// データ情報取得（1列目）
		acquireDataColumn(info.getRowList(), info.getHeader(), sheet);

		// データ情報からヘッダ情報チェック
		checkHeaderInfo(info.getHeader(), info.getRowList());

		// experimental
		if (this.workMinutes.intValue() > 0 || this.outOfWorkMinutes.intValue() > 0) {
			System.out.println("寄与率： " + this.workMinutes.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN)
					.divide(workMinutes.add(outOfWorkMinutes), 2, 0));
		}

		return info;
	}

	private void acquireHeaderInfo(RosterHeaderInfo header, Sheet sheet) throws ParseException {

		assert sheet != null;

		// チェック
		if (!"勤休日次一覧".equals(excelUtil.acquireStringValue(sheet, "B1"))) {
			throw new ParseException("フォーマットが一致しない", 0);
		}

		// 名前
		header.setName(excelUtil.acquireStringValue(sheet, "E7"));

		Calendar cal = excelUtil.acquireCalendarValue(sheet, "C5");

		// 年
		header.setYear(cal.get(Calendar.YEAR));

		// 月
		header.setMonth(cal.get(Calendar.MONTH) + 1);

		// 通常勤務時間（分）
		header.setNormalWorks(7 * 60 + 45);

		// 通常勤務開始時間（分）
		header.setNormalStart(9 * 60 + 0);

		// 通常勤務終了時間（分）
		header.setNormalEnd(17 * 60 + 30);

		// 残業時間合計（分）
		header.setSumOfExtra(formatUtil.getMinutes(excelUtil.acquireDateValue(sheet, "M15")));

		// 勤務時間合計（分）
		header.setSumOfWork(formatUtil.getMinutes(excelUtil.acquireDateValue(sheet, "C15")));

		// 休憩時間合計（分）(不稼働時間を引く)
		header.setSumOfInterval(formatUtil.getMinutes(excelUtil.acquireDateValue(sheet, "W15"))
				- formatUtil.getMinutes(excelUtil.acquireDateValue(sheet, "V15")));
	}

	private void acquireDataColumn(List<RosterRowInfo> list, RosterHeaderInfo header, Sheet sheet)
			throws ParseException {

		assert sheet != null;

		int row = START_ROW;
		for (int idx = 0; idx < ROWS_OF_PROCESS; idx++) {
			RosterRowInfo e = acquireDataInfo(header, sheet, row);
			if (e == ankerInfo) {
				break;
			}
			if (e != null) {
				list.add(e);
			}
			row += NUM_ROWS_PER_DAY;
		}
	}

	private RosterRowInfo acquireDataInfo(RosterHeaderInfo header, Sheet sheet, int row) throws ParseException {

		assert sheet != null;

		RosterRowInfo rowInfo = new RosterRowInfo();
		// 日
		String dateStr = excelUtil.acquireStringValue(sheet, row, 1);
		if ("合計".equals(dateStr) || "日付".equals(dateStr)) {
			// データなし
			return ankerInfo;
		}

		// experimental（作業寄与率算出）
		String workStr = excelUtil.acquireStringValue(sheet, row, 5);

		if (workStr != null && !workStr.isEmpty()) {
			int workMin = formatUtil.getMinutes(excelUtil.acquireDateValue(sheet, row, 4));
			if (workStr.contains("SKBNGAI：")) {
				this.outOfWorkMinutes = this.outOfWorkMinutes.add(new BigDecimal(workMin));
			} else {
				this.workMinutes = this.workMinutes.add(new BigDecimal(workMin));
			}
		}

		if (isEmpty(dateStr)) {
			// データなし
			return null;
		}
		rowInfo.setDate(Integer.parseInt(dateStr.substring(3, 5)));

		// // 出勤フラグ
		// String sign = excelUtil.acquireStringValue(sheet, row, 0);
		// if (isEmpty(sign)) {
		// // 出勤無、以降のデータ取得不要
		// rowInfo.setDetails("");
		// return rowInfo;
		// }

		// デフォルトをヘッダから取得
		String details = "";
		int start = header.getNormalStart();
		int end = header.getNormalEnd();
		int workMinutes = header.getNormalWorks();
		int extraMinutes = 0;

		String kin = excelUtil.acquireStringValue(sheet, row, 9);

		switch (kin) {
		case "週休日":
		case "日曜休日":
			kin = "";
			// fail-through
		case "祝祭日":
		case "振替日曜休日":
		case "振替祝祭日":
		case "創立記念日":
		case "特別休日":
		case "年末年始":
		case "メーデー":
		case "全日休業":
		case "契約外作業":
		case "自社作業":
		case "代休・徹休":
		case "その他不稼動":
		case "一斉休日":
		case "振替休日":
			// 休日出勤の考慮
			Date startDate = excelUtil.acquireDateValue(sheet, row, 10);
			Date endDate = excelUtil.acquireDateValue(sheet, row, 11);
			if (startDate == null || endDate == null) {
				// 出勤無、以降のデータ取得不要
				rowInfo.setDetails(kin);
				return rowInfo;
			}

			details = "休日出勤";
			// fail-through
		case "午前半休":
		case "午後半休":
			if (isEmpty(details)) {
				details = kin;
			}
			// fail-through
		case "":
			if (isEmpty(details)) {
				// とりあえず作番を埋める
				details = excelUtil.acquireStringValue(sheet, row, 5);
			}

			startDate = excelUtil.acquireDateValue(sheet, row, 10);
			endDate = excelUtil.acquireDateValue(sheet, row, 11);
			if (startDate == null || endDate == null) {
				// 出勤無、以降のデータ取得不要
				rowInfo.setDetails(kin);
				return rowInfo;
			}
			// 出勤は理由・開始・終了時間をそのまま差し替え
			// 開始時間（分）
			start = formatUtil.getMinutes(startDate);

			// 終了時間（分）
			end = formatUtil.getMinutes(endDate);
			break;

		default:
			throw new ParseException(dateStr + "日に入力された勤休区分「" + kin + "」が判断できません", 0);
		}

		// 午前の場合は翌日の時刻
		if (end <= 9 * 60) {
			end += 24 * 60;
		}

		// 稼働時間（分）
		Date workValue = excelUtil.acquireDateValue(sheet, row, 2);
		if (workValue != null) {
			workMinutes = formatUtil.getMinutes(workValue);
		}

		// 残業時間（分）
		Date extraValue = excelUtil.acquireDateValue(sheet, row, 12);
		if (extraValue != null) {
			extraMinutes = formatUtil.getMinutes(extraValue);
		}

		// 休憩時間（分）
		int intervalMinutes = 0;
		Date intervalValue = excelUtil.acquireDateValue(sheet, row, 22);
		if (intervalValue != null) {
			intervalMinutes = formatUtil.getMinutes(intervalValue);
		}

		// 不稼働時間（分）
		int noWorkMinutes = 0;
		Date noWorkValue = excelUtil.acquireDateValue(sheet, row, 21);
		if (noWorkValue != null) {
			noWorkMinutes = formatUtil.getMinutes(noWorkValue);
			// FIXME 暫定対応
			// *** 不稼動を遅刻として扱うならこちらを有効化
			// details = "遅刻計算に注意！";
			// start += noWorkMinutes;
			details = "早退計算に注意！";
			end -= noWorkMinutes;
			intervalMinutes -= noWorkMinutes;
		}

		// 解析結果のチェック
		if (end < start || extraMinutes > workMinutes || intervalMinutes < 0) {
			System.err.println("start:" + start);
			System.err.println("  end:" + end);
			System.err.println(" work:" + workMinutes);
			System.err.println("  int:" + intervalMinutes);
			throw new ParseException(dateStr + "日に入力された時間が異常です", 0);
		}

		if (intervalMinutes != (end - start - workMinutes)) {
			System.err.println("start:" + start);
			System.err.println("  end:" + end);
			System.err.println(" work:" + workMinutes);
			System.err.println("  int:" + intervalMinutes);
			throw new ParseException(dateStr + "日に入力された時間が異常です", 0);
		}

		// 出勤印
		rowInfo.setPresent(true);

		// 内容
		rowInfo.setDetails(details);

		// 開始時間（分）
		rowInfo.setStartOnMinute(start);

		// 終了時間（分）
		rowInfo.setEndOnMinute(end);

		rowInfo.setWorkMinutes(workMinutes);
		rowInfo.setExtraMinutes(extraMinutes);
		rowInfo.setIntervalMinutes(intervalMinutes);

		rowInfo.setRemarks("");

		return rowInfo;
	}

	private void checkHeaderInfo(RosterHeaderInfo header, List<RosterRowInfo> list) throws ParseException {

		int work = 0;
		int interval = 0;
		int extra = 0;
		for (RosterRowInfo row : list) {
			work += row.getWorkMinutes();
			interval += row.getIntervalMinutes();
			extra += row.getExtraMinutes();
		}

		if (work != header.getSumOfWork()) {
			System.err.println("all:" + work);
			System.err.println("sum:" + header.getSumOfWork());
			throw new ParseException("作業時間が一致しません", 0);
		}

		if (interval != header.getSumOfInterval()) {
			System.err.println("all:" + interval);
			System.err.println("sum:" + header.getSumOfInterval());
			throw new ParseException("休憩時間が一致しません", 0);
		}

		if (extra != header.getSumOfExtra()) {
			System.err.println("all:" + extra);
			System.err.println("sum:" + header.getSumOfExtra());
			throw new ParseException("残業時間が一致しません", 0);
		}

	}

	public LysitheaXlsReader() {

		super();

		// DI(=Autowired)対象
		excelUtil = new ExcelUtility();
		formatUtil = new FormatUtility();
	}
}
