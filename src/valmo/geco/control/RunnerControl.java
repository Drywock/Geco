/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import valmo.geco.core.Announcer;
import valmo.geco.core.TimeManager;
import valmo.geco.core.Util;
import valmo.geco.model.Category;
import valmo.geco.model.Course;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Aug 21, 2009
 *
 */
public class RunnerControl extends Control {
	
	private RunnerBuilder builder;
	
	public RunnerControl(GecoControl gecoControl) {
		super(gecoControl);
		builder = new RunnerBuilder(geco().factory());
	}
	
	private Announcer announcer() {
		return geco().announcer();
	}

	public Runner buildMockRunner() {
		Runner runner = factory().createRunner();
		runner.setCourse(registry().anyCourse());
		return runner;		
	}
	
	public Runner buildAnonymousRunner(String chip, Course course) {
		Runner runner = factory().createRunner();
		runner.setStartnumber(registry().detectMaxStartnumber() + 1);
		runner.setChipnumber(chip);
		runner.setFirstname("John");
		runner.setLastname("Doe");
		runner.setClub(registry().noClub());
		runner.setCategory(registry().noCategory());
		runner.setCourse(course);
		return runner;
	}

	public Runner createAnonymousRunner(Course course) {
		Runner runner = buildAnonymousRunner(newUniqueChipnumber(), course);
		registerRunner(runner, builder.buildRunnerData());
		return runner;
	}

	public Runner createAnonymousRunner() {
		return createAnonymousRunner(registry().anyCourse());
	}

	public String newUniqueChipnumber() {
		return Integer.toString(registry().detectMaxChipnumber() + 1);
	}
	
	public String deriveUniqueChipnumber(String chipnumber) {
		String[] chips = registry().collectChipnumbers();
		if( Util.different(chipnumber, -1, chips) ) {
			return chipnumber;
		} else 
			return deriveUniqueChipnumber(chipnumber + "a");
	}

	public RunnerRaceData registerRunner(Runner runner, RunnerRaceData runnerData) {
		registry().addRunner(runner);
		announcer().announceRunnerCreation(registerRunnerDataFor(runner, runnerData));
		return runnerData;
	}
	private RunnerRaceData registerRunnerDataFor(Runner runner, RunnerRaceData runnerData) {
		return builder.registerRunnerDataFor(registry(), runner, runnerData);
	}
	
	public void deleteRunner(RunnerRaceData data) {
		registry().removeRunner(data.getRunner());
		registry().removeRunnerData(data);
		announcer().announceRunnerDeletion(data);
	}
	
	public void updateRunnerDataFor(Runner runner, RunnerRaceData newData) {
		RunnerRaceData runnerData = registry().findRunnerData(runner);
		Status oldStatus = runnerData.getResult().getStatus();
		runnerData.copyFrom(newData);
		announcer().announceStatusChange(runnerData, oldStatus);
	}
	
	
	public boolean verifyStartnumber(Runner runner, String newStartString) {
		try {
			return verifyStartnumber(runner, new Integer(newStartString));
		} catch (NumberFormatException e) {
			geco().info("Bad format for start number", true);
			return false;
		}
	}

	public boolean verifyStartnumber(Runner runner, int newStart) {
		int oldStart = runner.getStartnumber();
		Integer[] startnums = registry().collectStartnumbers();
		boolean ok = Util.different(newStart, Arrays.binarySearch(startnums, oldStart), startnums);
		if( !ok )
			geco().info("Start number already used", true);
		return ok;
	}
	
	public boolean validateStartnumber(Runner runner, String newStartString) {
		try {
			int newStart = new Integer(newStartString);
			if( verifyStartnumber(runner, newStart) ) {
				runner.setStartnumber(newStart);
				return true;
			}
		} catch (NumberFormatException e) {
			geco().info("Bad format for start number", true);
		}
		return false;
	}
	
	public boolean verifyChipnumber(Runner runner, String newChipString) {
		String newChip = newChipString.trim();
		if( newChip.isEmpty() ) {
			geco().info("Chip number can not be empty", true);
			return false;
		}
		String oldChip = runner.getChipnumber();
		String[] chips = registry().collectChipnumbers();
		boolean ok = Util.different(newChip, Arrays.binarySearch(chips, oldChip), chips);
		if( !ok )
			geco().info("Chip number already used", true);
		return ok;
	}

	public boolean validateChipnumber(Runner runner, String newChip) {
		if( verifyChipnumber(runner, newChip) ) {
			String oldChip = runner.getChipnumber();
			runner.setChipnumber(newChip.trim());
			registry().updateRunnerChip(oldChip, runner);
			return true;
		} else {
			return false;
		}		
	}
	
