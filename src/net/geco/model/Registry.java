/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;



/**
 * Registry holds all data related to the stage itself and provides a facade to access them. It does not
 * hold properties related to the application (controls, widgets). It should evolve towards an interface
 * masking the persistence framework (database).
 * 
 * Currently, Registry is also able to perform some batch operations on itself.
 * 
 * @author Simon Denier
 * @since Jan 4, 2009
 *
 */
public class Registry {
	
	private Map<String, Course> courses;
	
	private Vector<String> sortedCoursenames;
	
	private Map<String, Club> clubs;

	private Vector<String> sortedClubnames;
	
	private Map<String, Category> categories;
	
	private Vector<String> sortedCategorynames;
	
	private Map<String, Runner> runnersByEcard;
	
	private Map<Course, List<Runner>> runnersByCourse;
	
	private Map<Category, List<Runner>> runnersByCategory;
	
	private Map<Runner, RunnerRaceData> runnerData;
	
	private Map<String, HeatSet> heatsets;
	
	private Object runnersLock = new Object();

	
	/**
	 * 
	 */
	public Registry() {
		courses = new HashMap<String, Course>();
		clubs = new HashMap<String, Club>();
		categories = new HashMap<String, Category>();
		runnersByEcard = new HashMap<String, Runner>();
		runnersByCategory = new HashMap<Category, List<Runner>>();
		runnersByCourse = new HashMap<Course, List<Runner>>();
		runnerData = new HashMap<Runner, RunnerRaceData>();
		heatsets = new HashMap<String, HeatSet>();
	}
	
	
	public Collection<Course> getCourses() {
		synchronized (courses) {
			return courses.values();			
		}
	}
	
	public Vector<Course> getSortedCourses() {
		Vector<Course> courseList = new Vector<Course>(getCourses());
		Collections.sort(courseList, new Comparator<Course>() {
			@Override
			public int compare(Course c1, Course c2) {
				return c1.getName().compareTo(c2.getName());
			}});
		return courseList;
	}

	public Vector<String> getCoursenames() {
		synchronized (courses) {
			return new Vector<String>(courses.keySet());	
		}
	}

	public Vector<String> getSortedCoursenames() {
		if( sortedCoursenames==null ) {
			sortedCoursenames = getCoursenames();
			Collections.sort(sortedCoursenames);
		}
		return sortedCoursenames;
	}
	
	public Course findCourse(String name) {
		synchronized (courses) {
			return courses.get(name);
		}
	}
	
	public void addCourse(Course course) {
		synchronized (courses) {
			courses.put(course.getName(), course);
			if( !runnersByCourse.containsKey(course) ) {
				// if creating a previously unknown course, we also
				// create the entry for runners
				runnersByCourse.put(course, new Vector<Runner>());
			}
			sortedCoursenames = null;
		}
	}

	public void removeCourse(Course course) {
		synchronized (courses) {
			courses.remove(course.getName());
			runnersByCourse.remove(course);
			sortedCoursenames = null;
		}
	}
	
	public void updateCoursename(Course course, String newName) {
		synchronized (courses) {
			courses.remove(course.getName());
			course.setName(newName);
			addCourse(course);
		}		
	}
	
	public Course anyCourse() {
		synchronized (courses) {
			try {
				return courses.values().iterator().next();
			} catch (NoSuchElementException e) {
				return null;
			}
		}
	}


	public Collection<Club> getClubs() {
		synchronized (clubs) {
			return clubs.values();
		}
	}

	public Vector<Club> getSortedClubs() {
		Vector<Club> clubList = new Vector<Club>(getClubs());
		Collections.sort(clubList, new Comparator<Club>() {
			@Override
			public int compare(Club c1, Club c2) {
				return c1.getName().compareTo(c2.getName());
			}});
		return clubList;
	}
	
	public Vector<String> getClubnames() {
		synchronized (clubs) {
			return new Vector<String>(clubs.keySet());
		}
	}
	
	public Vector<String> getSortedClubnames() {
		if( sortedClubnames==null ) {
			sortedClubnames = getClubnames();
			Collections.sort(sortedClubnames);
		}
		return sortedClubnames;
	}
	
	public Club findClub(String name) {
		synchronized (clubs) {
			return clubs.get(name);
		}
	}
	
	public void addClub(Club club) {
		synchronized (clubs) {
			clubs.put(club.getName(), club);
			sortedClubnames = null;
		}
	}
	
