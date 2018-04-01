/**
 * 
 */
package rhelper.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author
 *
 */
public class RosterInfo {

	@Getter
	@Setter
	private RosterHeaderInfo header;

	@Getter
	private List<RosterRowInfo> rowList;

	public RosterInfo() {
		header = new RosterHeaderInfo();
		rowList = new ArrayList<RosterRowInfo>();
	}

}
