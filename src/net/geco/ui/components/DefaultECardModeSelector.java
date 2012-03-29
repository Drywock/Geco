/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import net.geco.control.SIReaderHandler;
import net.geco.framework.IGeco;

/**
 * @author Simon Denier
 * @since Mar 20, 2012
 *
 */
public class DefaultECardModeSelector extends JComboBox implements ECardModeSelector {

	private ECardModeUI currentMode = ECardModeUI.OffMode;

	private IGeco geco;

	private ECardModeRenderer modeRenderer;

	private boolean recovery = false;
	
	public DefaultECardModeSelector(IGeco geco) {
		super(ECardModeUI.values());
		this.geco = geco;
		modeRenderer = new ECardModeRenderer();
		setRenderer(modeRenderer);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectMode((ECardModeUI) getSelectedItem());
			}
		});
	}

	private void selectMode(ECardModeUI selectedMode) {
		if( currentMode != selectedMode ){
			currentMode = selectedMode;
			if( !recovery ){
				SIReaderHandler siHandler = geco.siHandler();
				currentMode.select(siHandler);
				if( shouldStart() ) {
					if( currentMode.isReadMode() ){
						beforeStartingReadMode();
					}
					modeStarting();
					siHandler.start();					
				}
				if( shouldStop() ) {
					siHandler.stop();
				}
			}
		}
	}
	
	public boolean shouldStart() {
		return currentMode.isActiveMode() && ! geco.siHandler().isOn();
	}
	
	public boolean shouldStop() {
		return ! currentMode.isActiveMode() && geco.siHandler().isOn();
	}

	public void beforeStartingReadMode() {
		// can be overridden to perform external UI actions
	}
	
	public void modeStarting() {
		modeRenderer.setTemporaryLabel("Starting...");
	}
	
	public void modeActivated() {
		modeRenderer.setTemporaryLabel(null);
		repaint();
	}
	
	public void recoverOffMode() {
		this.recovery = true;
		setSelectedItem(ECardModeUI.OffMode);
		modeActivated();
		this.recovery = false;
	}
	
}
