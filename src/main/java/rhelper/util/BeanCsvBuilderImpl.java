package rhelper.util;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class BeanCsvBuilderImpl<T> implements BeanCsvBuilder<T> {

	private static final String EMPTY = "";
	private static final String QUOTATION = "\"";
	private static final String SEPARATOR = ",";

	private static final Pattern QUOTATION_PATTERN = Pattern.compile(QUOTATION);
	private static final Pattern SEPARATOR_PATTERN = Pattern.compile(SEPARATOR);

	private static final DateFormat DATE_FORMATER = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	//private Field[] fields;
	private List<Field> fieldList;

	/**
	 * コンストラクタ
	 */
	public BeanCsvBuilderImpl(Class<T> clazz) {
		// フィールドリストの作成
		this.fieldList = getFieldList(clazz);
	}

	private List<Field> getFieldList(Class<T> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		List<Field> fieldList = new ArrayList<>(fields.length);
		for (Field field : fields) {
			/// for jacocoDebug
			if (field.getName().startsWith("$jacoco")) {
				// jacocoが生成するフィールドは飛ばす
				continue;
			}
			field.setAccessible(true);
			fieldList.add(field);
		}
		return fieldList;
	}

	/* (非 Javadoc)
	 * @see rhelper.util.BeanCsvConverter1#convert(T)
	 */
	@Override
	public String convert(T bean) {
		StringBuilder line = new StringBuilder();
		for (Field field : fieldList) {
			try {
				Object obj = field.get(bean);
				String value = escape(obj);
				line.append(value).append(SEPARATOR);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// 到達することはない
				e.printStackTrace();
			}
		}
		line.deleteCharAt(line.length() - 1);
		return line.toString();
	}

	/**
	 * エスケープ処理
	 *
	 * @param obj
	 *            値
	 * @return 文字列
	 */
	private String escape(Object obj) {
		if (obj == null) {
			return EMPTY;
		}
		String str = null;
		if (obj instanceof Date) {
			synchronized (DATE_FORMATER) {
				str = DATE_FORMATER.format(obj);
			}
		} else {
			str = obj.toString();
		}
		if (QUOTATION_PATTERN.matcher(str).find()) {
			str = str.replaceAll(QUOTATION, QUOTATION + QUOTATION);
		}
		if (SEPARATOR_PATTERN.matcher(str).find()) {
			str = QUOTATION + str + QUOTATION;
		}
		return str;
	}
}
