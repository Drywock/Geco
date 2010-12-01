/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.print.PrinterJob;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.JFrame;
import javax.swing.JTextPane;

import valmo.geco.control.ResultBuilder.SplitTime;
import valmo.geco.core.Announcer.CardListener;
import valmo.geco.core.Announcer.StageListener;
import valmo.geco.core.Html;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Nov 25, 2010
 *
 */
public class SingleSplitPrinter extends Control implements StageListener, CardListener {
	
	public static enum SplitFormat { MultiColumns, Ticket }

	private static final boolean DEBUGMODE = false;
	
	private PrintService splitPrinter;
	private boolean autoPrint;
	private SplitFormat splitFormat = SplitFormat.MultiColumns;
	private MediaSizeName[] splitMedia;
	
	private final ResultBuilder builder;
	private final SplitExporter exporter;
	
	public SingleSplitPrinter(GecoControl gecoControl) {
		super(SingleSplitPrinter.class, gecoControl);
		builder = getService(ResultBuilder.class);
		exporter = getService(SplitExporter.class);
		geco().announcer().registerStageListener(this);
		geco().announcer().registerCardListener(this);
		changed(null, stage());
	}
	
	public String printSingleSplits(RunnerRaceData data) {
		if( getSplitPrinter()!=null ) {
			Html html = new Html();
			html.open("head");
			html.open("style", "type=\"text/css\"");
			html.contents(
					"body { font-size: " + splitFontSize() + "; background-color:white }\n" +
//					"table { border-width: 1px } \n" +
					"td, th { padding: 0px 0px 0px 10px; margin: 0px }");
			html.close("style");
			html.close("head");
			if( splitFormat==SplitFormat.Ticket ) {
				printSingleSplitsInLine(data, html);
			} else {
				printSingleSplitsInColumns(data, html);
			}
		
			final JTextPane ticket = new JTextPane(); 
			ticket.setContentType("text/html"); //$NON-NLS-1$
			String content = html.close();
			ticket.setText(content);
			
			final PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
			if( splitFormat==SplitFormat.Ticket ) {
				computeMediaForTicket(ticket, attributes);
			}
			
			Callable<Boolean> callable = new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return ticket.print(null, null, false, getSplitPrinter(), attributes, false);
				}
			};
			
