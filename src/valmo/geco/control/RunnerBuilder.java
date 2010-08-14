/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import valmo.geco.core.Announcer;
import valmo.geco.model.Factory;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Aug 21, 2009
 *
 */
public class RunnerBuilder extends Control {
	
	public RunnerBuilder(Factory factory, Stage stage) {
		super(factory);
		setStage(stage);
	}

	protected RunnerBuilder(Factory factory, Stage stage, Announcer announcer) {
		super(factory, stage, announcer);
	}

	public RunnerRaceData buildRunnerData() {
		RunnerRaceData data = factory().createRunnerRaceData();
		data.setResult(factory().createRunnerResult());
		return data;
	}
	
	public RunnerRaceData registerRunnerDataFor(Runner runner, RunnerRaceData runnerData) {
		runnerData.setRunner(runner);
		registry().addRunnerData(runnerData);
		return runnerData;
	}

	
	public void checkGecoData(PenaltyChecker checker) {
		checkNoDataRunners();
		// compute trace for data
		for (RunnerRaceData raceData : registry().getRunnersData()) {
			checker.computeStatus(raceData);	
		}
	}
	
	public void checkOrData(PenaltyChecker checker) {
		checkNoDataRunners();
		// compute status and trace for data
		for (RunnerRaceData raceData : registry().getRunnersData()) {
			// Special runner status (DNS) should have been set before this point
			if( raceData.getResult()==null ) {
				checker.check(raceData);	
			}
		}
	}
	
	public void checkNoDataRunners() {
		for (Runner runner : registry().getRunners()) {
			if( registry().findRunnerData(runner) == null ) {
				registerRunnerDataFor(runner, buildRunnerData());
			}
		}
	}

}
