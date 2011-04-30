/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.geco.basics.GecoResources;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Stage;
import net.geco.model.iocsv.CardDataIO;
import net.geco.model.iocsv.CategoryIO;
import net.geco.model.iocsv.ClubIO;
import net.geco.model.iocsv.CourseIO;
import net.geco.model.iocsv.CsvReader;
import net.geco.model.iocsv.HeatSetIO;
import net.geco.model.iocsv.OrStageIO;
import net.geco.model.iocsv.ResultDataIO;
import net.geco.model.iocsv.RunnerIO;


/**
 * StageBuilder is currently responsible for persistence of Stage and its data. It provides functions to
 * load/save Òr or Geco data (using RegistryBuilder as backend) as well as backup function.
 * 
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class StageBuilder extends BasicControl {
	
	private RegistryBuilder registryBuilder;

	private Stage currentStage;
	
	private final static String[] datafiles = new String[] {
		CategoryIO.sourceFilename(),
		ClubIO.orFilename(),
		CourseIO.orFilename(),
		CardDataIO.sourceFilename(),
		RunnerIO.sourceFilename()
	};
	
	
	public StageBuilder(Factory factory) {
		super(factory);
		this.registryBuilder = new RegistryBuilder(factory);
	}
	
	public Stage loadStage(String baseDir, PenaltyChecker checker) {
		try {
			BufferedReader reader = GecoResources.getReaderFor(propName(baseDir));
			return importGecoData(baseDir, reader, checker);
		} catch (FileNotFoundException e) {
			return importOrData(baseDir, checker);
		}
	}


	/**
	 * @param props
	 * @param checker
	 * @return
	 */
	public Stage importGecoData(String baseDir, BufferedReader props, PenaltyChecker checker) {
		currentStage = factory().createStage();
		currentStage.initialize(baseDir);
		loadStageProperties(currentStage, props);
		importDataIntoRegistry(baseDir, true);
		checker.postInitialize(currentStage); // post initialization
		new RunnerBuilder(factory()).checkGecoData(currentStage, checker);
		return currentStage;
	}

	public void loadStageProperties(Stage stage, BufferedReader propReader) {
		try {
			Properties props = new Properties();
			props.load(propReader);
			stage.loadProperties(props);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param string
	 * @param checker
	 * @return
	 */
	public Stage importOrData(String baseDir, PenaltyChecker checker) {
		CsvReader reader = new CsvReader().initialize(baseDir, OrStageIO.sourceFilename());
		try {
			currentStage = new OrStageIO(factory(), reader).getStage();
		} catch (IOException e) {
			e.printStackTrace();
			currentStage = factory().createStage();
		}
		currentStage.initialize(baseDir);
		importDataIntoRegistry(baseDir, false);
		checker.postInitialize(currentStage); // post initialization
		new RunnerBuilder(factory()).checkOrData(currentStage, checker);
		return currentStage;
	}

	private void importDataIntoRegistry(String baseDir, boolean importResult) {
		Registry registry = new Registry();
		currentStage.setRegistry(registry);
		this.registryBuilder.importAllData(registry, baseDir, importResult,
											getZerotimeFromProperties(currentStage));
	}
	
	public void save(Stage stage, Properties props, String backupname) {
		saveStageProperties(stage, props);
		registryBuilder.exportAllData(stage.registry(), stage.getBaseDir(),
											getZerotimeFromProperties(currentStage));
		backupData(stage.getBaseDir(), backupname);
	}
	
	private static long getZerotimeFromProperties(Stage stage) {
		return SIReaderHandler.readZeroTime(stage);
	}
	
	private void saveStageProperties(Stage stage, Properties properties) {
		stage.saveProperties(properties);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(propFile(stage.getBaseDir())));
			properties.store(writer, "Geco " + new Date(System.currentTimeMillis()).toString()); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MIGR11
	 */
	public void backupData(String basedir, String backupname) {
		try {
			ZipOutputStream zipStream = 
								new ZipOutputStream(new FileOutputStream(filepath(basedir, backupname)));
			for (String datafile : datafiles) {
				writeZipEntry(zipStream, datafile, basedir);	
			}
			if( fileExists(basedir, ResultDataIO.sourceFilename()) ) {
				writeZipEntry(zipStream, ResultDataIO.sourceFilename(), basedir);
			}
			if( fileExists(basedir, HeatSetIO.sourceFilename()) ) {
				writeZipEntry(zipStream, HeatSetIO.sourceFilename(), basedir);
			}
			if( propFile(basedir).exists() ) {
				writeZipEntry(zipStream, "geco.prop", basedir); //$NON-NLS-1$
			}
			zipStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeZipEntry(ZipOutputStream zipStream, String filename, String basedir)
					throws IOException, FileNotFoundException {
		ZipEntry zipEntry = new ZipEntry(filename);
		zipStream.putNextEntry(zipEntry);
		byte[] buffer = new byte[4096];
		BufferedInputStream inputStream =
								new BufferedInputStream(new FileInputStream(filepath(basedir, filename)));
		int len;
		while( (len = inputStream.read(buffer)) != -1 ) {
			zipStream.write(buffer, 0, len);
		}
		zipStream.closeEntry();
	}

	public static String propName(String baseDir) {
		return filepath(baseDir, "geco.prop"); //$NON-NLS-1$
	}
	
	public static File propFile(String baseDir) {
		return new File(propName(baseDir));
	}

	public static String filepath(String base, String filename) {
		return base + GecoResources.sep + filename;
	}
	
	public static boolean fileExists(String base, String filename) {
		return new File(filepath(base, filename)).exists();
	}
	
}
