package rhelper;

public final class StringUtils {

	private StringUtils() {
	}

	public static boolean isEmpty(String dateStr) {
		return dateStr == null || dateStr.length() <= 0;
	}
}
