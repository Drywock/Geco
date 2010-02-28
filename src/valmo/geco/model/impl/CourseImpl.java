/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import java.util.Arrays;

import valmo.geco.model.Course;


/**
 * @author Simon Denier
 * @since Jan 4, 2009
 *
 */
public class CourseImpl implements Course {
	
	private String name;
	
	private int length;
	
	private int climb;
	
	private int[] codes;

	
	public int getClimb() {
		return climb;
	}

	public int[] getCodes() {
		return codes;
	}

	public int getLength() {
		return length;
	}

	public String getName() {
		return name;
	}


	public void setClimb(int climb) {
		this.climb = climb;
	}

	public void setCodes(int[] codes) {
		this.codes = codes;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Course " + name + ", " + codes.length + "p (" + length +"m, " + climb + "m): "
				+ Arrays.toString(codes);
	} 

}
