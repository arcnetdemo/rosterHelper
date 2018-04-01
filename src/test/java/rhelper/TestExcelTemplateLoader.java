/**
 * 
 */
package rhelper;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @author akira
 *
 */
public class TestExcelTemplateLoader {

	public Sheet execute(String xlsFilepath) throws Exception {
		InputStream in = new FileInputStream(xlsFilepath);
		Workbook wb = WorkbookFactory.create(in);
		Sheet sheet = wb.getSheetAt(0);

		return sheet;
	}
}