	public void removeClub(Club club) {
		synchronized (clubs) {
			clubs.remove(club.getName());
			sortedClubnames = null;
		}		
	}
	
	public void updateClubname(Club club, String newName) {
		synchronized (clubs) {
			clubs.remove(club.getName());
			club.setName(newName);
			addClub(club);
		}
	}
	
	public Club noClub() {
		return findClub(Messages.getString("Registry.NoClubLabel")); //$NON-NLS-1$
	}

	public Club anyClub() {
		synchronized (clubs) {
			try {
				return clubs.values().iterator().next();
			} catch (NoSuchElementException e) {
				return null;
			}
		}
	}

	
	public Collection<Category> getCategories() {
		synchronized (categories) {
			return categories.values();
		}
	}
	
	public Vector<Category> getSortedCategories() {
		Vector<Category> catList = new Vector<Category>(getCategories());
		Collections.sort(catList, new Comparator<Category>() {
			@Override
			public int compare(Category c1, Category c2) {
				return c1.getName().compareTo(c2.getName());
			}});
		return catList;
	}

	public Vector<String> getCategorynames() {
		synchronized (categories) {
			return new Vector<String>(categories.keySet());
		}
	}

	public Vector<String> getSortedCategorynames() {
		if( sortedCategorynames==null ) {
			sortedCategorynames = getCategorynames();
			Collections.sort(sortedCategorynames);
		}
		return sortedCategorynames;
	}

	public Category findCategory(String name) {
		synchronized (categories) {
			return categories.get(name);
		}
	}
	
	public void addCategory(Category cat) {
		synchronized (categories) {
			categories.put(cat.getShortname(), cat);
			if( !runnersByCategory.containsKey(cat) ) {
				runnersByCategory.put(cat, new Vector<Runner>());
			}
			sortedCategorynames = null;
		}
	}
	
	public void removeCategory(Category cat) {
		synchronized (categories) {
			categories.remove(cat.getShortname());
			runnersByCategory.remove(cat);
			sortedCategorynames = null;
		}
	}

	public void updateCategoryname(Category cat, String newName) {
		synchronized (categories) {
			categories.remove(cat.getShortname());
			cat.setShortname(newName);
			addCategory(cat);
		}
	}
	
	public Category noCategory() {
		return findCategory(Messages.getString("Registry.NoCategoryLabel")); //$NON-NLS-1$
	}
	
	public Category anyCategory() {
		synchronized (categories) {
			try {
				return categories.values().iterator().next();
			} catch (NoSuchElementException e) {
				return null;
			}
		}
	}
	
	public Course getDefaultCourseOrAnyFor(Category cat) {
		return ( cat.getCourse()!=null ) ?
				cat.getCourse() :
				anyCourse();
	}


	public Collection<Runner> getRunners() {
		synchronized (runnersLock) {
			return runnersByEcard.values();
		}
	}
	
	public Runner findRunnerByEcard(String ecard) {
		synchronized (runnersLock) {
			return runnersByEcard.get(ecard);
		}
	}
	
	public void addRunner(Runner runner) {
		synchronized (runnersLock) {
			runnersByEcard.put(runner.getEcard(), runner);
			addRunnerinCategoryList(runner, runner.getCategory());
			addRunnerinCourseList(runner, runner.getCourse());
		}
	}
	
	private void addRunnerinCourseList(Runner runner, Course course) {
		synchronized (runnersLock) {
			runnersByCourse.get(course).add(runner);
		}
	}

	private void addRunnerinCategoryList(Runner runner, Category cat) {
		synchronized (runnersLock) {
			runnersByCategory.get(cat).add(runner);
		}
	}

	public void updateRunnerEcard(String oldEcard, Runner runner) {
		synchronized (runnersLock) {
			runnersByEcard.remove(oldEcard);
			runnersByEcard.put(runner.getEcard(), runner);
		}
	}

	public void updateRunnerCourse(Course oldCourse, Runner runner) {
		synchronized (runnersLock) {
			if( !oldCourse.equals(runner.getCourse() )) {
				runnersByCourse.get(oldCourse).remove(runner);
				addRunnerinCourseList(runner, runner.getCourse());
			}
		}
	}

	public void updateRunnerCategory(Category oldCat, Runner runner) {
		synchronized (runnersLock) {
			if( !oldCat.equals(runner.getCategory() )) {
				runnersByCategory.get(oldCat).remove(runner);
				addRunnerinCategoryList(runner, runner.getCategory());
			}
		}
	}
	
