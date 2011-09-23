/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.util.Collections;
import java.util.Vector;

import net.geco.basics.GecoRequestHandler;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Status;


/**
 * @author Simon Denier
 * @since Sep 26, 2010
 *
 */
public class AutoMergeHandler extends Control implements GecoRequestHandler {

	public AutoMergeHandler(GecoControl gecoControl) {
		super(AutoMergeHandler.class, gecoControl);
	}
	
	private RunnerControl runnerControl() {
		return geco().getService(RunnerControl.class);
	}

	@Override
	public String requestMergeExistingRunner(RunnerRaceData data, Runner target) {
		return processData(data, target.getEcard(), detectCourse(data), Status.DUP);
	}

	@Override
	public String requestMergeUnknownRunner(RunnerRaceData data, String ecard) {
		Course course = detectCourse(data);
		Runner r = detectArchiveRunner(data, ecard, course);
		if( r!=null ){
			// TODO: rethink this part with SIReaderHandler. We return null because detectArchiveRunner
			// already handled the case and announced CardRead (autoprinting).
			// Returning the e-card would led SIReaderHandler to announce an unknown card.
			return null;
		} else {
			return processData(data, ecard, course, Status.UNK);
		}
	}
	
	private static class CourseResult implements Comparable<CourseResult> {
		CourseResult(int dist, Course course) {
			this.course = course;
			this.dist = dist;
		}
		private Course course;
		private int dist;
		private RunnerResult result;
		@Override
		public int compareTo(CourseResult o) {
			return dist - o.dist;
		}
	}

	public Course detectCourse(RunnerRaceData data) {
		Vector<CourseResult> distances = new Vector<CourseResult>();
		int nbPunches = data.getPunches().length;
		for (Course course : registry().getCourses()) {
			distances.add(new CourseResult(Math.abs(nbPunches - course.nbControls()), course));
		}
		Collections.sort(distances);
		
		int minMps = Integer.MAX_VALUE;
		CourseResult bestResult = null;
		data.setRunner(runnerControl().buildMockRunner());
		for (CourseResult cResult : distances) {
			data.getRunner().setCourse(cResult.course);
			geco().checker().check(data);
			if( data.getResult().getNbMPs()==0 ){
				// early stop only if no MP detected
				// in some race case with orient'show, one trace may be ok with multiple courses (as soon as MPs < MP limit)
				// so we should continue to look for a better match even if status == OK
				return cResult.course;
			}
			int nbMPs = data.getResult().getNbMPs();
			if( nbMPs < minMps ){
				minMps = nbMPs;
				bestResult = cResult;
				bestResult.result = data.getResult(); // memoize result so we don't have to compute it again
			}
		}
		data.setResult(bestResult.result);
		return bestResult.course;
	}

	private Runner detectArchiveRunner(RunnerRaceData data, String ecard, Course course) {
		ArchiveManager archive = geco().getService(ArchiveManager.class);
		Runner newRunner = archive.findAndCreateRunner(ecard, course);
		if( newRunner==null ){
			return null;
		} else {
			runnerControl().registerRunner(newRunner, data);
			geco().announcer().announceCardRead(newRunner.getEcard());
			geco().log("Insertion " + data.infoString()); //$NON-NLS-1$
			return newRunner;
		}
	}
	
	private String processData(RunnerRaceData runnerData, String ecard, Course course, Status status) {
		String uniqueEcard = runnerControl().deriveUniqueEcard(ecard);
		try {
			// Create from scratch a brand new runner
			Runner newRunner = runnerControl().buildAnonymousRunner(uniqueEcard, course);
//			newRunner.setFirstname("Loisir");
//			newRunner.setLastname(uniqueEcard);
			runnerData.getResult().setStatus(status); // set custom (unresolved) status
			runnerControl().registerRunner(newRunner, runnerData);
			geco().log("Creation " + runnerData.infoString()); //$NON-NLS-1$
		} catch (RunnerCreationException e1) {
			e1.printStackTrace();
		}
		return uniqueEcard;
	}

}
