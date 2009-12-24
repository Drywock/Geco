/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.core.Util;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Feb 8, 2009
 *
 */
public class StagePanel extends TabPanel {

	Announcer announcer;
	
	public StagePanel(Geco geco, JFrame frame, Announcer announcer) {
		super(geco, frame, announcer);
		this.announcer = announcer;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		refresh();
	}
	
	public void refresh() {
		this.removeAll(); // not smart?
		JPanel configPanel = Util.embed(stageConfigPanel());		
		add(configPanel);
		add(Util.embed(checkerConfigPanel()));
		add(Util.embed(clubConfigPanel()));
		add(Util.embed(categoryConfigPanel()));
	}

	private JPanel stageConfigPanel() {
		JPanel panel = new JPanel(new GridLayout(0,2));
		panel.setBorder(BorderFactory.createTitledBorder("Stage Configuration"));
		panel.add(new JLabel("Stage name:"));
		panel.add(new JTextField(geco().stage().getName()));
		panel.add(new JLabel("Previous stage:"));
		panel.add(new JLabel(geco().getPreviousStageDir()));
		panel.add(new JLabel("Next stage:"));
		panel.add(new JLabel(geco().getNextStageDir()));
		return panel;
	}

	private JPanel checkerConfigPanel() {
		JPanel panel = new JPanel(new GridLayout(0,2));
		panel.setBorder(BorderFactory.createTitledBorder("Orientshow Configuration"));
		panel.add(new JLabel("MP limit:"));
		JTextField mplimitF = new JTextField("0");
		mplimitF.setToolTipText("Number of missing punches authorized before marking the runner as MP.");
		panel.add(mplimitF);
		panel.add(new JLabel("Time penalty:"));
		JTextField penaltyF = new JTextField("30");
		penaltyF.setToolTipText("Time penalty per missing punch in seconds");
		panel.add(penaltyF);
		return panel;
	}

	
	private JPanel clubConfigPanel() {
		final ConfigTablePanel<Club> panel = new ConfigTablePanel<Club>(geco(), frame());
		
		final ConfigTableModel<Club> tableModel = 
			new ConfigTableModel<Club>(new String[] {"Short name", "Long name"}) {
				@Override
				public Object getValueIn(Club club, int columnIndex) {
					switch (columnIndex) {
					case 0: return club.getShortname();
					case 1: return club.getName();
					default: return super.getValueIn(club, columnIndex);
					}
				}
				@Override
				public void setValueIn(Club club, Object value, int col) {
					switch (col) {
					case 0: geco().stageControl().updateShortname(club, (String) value); break;
					case 1: geco().stageControl().updateName(club, (String) value); break;
					default: break;
					}
				}
		};
		tableModel.setData(new Vector<Club>(registry().getClubs()));
		announcer.registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {}
			public void clubsChanged() {
				tableModel.setData(new Vector<Club>(registry().getClubs()));
//				panel.refreshTableData(new Vector<Club>(registry().getClubs()));
			}
			public void categoriesChanged() {}
		});
		
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().stageControl().createClub();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Club club = panel.getSelectedData();
				if( club!=null ) {
					boolean removed = geco().stageControl().removeClub(club);
					if( !removed ) {
						JOptionPane.showMessageDialog(frame(),
							    "This club can not be deleted because some runners are registered with it.",
							    "Action cancelled",
							    JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};

		panel.initialize("Club", tableModel, addAction, removeAction);
		return panel;
	}
	
	
	private JPanel categoryConfigPanel() {
		final ConfigTablePanel<Category> panel = new ConfigTablePanel<Category>(geco(), frame());
		
		final ConfigTableModel<Category> tableModel = 
			new ConfigTableModel<Category>(new String[] {"Short name", "Long name"}) {
				@Override
				public Object getValueIn(Category cat, int columnIndex) {
					switch (columnIndex) {
					case 0: return cat.getShortname();
					case 1: return cat.getLongname();
					default: return super.getValueIn(cat, columnIndex);
					}
				}
				@Override
				public void setValueIn(Category cat, Object value, int col) {
					switch (col) {
					case 0: cat.setShortname((String) value); break;
					case 1: cat.setLongname((String) value); break;
					default: break;
					}
					// TODO: announce change in club name
				}
		};
		tableModel.setData(new Vector<Category>(registry().getCategories()));
		
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				geco().stageControl().createCategory();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Category cat = panel.getSelectedData();
				if( cat!=null ) {
//					geco().stageControl().removeCategory(cat);
				}
			}
		};

		panel.initialize("Category", tableModel, addAction, removeAction);
		return panel;
	}
	
	@Override
	public void changed(Stage previous, Stage next) {
		refresh();
		frame().repaint();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		// TODO save stage properties
		super.saving(stage, properties);
	}
	

}
