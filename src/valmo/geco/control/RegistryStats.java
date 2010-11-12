/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import valmo.geco.core.Announcer;
import valmo.geco.core.Messages;
import valmo.geco.model.Course;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Status;

/**
 * Compute statistics on registry after initial upload and keep numbers up to date by following events.
 * 
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class RegistryStats extends Control
	implements Announcer.StageListener, Announcer.RunnerListener, Announcer.StageConfigListener {

	public static enum StatItem {
		Registered {
			@Override
			public String toString() {
				return Messages.getString("RegistryStats.RegisteredLabel"); //$NON-NLS-1$
			}},
		Present {
			@Override
			public String toString() {
				return Messages.getString("RegistryStats.PresentLabel"); //$NON-NLS-1$
			}},
		Unresolved {
			@Override
			public String toString() {
				return Messages.getString("RegistryStats.UnresolvedLabel"); //$NON-NLS-1$
			}},
		Finished {
			@Override
			public String toString() {
				return Messages.getString("RegistryStats.FinishedLabel"); //$NON-NLS-1$
			}},
		OK, MP, DNS, DNF, DSQ, NDA, UNK, DUP;
	}

	public static final StatItem[] shortStatusList = new StatItem[] {
		StatItem.Present, StatItem.Unresolved, StatItem.Finished, StatItem.OK, StatItem.MP, StatItem.NDA,
	};
	
	// Courses stats
	private Map<String, Map<StatItem, Integer>> stats;

	// Memo fields for computing total stats
	private int totalOk;
	private int totalMp;
	private int totalDns;
	private int totalDnf;
	private int totalDsq;
	private int totalNda;
	private int totalUnk;
	private int totalDup;
	
	public RegistryStats(GecoControl gecoControl) {
		super(gecoControl);
		Announcer announcer = gecoControl.announcer();
		announcer.registerStageListener(this);
		announcer.registerRunnerListener(this);
		announcer.registerStageConfigListener(this);
		fullUpdate();
	}
	
	public StatItem[] longStatuses() {
		return StatItem.values();
	}
	
	public StatItem[] shortStatuses() {
		return shortStatusList;
	}
	
	public String[] sortedEntries() {
		Vector<String> entries = new Vector<String>(registry().getSortedCoursenames());
		entries.add(totalName());
		return entries.toArray(new String[0]);
	}
	
	private String totalName() {
		return Messages.getString("RegistryStats.TotalLabel"); //$NON-NLS-1$
	}

	public Map<StatItem, Integer> getCourseStatsFor(String coursename) {
		return stats.get(coursename);
	}

	public Map<StatItem, Integer> getTotalCourse() {
		return stats.get(totalName());
	}
	
	public Integer getCourseStatsFor(String course, StatItem item) {
		return getCourseStatsFor(course).get(item);
	}
	
	public void fullUpdate() {
		initStatMaps();
		for (Course course : registry().getCourses()) {
			stats.put(course.getName(), new HashMap<StatItem, Integer>());
			computeCourseStats(course);
		}
		computeTotalStats();
	}
	
	public StatItem convertStatus(Status status) {
		return StatItem.valueOf(status.name());
	}
	
	
	private void initStatMaps() {
		stats = new HashMap<String, Map<StatItem,Integer>>();
		stats.put(totalName(), new HashMap<StatItem, Integer>());
		totalOk = 0;
		totalMp = 0;
		totalDns = 0;
		totalDnf = 0;
		totalDsq = 0;
		totalNda = 0;
		totalUnk = 0;
		totalDup = 0;
	}
	
	private void computeCourseStats(Course course) {
		int courseOk = 0, courseMp = 0, courseDns = 0, courseDnf = 0, courseDsq = 0;
		int courseNda = 0, courseUnk = 0, courseDup = 0;
		List<Runner> courseData = registry().getRunnersFromCourse(course);
		int total = courseData.size();
		for (Runner runner : courseData) {
			switch (registry().findRunnerData(runner).getResult().getStatus()) {
			case OK: courseOk++; totalOk++; break;
			case MP: courseMp++; totalMp++; break;
			case DNS: courseDns++; totalDns++; break;
			case DNF: courseDnf++; totalDnf++; break;
			case DSQ: courseDsq++; totalDsq++; break;
			case NDA: courseNda++; totalNda++; break;
			case UNK: courseUnk++; totalUnk++; break;
			case DUP: courseDup++; totalDup++; break;
			}
		}
		Map<StatItem, Integer> courseStats = stats.get(course.getName());
		storeStats(courseOk, courseMp, courseDns, courseDnf, courseDsq, courseNda, courseUnk, courseDup,
					total, courseStats);
	}

	private void computeTotalStats() {
		int total = registry().getRunners().size();
		storeStats(totalOk, totalMp, totalDns, totalDnf, totalDsq, totalNda, totalUnk, totalDup,
					total, getTotalCourse());
	}
	
	private void storeStats(int ok, int mp, int dns, int dnf, int dsq, int nda, int unk, int dup, 
			int total, Map<StatItem, Integer> courseStats) {
		courseStats.put(StatItem.OK, ok);
		courseStats.put(StatItem.MP, mp);
		courseStats.put(StatItem.DNS, dns);
		courseStats.put(StatItem.DNF, dnf);
		courseStats.put(StatItem.DSQ, dsq);
		courseStats.put(StatItem.NDA, nda);
		courseStats.put(StatItem.UNK, unk);
		courseStats.put(StatItem.DUP, dup);
		int unresolved = nda + unk + dup;
		courseStats.put(StatItem.Registered, total);
		courseStats.put(StatItem.Present, (total - dns));
		courseStats.put(StatItem.Unresolved, unresolved);
		courseStats.put(StatItem.Finished, (total - dns - unresolved));
	}

	private void updateCourseStats(Map<StatItem, Integer> courseStats, int total) {
		int dns = courseStats.get(StatItem.DNS);
		int nda = courseStats.get(StatItem.NDA);
		int unk = courseStats.get(StatItem.UNK);
		int dup = courseStats.get(StatItem.DUP);
		int unresolved = nda + unk + dup;
		courseStats.put(StatItem.Registered, total);
		courseStats.put(StatItem.Present, (total - dns));
		courseStats.put(StatItem.Unresolved, unresolved);
		courseStats.put(StatItem.Finished, (total - dns - unresolved));
	}

	private int inc(StatItem item, Map<StatItem, Integer> map) {
		int value = map.get(item) + 1;
		map.put(item, value);
		return value;
	}

	private int dec(StatItem item, Map<StatItem, Integer> map) {
		int value = map.get(item) - 1;
		map.put(item, value);
		return value;
	}


	@Override
	public void runnerCreated(RunnerRaceData data) {
		StatItem item = convertStatus(data.getStatus());
		
		Map<StatItem, Integer> courseStats = getCourseStatsFor(data.getCourse().getName());
		inc(item, courseStats);
		int courseTotal = inc(StatItem.Registered, courseStats);
		updateCourseStats(courseStats, courseTotal);
		
		inc(item, getTotalCourse());
		int total = inc(StatItem.Registered, getTotalCourse());
		updateCourseStats(getTotalCourse(), total);
	}

	@Override
	public void runnerDeleted(RunnerRaceData data) {
		StatItem item = convertStatus(data.getStatus());
		
		Map<StatItem, Integer> courseStats = getCourseStatsFor(data.getCourse().getName());
		dec(item, courseStats);
		int courseTotal = dec(StatItem.Registered, courseStats);
		updateCourseStats(courseStats, courseTotal);
		
		dec(item, getTotalCourse());
		int total = dec(StatItem.Registered, getTotalCourse());
		updateCourseStats(getTotalCourse(), total);
	}

	@Override
	public void statusChanged(RunnerRaceData data, Status oldStatus) {
		StatItem item = convertStatus(data.getStatus());
		StatItem oldItem = convertStatus(oldStatus);
		
		Map<StatItem, Integer> courseStats = getCourseStatsFor(data.getCourse().getName());
		inc(item, courseStats);
		dec(oldItem, courseStats);
		int courseTotal = getCourseStatsFor(data.getCourse().getName(), StatItem.Registered);
		updateCourseStats(courseStats, courseTotal);
		
		inc(item, getTotalCourse());
		dec(oldItem, getTotalCourse());
		int total = getCourseStatsFor(totalName(), StatItem.Registered);
		updateCourseStats(getTotalCourse(), total);
	}

	@Override
	public void courseChanged(Runner runner, Course oldCourse) {
		computeCourseStats(oldCourse);
		computeCourseStats(runner.getCourse());
	}

	@Override
	public void categoriesChanged() {}

	@Override
	public void clubsChanged() {}

	@Override
	public void coursesChanged() {
		fullUpdate();
	}

	@Override
	public void runnersChanged() {
		fullUpdate();		
	}
	
	@Override
	public void changed(Stage previous, Stage next) {
		fullUpdate();
	}

	@Override
	public void saving(Stage stage, Properties properties) {	}

	@Override
	public void closing(Stage stage) {	}

	
}
