/**
 * 
 */
package rhelper;

import java.io.IOException;
import java.io.Writer;

import lombok.Setter;
import rhelper.dto.RosterHeaderInfo;
import rhelper.dto.RosterInfo;
import rhelper.dto.RosterRowInfo;
import rhelper.util.EmptyFalseFormat;

import com.orangesignal.csv.CsvConfig;
import com.orangesignal.csv.CsvWriter;
import com.orangesignal.csv.bean.CsvColumnPositionMappingBeanTemplate;
import com.orangesignal.csv.io.CsvColumnPositionMappingBeanWriter;

/**
 * @author akira
 *
 */
public class RosterCsvWriter {

	@Setter
	private RosterInfo info;

	@Setter
	private Writer writer;

	private CsvConfig cfg;
	private CsvColumnPositionMappingBeanTemplate<RosterHeaderInfo> templateHeader;
	private CsvColumnPositionMappingBeanTemplate<RosterRowInfo> templateRow;

	/**
	 * 
	 */
	public RosterCsvWriter() {
		super();

		// 空行は読み飛ばす
		cfg = new CsvConfig();
		cfg.setIgnoreEmptyLines(true);

		// ヘッダ行読み込み用
		templateHeader = new CsvColumnPositionMappingBeanTemplate<RosterHeaderInfo>(
				RosterHeaderInfo.class);
		templateHeader.column("name").column("year").column("month")
				.column("normalWorks").column("normalStart")
				.column("normalEnd").column("sumOfExtra").column("sumOfWork")
				.column("sumOfInterval");

		templateRow = new CsvColumnPositionMappingBeanTemplate<RosterRowInfo>(
				RosterRowInfo.class);
		templateRow.column("date").column("details")
				.column("present", new EmptyFalseFormat()).column("startOnMinute")
				.column("endOnMinute").column("workMinutes")
				.column("extraMinutes").column("intervalMinutes");
	}

	/**
	 * @throws Exception
	 */
	public void execute() throws Exception {

		checkData();

		assert writer != null;
		assert cfg != null;

		CsvWriter csvWriter = new CsvWriter(writer, cfg);

		establishHeader(csvWriter);

		establishRow(csvWriter);
//		
//		CsvHandler<List<RosterHeaderInfo>> handler = new CsvEntityListHandler<RosterHeaderInfo>(RosterHeaderInfo.class);
//		List<RosterHeaderInfo> headerList = Arrays.asList(info.getHeader());
//		Csv.save(headerList, writer, cfg, handler);
	}

	/**
	 * @param csvWriter
	 */
	@SuppressWarnings("resource")
	private void establishHeader(CsvWriter csvWriter) throws IOException {

		assert templateHeader != null;

		CsvColumnPositionMappingBeanWriter<RosterHeaderInfo> saver = new CsvColumnPositionMappingBeanWriter<RosterHeaderInfo>(
				csvWriter, templateHeader, false);
		try {
			saver.write(info.getHeader());
		} catch (IOException e) {
			saver.close();
			throw e;
		}
	}

	/**
	 * @param csvWriter
	 * @throws IOException 
	 */
	private void establishRow(CsvWriter csvWriter) throws IOException {

		assert templateRow != null;

		try (CsvColumnPositionMappingBeanWriter<RosterRowInfo> saver = new CsvColumnPositionMappingBeanWriter<RosterRowInfo>(
				csvWriter, templateRow, false)) {
			for (RosterRowInfo row : info.getList()) {
				saver.write(row);
			}
		}
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

		if (header.getSumOfInterval() > 0 && header.getSumOfInterval() != intervalMinutes) {
			System.out.println("総休憩時間が一致しません");
		}

		if (header.getSumOfExtra() > 0 && header.getSumOfExtra() != extraMinutes) {
			System.out.println("総残業時間が一致しません");
		}
	}
}
