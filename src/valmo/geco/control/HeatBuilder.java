/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import valmo.geco.basics.Html;
import valmo.geco.model.Course;
import valmo.geco.model.Heat;
import valmo.geco.model.HeatSet;
import valmo.geco.model.RankedRunner;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;
import valmo.geco.model.iocsv.CsvWriter;
import valmo.geco.model.iocsv.RunnerIO;

/**
 * @author Simon Denier
 * @since Jan 18, 2009
 *
 */
public class HeatBuilder extends Control {
	
	private int startnumber;

	private Vector<Heat> heats;

	private ResultBuilder resultBuilder;


	public HeatBuilder(GecoControl gecoControl, ResultBuilder resultBuilder) {
		super(gecoControl);
		this.resultBuilder = resultBuilder;
	}
	
	public HeatSet createHeatSet() {
		return factory().createHeatSet();
	}

	private Vector<Heat> getHeats(HeatSet[] selectedHeatsets) {
		if( heats==null ) {
			buildHeats(selectedHeatsets);
		}
		return heats;
	}

	private Vector<Heat> buildHeats(HeatSet[] selectedHeatsets) {
		heats = new Vector<Heat>();
		for (HeatSet heatset : selectedHeatsets) {
			List<Result> heatsetResults
							= resultBuilder.buildResults(heatset.getSelectedPools(), heatset.getSetType());
			List<Heat> heatsForCurrentHeatset = buildHeatsFromResults(
					heatsetResults, 
					heatset.getName(),
					heatset.getHeatNames(), 
					heatset.getQualifyingRank());
			heats.addAll(heatsForCurrentHeatset);
		}
		return heats;
	}

	public List<Heat> buildHeatsFromResults(
							List<Result> results,
							String heatsetName,
							String[] heatnames,
							int qualifyingRank) {
		int nbHeats = heatnames.length;
		Vector<Heat> heats = new Vector<Heat>(nbHeats);
		for (String name : heatnames) {
			Heat h = factory().createHeat();
			h.setHeatSetName(heatsetName);
			h.setName(name);
			heats.add(h);
		}
		Vector<List<RankedRunner>> rankings = new Vector<List<RankedRunner>>();
		for (Result result : results) {
			rankings.add(result.getRanking());
		}

		int nbRankings = rankings.size();
		int miss = 0; // number of misses per pass through rankings
		int pos = 0; // current position requested in each ranking
		int heat = 0; // current heat where to add next qualified runner
		while( miss<nbRankings ) {
			miss = 0;
			int[] series = getRandomSeries(nbRankings);
			for (int j : series) {
				try {
					RankedRunner runner = rankings.get(j).get(pos);
					if( runner.getRank() <= qualifyingRank ) {
						heats.get(heat).addQualifiedRunner(runner.getRunnerData().getRunner());
						heat = (heat + 1) % nbHeats;
					} else {
						miss++;
					}					
				} catch (IndexOutOfBoundsException e) {
					miss++;
				}				
			}
			pos++;
		}

		return heats;
	}
	
	private int[] getRandomSeries(int nb) {
		// makes nb random permutations in an array of [0,..,nb-1]
		int[] series = new int[nb];
		for (int i = 0; i < series.length; i++) {
			series[i] = i;
		}
		Random random = new Random();
		for (int i = 0; i < nb; i++) {
			int j = random.nextInt(nb);
			int old = series[j];
			series[j] = series[i];
			series[i] = old;
		}
		return series;
	}

	
	public String refreshHtmlHeats(HeatSet[] selectedHeatsets) {
		heats = null;
		return generateHtmlHeats(selectedHeatsets);
	}
	
	public void exportFile(String filename, String format, HeatSet[] selectedHeatsets) throws IOException {
		if( !filename.endsWith(format) ) {
			filename = filename + "." + format; //$NON-NLS-1$
		}
		if( format.equals("html") ) { //$NON-NLS-1$
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(generateHtmlHeats(selectedHeatsets));	
			writer.close();
		}
		if( format.equals("csv") ) { //$NON-NLS-1$
			generateCsvHeats(filename, selectedHeatsets);
		}
	}

	public String generateHtmlHeats(HeatSet[] selectedHeatsets) {
		Vector<Heat> heats = getHeats(selectedHeatsets);
		Html html = new Html();
		for (Heat heat : heats) {
			appendHtmlHeat(heat, html);
		}
		return html.close();
	}
	private void appendHtmlHeat(Heat heat, Html html) {
		html.tag("h1", heat.getHeatSetName() + " " + heat.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		html.open("table"); //$NON-NLS-1$
		int i = 1;
		for (Runner runner : heat.getQualifiedRunners()) {
			html.open("tr").td(Integer.toString(i)).td(runner.getName()).close("tr"); //$NON-NLS-1$ //$NON-NLS-2$
			i++;
		}
		html.close("table"); //$NON-NLS-1$
	}
	
	public void generateCsvHeats(String filename, HeatSet[] selectedHeatsets) throws IOException {
		Vector<Heat> heats = getHeats(selectedHeatsets);
		resetStartnumber();
		CsvWriter writer = new CsvWriter();
		writer.initialize(filename);
		writer.open();
		for (Heat heat : heats) {
			appendCsvHeat(heat, writer);
		}
		writer.close();
	}
	private void appendCsvHeat(Heat heat, CsvWriter writer) throws IOException {
		RunnerIO runnerIO = new RunnerIO(null, null, writer, null,
											getService(SIReaderHandler.class).getZeroTime());
		Course heatCourse = factory().createCourse();
		heatCourse.setName(heat.getName());
		for (Runner runner : heat.getQualifiedRunners()) {
			writer.writeRecord(runnerIO.exportTData(cloneRunnerForHeat(runner, heatCourse)));
		}
	}
	private Runner cloneRunnerForHeat(Runner runner, Course heatCourse) {
		Runner newRunner = factory().createRunner();
		newRunner.setStartnumber(newStartnumber());
		newRunner.setEcard(runner.getEcard());
		newRunner.setFirstname(runner.getFirstname());
		newRunner.setLastname(runner.getLastname());
		newRunner.setCategory(runner.getCategory());
		newRunner.setClub(runner.getClub());
		newRunner.setNC(runner.isNC());
		newRunner.setCourse(heatCourse);
		return newRunner;
	}
	
	private void resetStartnumber() {
		startnumber = 0;
	}
	private int newStartnumber() {
		return ++startnumber;
	}
	
}
