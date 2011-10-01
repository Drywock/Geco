/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;

import net.geco.basics.GecoResources;


/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class LiveMapComponent extends Component {
	
	private BufferedImage mapImage;

	private Collection<ControlCircle> controls;

	private LivePunch startPunch;
	
	public LiveMapComponent() {
		mapImage = new BufferedImage(550, 550, BufferedImage.TYPE_3BYTE_BGR);
	}
	
	public void loadMapImage(String filename) {
		try {
			// TODO: check null
			mapImage = ImageIO.read(GecoResources.getStreamFor(filename));
			repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public Dimension getPreferredSize() {
        return new Dimension(mapImage.getWidth(), mapImage.getHeight());
    }
	
	public Dimension getMinimumSize() {
		return new Dimension(800, 500);
	}
	
	public ControlCircle findControlNextTo(Point p) {
		for (ControlCircle control : controls) {
			if( p.distance(control.getPosition()) < 5 ){
				return control;
			}
		}
		return null;
	}
	
	public void showControls(Collection<ControlCircle> controls) {
		this.controls = controls;
		this.startPunch = null;
		repaint();
	}
	
	public void showTrace(LivePunch punch) {
		this.controls = null;
		this.startPunch = punch;
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
		g2.drawImage(mapImage, null, 0, 0);
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 15));
		if( controls!=null ) {
			for (ControlCircle control : controls) {
				control.drawOn(g2);
			}
		}
		if( startPunch!=null ) {
			startPunch.drawOn(g2);
		}
	}
	
	
}
