/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;

import valmo.geco.Geco;
import valmo.geco.control.IResultBuilder;
import valmo.geco.control.ResultBuilder;
import valmo.geco.control.ResultBuilder.ResultConfig;
import valmo.geco.core.Announcer.StageConfigListener;
import valmo.geco.model.ResultType;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public class ResultsPanel extends TabPanel implements StageConfigListener {
	
	private static final int AutoexportDelay = 60;
	
	private Vector<String> coursenames;
	private Vector<String> categorynames;
	private JTextPane resultTA;
	
	private JComboBox resultTypeCB;
	private JRadioButton rankingResultRB;
	private JRadioButton splitResultRB;

	private JButton refreshB;
	private JButton exportB;

	private JCheckBox showNcC;
	private JCheckBox showOtC;
	private JCheckBox showEsC;
	private JCheckBox showPeC;

	private String exportFormat;
	private JFileChooser filePane;

	private JButton selectAllB;
	private JButton selectNoneB;
	private JList poolList;
	
	private Thread autoexportThread;
	private JButton autoexportB;
	private JSpinner autodelayS;
	private JRadioButton refreshRB;


	/**
	 * @param geco
	 * @param frame 
	 * @param announcer 
	 */
	public ResultsPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		updateNames();
		initResultsPanel(this);
		initFileDialog();
		createListeners();
		geco().announcer().registerStageConfigListener(this);
	}

	private void updateNames() {
		coursenames = registry().getSortedCoursenames();
		categorynames = registry().getSortedCategorynames();
	}
	private void updateCourseList() {
		poolList.setModel(new AbstractListModel() {
			public int getSize() {
				return coursenames.size();
			}
			public Object getElementAt(int index) {
				return coursenames.get(index);
			}
		});
		selectAllPools();
	}
	private void updateCategoryList() {
		poolList.setModel(new AbstractListModel() {
			public int getSize() {
				return categorynames.size();
			}
			public Object getElementAt(int index) {
				return categorynames.get(index);
			}
		});
		selectAllPools();
	}
	private void selectAllPools() {
		poolList.setSelectionInterval(0, poolList.getModel().getSize() - 1);
	}
	private void selectNoPool() {
		poolList.clearSelection();
	}
	private ResultType getResultType() {
		return (ResultType) resultTypeCB.getSelectedItem();
	}
	private boolean showCourses() {
		return getResultType() == ResultType.CourseResult;
	}
	private ResultConfig createResultConfig() {
		return ResultBuilder.createResultConfig(
				poolList.getSelectedValues(), 
				getResultType(),
				showEsC.isSelected(),
				showNcC.isSelected(),
				showOtC.isSelected(),
				showPeC.isSelected());
	}

	public IResultBuilder resultBuilder() {
		if( rankingResultRB.isSelected() ) {
			return geco().resultBuilder();
		} else {
			return geco().splitsBuilder();
		}
	}

	public void createListeners() {
		resultTypeCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch (getResultType()) {
				case CourseResult:
					updateCourseList();
					break;
				case CategoryResult:
				case MixedResult:
					updateCategoryList();
					break;
				}
			}
		});
		selectAllB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAllPools();
			}
		});
		selectNoneB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNoPool();
			}
		});
		refreshB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshResultView();
			}
		});
		exportB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String resultFile;
				if( rankingResultRB.isSelected() ) {
					resultFile = geco().getCurrentStagePath() + File.separator + "ranking";
				} else {
					resultFile = geco().getCurrentStagePath() + File.separator + "splits";
				}
				filePane.setSelectedFile(new File(resultFile).getAbsoluteFile());
				int response = filePane.showSaveDialog(frame());
				if( response==JFileChooser.APPROVE_OPTION ) {
					String filename = filePane.getSelectedFile().getAbsolutePath();
					try {
						resultBuilder().exportFile(filename, exportFormat, createResultConfig(), -1);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(frame(), "Error while saving " + filename + "(" + ex +")",
								"Export Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		autoexportB.addActionListener(new ActionListener() {
			private Color defaultColor;
			@Override
			public void actionPerformed(ActionEvent e) {
				if( autoexportB.isSelected() ) {
					autoexportB.setSelected(false);
					autoexportB.setBackground(defaultColor);
					autodelayS.setEnabled(true);
					stopAutoexport();
				} else {
					autoexportB.setSelected(true);
					defaultColor = autoexportB.getBackground();
					autoexportB.setBackground(Color.GREEN);
					autodelayS.setEnabled(false);
					startAutoexport();
				}
			}
		});
	}

	/**
	 * @param panel
	 */
	public void initResultsPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		JPanel resultSelectionPanel = initSelectionPanel();
		JTextPane resultTA = initResultPanel();
		JScrollPane scrollPane = new JScrollPane(resultTA);
		panel.add(resultSelectionPanel, BorderLayout.WEST);
		panel.add(scrollPane, BorderLayout.CENTER);
	}

	private JPanel initSelectionPanel() {

		// Commands: options and actions
		resultTypeCB = new JComboBox(ResultType.values());

		rankingResultRB = new JRadioButton("Ranking");
		splitResultRB = new JRadioButton("Splits");
		ButtonGroup builderGroup = new ButtonGroup();
		builderGroup.add(rankingResultRB);
		builderGroup.add(splitResultRB);
		builderGroup.setSelected(rankingResultRB.getModel(), true);
		
		showNcC = new JCheckBox("Show NC");
		showOtC = new JCheckBox("Show Others");
		showPeC = new JCheckBox("Show Penalties");
		showEsC = new JCheckBox("Show Empty Sets");
		JPanel optionsPanel = new JPanel(new GridLayout(0, 2));
		optionsPanel.add(showNcC);
		optionsPanel.add(showOtC);
		optionsPanel.add(showPeC);
		optionsPanel.add(showEsC);
		
		refreshB = new JButton("Refresh");
		exportB = new JButton("Export");
		JButton printB = new JButton("Print");
		
		printB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					refreshResultView();
					resultTA.print();
				} catch (PrinterException e1) {
					JOptionPane.showMessageDialog(frame(), "Fail to print", "Printing Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		// Layout of Command panel
		JPanel commandPanel = new JPanel();
		commandPanel.setBorder(BorderFactory.createTitledBorder("Commands"));
		commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.Y_AXIS));
		commandPanel.add(SwingUtils.embed(resultTypeCB));
		commandPanel.add(SwingUtils.makeButtonBar(FlowLayout.CENTER, rankingResultRB, splitResultRB));
		commandPanel.add(Box.createVerticalStrut(10));
		commandPanel.add(optionsPanel);
		commandPanel.add(Box.createVerticalStrut(10));
		commandPanel.add(SwingUtils.makeButtonBar(FlowLayout.CENTER, refreshB, exportB, printB));

		
		// Pool list
		selectAllB = new JButton("All");
		selectNoneB = new JButton("None");
		JPanel listButtonsPanel = new JPanel();
		listButtonsPanel.setLayout(new BoxLayout(listButtonsPanel, BoxLayout.Y_AXIS));
		listButtonsPanel.add(SwingUtils.embed(selectAllB));
		listButtonsPanel.add(SwingUtils.embed(selectNoneB));

		poolList = new JList(coursenames);
		selectAllPools();
		JScrollPane scrollPane = new JScrollPane(poolList);
		scrollPane.setPreferredSize(new Dimension(150, 250));
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(SwingUtils.embed(scrollPane), BorderLayout.CENTER);
		listPanel.add(SwingUtils.embed(listButtonsPanel), BorderLayout.EAST);

		
		// Automode
		JPanel autoPanel = new JPanel(new GridLayout(0, 2));
		autoPanel.setBorder(BorderFactory.createTitledBorder("Automode"));

		ButtonGroup autoGroup = new ButtonGroup();
		refreshRB = new JRadioButton("Refresh");
		JRadioButton exportRB = new JRadioButton("Export");
		autoGroup.add(refreshRB);
		autoGroup.add(exportRB);
		refreshRB.setSelected(true);
		autoPanel.add(SwingUtils.embed(refreshRB));
		autoPanel.add(SwingUtils.embed(exportRB));

		autoexportB = new JButton("Auto");
		autodelayS = new JSpinner(new SpinnerNumberModel(AutoexportDelay, 1, null, 10));
		autodelayS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		autodelayS.setToolTipText("Auto delay in seconds");
		autoPanel.add(SwingUtils.embed(autoexportB));
		autoPanel.add(SwingUtils.embed(autodelayS));

		
		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.add(commandPanel, BorderLayout.NORTH);
		selectionPanel.add(listPanel, BorderLayout.CENTER);
		selectionPanel.add(autoPanel, BorderLayout.SOUTH);
		return selectionPanel;
	}
	
	private JTextPane initResultPanel() {
		resultTA = new JTextPane();
		resultTA.setContentType("text/html");
		resultTA.setEditable(false);
		return resultTA;
	}

	public void initFileDialog() {
		JPanel fileFormatRB = new JPanel();
		fileFormatRB.setLayout(new BoxLayout(fileFormatRB, BoxLayout.Y_AXIS));
		fileFormatRB.setBorder(BorderFactory.createTitledBorder("Format"));
		JRadioButton selectHtmlB = new JRadioButton("HTML");
		selectHtmlB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportFormat = "html";
			}
		});
		JRadioButton selectCsvB = new JRadioButton("CSV");
		selectCsvB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportFormat = "csv";
			}
		});
		JRadioButton selectCNCsvB = new JRadioButton("CN CSV");
		selectCNCsvB.setToolTipText("CSV format for french CN (Classement National)");
		selectCNCsvB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportFormat = "cn.csv";
			}
		});
		ButtonGroup group = new ButtonGroup();
		group.add(selectHtmlB);
		group.add(selectCsvB);
		group.add(selectCNCsvB);
		group.setSelected(selectHtmlB.getModel(), true);
		exportFormat = "html";
		fileFormatRB.add(selectHtmlB);
		fileFormatRB.add(selectCsvB);
		fileFormatRB.add(selectCNCsvB);
		
		filePane = new JFileChooser();
		filePane.setAccessory(fileFormatRB);
	}


	public void refreshResultView() {
		String htmlResults = resultBuilder().generateHtmlResults(createResultConfig(), -1);
		resultTA.setText(htmlResults);
	}

	
	public Thread startAutoexport() {
		autoexportThread = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				int autoexportDelay = ((Integer) autodelayS.getValue()).intValue();
				if( refreshRB.isSelected() ) {
					autorefresh(autoexportDelay);
				} else {
					autoexport(autoexportDelay);
				}
			}});
		autoexportThread.start();
		return autoexportThread;
	}
	private synchronized void autorefresh(long autoexportDelay) {
		long delay = 1000 * autoexportDelay;
		while( true ){
			try {
				refreshResultView();
				wait(delay);
			} catch (InterruptedException e) {
				return;
			}					
		}	
	}
	private synchronized void autoexport(int refreshDelay) {
		long delay = 1000 * refreshDelay;
		while( true ){
			String resultFile = geco().getCurrentStagePath() + File.separator + "lastresults";
			try {
				try {
					resultBuilder().exportFile(resultFile, exportFormat, createResultConfig(), refreshDelay);
				} catch (IOException ex) {
					geco().logger().debug(ex);
				}
				wait(delay);
			} catch (InterruptedException e) {
				return;
			}					
		}	
	}
	
	public void stopAutoexport() {
		if( autoexportThread!=null ) {
			autoexportThread.interrupt();
		}
	}

	@Override
	public void changed(Stage previous, Stage next) {
		updateNames();
		if( showCourses() ) {
			updateCourseList();
		} else {
			updateCategoryList();
		}
		repaint();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		File selectedFile = filePane.getSelectedFile();
		if( selectedFile!=null ){
			properties.setProperty("LastResultFile", selectedFile.getName());
		}
	}

	
	@Override
	public void categoriesChanged() {
		changed(null, null);
	}

	@Override
	public void clubsChanged() {}

	@Override
	public void coursesChanged() {
		changed(null, null);
	}
	
	@Override
	public void componentShown(ComponentEvent e) {
		refreshB.requestFocusInWindow();
	}


}
