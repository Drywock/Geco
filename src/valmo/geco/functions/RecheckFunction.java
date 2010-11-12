/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.functions;

import valmo.geco.control.GecoControl;
import valmo.geco.control.RunnerControl;
import valmo.geco.core.Messages;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class RecheckFunction extends AbstractRunnerFunction {

	public RecheckFunction(GecoControl gecoControl) {
		super(gecoControl);
	}

	@Override
	public String toString() {
		return Messages.uiGet("RecheckFunction.RecheckTitle"); //$NON-NLS-1$
	}

	@Override
	public String executeTooltip() {
		return Messages.uiGet("RecheckFunction.ExecuteTooltip"); //$NON-NLS-1$
	}

	@Override
	protected boolean acceptRunnerData(RunnerRaceData runnerRaceData) {
		return runnerRaceData.statusIsRecheckable();
	}

	@Override
	public void execute() {
		RunnerControl runnerControl = getService(RunnerControl.class);
		for (RunnerRaceData runnerData : selectedRunners()) {
			if( runnerData.statusIsRecheckable() ){
				runnerControl.recheckRunner(runnerData);
			}
		}
	}


}