			if( ! DEBUGMODE ) {
				ExecutorService pool = Executors.newCachedThreadPool();
				pool.submit(callable);
			} else {
				JFrame jFrame = new JFrame();
				jFrame.add(ticket);
				jFrame.pack();
				jFrame.setVisible(true);
			}
			
//			try {
//				ticket.print(null, null, false, getSplitPrinter(), attributes, true);
//			} catch (PrinterException e) {
//				geco().debug(e.getLocalizedMessage());
//			}
			return content;
		}
		return ""; //$NON-NLS-1$
	}


	private void printSingleSplitsInColumns(RunnerRaceData data, Html html) {
		html.b(data.getRunner().getName() + " - " //$NON-NLS-1$
				+ geco().stage().getName() + " - " //$NON-NLS-1$
				+ data.getCourse().getName() + " - " //$NON-NLS-1$
				+ data.getResult().shortFormat());
		html.open("table"); //$NON-NLS-1$
		exporter.appendHtmlSplitsInColumns(
				builder.buildNormalSplits(data, null),
				new SplitTime[0],
				exporter.nbColumns(),
				html);
		html.close("table"); //$NON-NLS-1$
		html.tag("div", //$NON-NLS-1$
				"align=\"center\"", //$NON-NLS-1$
				"Geco for orienteering - http://bitbucket.org/sdenier/geco"); //$NON-NLS-1$
	}


	private void printSingleSplitsInLine(RunnerRaceData data, Html html) {
	//		char[] chars = Character.toChars(0x2B15); // control flag char :)
	//		html.contents(new String(chars));
			html.open("div", "align=\"center\"");
			html.contents(geco().stage().getName()).br();
			html.b(data.getRunner().getName()).br();
			html.br();
			html.b(data.getCourse().getName() + " - " //$NON-NLS-1$
					+ data.getResult().shortFormat());
			html.close("div"); // don't center table, it wastes too much space for some formats.
			html.open("table", "width=\"75%\""); //$NON-NLS-1$
			exporter.appendHtmlSplitsInLine(builder.buildLinearSplits(data), html);
			html.close("table").br(); //$NON-NLS-1$
			html.contents("Geco for orienteering").br();
			html.contents("http://bitbucket.org/sdenier/geco");
		}


	private void computeMediaForTicket(final JTextPane ticket,
			final PrintRequestAttributeSet attributes) {
		Dimension preferredSize = ticket.getPreferredSize();
		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		float height = ((float) preferredSize.height) / dpi;
		float width = ((float) preferredSize.width) / dpi;
//		float width = 2.76f;

		if( DEBUGMODE ){
			System.out.println("Font size: " + splitFontSize());
			System.out.print("Request: ");
			System.out.print(height * 25.4);
			System.out.print("x");
			System.out.print(width * 25.4);
			System.out.println(" mm");
		}

		MediaSizeName bestMedia = null;
		float bestFit = Float.MAX_VALUE;
		for (MediaSizeName media : getSplitMedia()) {
			MediaSize mediaSize = MediaSize.getMediaSizeForName(media);
			if( mediaSize!=null ){
				if( DEBUGMODE ){
					System.out.print(mediaSize.toString(MediaSize.MM, "mm"));
					System.out.println(" - " + media);
				}
				float dy = mediaSize.getY(MediaSize.INCH) - height;
				float dx = mediaSize.getY(MediaSize.INCH) - width;
				float fit = dy + dx;
				if( dy >= 0 && dx >= 0 && fit <= bestFit ){
					bestFit = fit;
					bestMedia = media;
				}
			}
		}
		if( bestMedia==null ){
			bestMedia = MediaSize.findMedia(width, height, MediaSize.INCH);
			geco().debug("Ticket size may be too small");
			if( DEBUGMODE ){
				System.out.print("Found: ");
			}			
		} else {
			if( DEBUGMODE ){
				System.out.print("Chosen: ");
			}			
		}
		if( bestMedia!=null ){
			attributes.add(bestMedia);
			MediaSize fitSize = MediaSize.getMediaSizeForName(bestMedia);
			if( DEBUGMODE ){
				System.out.println(fitSize.toString(MediaSize.MM, "mm"));
			}
		} else {
			geco().log("Can't find a matching size for ticket");
		}
	}


	private MediaSizeName[] getSplitMedia() {
		if( splitMedia==null ) {
			Vector<MediaSizeName> mediaSizenames = new Vector<MediaSizeName>();
			Media[] media = (Media[]) getSplitPrinter().getSupportedAttributeValues(Media.class, null, null);
			for (Media m : media) {
				if( m!=null && m instanceof MediaSizeName ){
					mediaSizenames.add((MediaSizeName) m);
				}
			}
			splitMedia = mediaSizenames.toArray(new MediaSizeName[0]);
		}
		return splitMedia;
	}

	public Vector<String> listPrinterNames() {
		Vector<String> printerNames = new Vector<String>();
		for (PrintService printer : PrinterJob.lookupPrintServices()) {
			printerNames.add(printer.getName());
		}
		return printerNames;
	}
	
	protected PrintService getSplitPrinter() {
		if( splitPrinter==null ) {
			splitPrinter = PrintServiceLookup.lookupDefaultPrintService();
		}
		return splitPrinter;
	}
	
	public String getSplitPrinterName() {
		return ( getSplitPrinter()==null ) ? "" : getSplitPrinter().getName(); //$NON-NLS-1$
	}
	
	public String getDefaultPrinterName() {
		PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
		return ( defaultService==null ) ? "" : defaultService.getName(); //$NON-NLS-1$
	}
	
	public boolean setSplitPrinterName(String name) {
		splitMedia = null; // reset cache
		for (PrintService printer : PrinterJob.lookupPrintServices()) {
			if( printer.getName().equals(name) ) {
				splitPrinter = printer;
				return true;
			}
		}
		splitPrinter = null;
		return false;
	}

	
	public void enableAutoprint() {
		this.autoPrint = true;
	}
	
	public void disableAutoprint() {
		this.autoPrint = false;
	}
	
	public SplitFormat getSplitFormat() {
		return this.splitFormat;
	}
	
	public void setSplitFormat(SplitFormat format) {
		this.splitFormat = format;
	}

	public int splitFontSize() {
		return 10; // 8 for race with more than 30+ punches
	}


	@Override
	public void cardRead(String chip) {
		if( autoPrint ) {
			printSingleSplits(registry().findRunnerData(chip));
		}
	}
	@Override
	public void unknownCardRead(String chip) {	}
	@Override
	public void cardReadAgain(String chip) {	}
	@Override
	public void rentedCard(String siIdent) {	}


	@Override
	public void changed(Stage previous, Stage current) {
		Properties props = stage().getProperties();
		setSplitPrinterName(props.getProperty(splitPrinterProperty()));
		String format = props.getProperty(splitFormatProperty());
		if( format!=null ) {
			setSplitFormat(SplitFormat.valueOf(format));
		}
	}
	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(splitPrinterProperty(), getSplitPrinterName());
		properties.setProperty(splitFormatProperty(), getSplitFormat().name());
	}
	@Override
	public void closing(Stage stage) {	}

	public static String splitPrinterProperty() {
		return "SplitPrinter"; //$NON-NLS-1$
	}
	public static String splitFormatProperty() {
		return "SplitFormat"; //$NON-NLS-1$
	}
	
}
