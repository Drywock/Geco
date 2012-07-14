/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.geco.control.MergeControl;
import net.geco.framework.IGeco;
import net.geco.model.Messages;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.ui.basics.PunchPanel;

/**
 * @author Simon Denier
 * @since Jul 5, 2012
 *
 */
public class MergeWizard extends JDialog {

	private RunnerRaceData ecardData;
	private Runner sourceRunner;

	private IGeco geco;
	private MergeControl mergeControl;

	private ECardBoard ecardBoard;
	private RegistryBoard registryBoard;
	private PunchPanel punchPanel;
	private ArchiveBoard archiveBoard;

	
	public MergeWizard(IGeco geco, JFrame frame, String title) {
		super(frame, title, true);
		this.geco = geco;
		this.mergeControl = geco.mergeControl();
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
		setResizable(false);
		add(new JLabel("Merge Runner"), BorderLayout.NORTH);
		add(createMergePanel(), BorderLayout.CENTER);
		add(createPunchPanel(), BorderLayout.EAST);
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel createMergePanel() {
		JPanel mergePanel = new JPanel(new GridBagLayout());
		ecardBoard = new ECardBoard(this, mergePanel, 0);
		registryBoard = new RegistryBoard(this, mergePanel, 4);
		archiveBoard = new ArchiveBoard(this, mergePanel, 9);
		return mergePanel;
	}
	
	private JPanel createPunchPanel() {
		punchPanel = new PunchPanel();
//		punchPanel.setBorder(BorderFactory.createTitledBorder("Trace"));
		return punchPanel;
	}
	
	public void close() {
//		mergedCard = null;
		setVisible(false);
	}

	public void closeAfterCreate() {
		// TODO: log creation ?
//		geco.log("Creation " + runnerData.infoString()); //$NON-NLS-1$
		close();
	}
	
	public void closeAfterMerge() {
		askForRunnerDeletion();
		// TODO: log deletion, merge ?
		close();
	}

	public void closeAfterInsert() {
		askForRunnerDeletion();
		// TODO log insert
		close();
	}

	private void askForRunnerDeletion() {
		if( sourceRunner != null ) {// offer to delete source runner if applicable
			int confirm = JOptionPane.showConfirmDialog(
							this,
							Messages.uiGet("MergeRunnerDialog.RunnerDeletionLabel") + sourceRunner.idString(), //$NON-NLS-1$
							Messages.uiGet("MergeRunnerDialog.RunnerDeletionTitle"), //$NON-NLS-1$
							JOptionPane.YES_NO_OPTION);
			if( confirm == JOptionPane.YES_OPTION ) {
				mergeControl().deleteRunner(sourceRunner);
			}
		}
	}

	public void showMergeRunner(RunnerRaceData data) {
		sourceRunner = data.getRunner();
		initMockRunner(data, sourceRunner.getEcard());
		updatePanels();
		setVisible(true);		
	}
	
	private void initMockRunner(RunnerRaceData data, String ecard) {
		this.ecardData = data;
		Runner mockRunner = mergeControl().buildMockRunner();
		mockRunner.setEcard(ecard);
		mockRunner.setCourse(data.getCourse());
		data.setRunner(mockRunner);
	}

	public void updatePanels() {
		ecardBoard.updatePanel();
		registryBoard.updatePanel();
		archiveBoard.updatePanel();
		punchPanel.refreshPunches(this.ecardData);
	}
	
	public void updateResults() {
		ecardBoard.updateResults();
		punchPanel.refreshPunches(this.ecardData);		
	}

	protected Registry registry() {
		return geco.registry();
	}

	protected MergeControl mergeControl() {
		return mergeControl;
	}
	
	protected RunnerRaceData getECardData() {
		return ecardData;
	}
	
	protected Runner getSourceRunner() {
		return sourceRunner;
	}

	public void catchException(IOException ex) {
		JOptionPane.showMessageDialog(
				this,
				ex.toString(),
				"Exception in wizard",
				JOptionPane.ERROR_MESSAGE);
	}
	
}
