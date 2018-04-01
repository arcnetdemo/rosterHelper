/**
 * 
 */
package rhelper;

import java.util.Calendar;

import rhelper.dto.RosterHeaderInfo;
import rhelper.dto.RosterInfo;
import rhelper.dto.RosterRowInfo;

/**
 * @author akira
 *
 */
public class TestDataGenerator {

	public RosterInfo execute(RosterInfo info) {
		
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2014, 11, 1);

		RosterHeaderInfo header = info.getHeader();

		header.setName("テスト名称");
		header.setYear(cal.get(Calendar.YEAR));
		header.setMonth(cal.get(Calendar.MONTH) + 1);

		int weekDays = 0;
		int workMinutes = 7*60+45;
		int extraMinutes = 0;
		int intervalMinutes = 45;

		int month = cal.get(Calendar.MONTH);
		for (;month == cal.get(Calendar.MONTH); cal.add(Calendar.DATE, 1)) {
			RosterRowInfo row = new RosterRowInfo();
			row.setDate(cal.get(Calendar.DATE));
			row.setDetails(null);
			row.setRemarks(null);
			if (isWeekDay(cal)) {
				weekDays ++;
				row.setPresent(true);
				row.setStartOnMinute(9*60+0);
				row.setEndOnMinute(17*60+45);
				row.setWorkMinutes(workMinutes);
				row.setExtraMinutes(extraMinutes);
				row.setIntervalMinutes(intervalMinutes);
			}
			info.getList().add(row);
		}
		header.setSumOfWork(workMinutes * weekDays);
		header.setSumOfExtra(extraMinutes * weekDays);
		header.setSumOfInterval(intervalMinutes * weekDays);
		
		return info;
	}

	private boolean isWeekDay(Calendar cal) {
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SATURDAY
				|| dayOfWeek == Calendar.SUNDAY) {
			return false;
		}
		return true;
	}
}
