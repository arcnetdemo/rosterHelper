/**
 *
 */
package rhelper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Before;
import org.junit.Test;

import rhelper.dto.RosterInfo;

/**
 * @author akira
 *
 */
public class RosterCsvReaderTest {

	private RosterCsvReader csvReader;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		csvReader = new RosterCsvReader();
	}

	@Test
	public void test() throws Exception {

		// 出力先
		Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream("csv.csv"), "UTF-8"));

		csvReader.setReader(reader);
		RosterInfo info = csvReader.execute();
		// dummy
		info.notify();
		System.out.println("CSVファイルの読み込みが完了しました！");
	}

}
