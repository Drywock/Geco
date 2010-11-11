/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import valmo.geco.Geco;
import valmo.geco.core.Announcer.Logging;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class LogPanel extends TabPanel implements Logging {

	private FunctionsPanel funtionsPanel;

	private JTextArea logArea;
	
	
	public LogPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		initPanels(this);
		geco().announcer().registerLogger(this);
	}

	public void initPanels(JPanel panel) {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		splitPane.setOneTouchExpandable(true);
//		splitPane.add(initLogArea());
		splitPane.add(initFunctionLogPanel());
		splitPane.add(initStatsPanel());
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		panel.add(splitPane);
	}
	
	public JPanel initFunctionLogPanel() {
		JPanel fPanel = new JPanel(new BorderLayout());
		funtionsPanel = new FunctionsPanel(geco(), frame(), initClearLogButton());
		fPanel.add(funtionsPanel, BorderLayout.NORTH);
		fPanel.add(initLogArea(), BorderLayout.CENTER);
		return fPanel;
	}

	private JButton initClearLogButton() {
		JButton clearB = new JButton("Clear log view");
		clearB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		return clearB;
	}

	public JPanel initLogArea() {
		logArea = new JTextArea(20, 70);
		logArea.setEditable(false);
		logArea.setLineWrap(true);

		JPanel logPanel = new JPanel(new BorderLayout());
		logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
//		logPanel.add(SwingUtils.embed(initClearLogButton()), BorderLayout.SOUTH);

		return logPanel;
	}

	private void clear() {
		logArea.setText("");
	}

	private JPanel initStatsPanel() {
		return new HStatsPanel(geco(), frame());
	}
	
	public void displayLog(String message) {
		logArea.append("\n");
		logArea.append(message);
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	@Override
	public void info(String message, boolean warning) {
//		displayLog(message);
	}

	@Override
	public void log(String message, boolean warning) {
		displayLog(message);
	}

	@Override
	public void dataInfo(String data) {
		displayLog(data);
	}

	@Override
	public void componentShown(ComponentEvent e) {
		funtionsPanel.componentShown();
		logArea.requestFocusInWindow();
	}

	@Override
	public void changed(Stage previous, Stage current) {
		clear();
	}
	
}