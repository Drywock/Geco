/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import valmo.geco.core.Announcer;
import valmo.geco.core.Logger;
import valmo.geco.model.Factory;
import valmo.geco.model.Registry;
import valmo.geco.model.Stage;
import valmo.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since Aug 20, 2010
 *
 */
public class GecoControl {
	
	private class RuntimeStage {
		private Stage stage;
		
		private Logger logger;

		public RuntimeStage(Stage stage, Logger logger) {
			super();
			this.stage = stage;
			this.logger = logger;
		}

		public Stage stage() {
			return this.stage;
		}
		
		public Logger logger() {
			return this.logger;
		}
	}
	
	/*
	 * stupid accessor against null value
	 */
	private static Stage getStage(RuntimeStage rstage) {
		return ( rstage!=null ) ? rstage.stage() : null;
	}
	

	private final Factory factory;
	
	private final Announcer announcer;

	private StageBuilder stageBuilder;

	private PenaltyChecker checker;

	private Thread autosaveThread;
	
	/*
	 * Stage
	 */
	private RuntimeStage current;
	
	private RuntimeStage previous;
	
	private RuntimeStage next;

	
	public GecoControl(String startDir) {
		factory = new POFactory();
		announcer = new Announcer();

		// early controls
		stageBuilder = new StageBuilder(factory);
		checker = new PenaltyChecker(this);
		
		openStage(startDir);
	}
	
	public Factory factory() {
		return this.factory;
	}
	public Announcer announcer() {
		return this.announcer;
	}
	public Stage stage() {
		return current.stage();
	}
	public Registry registry() {
		return stage().registry();
	}
	public PenaltyChecker checker() {
		return checker;
	}

	
	public void openStage(String baseDir) {
		stopAutosave();
		RuntimeStage oldStage = current;

		RuntimeStage newStage = loadStage(baseDir);
		closeAllStages();
		current = newStage;

		announcer.announceChange(getStage(oldStage), stage());
		startAutosave();
	}
	private RuntimeStage loadStage(String baseDir) {
		Stage stage = stageBuilder.loadStage(baseDir, checker);
//		stageBuilder.backupData(stage.getBaseDir(),
//								backupFilename( new SimpleDateFormat("yyMMdd-HHmmss'i'").format(new Date()) ));
		Logger logger = initializeLogger(stage);
		return new RuntimeStage(stage, logger);
	}
	
	private Logger initializeLogger(Stage stage) {
		Logger logger = new Logger(stage.getBaseDir(), "geco.log");
		logger.initSessionLog(stage.getName());
		return logger;
	}
	public Logger logger() {
		return current.logger();
	}
	public void debug(String message) {
		logger().debug(message);
		announcer().log(message, true);
	}
	public void log(String message) {
		logger().log(message);
		announcer().log(message, false);
	}
	public void info(String message, boolean warning) {
		announcer().info(message, warning);
	}
	
	public Thread startAutosave() {
		final long saveDelay = stage().getAutosaveDelay() * 60 * 1000;
		autosaveThread = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				int id = stage().getNbAutoBackups();
				while( true ){
					try {
						wait(saveDelay);
						id ++;
						if( id > stage().getNbAutoBackups() ) {
							id = 1;
						}
						saveStage(backupFilename(new Integer(id).toString()));
					} catch (InterruptedException e) {
						return;
					}					
				}
			}});
		autosaveThread.start();
		return autosaveThread;
	}
	
	public void stopAutosave() {
		if( autosaveThread!=null ) {
			autosaveThread.interrupt();
			try {
				autosaveThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private String backupFilename(String id) {
		return "backups" + File.separator + "backup" + id + ".zip";
	}
	
	private void saveStage(String backupName) {
		Properties props = new Properties();
		announcer.announceSave(stage(), props);
		stageBuilder.save(	stage(), 
							props, 
							backupName);
	}

	public void saveCurrentStage() {
		saveStage( backupFilename( new SimpleDateFormat("yyMMdd-HHmmss").format(new Date()) ));
	}
	
	public void closeStage(RuntimeStage runStage) {
		if( runStage!=null ) {
			announcer.announceClose(runStage.stage());
			runStage.stage().close();
			runStage.logger().close();
		}
	}
	private void closeCurrentStage() {
		if( current!=null ) {
			saveCurrentStage();
		}
		closeStage(current);
	}
	private void closeNextStage() {
		closeStage(next);
		next = null;
	}
	private void closePreviousStage() {
		closeStage(previous);
		previous = null;
	}
	public void closeAllStages() {
		closeCurrentStage();
		closePreviousStage();
		closeNextStage();
	}

	/**
	 * Private method, do not call!
	 * @param previousPath
	 * @see valmo.geco.core.Geco.switchToPreviousStage()
	 */
	public void preloadPreviousStage(String previousPath) {
		stopAutosave();
		saveCurrentStage();
		if( previous==null ) {
			previous = loadStage(previousPath);
		}
	}

	/**
	 * Private method, do not call! Previous loaded, proceed with switching references around
	 * @see valmo.geco.core.Geco.switchToPreviousStage()
	 */
	public void switchToPreviousStage() {
		closeNextStage();
		next = current; // current becomes next
		current = previous; // previous becomes current
		previous = null; // unset previous ref (we dont want to automatically load previous one)
		announcer.announceChange(getStage(next), stage());
		startAutosave();
	}

	/**
	 * Private method, do not call!
	 * @param nextPath 
	 * @see valmo.geco.core.Geco.switchToNextStage()
	 */
	public void preloadNextStage(String nextPath) {
		stopAutosave();
		saveCurrentStage();
		if( next==null ) {
			next = loadStage(nextPath);
		}
	}
	
	/**
	 * Private method, do not call! next loaded, proceed with switching references around
	 * @see valmo.geco.core.Geco.switchToNextStage()
	 */
	public void switchToNextStage() {
		closePreviousStage();
		previous = current;
		current = next;
		next = null;
		announcer.announceChange(getStage(previous), stage());
		startAutosave();
	}

}
