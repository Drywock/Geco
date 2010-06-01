/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import valmo.geco.core.Geco;
import valmo.geco.core.Util;
import valmo.geco.core.Announcer.RunnerListener;
import valmo.geco.model.Course;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class LogPanel extends TabPanel implements RunnerListener {

	private JTextArea chiplogArea;
	
	
	/**
	 * @param geco
	 * @param frame
	 */
	public LogPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		initPanels(this);
		geco().announcer().registerRunnerListener(this);
	}

	
	public void initPanels(JPanel panel) {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		splitPane.setOneTouchExpandable(true);
		splitPane.add(initChipPanel());
		splitPane.add(initStatsPanel());
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		panel.add(splitPane);
	}
	
	public JPanel initChipPanel() {
		chiplogArea = new JTextArea(30, 80);
		chiplogArea.setEditable(false);
		chiplogArea.setLineWrap(true);
		chiplogArea.setText("Waiting for data...");
		JButton clearB = new JButton("Clear log view");
		clearB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chiplogArea.setText("");
			}
		});
		JPanel chipPanel = new JPanel(new BorderLayout());
		chipPanel.add(Util.embed(clearB), BorderLayout.NORTH);
		chipPanel.add(new JScrollPane(chiplogArea), BorderLayout.CENTER);
		return chipPanel;
	}

	
	/**
	 * @param announcer 
	 * @return
	 */
	private JPanel initStatsPanel() {
		return new StatsPanel(geco(), frame());
	}
	
	public void displayLog(String message) {
		chiplogArea.append("\n");
		chiplogArea.append(message);
	}

	
	/* (non-Javadoc)
	 * @see valmo.geco.core.Announcer.RunnerListener#courseChanged(valmo.geco.model.Runner, valmo.geco.model.Course)
	 */
	@Override
	public void courseChanged(Runner runner, Course oldCourse) {
	}


	/* (non-Javadoc)
	 * @see valmo.geco.core.Announcer.RunnerListener#runnerCreated(valmo.geco.model.RunnerRaceData)
	 */
	@Override
	public void runnerCreated(RunnerRaceData runner) {
	}


	/* (non-Javadoc)
	 * @see valmo.geco.core.Announcer.RunnerListener#runnerDeleted(valmo.geco.model.RunnerRaceData)
	 */
	@Override
	public void runnerDeleted(RunnerRaceData runner) {
	}


	/* (non-Javadoc)
	 * @see valmo.geco.core.Announcer.RunnerListener#statusChanged(valmo.geco.model.RunnerRaceData, valmo.geco.model.Status)
	 */
	@Override
	public void statusChanged(RunnerRaceData runner, Status oldStatus) {
	}


	/* (non-Javadoc)
	 * @see valmo.geco.core.Announcer.RunnerListener#cardRead(java.lang.String)
	 */
	@Override
	public void cardRead(String chip) {
		chiplogArea.append("\n");
		chiplogArea.append(chip);
	}


	/* (non-Javadoc)
	 * @see valmo.geco.core.Announcer.RunnerListener#runnersChanged()
	 */
	@Override
	public void runnersChanged() {
	}

	
}
