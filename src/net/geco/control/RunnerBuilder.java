/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Aug 21, 2009
 *
 */
public class RunnerBuilder extends BasicControl {
	
	public RunnerBuilder(Factory factory) {
		super(factory);
	}

	public RunnerRaceData buildRunnerData() {
		RunnerRaceData data = factory().createRunnerRaceData();
		data.setResult(factory().createRunnerResult());
		return data;
	}
	
	public RunnerRaceData registerRunnerDataFor(Registry registry, Runner runner, RunnerRaceData runnerData) {
		runnerData.setRunner(runner);
		registry.addRunnerData(runnerData);
		return runnerData;
	}
	
	public void checkGecoData(Stage currentStage, Checker checker) {
		checkNoDataRunners(currentStage.registry());
		for (RunnerRaceData raceData : currentStage.registry().getRunnersData()) {
			if( raceData.hasTrace() ) {
				// compute trace, mps, penalties for race data
				// TODO: non functional, bad smell
				checker.computeStatus(raceData);
				checker.computeOfficialRaceTime(raceData);
			}
		}
	}
	
	public void checkNoDataRunners(Registry registry) {
		for (Runner runner : registry.getRunners()) {
			if( registry.findRunnerData(runner) == null ) {
				registerRunnerDataFor(registry, runner, buildRunnerData());
			}
		}
	}

}
