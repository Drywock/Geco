/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.geco.basics.Html;
import net.geco.model.Messages;
import net.geco.ui.basics.StartStopButton;
import net.geco.ui.basics.SwingUtils;


/**
 * @author Simon Denier
 * @since Sep 5, 2010
 *
 */
public class LiveConfigPanel extends JPanel {

	public enum AdjustStep { START_CONTROL, START_MAP, END_CONTROL, END_MAP };
	
	public class AdjustButton extends StartStopButton {
		private MouseAdapter mapAdjuster;
		private Color defaultColor;
		
		@Override
		public void initialize() {
			super.initialize();
			setText("Adjust");
		}
		@Override
		public void actionOn() {
			defaultColor = getBackground();
			setBackground(Color.green);
				
			mapAdjuster = new MouseAdapter() {
				private AdjustStep step = AdjustStep.START_CONTROL;
				private Point controlPoint;
				private Point controlPoint2;
				
				public void mouseClicked(MouseEvent e) {
					ControlCircle control;
					super.mouseClicked(e);
					switch (step) {
					case START_CONTROL:
						control = liveComponent.mapComponent().findControlNextTo(e.getPoint());
						if( control!=null ){
							controlL.setText(control.getCode());
							controlPoint = control.getPosition();
							step = AdjustStep.START_MAP;
						}
						instructionsL.setText(Html.htmlTag("i", "Step 2/4. <br />Click on new map position<br /> for selected control."));
						break;
					case START_MAP:
						mapPoint = e.getPoint(); // new origin
						int ddx = mapPoint.x - controlPoint.x;
						xtranS.setValue(new Integer(((Integer) xtranS.getValue()).intValue() + ddx));
						int ddy = mapPoint.y - controlPoint.y;
						ytranS.setValue(new Integer(((Integer) ytranS.getValue()).intValue() + ddy));
						translateControls(ddx, ddy);
						liveComponent.displayAllControls();
						step = AdjustStep.END_CONTROL;
						instructionsL.setText(Html.htmlTag("i", "Step 3/4. Click on a second control."));
						break;
					case END_CONTROL:
						control = liveComponent.mapComponent().findControlNextTo(e.getPoint());
						if( control!=null ){
							controlPoint2 = control.getPosition();
							step = AdjustStep.END_MAP;
							instructionsL.setText(Html.htmlTag("i", "Step 4/4. <br />Click on new map position<br /> for the second control."));
						}
						break;
					case END_MAP:
						Point mapPoint2 = e.getPoint();
						float controlDx = controlPoint2.x - mapPoint.x;
						float mapDx = mapPoint2.x - mapPoint.x;
						xfactorS.setValue(new Float(mapDx / controlDx));

						float controlDy = controlPoint2.y - mapPoint.y;
						float mapDy = mapPoint2.y - mapPoint.y;
						yfactorS.setValue(new Float(mapDy / controlDy));
						
						step = AdjustStep.START_CONTROL;
						adjustControls();
						liveComponent.displayAllControls();
						doOffAction();
						break;
					}
				}
			};
			liveComponent.mapComponent().addMouseListener(mapAdjuster);
			instructionsL.setText(Html.htmlTag("i", "Step 1/4. Click on a control to select it."));
			instructionsL.setVisible(true);
		}
		
		@Override
		public void actionOff() {
			instructionsL.setVisible(false);
			liveComponent.mapComponent().removeMouseListener(mapAdjuster);
			setBackground(defaultColor);
		}
	}

	private Component frame;
	private LiveComponent liveComponent;

	private JButton mapfileB;
	private JButton coursefileB;
	private JLabel mapfileL;
	private JLabel coursefileL;

	private JSpinner dpiS;
	private JSpinner xtranS;
	private JSpinner ytranS;
	private Point mapPoint;
	private JLabel controlL;
	private JSpinner xfactorS;
	private JSpinner yfactorS;
	private JButton refreshB;
	private AdjustButton adjustB;
	
	private JButton showControlsB;
	private JButton showMapB;
	private JComboBox showCourseCB;
	private JLabel instructionsL;
	

	public LiveConfigPanel(JFrame frame, LiveComponent liveComp) {
		this(frame, liveComp, false);
	}
	
	public LiveConfigPanel(JFrame frame, LiveComponent liveComp, boolean withLiveNetwork) {
		this.frame = frame;
		this.liveComponent = liveComp;
		createComponents(withLiveNetwork);
		initListeners();
		initConfigPanel();
	}
	
	public static float dpi2dpmmFactor(float dpi) {
		return dpi / 25.4f; // dpi / mm/inch
	}
	
	private void createComponents(boolean withLiveNetwork) {
		// data files
		mapfileB = new JButton(Messages.liveGet("LiveConfigPanel.MapImageLabel")); //$NON-NLS-1$
		mapfileL = new JLabel();
		coursefileB = new JButton(Messages.liveGet("LiveConfigPanel.CourseFileLabel")); //$NON-NLS-1$
		coursefileL = new JLabel();
		// map config
		dpiS = new JSpinner(new SpinnerNumberModel(150, 0, null, 50));
		dpiS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		xtranS = new JSpinner(new SpinnerNumberModel(0, null, null, 1));
		xtranS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		ytranS = new JSpinner(new SpinnerNumberModel(0, null, null, 1));
		ytranS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		controlL = new JLabel();
		xfactorS = new JSpinner(new SpinnerNumberModel(1.0f, 0f, null, 0.1f));
		xfactorS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		yfactorS = new JSpinner(new SpinnerNumberModel(1.0f, 0f, null, 0.1f));
		yfactorS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		refreshB = new JButton(Messages.liveGet("LiveConfigPanel.RefreshLabel")); //$NON-NLS-1$
		// course config
		showControlsB = new JButton(Messages.liveGet("LiveConfigPanel.ShowControlsLabel")); //$NON-NLS-1$
		showMapB = new JButton(Messages.liveGet("LiveConfigPanel.ShowMapLabel")); //$NON-NLS-1$
		showCourseCB = new JComboBox();
	}
	
