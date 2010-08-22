/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.util.Vector;

import valmo.geco.core.TimeManager;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Aug 22, 2010
 *
 */
public class PoolMerger extends Control {

	/**
	 * @param gecoControl
	 */
	public PoolMerger(GecoControl gecoControl) {
		super(gecoControl);
	}

	/**
	 * @param poolStages
	 */
	public void merge(Vector<Stage> poolStages) {
		for (RunnerRaceData runnerData : registry().getRunnersData()) {
			mergeRunnerData(runnerData, poolStages);
		}
	}

	private void mergeRunnerData(RunnerRaceData runnerData, Vector<Stage> poolStages) {
		long mergedTime = 0;
		Status mergedStatus = Status.OK;
		for (Stage stage : poolStages) {
			RunnerRaceData poolData = stage.registry().findRunnerData(runnerData.getRunner().getChipnumber());
			mergedTime = mergeTime(mergedTime, poolData.getResult().getRacetime());
			mergedStatus = mergeStatus(mergedStatus, poolData.getResult().getStatus());
		}
		runnerData.getResult().setRacetime(mergedTime);
		runnerData.getResult().setStatus(mergedStatus);
	}

	private long mergeTime(long mergedTime, long racetime) {
		if( racetime==TimeManager.NO_TIME_l ) {
			return TimeManager.NO_TIME_l;
		} else {
			return mergedTime + racetime;
		}
	}

	private Status mergeStatus(Status mergedStatus, Status poolStatus) {
		if( mergedStatus.equals(Status.OK) ) {
			return poolStatus;
		} else {
			return mergedStatus;
		}
	}

}
