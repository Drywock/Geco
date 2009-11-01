/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import valmo.geco.control.RunnerControl;
import valmo.geco.core.Geco;
import valmo.geco.core.TimeManager;
import valmo.geco.core.Util;
import valmo.geco.model.Course;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public class RunnerPanel extends GecoPanel {
	/*
	 * Dev note: since extracting the RunnerControl from RunnerPanel, there remains some
	 * strong implicit dependencies between both (ie how values are checked, what does true/false
	 * means when validateXXX() returns and when to refresh or not a UI component).
	 * Not so good. 
	 */

	private RunnerRaceData runnerData;
	private Runner runner;
	private RunnersPanel parentContainer;
	private PunchPanel punchPanel;
	
	private JTextField startF;
	private JTextField chipF;
	private JTextField fNameF;
	private JTextField lNameF;
	private JComboBox clubCB;
	private JComboBox catCB;
	private JComboBox courseCB;
	private JCheckBox ncB;
	private JTextField cTimeF;
	private JTextField eTimeF;
	private JTextField sTimeF;
	private JTextField fTimeF;
	private JTextField rTimeF;
	private JLabel realTimeL;
	private JComboBox statusCB;
	private JTextField mpF;
	private JLabel penaltyL;
	private JButton resetRTimeB;
	private JButton recheckStatusB;
	
	// this really is a hack, a flag set up while refreshing the panel so that listeners on comboboxes
	// will do nothing when activated by refreshing the combo boxes selected item.  
	private boolean inRefreshMode;

	
	public RunnerPanel(Geco geco, JFrame frame, RunnersPanel parent) {
		super(geco, frame);
		this.parentContainer = parent;
		this.punchPanel = new PunchPanel(geco, frame);
		createComponents();
		createListeners();
		initPanel(this);
	}
	
	private RunnerControl control() {
		return geco().runnerControl();
	}
	
	public void updateRunner(String chipnumber) {
		this.runnerData = registry().findRunnerData(chipnumber);
		this.runner = runnerData.getRunner();
		refreshPanel();
	}
	
	/**
	 * 
	 */
	public void refreshPanel() {
		this.inRefreshMode = true; // happy hack :(
		startF.setText(Integer.toString(runner.getStartnumber()));
		chipF.setText(runner.getChipnumber());
		fNameF.setText(runner.getFirstname());
		lNameF.setText(runner.getLastname());
		clubCB.setSelectedItem(runner.getClub().getName());
		catCB.setSelectedItem(runner.getCategory().getShortname());
		courseCB.setSelectedItem(runner.getCourse().getName());
		ncB.setSelected(runner.isNC());
		eTimeF.setText(TimeManager.fullTime(runnerData.getErasetime()));
		cTimeF.setText(TimeManager.fullTime(runnerData.getControltime()));
		sTimeF.setText(TimeManager.fullTime(runnerData.getStarttime()));
		fTimeF.setText(TimeManager.fullTime(runnerData.getFinishtime()));
		rTimeF.setText(TimeManager.time(runnerData.getResult().getRacetime()));
		realTimeL.setText(TimeManager.time(geco().checker().computeRaceTime(runnerData)));
		statusCB.setSelectedItem(runnerData.getResult().getStatus());
		mpF.setText(Integer.toString(runnerData.getResult().getNbMPs()));
		penaltyL.setText(Integer.toString(0)); // TODO: compute penalty here???
		punchPanel.refreshPunches(runnerData);
		this.inRefreshMode = false;
	}
	
	
	public void createComponents() {
		startF = new JTextField(2);
		chipF = new JTextField(7);
		fNameF = new JTextField(10);
		lNameF = new JTextField(10);
		clubCB = new JComboBox(registry().getClubnames());
		catCB = new JComboBox(registry().getCategorynames());
		courseCB = new JComboBox(registry().getCoursenames());
		ncB = new JCheckBox("NC");
		
		cTimeF = new JTextField(5);
		cTimeF.setEditable(false);
		eTimeF = new JTextField(5);
		eTimeF.setEditable(false);
		sTimeF = new JTextField(5);
		sTimeF.setEditable(false);
		fTimeF = new JTextField(5);
		fTimeF.setEditable(false);
		rTimeF = new JTextField(5);
		realTimeL = new JLabel();
		statusCB = new JComboBox(Status.values());
		mpF = new JTextField();
		penaltyL = new JLabel();
		
		resetRTimeB = new JButton("Reset Time");
		recheckStatusB = new JButton("Refresh Status");
	}
	
	public void createListeners() {
		startF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Integer newStart = new Integer(startF.getText());
					if( control().validateStartnumber(runner, newStart)) {
						parentContainer.refreshSelectionInTable();
					} else {
						JOptionPane.showMessageDialog(frame(),
							    "Start number already used. Reverting to previous start number.",
							    "Invalid Entry",
							    JOptionPane.ERROR_MESSAGE);
					}					
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(frame(),
						    "Bad format for start number.",
						    "Invalid Entry",
						    JOptionPane.ERROR_MESSAGE);
				}
				startF.setText(Integer.toString(runner.getStartnumber()));
			}
		});
		chipF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newChip = chipF.getText();
				if( control().validateChipnumber(runner, newChip)) {
					parentContainer.refreshSelectionInTable();
				} else {
					JOptionPane.showMessageDialog(frame(),
						    "Chip number already in use. Reverting to previous chip number.",
						    "Invalid Entry",
						    JOptionPane.ERROR_MESSAGE);
					chipF.setText(runner.getChipnumber());
				}
			}
		});
		fNameF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( control().validateFirstname(runner, fNameF.getText()) ) {
					parentContainer.refreshSelectionInTable();					
				}
			}
		});
		lNameF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newName = lNameF.getText();
				if( control().validateLastname(runner, newName)) {
					parentContainer.refreshSelectionInTable();
				} else {
					JOptionPane.showMessageDialog(frame(),
						    "Last name can not be empty. Reverting.",
						    "Invalid Entry",
						    JOptionPane.ERROR_MESSAGE);
					lNameF.setText(runner.getLastname());
				}
			}
		});
		clubCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( !inRefreshMode ) {
					if( control().validateClub(runner, (String) clubCB.getSelectedItem())) {
						parentContainer.refreshSelectionInTable();						
					}
				}
			}
		});
		catCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( !inRefreshMode ) {
					String newCat = (String) catCB.getSelectedItem();
					if( control().validateCategory(runner, newCat)) {
						parentContainer.refreshSelectionInTable();						
					}
				}
			}
		});
		courseCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( !inRefreshMode ) {
					Course oldCourse = runner.getCourse();
					String newCourse = (String) courseCB.getSelectedItem();
					if( !oldCourse.getName().equals(newCourse) ) {
						int confirm = JOptionPane.showConfirmDialog(frame(), "This will trigger a status check. Please confirm the change of runner's course?", "Update Runner", JOptionPane.YES_NO_OPTION);
						if( confirm==JOptionPane.YES_OPTION ) {
							if( control().validateCourse(runnerData, newCourse)) {
								parentContainer.refreshSelectionInTable();
							}
						}
						refreshPanel(); // refresh panel in any case
					}					
				}
			}
		});
		ncB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				control().validateNCStatus(runner, ncB.isSelected());
			}
		});
		
