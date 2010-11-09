/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public interface IRunner {
	
	public String getName();

	public String getNameR();

	public String getFirstname();

	public void setFirstname(String firstname);

	public String getLastname();

	public void setLastname(String lastname);

	public Club getClub();

	public void setClub(Club club);

	public String getChipnumber();

	public void setChipnumber(String chipnumber);

	public Category getCategory();

	public void setCategory(Category category);

	public String toString();

	public String idString();

}