	public void removeRunner(Runner runner) {
		synchronized (runnersLock) {
			runnersByEcard.remove(runner.getEcard());
			runnersByCategory.get(runner.getCategory()).remove(runner);
			runnersByCourse.get(runner.getCourse()).remove(runner);
		}
	}

	public Collection<RunnerRaceData> getRunnersData() {
		synchronized (runnersLock) {
			return runnerData.values();
		}
	}
	
	public RunnerRaceData findRunnerData(Runner runner) {
		synchronized (runnersLock) {
			return runnerData.get(runner);
		}
	}

	public RunnerRaceData findRunnerData(String ecard) {
		synchronized (runnersLock) {
			return runnerData.get(findRunnerByEcard(ecard));
		}
	}
	
	public void addRunnerData(RunnerRaceData data) {
		synchronized (runnersLock) {
			runnerData.put(data.getRunner(), data);
		}
	}
	
	public void removeRunnerData(RunnerRaceData data) {
		synchronized (runnersLock) {
			runnerData.remove(data.getRunner());
		}
	}
	
	public List<Runner> getRunnersFromCategory(Category cat) {
		synchronized (runnersLock) {
			return runnersByCategory.get(cat);
		}
	}

	public List<Runner> getRunnersFromCategory(String catName) {
		synchronized (runnersLock) {
			return runnersByCategory.get(findCategory(catName));
		}
	}
	
	public Map<Course, List<Runner>> getRunnersByCourseFromCategory(String catName) {
		synchronized (runnersLock) {
			HashMap<Course, List<Runner>> map = new HashMap<Course, List<Runner>>();
			List<Runner> runners = getRunnersFromCategory(catName);
			if( runners==null ) return map;
			for (Runner runner : runners) {
				if( ! map.containsKey(runner.getCourse()) ) {
					map.put(runner.getCourse(), new Vector<Runner>());
				}
				map.get(runner.getCourse()).add(runner);
			}
			return map;
		}
	}

	public List<Runner> getRunnersFromCourse(Course course) {
		synchronized (runnersLock) {
			return runnersByCourse.get(course);
		}
	}
	
	public List<Runner> getRunnersFromCourse(String courseName) {
		synchronized (runnersLock) {
			return runnersByCourse.get(findCourse(courseName));
		}
	}

	
	public Collection<HeatSet> getHeatSets() {
		synchronized (heatsets) {
			return heatsets.values();
		}
	}

	public Vector<String> getHeatSetnames() {
		synchronized (heatsets) {
			return new Vector<String>(heatsets.keySet());
		}
	}
	
	public HeatSet findHeatSet(String name) {
		synchronized (heatsets) {
			return heatsets.get(name);
		}
	}
	
	public void addHeatSet(HeatSet heatset) {
		synchronized (heatsets) {
			heatsets.put(heatset.getName(), heatset);
		}
	}

	public void removeHeatset(HeatSet heatSet) {
		synchronized (heatsets) {
			heatsets.remove(heatSet.getName());
		}
	}


	/**
	 * @return
	 */
	public Integer[] collectStartnumbers() { // TODO: maybe use the Integer type for startnumber, it will save some code
		synchronized (runnersLock) {
			Integer[] startnums = new Integer[runnersByEcard.size()];
			int i = 0;
			for (Runner runner : runnersByEcard.values()) {
				startnums[i] = runner.getStartnumber();
				i++;
			}
			Arrays.sort(startnums);
			return startnums;
		}
	}
	
	public Integer detectMaxStartnumber() {
		synchronized (runnersLock) {
			int max = 0;
			for (Runner runner : runnersByEcard.values()) {
				max = Math.max(max, runner.getStartnumber());
			}
			return max;
		}
	}

	public Integer detectMaxEcardNumber() {
		synchronized (runnersLock) {
			int max = 0;
			for (String ecard : runnersByEcard.keySet()) {
				try {
					Integer ecardi = Integer.valueOf(ecard);					
					max = Math.max(max, ecardi);
				} catch (NumberFormatException e) {
					// bypass ecard number xxxxa (cloned entries)
				}
			}
			return max;
		}
	}
	
	public String[] collectEcardNumbers() {
		synchronized (runnersLock) {
			String[] ecards = runnersByEcard.keySet().toArray(new String[0]);
			Arrays.sort(ecards);
			return ecards;
		}
	}

}
