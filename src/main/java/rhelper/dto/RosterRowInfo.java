/**
 * 
 */
package rhelper.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 勤務各日情報クラス(POJO)
 * 
 * @author akira
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class RosterRowInfo {

	/** 日 */
	private int date;

	/** 内容 */
	private String details;

	/** 出勤フラグ */
	private boolean present;

	/** 開始時刻（分） */
	private int startOnMinute;

	/** 終了時刻（分） */
	private int endOnMinute;

	/** 稼働時間（分） */
	private int workMinutes;

	/** 残業時間（分） */
	private int extraMinutes;

	/** 休憩時間（分） */
	private int intervalMinutes;

	/** 備考 */
	private String remarks;
}
