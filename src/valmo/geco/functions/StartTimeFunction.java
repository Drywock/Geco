/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.functions;

import java.awt.Component;
import java.text.ParseException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import valmo.geco.basics.TimeManager;
import valmo.geco.control.GecoControl;
import valmo.geco.control.SIReaderHandler;
import valmo.geco.model.Messages;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class StartTimeFunction extends AbstractRunnerFunction {

	private JCheckBox resetAllRunnersC;
	private JTextField starttimeF;

	public StartTimeFunction(GecoControl gecoControl) {
		super(gecoControl);
	}

	@Override
	public String toString() {
		return Messages.uiGet("StartTimeFunction.StarttimeTitle"); //$NON-NLS-1$
	}

	@Override
	public String executeTooltip() {
		return Messages.uiGet("StartTimeFunction.ExecuteTooltip"); //$NON-NLS-1$
	}

	@Override
	public void execute() {
		try {
			Date startTime = TimeManager.userParse(starttimeF.getText());
			String start = TimeManager.time(startTime);
			for (RunnerRaceData r : selectedRunners()) {
				Runner runner = r.getRunner(); // TODO: Function works on Runner, not RunnerRaceData anymore, refactor
				if( runner.getRegisteredStarttime().equals(TimeManager.NO_TIME) || resetAllRunnersC.isSelected() ){
					Date oldStart = runner.getRegisteredStarttime();
					runner.setRegisteredStarttime(startTime);
					if( ! oldStart.equals(TimeManager.NO_TIME) ){
						geco().log(Messages.uiGet("StartTimeFunction.StarttimeChangeMessage1") + start + Messages.uiGet("StartTimeFunction.StarttimeChangeMessage2") + runner.toString() //$NON-NLS-1$ //$NON-NLS-2$
								+ Messages.uiGet("StartTimeFunction.StarttimeChangeMessage3") + TimeManager.time(oldStart) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						geco().log(Messages.uiGet("StartTimeFunction.StarttimeSetMessage1") + start + Messages.uiGet("StartTimeFunction.StarttimeSetMessage2") + runner.toString()); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}			
		} catch (ParseException e) {
			geco().info(e.getLocalizedMessage(), true);
		}
	}

	private SIReaderHandler siHandler() {
		return geco().getService(SIReaderHandler.class);
	}
	
	private String getDefaultStarttime() {
		long zeroTime = siHandler().getZeroTime();
		return TimeManager.time(zeroTime);
	}

	@Override
	public JComponent getParametersConfig() {
		resetAllRunnersC = new JCheckBox(Messages.uiGet("StartTimeFunction.ResetStarttimeLabel")); //$NON-NLS-1$
		resetAllRunnersC.setToolTipText(
				Messages.uiGet("StartTimeFunction.ResetStarttimeTooltip1") //$NON-NLS-1$
				+ Messages.uiGet("StartTimeFunction.ResetStarttimeTooltip2")); //$NON-NLS-1$
		starttimeF = new JTextField(6);
		starttimeF.setText(getDefaultStarttime());

		resetAllRunnersC.setAlignmentX(Component.LEFT_ALIGNMENT);
		starttimeF.setMaximumSize(starttimeF.getPreferredSize());		
		starttimeF.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		Box box = Box.createVerticalBox();
		box.add(resetAllRunnersC);
		box.add(Box.createVerticalStrut(5));
		box.add(starttimeF);
		box.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		box.setAlignmentY(Component.TOP_ALIGNMENT);
		
		JComponent config = super.getParametersConfig();
		config.add(box);
		return config;
	}

	@Override
	public void updateUI() {
		super.updateUI();
		starttimeF.setText(getDefaultStarttime());
	}

}