//		eTimeF.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				try {
//					Date newTime = TimeManager.userParse(eTimeF.getText());
//					runnerData.setErasetime(newTime);
//					eTimeF.setText(TimeManager.fullTime(newTime)); // reformat the entry
//				} catch (ParseException e1) {
//					showBadTimeDialog();
//					eTimeF.setText(TimeManager.fullTime(runnerData.getErasetime()));
//				}
//			}
//		});
//		cTimeF.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				try {
//					Date newTime = TimeManager.userParse(cTimeF.getText());
//					runnerData.setControltime(newTime);
//					cTimeF.setText(TimeManager.fullTime(newTime)); // reformat the entry
//				} catch (ParseException e1) {
//					showBadTimeDialog();
//					cTimeF.setText(TimeManager.fullTime(runnerData.getControltime()));
//				}
//			}
//		});
//		sTimeF.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				try {
//					Date newTime = TimeManager.userParse(sTimeF.getText());
//					runnerData.setStarttime(newTime);
//					sTimeF.setText(TimeManager.fullTime(newTime)); // reformat the entry
//				} catch (ParseException e1) {
//					showBadTimeDialog();
//					sTimeF.setText(TimeManager.fullTime(runnerData.getStarttime()));
//				}
//			}
//		});
//		fTimeF.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				try {
//					Date newTime = TimeManager.userParse(fTimeF.getText());
//					runnerData.setFinishtime(newTime);
//					fTimeF.setText(TimeManager.fullTime(newTime)); // reformat the entry
//				} catch (ParseException e1) {
//					showBadTimeDialog();
//					fTimeF.setText(TimeManager.fullTime(runnerData.getFinishtime()));
//				}
//			}
//		});
		rTimeF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( control().validateRaceTime(runnerData, rTimeF.getText()) ){
					parentContainer.refreshSelectionInTable();
				} else {
					showBadTimeDialog();
				}
				// reformat the entry from current data anyway
				rTimeF.setText( TimeManager.time(runnerData.getResult().getRacetime()) );
			}
		});
		resetRTimeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( control().resetRaceTime(runnerData) ){
					// reformat the entry
					rTimeF.setText( TimeManager.time(runnerData.getResult().getRacetime()) );
					parentContainer.refreshSelectionInTable();					
				}
			}
		});
		
		statusCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( !inRefreshMode ) {
					Status newStatus = (Status) statusCB.getSelectedItem();
					if( !newStatus.equals(runnerData.getResult().getStatus()) ) {
						int confirm = JOptionPane.showConfirmDialog(frame(), "Please confirm the change of runner's status", "Update Runner", JOptionPane.YES_NO_OPTION);
						if( confirm==JOptionPane.YES_OPTION ) {
							if( control().validateStatus(runnerData, newStatus)) {
								parentContainer.refreshSelectionInTable();	
							}
						} else {
							refreshPanel(); //reset status CB
						}
					}	
				}
			}
		});
		recheckStatusB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( control().resetStatus(runnerData) ){
					refreshPanel();
					parentContainer.refreshSelectionInTable();					
				}
			}
		});
	}
	
	private void showBadTimeDialog() {
		JOptionPane.showMessageDialog(frame(),
			    "Bad time format. Reverting.",
			    "Invalid Entry",
			    JOptionPane.ERROR_MESSAGE);
	}

	public JPanel initPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		panel.add(Util.embed(initRunnerPanel(new JPanel())), BorderLayout.WEST);
		panel.add(Util.embed(this.punchPanel), BorderLayout.EAST);
		return panel;
	}

	
	public JPanel initRunnerPanel(JPanel panel) {
		panel.setLayout(new GridLayout(0,5)); // TODO: use GridBagLayout
		panel.setBorder(BorderFactory.createTitledBorder("Runner"));

		Component[] comps = new Component[] {
				new JLabel("Startnumber"),
				new JLabel("Chip"),
				new JLabel("First name"),
				new JLabel("Last name"),
				Box.createGlue(),
				startF,
				chipF,
				fNameF,
				lNameF,
				Box.createGlue(),
				
				new JLabel("Club"),
				new JLabel("Category"),
				new JLabel("Course"),
				Box.createGlue(),
				Box.createGlue(),
				clubCB,
				catCB,
				courseCB,
				ncB,
				Box.createGlue(),
				
				Box.createGlue(),
				Box.createGlue(),
				Box.createGlue(),
				Box.createGlue(),
				Box.createGlue(),
				
				new JLabel("Erase time"),
				new JLabel("Control time"),
				new JLabel("Start time"),
				new JLabel("Finish time"),
				new JLabel("Real race time"),
				eTimeF,
				cTimeF,
				sTimeF,
				fTimeF,
				realTimeL,

				recheckStatusB,
				new JLabel("Status"),
				new JLabel("MPs"),
				new JLabel("Time penalty"),
				new JLabel("Race time"),
				resetRTimeB,
				statusCB,
				mpF,
				penaltyL,
				rTimeF,
		};
		
		for (int i = 0; i < comps.length; i++) {
			panel.add(comps[i]);
		}
		return panel;
	}
	
}
