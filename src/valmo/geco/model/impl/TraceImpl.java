/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import java.util.Date;

import valmo.geco.model.Punch;
import valmo.geco.model.Trace;


public class TraceImpl implements Trace {

	private String code;
	private Date time;
	
	public TraceImpl(Punch punch) {
		this(Integer.toString(punch.getCode()), punch.getTime());
	}
	
	public TraceImpl(String code, Date time) {
		this.code = code;
		this.time = time;
	}
	
	public TraceImpl clone() {
		try {
			TraceImpl clone = (TraceImpl) super.clone();
			clone.time = (Date) time.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getCode() {
		return code;
	}
	public Date getTime() {
		return time;
	}
	
	public String toString() {
		return getCode();
	}

	@Override
	public boolean isOK() {
		return !( isMP() || isAdded() );
	}
	@Override
	public boolean isMP() {
		return code.startsWith("-");
	}
	@Override
	public boolean isAdded() {
		return code.startsWith("+");
	}
	@Override
	public boolean isSubst() {
		// Notice that isSub => isMP
		return code.startsWith("-") && code.contains("+");
	}
}