/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import valmo.geco.control.ResultBuilder;
import valmo.geco.control.ResultBuilder.ResultConfig;
import valmo.geco.core.Geco;
import valmo.geco.core.Util;
import valmo.geco.core.Announcer.StageConfigListener;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public class ResultsPanel extends TabPanel implements StageConfigListener {
	
	// cache field
	private Vector<String> coursenames;
	private Vector<String> categorynames;
	
	private String exportFormat;
	
	private JList list;
	private JTextPane resultTA;
	private JRadioButton selectCourseB;
	private JRadioButton selectCatB;
	private JButton refreshB;
	private JButton exportB;
	private JButton selectAllB;
	private JButton selectNoneB;
	private JCheckBox showNcC;
	private JCheckBox showOtC;
	private JCheckBox showEsC;
	private JFileChooser filePane;

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
		list.setModel(new AbstractListModel() {
			public int getSize() {
				return nbCourses();
			}
			public Object getElementAt(int index) {
				return coursenames.get(index);
			}
		});
		selectAllCourses(list);
	}
	private void updateCategoryList() {
		list.setModel(new AbstractListModel() {
			public int getSize() {
				return nbCategories();
			}
			public Object getElementAt(int index) {
				return categorynames.get(index);
			}
		});
		selectAllCategories(list);
	}

	/**
	 * 
	 */
	public void createListeners() {
		selectCourseB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateCourseList();
			}
		});
		selectCatB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateCategoryList();
			}
		});
		selectAllB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( showCourses() ) {
					selectAllCourses(list);
				} else {
					selectAllCategories(list);
				}
			}
		});
		selectNoneB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNone(list);
			}
		});
		refreshB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshResultView();
			}
		});
		exportB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String resultFile = geco().getCurrentStagePath() + File.separator + "ranking";
				System.out.println(resultFile);
				filePane.setSelectedFile(new File(resultFile).getAbsoluteFile());
				int response = filePane.showSaveDialog(frame());
				if( response==JFileChooser.APPROVE_OPTION ) {
					String filename = filePane.getSelectedFile().getAbsolutePath();
					try {
						geco().resultBuilder().exportFile(filename, exportFormat, createResultConfig());
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(frame(), "Error while saving " + filename + "(" + ex +")",
								"Export Error", JOptionPane.ERROR_MESSAGE);
					}
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
		JPanel commandPanel = new JPanel();
		commandPanel.setLayout(new GridLayout(0, 2));

		selectCourseB = new JRadioButton("Courses");
		selectCatB = new JRadioButton("Categories");
		ButtonGroup group = new ButtonGroup();
		group.add(selectCourseB);
		group.add(selectCatB);
		group.setSelected(selectCourseB.getModel(), true);
		commandPanel.add(selectCourseB);
		commandPanel.add(selectCatB);
		
		showNcC = new JCheckBox("Show NC");
		commandPanel.add(showNcC);
		showOtC = new JCheckBox("Show Others");
		commandPanel.add(showOtC);
		showEsC = new JCheckBox("Show empty sets");
		commandPanel.add(showEsC);
//		commandPanel.add(Box.createHorizontalStrut(10));
		
		refreshB = new JButton("Refresh");
		refreshB.requestFocusInWindow();
		JButton printB = new JButton("Print");
		exportB = new JButton("Export");
		commandPanel.add(Util.embed(refreshB));
		commandPanel.add(Util.embed(printB));
		commandPanel.add(Util.embed(exportB));
		
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
		
		selectAllB = new JButton("All");
		selectNoneB = new JButton("None");
		commandPanel.add(Util.embed(selectAllB));
		commandPanel.add(Util.embed(selectNoneB));
		
		commandPanel.setBorder(BorderFactory.createLineBorder(Color.gray));

		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.add(Util.embed(commandPanel), BorderLayout.NORTH);
	
		list = new JList(coursenames);
		selectAllCourses(list);
		JScrollPane scrollPane = new JScrollPane(list);
		JPanel embed = Util.embed(scrollPane);
		scrollPane.setPreferredSize(new Dimension(150, 300));
		selectionPanel.add(embed, BorderLayout.CENTER);

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
		ButtonGroup group = new ButtonGroup();
		group.add(selectHtmlB);
		group.add(selectCsvB);
		group.setSelected(selectHtmlB.getModel(), true);
		exportFormat = "html";
		fileFormatRB.add(selectHtmlB);
		fileFormatRB.add(selectCsvB);
		
		filePane = new JFileChooser();
		filePane.setAccessory(fileFormatRB);
	}

	private boolean showCourses() {
		return selectCourseB.isSelected();
	}

	private void selectAllCourses(JList list) {
		list.setSelectionInterval(0, nbCourses() -1);
	}

	private int nbCourses() {
		return coursenames.size();
	}

	private void selectAllCategories(JList list) {
		list.setSelectionInterval(0, nbCategories() -1);
	}

	private int nbCategories() {
		return categorynames.size();
	}
	
	private void selectNone(JList list) {
		list.clearSelection();
	}

	public void refreshResultView() {
		resultTA.setText(geco().resultBuilder().generateHtmlResults(createResultConfig()));
	}
	
	private ResultConfig createResultConfig() {
		return ResultBuilder.createResultConfig(
				list.getSelectedValues(), 
				showCourses(),
				showEsC.isSelected(),
				showNcC.isSelected(),
				showOtC.isSelected());
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

}
