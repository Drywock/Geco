/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import valmo.geco.control.RegistryStats.StatItem;
import valmo.geco.framework.IGecoApp;
import valmo.geco.model.Messages;
import valmo.geco.model.Stage;
import valmo.geco.ui.basics.SwingUtils;
import valmo.geco.ui.framework.StatsPanel;

/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class VStatsPanel extends StatsPanel {

	private AbstractTableModel courseTableModel;
	
	private StatItem[] statusKeys = new StatItem[0];

	private JCheckBox viewCh;

	private String selectedCourse;

	private JList coursesL;
	
	/**
	 * @param geco
	 * @param frame
	 */
	public VStatsPanel(IGecoApp geco, JFrame frame) {
		super(geco, frame);
		initStatsPanel(this);
		refreshTableKeys();
		startAutoUpdate();
	}


	@Override
	protected void updateTable() {
		courseTableModel.fireTableDataChanged();		
	}

	protected void initStatsPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		
		JPanel controlP = new JPanel(new FlowLayout());
		viewCh = new JCheckBox(Messages.uiGet("StatsPanel.ShortViewLabel")); //$NON-NLS-1$
		viewCh.setToolTipText(Messages.uiGet("StatsPanel.ShortViewTooltip")); //$NON-NLS-1$
		viewCh.setSelected(true);
		viewCh.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if( viewCh.isSelected() ){
					statusKeys = stats().shortStatuses();
				} else {
					statusKeys = stats().longStatuses();
				}
				courseTableModel.fireTableStructureChanged();
			}
		});
		controlP.add(viewCh);
		
		JButton refreshB = new JButton(Messages.uiGet("StatsPanel.RefreshLabel")); //$NON-NLS-1$
		refreshB.setToolTipText(Messages.uiGet("StatsPanel.RefreshTooltip")); //$NON-NLS-1$
		refreshB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stats().fullUpdate();
				updateTable();
			}
		});
		controlP.add(refreshB);
		
		coursesL = new JList();
		coursesL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		coursesL.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if( !e.getValueIsAdjusting() ) {
					selectedCourse = (String) coursesL.getSelectedValue();
					updateTable();
				}
			}
		});
		
		JPanel listP = new JPanel(new BorderLayout());
		JScrollPane jsp1 = new JScrollPane(coursesL);
		jsp1.setPreferredSize(new Dimension(250, 195));
		listP.add(jsp1, BorderLayout.CENTER);
		listP.add(controlP, BorderLayout.SOUTH);
		panel.add( SwingUtils.embed(listP), BorderLayout.NORTH);
		
		courseTableModel = createCourseTableModel();
		JTable table = new JTable(courseTableModel);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		JScrollPane jsp = new JScrollPane(table);
		jsp.setPreferredSize(new Dimension(250, 235));
		panel.add( SwingUtils.embed(jsp), BorderLayout.CENTER );
	}
	
	protected AbstractTableModel createCourseTableModel() {
		return new AbstractTableModel() {
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				String content;
				if( columnIndex==0 )
					content = statusKeys[rowIndex].toString();
				else 
					content = stats().getCourseStatsFor(selectedCourse,
														statusKeys[rowIndex]).toString();
				return content;

			}
			@Override
			public int getRowCount() {
				return statusKeys.length;
			}
			@Override
			public int getColumnCount() {
				return 2;
			}
			@Override
			public String getColumnName(int column) {
				if( column==0 )
					return Messages.uiGet("StatsPanel.StatusHeader"); //$NON-NLS-1$
				else
					return Messages.uiGet("StatsPanel.CountHeader"); //$NON-NLS-1$
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if( columnIndex==0 ){
					return String.class;
				} else {
					return Integer.class;
				}
			}
		};
	}
	
	protected void refreshTableKeys() {
		statusKeys = stats().shortStatuses();
		coursesL.setListData(stats().sortedEntries());
		coursesL.setSelectedValue("Total", true); //$NON-NLS-1$
	}

	@Override
	public void changed(Stage previous, Stage next) {
		refreshTableKeys();
		viewCh.setSelected(true);
//		courseTableModel.fireTableStructureChanged(); // called by viewCh item listener
	}


}