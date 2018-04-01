/**
 *
 */
package rhelper;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;

import rhelper.dto.RosterInfo;

/**
 * @author akira
 *
 */
public class RosterCsvWriterTest {

	private TestDataGenerator generator = new TestDataGenerator();
	private RosterCsvWriter csvWriter;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		csvWriter = new RosterCsvWriter();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Test
	public void test() throws Exception {

		// データ取得
		RosterInfo info = generator.execute(new RosterInfo());

		// 出力先
		Writer writer = new OutputStreamWriter(new FileOutputStream("csv.csv"), "UTF-8");

		csvWriter.setInfo(info);
		csvWriter.setWriter(writer);
		csvWriter.execute();
		System.out.println("CSVファイルの作成が完了しました！");
	}
}
