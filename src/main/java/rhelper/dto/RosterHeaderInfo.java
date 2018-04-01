/**
 * 
 */
package rhelper.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 勤務基本情報(POJO)
 * 
 * @author akira
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class RosterHeaderInfo {

	/** 名前 */
	private String name;

	/** 年 */
	private int year;

	/** 月 */
	private int month;

	/** 通常勤務時間（分） */
	/*(optional)*/
	private int normalWorks;

	/** 通常勤務開始時刻（分） */
	/*(optional)*/
	private int normalStart;

	/** 通常勤務終了時刻（分） */
	/* (optional) */
	private int normalEnd;

	/** 残業時間合計（分） */
	/* (optional) */
	private int sumOfExtra;

	/** 勤務時間合計（分） */
	/* (optional) */
	private int sumOfWork;

	/** 休憩時間合計（分） */
	/* (optional) */
	private int sumOfInterval;
}
