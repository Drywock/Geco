/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.control;

import valmo.geco.core.Announcer;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.HeatSet;
import valmo.geco.model.Pool;
import valmo.geco.model.Runner;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Aug 21, 2009
 *
 */
public class StageControl extends Control {
	
	private Announcer announcer;
	
	/**
	 * @param factory
	 * @param stage
	 * @param announcer 
	 */
	public StageControl(Factory factory, Stage stage, Announcer announcer) {
		super(factory, stage, announcer);
		this.announcer = announcer;
	}
	
	private Announcer announcer() {
		return announcer;
	}
	
	public Club createClub() {
		Club club = factory().createClub();
		club.setName("Club" + (registry().getClubs().size() + 1));
		club.setShortname("");
		registry().addClub(club);
		announcer().announceClubsChanged();
		return club;
	}

	public void updateName(Club club, String newName) {
		if( !club.getName().equals(newName) ) {
			registry().updateClubname(club, newName);
			announcer().announceClubsChanged();
		}
	}
	
	public void updateShortname(Club club, String newName) {
		if( !club.getShortname().equals(newName) ) {
			club.setShortname(newName);
			announcer().announceClubsChanged();
		}
	}
	
	public boolean removeClub(Club club) {
		if( canRemoveClub(club) ) {
			stage().registry().removeClub(club);
			announcer().announceClubsChanged();
			return true;
		}
		return false;
	}
	
	public boolean canRemoveClub(Club club) {
		boolean clubHasRunners = false;
		for (Runner runner : registry().getRunners()) {
			clubHasRunners |= (runner.getClub() == club);
		}
		return !clubHasRunners;
	}

	/**
	 * 
	 */
	public Course createCourse() {
		Course course = factory().createCourse();
		course.setName("Course" + (registry().getCourses().size() + 1));
		course.setCodes(new int[0]);
		registry().addCourse(course);
		announcer().announceCoursesChanged();
		return course;
	}

	/**
	 * @param course
	 * @return
	 * @throws Exception 
	 */
	public boolean removeCourse(Course course) throws Exception {
		if( canRemoveCourse(course) ) {
			stage().registry().removeCourse(course);
			announcer().announceCoursesChanged();
			return true;
		}
		return false;
	}

	public boolean canRemoveCourse(Course course) throws Exception {
		for (Category cat : registry().getCategories()) {
			if( cat.getCourse() == course ) {
				throw new Exception("Categories use course");
			}
		}
		for (Runner runner : registry().getRunners()) {
			if( runner.getCourse() == course ) {
				throw new Exception("Runners use course");
			}
		}
		for (HeatSet set : registry().getHeatSets()) {
			if( set.isCourseType() ) {
				for (Pool pool : set.getSelectedPools()) {
					if( pool == course ) {
						throw new Exception("Heatsets use course");
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * @param course
	 * @param value
	 */
	public void updateName(Course course, String newName) {
		if( !course.getName().equals(newName) ) {
			registry().updateCoursename(course, newName);
			announcer().announceCoursesChanged();
		}		
	}


	/**
	 * 
	 */
	public Category createCategory() {
		Category cat = factory().createCategory();
		cat.setShortname("Category" + (registry().getCategories().size() + 1));
		registry().addCategory(cat);
		announcer().announceCategoriesChanged();
		return cat;		
	}

	/**
	 * @param cat
	 * @throws Exception 
	 */
	public boolean removeCategory(Category cat) throws Exception {
		if( canRemoveCategory(cat) ) {
			stage().registry().removeCategory(cat);
			announcer().announceCategoriesChanged();
			return true;
		}
		return false;		
	}

	/**
	 * @param cat
	 * @return
	 * @throws Exception 
	 */
	private boolean canRemoveCategory(Category cat) throws Exception {
		for (Runner runner : registry().getRunners()) {
			if( runner.getCategory() == cat ) {
				throw new Exception("Runners use category");
			}
		}
		for (HeatSet set : registry().getHeatSets()) {
			if( set.isCategoryType() ) {
				for (Pool pool : set.getSelectedPools()) {
					if( pool == cat ) {
						throw new Exception("Heatsets use category");
					}
				}
			}
		}
		return true;
	}

	/**
	 * @param cat
	 * @param value
	 */
	public void updateShortname(Category cat, String newName) {
		if( !cat.getShortname().equals(newName) ) {
			registry().updateCategoryname(cat, newName);
			announcer().announceCategoriesChanged();
		}		
	}

	/**
	 * @param cat
	 * @param value
	 */
	public void updateName(Category cat, String newName) {
		if( !cat.getLongname().equals(newName) ) {
			cat.setLongname(newName);
			announcer().announceCategoriesChanged();
		}
	}
	
}
