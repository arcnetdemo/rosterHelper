package rhelper.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import rhelper.StringUtils;

//import org.springframework.stereotype.Component;

//@Component
public class FormatUtility {

	public FormatUtility() {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(1899, 11, 31); // 1900年1月0日 = 1899年12月31日
		baseDate = cal.getTimeInMillis();
	}

	private long baseDate;

//	public int getMinutes(Date dateValue) {
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(dateValue);
//		return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
//	}

	public int getMinutes(Date dateValue) {
		if (dateValue == null) {
			return 0;
		}
		long millisec = dateValue.getTime() - baseDate;
		long sec = millisec / 1000L;
		return (int) (sec / 60);
	}

	public int getMinutes(String strValue) throws ParseException {
		if (StringUtils.isEmpty(strValue)) {
			return 0;
		}
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		cal.setTime(format.parse(strValue));
		return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
	}

}
