/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import java.util.Collections;
import java.util.Vector;

import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.model.Course;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class CourseDetector extends Control {

	private RunnerControl runnerControl;

	public CourseDetector(GecoControl gecoControl) {
		super(CourseDetector.class, gecoControl);
		runnerControl = gecoControl.getService(RunnerControl.class);
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
		data.setRunner(runnerControl.buildMockRunner());
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

}
