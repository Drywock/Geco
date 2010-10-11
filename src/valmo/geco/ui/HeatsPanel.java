/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import valmo.geco.Geco;
import valmo.geco.core.Announcer;
import valmo.geco.model.HeatSet;
import valmo.geco.model.Pool;
import valmo.geco.model.Stage;
import valmo.geco.model.iocsv.RunnerIO;

/**
 * 
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public class HeatsPanel extends TabPanel implements Announcer.StageConfigListener {

	private Vector<String> coursenames;	
	private Vector<String> categorynames;
	private DefaultListModel heatlistModel;
	private HeatSetDialog heatDialog;
	
	private JTextPane heatsTA;
	private JList heatList;
	private JList poolList;
	private JButton newB;
	private JButton deleteB;
	private JButton refreshB;
	private JButton exportB;
	private JFileChooser filePane;
	private String exportFormat;

	
	/**
	 * @param geco
	 * @param frame 
	 * @param announcer 
	 */
	public HeatsPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		heatlistModel = new DefaultListModel();
		heatDialog = new HeatSetDialog(frame);
		refresh();
		initHeatsPanel(this);
		initFileDialog();
		createListeners();
		geco().announcer().registerStageConfigListener(this);
	}

	private void updatePoolnames() {
		coursenames = registry().getSortedCoursenames();
		categorynames = registry().getSortedCategorynames();
	}
	private Vector<String> getAllPoolnames(HeatSet heatset) {
		if( heatset.isCourseType() ) {
			return coursenames;
		} else {
			return categorynames;
		}
	}
	
	public HeatSet getSelectedHeatset() {
		return (HeatSet) heatList.getSelectedValue();
	}
	
	public HeatSet[] getSelectedHeatsets() {
		Object[] selectedValues = heatList.getSelectedValues();
		return Arrays.copyOf(selectedValues, selectedValues.length, HeatSet[].class ); 
	}
	
	public void showPoolList() {
		HeatSet currentHeatset = getSelectedHeatset();
		final Vector<String> poolnames = getAllPoolnames(currentHeatset);
		poolList.setModel(new AbstractListModel() {
			public int getSize() {
				return poolnames.size();
			}
			public Object getElementAt(int index) {
				return poolnames.get(index);
			}
		});
		poolList.setSelectedIndices(getSelectedIndices(currentHeatset));
	}
	
	private int[] getSelectedIndices(HeatSet heatset) {
		int[] indices = new int[heatset.getSelectedPools().length];
		Vector<String> nameset = getAllPoolnames(heatset);
		int i = 0;
		for (Pool pool : heatset.getSelectedPools()) {
			indices[i] = nameset.indexOf(pool.getName());
			i++;
		}
		return indices;
	}
	
	
	private void setPoolsForHeatSet() {
		HeatSet set = getSelectedHeatset();
		set.setSelectedPools(getSelectedPoolsFromList(set, poolList.getSelectedValues()));
	}
	
	private Pool[] getSelectedPoolsFromList(HeatSet heatSet, Object[] selectedValues) {
		Pool[] selectedPools = new Pool[selectedValues.length];
		if( heatSet.isCourseType() ) {
			for (int i = 0; i < selectedValues.length; i++) {
				selectedPools[i] = registry().findCourse((String) selectedValues[i]);
			}
		} else {
			for (int i = 0; i < selectedValues.length; i++) {
				selectedPools[i] = registry().findCategory((String) selectedValues[i]);
			}			
		}
		return selectedPools;
	}


	public void showHeatSetCreationDialog() {
		heatDialog.showHeatSet(geco().heatBuilder().createHeatSet());
		if( !heatDialog.cancelled() ) {
			HeatSet newHeatset = heatDialog.getHeatSet();
			heatlistModel.addElement(newHeatset);
			heatList.setSelectedValue(newHeatset, true);
			registry().addHeatSet(newHeatset);
		}
	}
	
	public void showHeatSetDialog() {
		Object selectedHeatset = heatList.getSelectedValue();
		if( selectedHeatset!=null ) {
			heatDialog.showHeatSet((HeatSet) selectedHeatset);
			// selection listener not triggered because heatset already selected
			showPoolList(); // so directly refresh the setlist
		}
	}


	
	public void createListeners() {
		newB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showHeatSetCreationDialog();
			}
		});
		deleteB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int remove = heatList.getSelectedIndex();
				if( remove!=-1 )
					registry().removeHeatset(getSelectedHeatset());
					heatlistModel.remove(remove);
			}
		});
		
		heatList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selectionSize = heatList.getSelectedIndices().length;
				if( selectionSize==1 ) {
					showPoolList();
					poolList.setVisible(true);
				} else {
					poolList.setVisible(false);
				}
			}
		});
		heatList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if( e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2 ) {
					showHeatSetDialog();
				}
			}
		});
		poolList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if( e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==1 ) {
					setPoolsForHeatSet();
				}
			}
		});

		refreshB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshHeatView();
			}
		});
		exportB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String heatFile;
				if( geco().hasNextStage() ) {
					heatFile = geco().getNextStagePath() + File.separator + RunnerIO.sourceFilename();
				} else {
					heatFile = geco().getCurrentStagePath() + File.separator + "heats";
				}
				filePane.setSelectedFile(new File(heatFile).getAbsoluteFile());
				int response = filePane.showSaveDialog(frame());
				if( response==JFileChooser.APPROVE_OPTION ) {
					String filename = filePane.getSelectedFile().getAbsolutePath();
					try {
						geco().heatBuilder().exportFile(filename, exportFormat, getSelectedHeatsets());
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(frame(), "Error while saving " + filename + "(" + e +")",
								"Export Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
	}

	/**
	 * @param panel
	 */
	public void initHeatsPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		JPanel resultSelectionPanel = initBuilderPanel();
		JTextPane resultTA = initHeatViewPanel();
		JScrollPane scrollPane = new JScrollPane(resultTA);
		panel.add(resultSelectionPanel, BorderLayout.WEST);
		panel.add(scrollPane, BorderLayout.CENTER);
	}

	private JPanel initBuilderPanel() {
		// select, new, delete heatset
		JPanel heatCreationPanel = new JPanel();
		heatCreationPanel.setLayout(new BoxLayout(heatCreationPanel, BoxLayout.X_AXIS));
		
		heatList = new JList(heatlistModel);
		JScrollPane spane = new JScrollPane(heatList);
		spane.setPreferredSize(new Dimension(90, 90));
		heatCreationPanel.add(SwingUtils.embed(spane));

		JPanel butPanel = new JPanel();
		butPanel.setLayout(new BoxLayout(butPanel, BoxLayout.Y_AXIS));
		newB = new JButton("New");
		deleteB = new JButton("Delete");
		butPanel.add(SwingUtils.embed(newB));
		butPanel.add(SwingUtils.embed(deleteB));
		heatCreationPanel.add(butPanel);

		// buttons
		refreshB = new JButton("Refresh");
		JButton printB = new JButton("Print");
		exportB = new JButton("Export");
		
		printB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					refreshHeatView();
					heatsTA.print();
				} catch (PrinterException e1) {
					JOptionPane.showMessageDialog(frame(), "Fail to print", "Printing Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// command panel with heatset and buttons
		JPanel commandPanel = new JPanel(new BorderLayout());
		commandPanel.add(heatCreationPanel, BorderLayout.NORTH);
		commandPanel.add(
				SwingUtils.makeButtonBar(FlowLayout.CENTER, refreshB, exportB, printB),
				BorderLayout.CENTER);
		commandPanel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);

		// selection panel for pools
		poolList = new JList();
		poolList.setVisible(false);
		JScrollPane scrollPane = new JScrollPane(poolList);
		scrollPane.setPreferredSize(new Dimension(150, 250));

		JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		selectionPanel.add(scrollPane);
		
		JPanel builderPanel = new JPanel(new BorderLayout());
		builderPanel.add(commandPanel, BorderLayout.NORTH);
		builderPanel.add(selectionPanel, BorderLayout.CENTER);
		return builderPanel;
	}
	
	private JTextPane initHeatViewPanel() {
		heatsTA = new JTextPane();
		heatsTA.setContentType("text/html");
		heatsTA.setEditable(false);
		return heatsTA;
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
		group.setSelected(selectCsvB.getModel(), true);
		exportFormat = "csv";
		fileFormatRB.add(selectHtmlB);
		fileFormatRB.add(selectCsvB);
		
		filePane = new JFileChooser();
		filePane.setAccessory(fileFormatRB);
	}
	
	public void refreshHeatView() {
		heatsTA.setText(geco().heatBuilder().refreshHtmlHeats(getSelectedHeatsets()));
	}

	
	/* (non-Javadoc)
	 * @see valmo.geco.ui.EventRegistry.Listener#changed(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void changed(Stage previous, Stage next) {
		refresh();
	}

	private void refresh() {
		updatePoolnames();
		heatlistModel.clear();
		for (HeatSet heatset : registry().getHeatSets() ) {
			heatlistModel.addElement(heatset);	
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
