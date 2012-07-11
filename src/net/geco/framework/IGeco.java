/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.framework;

import net.geco.basics.Announcer;
import net.geco.basics.GecoRequestHandler;
import net.geco.basics.Logger;
import net.geco.control.ArchiveManager;
import net.geco.control.CNCalculator;
import net.geco.control.Checker;
import net.geco.control.HeatBuilder;
import net.geco.control.MergeControl;
import net.geco.control.RegistryStats;
import net.geco.control.ResultBuilder;
import net.geco.control.ResultExporter;
import net.geco.control.RunnerControl;
import net.geco.control.SIReaderHandler;
import net.geco.control.SingleSplitPrinter;
import net.geco.control.SplitExporter;
import net.geco.control.StageControl;
import net.geco.control.StartlistImporter;
import net.geco.model.Registry;

/**
 * @author Simon Denier
 * @since Apr 3, 2011
 *
 */
public interface IGeco {

	public Registry registry();
	public Announcer announcer();
	public GecoRequestHandler defaultMergeHandler();
	
	public Logger logger();
	public void debug(String message);
	public void log(String message);
	public void info(String message, boolean warning);
	
	public Checker checker();
	public StageControl stageControl();
	public RunnerControl runnerControl();
	public ResultBuilder resultBuilder();
	public ResultExporter resultExporter();
	public SplitExporter splitsExporter();
	public SingleSplitPrinter splitPrinter();
	public HeatBuilder heatBuilder();	
	public RegistryStats registryStats();
	public SIReaderHandler siHandler();
	public ArchiveManager archiveManager();
	public StartlistImporter startlistImporter();
	public CNCalculator cnCalculator();
	public MergeControl mergeControl();
	
}