	private void initListeners() {
		mapfileB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Messages.liveGet("LiveConfigPanel.ImageDialogTitle")); //$NON-NLS-1$
				int answer = fileChooser.showOpenDialog(frame);
				if( answer==JFileChooser.APPROVE_OPTION ) {
					try {
						mapfileL.setText(fileChooser.getSelectedFile().getName());
						liveComponent.loadMapImage(fileChooser.getSelectedFile().getCanonicalPath());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		coursefileB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Messages.liveGet("LiveConfigPanel.XmlDialogTitle")); //$NON-NLS-1$
				int answer = fileChooser.showOpenDialog(frame);
				if( answer==JFileChooser.APPROVE_OPTION ) {
					try {
						coursefileL.setText(fileChooser.getSelectedFile().getName());
						liveComponent.importCourseData(fileChooser.getSelectedFile().getCanonicalPath());
						showCourseCB.setModel(new DefaultComboBoxModel(liveComponent.coursenames()));
						refreshCourses();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		dpiS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float factor = dpi2dpmmFactor(((Number) dpiS.getValue()).floatValue());
				liveComponent.createControls(factor);
				liveComponent.createCourses();
				liveComponent.displayAllControls();
			}
		});
		refreshB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshCourses();
			}
		});
		adjustB = new AdjustButton();
		showControlsB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				liveComponent.displayAllControls();
			}
		});
		showMapB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				liveComponent.displayMap();
			}
		});
		showCourseCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				liveComponent.displayCourse((String) showCourseCB.getSelectedItem());
			}
		});
		instructionsL = new JLabel();
		instructionsL.setVisible(false);
	}
	
	private void translateControls() {
		translateControls(
				((Number) xtranS.getValue()).intValue(),
				((Number) ytranS.getValue()).intValue());
	}

	private void translateControls(int dx, int dy) {
		liveComponent.translateControls(dx, dy);
	}

	private void adjustControls() {
		if( mapPoint!=null ){
			liveComponent.adjustControls(
					mapPoint.x,
					mapPoint.y,
					((Number) xfactorS.getValue()).floatValue(),
					((Number) yfactorS.getValue()).floatValue());			
		}
	}
	
	private void refreshCourses() {
		float factor = dpi2dpmmFactor(((Number) dpiS.getValue()).floatValue());
		liveComponent.createControls(factor);
		liveComponent.createCourses();
		translateControls();
		adjustControls();
		liveComponent.displayAllControls();
	}

	private JPanel initConfigPanel() {
		JPanel datafileP = new JPanel(new GridLayout(0, 2));
		datafileP.setBorder(BorderFactory.createTitledBorder(Messages.liveGet("LiveConfigPanel.LoadDataLabel"))); //$NON-NLS-1$
		addComponent(datafileP, mapfileB);
		addComponent(datafileP, mapfileL);
		addComponent(datafileP, coursefileB);
		addComponent(datafileP, coursefileL);
				
		JPanel mapConfigP = new JPanel(new GridLayout(0, 2));
		mapConfigP.setBorder(BorderFactory.createTitledBorder(Messages.liveGet("LiveConfigPanel.SetupLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.ImageDpiLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, dpiS);
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.XTranslationLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.YTranslationLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, xtranS);
		addComponent(mapConfigP, ytranS);
		addComponent(mapConfigP, new JLabel("Centered Control"));
		addComponent(mapConfigP, controlL);
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.XFactorLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.YFactorLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, xfactorS);
		addComponent(mapConfigP, yfactorS);
		addComponent(mapConfigP, refreshB);
		addComponent(mapConfigP, adjustB);
		
		JPanel courseConfigP = new JPanel(new GridLayout(0, 2));
		courseConfigP.setBorder(BorderFactory.createTitledBorder(Messages.liveGet("LiveConfigPanel.CheckCoursesLabel"))); //$NON-NLS-1$
		addComponent(courseConfigP, showControlsB);
		addComponent(courseConfigP, showMapB);
		addComponent(courseConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.ShowCourseLabel"))); //$NON-NLS-1$
		addComponent(courseConfigP, showCourseCB);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(datafileP);
		add(mapConfigP);
		add(courseConfigP);
		
		add(SwingUtils.embed(new JButton("Save parameters")));
		add(SwingUtils.embed(instructionsL));
		
		return this;
	}
	
	public void addComponent(Container cont, Component comp) {
		cont.add(SwingUtils.embed(comp));
	}

	public void setProperties(Properties liveProp) {
		mapfileL.setText(liveProp.getProperty("MapFile")); //$NON-NLS-1$
		coursefileL.setText(liveProp.getProperty("CourseFile")); //$NON-NLS-1$
		dpiS.setValue(new Integer(liveProp.getProperty("DPI"))); //$NON-NLS-1$
		xtranS.setValue(new Integer(liveProp.getProperty("XTrans"))); //$NON-NLS-1$
		ytranS.setValue(new Integer(liveProp.getProperty("YTrans"))); //$NON-NLS-1$
		showCourseCB.setModel(new DefaultComboBoxModel(liveComponent.coursenames()));
		refreshCourses();
	}
	
}
