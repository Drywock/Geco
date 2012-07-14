/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.IOException;

import net.geco.control.ecardmodes.CourseDetector;
import net.geco.model.Archive;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Jul 11, 2012
 *
 */
public class MergeControl extends Control {

	private RunnerControl runnerControl;

	public MergeControl(GecoControl gecoControl) {
		super(MergeControl.class, gecoControl);
		this.runnerControl = getService(RunnerControl.class);
	}

	public Runner buildMockRunner() {
		return runnerControl.buildMockRunner();
	}

	public void checkTentativeCourse(RunnerRaceData ecardData, String coursename) {
		ecardData.getRunner().setCourse(registry().findCourse(coursename));
		geco().checker().check(ecardData);
	}

	public void detectCourse(RunnerRaceData ecardData) {
		CourseDetector courseDetector = new CourseDetector(geco());
		Course bestMatch = courseDetector.detectCourse(ecardData);
		ecardData.getRunner().setCourse(bestMatch);
	}

	public void registerAnonymousRunner(RunnerRaceData ecardData) {
		try {
			Runner anonymousRunner = runnerControl.buildAnonymousRunner(ecardData.getRunner().getEcard(), ecardData.getCourse());
			runnerControl.registerRunner(anonymousRunner, ecardData);
		} catch (RunnerCreationException e) {
			geco().debug(e.getLocalizedMessage());
		}
//		geco.log("Creation " + runnerData.infoString()); //$NON-NLS-1$
	}
	
	public void mergeRunnerWithData(Runner targetRunner, RunnerRaceData eCardData, Runner sourceRunner) {
		if( sourceRunner != targetRunner ) {
			if( sourceRunner != null) {
				runnerControl.validateEcard(sourceRunner, "");
			}
			runnerControl.validateEcard(targetRunner, eCardData.getRunner().getEcard());			
		}
		runnerControl.updateCourse(targetRunner, targetRunner.getCourse(), eCardData.getCourse());
		runnerControl.updateRunnerDataFor(targetRunner, eCardData);
	}

	public void deleteRunner(Runner sourceRunner) {
		runnerControl.deleteRunner(registry().findRunnerData(sourceRunner));
	}
	
	public Archive archive() throws IOException {
		return getService(ArchiveManager.class).archive();
	}
	
}
