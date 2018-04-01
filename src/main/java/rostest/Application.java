package rostest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import rhelper.HhsXlsReader;
import rhelper.LysitheaXlsReader;
import rhelper.dto.RosterHeaderInfo;
import rhelper.dto.RosterInfo;
import rhelper.dto.RosterRowInfo;
import rhelper.util.BeanCsvBuilderImpl;
import rhelper.util.BeanCsvBuilder;

public class Application {

	private static final char CHAR_PERIOD = '.';
	private static final char CHAR_PATH_SEPARATOR1 = '\\';
	private static final char CHAR_PATH_SEPARATOR2 = '/';
	private static final int CSV_BUFFER_SIZE = 2048;
	private static final int CSV_OUTPUT_LINE = 32;
	private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
	private static final String LINE_SEPARATOR = "\r\n";
	private static final String BOM = "\ufeff";
	private static final String CSV_EXTENSION = ".csv";

	private LysitheaXlsReader lysitheaReader;
	private HhsXlsReader xlsReader;
	private BeanCsvBuilder<RosterHeaderInfo> headerConverter;
	private BeanCsvBuilder<RosterRowInfo> rowConverter;

	public static void main(String[] args) {

		try {
			checkArgs(args);
			new Application().execute(args[0]);
		} catch (ApplicationException e) {
			// デバッグ時は下記を有効に
			e.printStackTrace();
			System.err.println(e.getLocalizedMessage());
			System.exit(-1);
		}
	}

	private static void checkArgs(String[] args) throws ApplicationException {

		if (args.length != 1) {
			throw new ApplicationException("パラメータが異常です。");
		}
	}

	public Application() {

		super();

		// DI(=Autowired)対象
		lysitheaReader = new LysitheaXlsReader();
		xlsReader = new HhsXlsReader();
		headerConverter = new BeanCsvBuilderImpl<RosterHeaderInfo>(RosterHeaderInfo.class);
		rowConverter = new BeanCsvBuilderImpl<RosterRowInfo>(RosterRowInfo.class);
	}

	private void execute(String filePath) throws ApplicationException {

		// 入力ファイルチェック
		File inputFile = acquireInputFile(filePath);

		// 出力ファイルチェック
		File outputFile = acquireOutputFile(filePath);

		System.err.println("Output path: " + outputFile.getPath());
		System.err.println(" Input size: " + Long.toString(inputFile.length()));

		byte[] data = createCsvFileData(inputFile);

		System.err.println("Output size: " + Integer.toString(data.length));

		// ファイル出力
		writeFile(outputFile, data);
	}

	private byte[] createCsvFileData(File inputFile) throws ApplicationException {

		// excel→dto取得
		RosterInfo rosterInfo = acquireRosterInfo(inputFile);

		// dto→CSV
		List<String> csvList = acquireCsvList(rosterInfo);

		// 出力データ生成(文字コード変換等)
		return createRowData(csvList);
	}

	private void writeFile(File outputFile, byte[] data)
			throws ApplicationException {

		try (FileOutputStream fos = new FileOutputStream(outputFile)) {
			fos.write(data);
		} catch (IOException e) {
			throw new ApplicationException("ファイルに書き込めません。:"
					+ outputFile.getName(), e);
		}
	}

	private File acquireOutputFile(String filePath) throws ApplicationException {

		// 出力ファイルパス生成
		File outputFile = new File(getOutputCsvFilePath(filePath));
		if (!isAbleWrite(outputFile)) {
			throw new ApplicationException("ファイルを書き込みオープンできません。:"
					+ outputFile.getName());
		}
		return outputFile;
	}

	private File acquireInputFile(String filePath) throws ApplicationException {

		File inputFile = new File(filePath);
		if (!isAbleRead(inputFile)) {
			throw new ApplicationException("ファイルを読み込みオープンできません。:"
					+ inputFile.getName());
		}
		return inputFile;
	}

	private byte[] createRowData(List<String> csvList) throws ApplicationException {

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(CSV_BUFFER_SIZE)) {
			writeCsvRowData(bos, csvList);
			return bos.toByteArray();
		} catch (IOException e) {
			// 未到達コード(ByteArrayOutputStreamの為)
			throw new ApplicationException("CSVデータの生成に失敗しました。", e);
		}
	}

	private RosterInfo acquireRosterInfo(File inputFile)
			throws ApplicationException {

		try (FileInputStream fis = new FileInputStream(inputFile)) {
			return acquireInfoFromXls(fis);
		} catch (Exception e) {
			// no operation.
		}

		try (FileInputStream fis = new FileInputStream(inputFile)) {
			return acquireInfoFromXls2(fis);
		} catch (InvalidFormatException | IOException | ParseException e) {
			throw new ApplicationException("Excelファイルが読み込めません。:"
					+ inputFile.getName(), e);
		}
	}

	private boolean isAbleRead(File inputFile) {
		return inputFile.exists() && inputFile.canRead();
	}

	private boolean isAbleWrite(File outputFile) {
		return !outputFile.exists() || outputFile.canWrite();
	}

	private void writeCsvRowData(OutputStream os, List<String> csvList) throws IOException {

		try (OutputStreamWriter osw = new OutputStreamWriter(os, CHARSET_UTF8)) {
			// BOM出力
			osw.append(BOM);
			writeCsv(osw, csvList);
		}
	}

	private void writeCsv(Writer writer, List<String> csvList)
			throws IOException {

		// リスト出力
		for (String list : csvList) {
			writer.append(list).append(LINE_SEPARATOR);
		}
	}

	private RosterInfo acquireInfoFromXls(InputStream is) throws IOException,
			InvalidFormatException, ParseException {

		RosterInfo acquireInfo;
		xlsReader.setIs(is);
		acquireInfo = xlsReader.acquireInfo();
		return acquireInfo;
	}

	private RosterInfo acquireInfoFromXls2(InputStream is) throws IOException,
			InvalidFormatException, ParseException {

		RosterInfo acquireInfo;
		lysitheaReader.setIs(is);
		acquireInfo = lysitheaReader.acquireInfo();
		return acquireInfo;
	}

	private List<String> acquireCsvList(RosterInfo rosterInfo) {

		List<String> list = new ArrayList<String>(CSV_OUTPUT_LINE);
		return addAllToList(list, rosterInfo);
	}

	private List<String> addAllToList(List<String> list, RosterInfo rosterInfo) {

		list.add(headerConverter.convert(rosterInfo.getHeader()));
		for (RosterRowInfo row : rosterInfo.getRowList()) {
			list.add(rowConverter.convert(row));
		}
		return list;
	}

	private String getOutputCsvFilePath(String filePath) {

		int extPoint = getExtensionPosision(filePath);
		return filePath.substring(0, extPoint) + CSV_EXTENSION;
	}

	private int getExtensionPosision(String filePath) {

		int lastPeriod = filePath.lastIndexOf(CHAR_PERIOD);
		int lastSeparator = Math.max(filePath.lastIndexOf(CHAR_PATH_SEPARATOR1),
				filePath.lastIndexOf(CHAR_PATH_SEPARATOR2));

		if (lastPeriod < lastSeparator || lastPeriod < 0) {
			return filePath.length();
		}
		return lastPeriod;
	}
}
