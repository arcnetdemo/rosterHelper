/**
 * 
 */
package rhelper.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 勤務情報クラス(POJO)
 * 
 * @author akira
 *
 */
public class RosterInfo {

	/**
	 * デフォルトレコード数（日数）
	 */
	private static final int DAYS = 31;

	@Getter
	@Setter
	private RosterHeaderInfo header;
	
	@Getter
	private List<RosterRowInfo> list;
	
	public RosterInfo() {
		header = new RosterHeaderInfo();
		list = new ArrayList<RosterRowInfo>(DAYS);
	}

}