	public boolean validateFirstname(Runner runner, String newName) {
		runner.setFirstname(newName.trim());
		return true;
	}

	public boolean verifyLastname(String newName) {
		boolean ok = ! newName.trim().isEmpty();
		if( !ok ) {
			geco().info("Last name can not be empty", true);
		}
		return ok;
	}

	public boolean validateLastname(Runner runner, String newName) {
		if( verifyLastname(newName) ) {
			runner.setLastname(newName.trim());
			return true;
		} else {
			return false;
		}
	}
	
	public boolean validateClub(Runner runner, String newClub) {
		runner.setClub( registry().findClub(newClub) );
		return true;
	}
	
	public boolean validateCategory(Runner runner, String newCat) {
		Category oldCat = runner.getCategory();
		if( !oldCat.getShortname().equals(newCat) ) {
			runner.setCategory(registry().findCategory(newCat));
			registry().updateRunnerCategory(oldCat, runner);
			geco().log("Category change for " + runner.idString() + " from " + oldCat.getShortname() + " to " + newCat);
			return true;
		}
		return false;
	}
	
	public boolean validateCourse(RunnerRaceData runnerData, String newCourse) {
		Runner runner = runnerData.getRunner();
		Course oldCourse = runner.getCourse();
		if( !oldCourse.getName().equals(newCourse) ) {
			runner.setCourse(registry().findCourse(newCourse));
			registry().updateRunnerCourse(oldCourse, runner);
			announcer().announceCourseChange(runner, oldCourse);
			geco().log("Course change for " + runner.idString() + " from " + oldCourse.getName() + " to " + newCourse);
			// Proceed by checking the new status
			Status oldStatus = runnerData.getResult().getStatus();
			geco().checker().check(runnerData); // use and share an action with refresh button
			announcer().announceStatusChange(runnerData, oldStatus);
			return true;
		}
		return false;
	}

	public boolean validateNCStatus(Runner runner, boolean nc) {
		runner.setNC(nc);
		return true;
	}
	
	public boolean verifyRaceTime(String raceTime) {
		try {
			TimeManager.userParse(raceTime);
			return true;
		} catch (ParseException e1) {
			geco().info("Bad time format", true);
			return false;
		}		
	}
	
	public boolean validateRaceTime(RunnerRaceData runnerData, String raceTime) {
		try {
			long oldTime = runnerData.getResult().getRacetime();
			Date newTime = TimeManager.userParse(raceTime);
			if( oldTime!=newTime.getTime() ) {
				runnerData.getResult().setRacetime(newTime.getTime());
				geco().log("Race time change for " + runnerData.getRunner().idString() + " from " + TimeManager.fullTime(oldTime) + " to " + TimeManager.fullTime(newTime));
			}
			return true;
		} catch (ParseException e1) {
			geco().info("Bad time format", true);
			return false;
		}
	}
	
	public boolean resetRaceTime(RunnerRaceData runnerData) {
		long oldTime = runnerData.getResult().getRacetime();
		geco().checker().resetRaceTime(runnerData);
		long newTime = runnerData.getResult().getRacetime();
		if( oldTime!=newTime ) {
			geco().log("Race time reset for " + runnerData.getRunner().idString() + " from " + TimeManager.fullTime(oldTime) + " to " + TimeManager.fullTime(newTime));
		}
		return true;
	}
	
	public boolean validateStatus(RunnerRaceData runnerData, Status newStatus) {
		Status oldStatus = runnerData.getResult().getStatus();
		if( !newStatus.equals(oldStatus) ) {
			runnerData.getResult().setStatus(newStatus);
			geco().log("Status change for " + runnerData.getRunner().idString() + " from " + oldStatus + " to " + newStatus);
			announcer().announceStatusChange(runnerData, oldStatus);
			return true;
		}
		return false;
	}
	
	public boolean resetStatus(RunnerRaceData runnerData) {
		Status oldStatus = runnerData.getResult().getStatus();
		geco().checker().check(runnerData);
		Status newStatus = runnerData.getResult().getStatus();
		if( !oldStatus.equals(newStatus) ) {
			geco().log("Status reset for " + runnerData.getRunner().idString() + " from " + oldStatus + " to " + newStatus);
			announcer().announceStatusChange(runnerData, oldStatus);
			return true;
		}
		return false;
	}
	
	public void recheckAllRunners() {
		for (RunnerRaceData data: registry().getRunnersData()) {
			if( data.getResult().is(Status.OK) 
					|| data.getResult().is(Status.MP) ) {
				geco().checker().check(data);
			}
		}
		announcer().announceRunnersChange();
		geco().log("Recheck all OK|MP data");
	}


}
