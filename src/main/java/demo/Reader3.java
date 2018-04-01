/**
 *
 */
package demo;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.orangesignal.csv.CsvConfig;
import com.orangesignal.csv.CsvReader;
import com.orangesignal.csv.bean.CsvColumnPositionMappingBeanTemplate;
import com.orangesignal.csv.io.CsvColumnPositionMappingBeanReader;

import lombok.Setter;
import rhelper.dto.RosterHeaderInfo;
import rhelper.dto.RosterInfo;
import rhelper.dto.RosterRowInfo;

/**
 * @author akira
 *
 */
public class Reader3 {

	@Setter
	private Reader reader;

	private CsvConfig cfg;
	private CsvColumnPositionMappingBeanTemplate<RosterHeaderInfo> templateHeader;
	private CsvColumnPositionMappingBeanTemplate<RosterRowInfo> templateRow;

	public RosterInfo read() throws IOException {
		assert reader != null;
		assert cfg != null;
		CsvReader csvReader = new CsvReader(reader, cfg);
		RosterHeaderInfo header = acquireHeader(csvReader);
		if (header == null) {
			return null;
		}
		RosterInfo info = new RosterInfo();
		info.setHeader(header);
		List<RosterRowInfo> list = acquireRowList(csvReader);
		if (list == null) {
			return null;
		}
		info.getList().addAll(list);
		return info;
	}

	private List<RosterRowInfo> acquireRowList(CsvReader csvReader)
			throws IOException {
		assert templateRow != null;
		try (CsvColumnPositionMappingBeanReader<RosterRowInfo> loader = new CsvColumnPositionMappingBeanReader<RosterRowInfo>(
				csvReader, templateRow)) {
			List<RosterRowInfo> list = new ArrayList<RosterRowInfo>();
			RosterRowInfo row;
			while ((row = loader.read()) != null) {
				list.add(row);
			}
			return list;
		} catch (IOException e) {
			throw new IOException(e);
		}
	}

	@SuppressWarnings("resource")
	private RosterHeaderInfo acquireHeader(CsvReader csvReader)
			throws IOException {
		assert templateHeader != null;
		CsvColumnPositionMappingBeanReader<RosterHeaderInfo> loader = new CsvColumnPositionMappingBeanReader<RosterHeaderInfo>(
				csvReader, templateHeader);
		try {
			return loader.read();
		} catch (IOException e) {
			try {
				loader.close();
			} catch (IOException e1) {// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new IOException(e);
		}
	}

	Reader3() {
		super();
		cfg = new CsvConfig();
		cfg.setIgnoreEmptyLines(true);
		templateHeader = new CsvColumnPositionMappingBeanTemplate<RosterHeaderInfo>(
				RosterHeaderInfo.class);
		templateHeader.column("name").column("year").column("month")
				.column("normalWorks").column("normalStart")
				.column("normalEnd").column("sumOfExtra").column("sumOfWork")
				.column("sumOfInterval");
		templateRow = new CsvColumnPositionMappingBeanTemplate<RosterRowInfo>(
				RosterRowInfo.class);
		templateRow.column("date").column("details")
				.column("present"/* TODO:, new PresentFormat()*/).column("startOnMinute")
				.column("endOnMinute").column("workMinutes")
				.column("extraMinutes").column("intervalMinutes");
	}
}