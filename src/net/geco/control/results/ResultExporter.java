/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.geco.basics.CsvWriter;
import net.geco.basics.GecoResources;
import net.geco.basics.Html;
import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.control.results.ResultBuilder.ResultConfig;
import net.geco.model.Messages;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

import com.samskivert.mustache.Mustache;

/**
 * @author Simon Denier
 * @since Dec 1, 2010
 *
 */
public class ResultExporter extends AResultExporter {
	
	private int refreshInterval;

	public ResultExporter(GecoControl gecoControl) {
		super(ResultExporter.class, gecoControl);
	}

	@Override
	protected void exportHtmlFile(String filename, ResultConfig config, int refreshInterval)
			throws IOException {
		BufferedWriter writer = GecoResources.getSafeWriterFor(filename);
		generateHtmlResults("results_ranking.mustache", config, refreshInterval, writer, OutputType.FILE);
		writer.close();
	}

	public void generateHtmlResults(String templateFile, ResultConfig config, int refreshInterval,
			Writer out, OutputType outputType) throws IOException {
		Reader template = new BufferedReader(new FileReader("formats/" + templateFile));
		// TODO: lazy cache of template
		Mustache.compiler().defaultValue("N/A").compile(template).execute(buildDataContext(config, refreshInterval, outputType), out);
		template.close();
	}

	
	protected Object buildDataContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		boolean isSingleCourseResult = config.resultType != ResultType.CategoryResult;

		// TODO remove show empty/others from config
		// TODO utf8 yes for file, no for internal, for print mode?
		Map<String, Object> stageContext = new HashMap<String, Object>();
		stageContext.put("geco_StageTitle", stage().getName());

		// General layout
		stageContext.put("geco_CourseInfo?", isSingleCourseResult);
		stageContext.put("geco_RunnerCategory?", isSingleCourseResult);
		stageContext.put("geco_Penalties?", config.showPenalties);

		// Meta info
		stageContext.put("geco_AutoRefresh?", refreshInterval > 0);
		stageContext.put("geco_RefreshInterval", refreshInterval);
		stageContext.put("geco_PrintMode?", outputType == OutputType.PRINTER);
		stageContext.put("geco_Timestamp", new SimpleDateFormat("H:mm").format(new Date()));
		
		List<Result> results = buildResults(config);
		List<Map<String,Object>> resultsCollection = new ArrayList<Map<String, Object>>(results.size());
		stageContext.put("geco_ResultsCollection", resultsCollection);
		for (Result result : results) {
			if( ! result.isEmpty() ) {
				boolean paceComputable = isSingleCourseResult && result.anyCourse().hasDistance();
				long bestTime = result.bestTime();

				Map<String, Object> resultContext = new HashMap<String, Object>();
				resultsCollection.add(resultContext);

				resultContext.put("geco_ResultName", result.getIdentifier());
				resultContext.put("geco_NbFinishedRunners", result.nbFinishedRunners());
				resultContext.put("geco_NbPresentRunners", result.nbPresentRunners());
				resultContext.put("geco_RunnerPace?", paceComputable);
				if( isSingleCourseResult ) {
					resultContext.put("geco_CourseLength", result.anyCourse().getLength());
					resultContext.put("geco_CourseClimb", result.anyCourse().getClimb());
				}
				
				List<Map<String,Object>> runnersCollection = new ArrayList<Map<String, Object>>(result.getRanking().size());
				resultContext.put("geco_RankedRunners", runnersCollection);
				for (RankedRunner rankedRunner : result.getRanking()) {
					RunnerRaceData data = rankedRunner.getRunnerData();
					Runner runner = data.getRunner();

					Map<String, Object> runnerContext = new HashMap<String, Object>();
					runnersCollection.add(runnerContext);

					runnerContext.put("geco_RunnerRank", rankedRunner.getRank());
					runnerContext.put("geco_RunnerFirstName", runner.getFirstname());
					runnerContext.put("geco_RunnerLastName", runner.getLastname());
					runnerContext.put("geco_RunnerClubName", runner.getClub().getName());
					runnerContext.put("geco_RunnerCategory", runner.getCategory().getName());
					runnerContext.put("geco_RunnerStatus", data.getResult().formatStatus());
					runnerContext.put("geco_RunnerResultTime", data.getResult().formatRacetime());
					runnerContext.put("geco_RunnerDiffTime", rankedRunner.formatDiffTime(bestTime));
					
					if( paceComputable ) {
						runnerContext.put("geco_RunnerPace", data.formatPace());
					}
					if( config.showPenalties ) {
						// TODO put penalties
						
					}
				}

				runnersCollection = new ArrayList<Map<String, Object>>();
				resultContext.put("geco_UnrankedRunners", runnersCollection);
				for (RunnerRaceData data : result.getUnrankedRunners()) {
					Runner runner = data.getRunner();
					if( ! runner.isNC() || config.showNC ) {
						Map<String, Object> runnerContext = new HashMap<String, Object>();
						runnersCollection.add(runnerContext);
						
						runnerContext.put("geco_RunnerRank", runner.isNC() ? "NC" : "");
						runnerContext.put("geco_RunnerFirstName", runner.getFirstname());
						runnerContext.put("geco_RunnerLastName", runner.getLastname());
						runnerContext.put("geco_RunnerClubName", runner.getClub().getName());
						runnerContext.put("geco_RunnerCategory", runner.getCategory().getName());
						runnerContext.put("geco_RunnerStatus", data.getResult().shortFormat()); // TODO mixed field for NC? status/time
					}
					// TODO pace?
					if( config.showPenalties ) {
						// TODO put penalties
						
					}
				}
			}
		}
		return stageContext;
	}

	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval, OutputType outputType) {
		List<Result> results = buildResults(config);
		this.refreshInterval = refreshInterval;
		Html html = new Html();
		includeHeader(html, "result.css", outputType); //$NON-NLS-1$
		if( outputType != OutputType.DISPLAY ) {
			html.nl().tag("h1", stage().getName() //$NON-NLS-1$
								+ " - "			  //$NON-NLS-1$
								+ Messages.getString("ResultExporter.ResultsOutputTitle")); //$NON-NLS-1$
		}
		String timestamp = null;
		if( outputType == OutputType.PRINTER ) {
			SimpleDateFormat tsFormat = new SimpleDateFormat("H:mm"); //$NON-NLS-1$
			timestamp = tsFormat.format(new Date());
		}
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendHtmlResult(result, config, html, timestamp);
			}
		}
		return html.close();
	}
	
	protected void generateHtmlHeader(Html html) {
		if( refreshInterval>0 ) {
			html.contents("<meta http-equiv=\"refresh\" content=\"" //$NON-NLS-1$
					+ refreshInterval + "\" />"); //$NON-NLS-1$
		}
	}

	/**
	 * @param result
	 * @param html
	 * @param appendTimestamp 
	 */
	private void appendHtmlResult(Result result, ResultConfig config, Html html, String timestamp) {
		boolean paceComputable = ! result.isEmpty()
								&& ! config.resultType.equals(ResultType.CategoryResult)
								&& result.anyCourse().hasDistance();
		// compute basic stats
		StringBuilder resultLabel = new StringBuilder(result.getIdentifier());
		int finished = result.getRanking().size() + result.getUnrankedRunners().size();
		int present = finished;
		for (RunnerRaceData other : result.getUnresolvedRunners()) {
			if( other.getResult().getStatus().isUnresolved() ) {
				present++;
			}
		}
		resultLabel.append(" (").append(Integer.toString(finished)).append("/") //$NON-NLS-1$ //$NON-NLS-2$
					.append(Integer.toString(present)).append(")"); //$NON-NLS-1$
		if( paceComputable ){			
			resultLabel.append(" - ").append(result.anyCourse().formatDistanceClimb()); //$NON-NLS-1$
		}
		html.nl().tag("h2", resultLabel.toString()).nl(); //$NON-NLS-1$
		
		html.open("table").nl(); //$NON-NLS-1$
		html.openTr("runner") //$NON-NLS-1$
			.th("") //$NON-NLS-1$
			.th(Messages.getString("ResultBuilder.NameHeader"), "class=\"left\"") //$NON-NLS-1$ //$NON-NLS-2$
			.th(Messages.getString("ResultBuilder.ClubHeader"), "class=\"left\"") //$NON-NLS-1$ //$NON-NLS-2$
			.th(Messages.getString("ResultBuilder.CategoryHeader"), "class=\"left\"") //$NON-NLS-1$ //$NON-NLS-2$
			.th(Messages.getString("ResultBuilder.TimeHeader"), "class=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
			.th(Messages.getString("ResultExporter.DiffHeader"), "class=\"right\""); //$NON-NLS-1$ //$NON-NLS-2$
		if( paceComputable ){
			html.th(Messages.getString("ResultExporter.minkmLabel"), "class=\"right\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if( config.showPenalties ){
			html.th(Messages.getString("ResultBuilder.MPHeader"), "class=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("ResultBuilder.RacetimeHeader"), "class=\"right\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		html.closeTr();
		// Format: rank, first name + last name, club, cat, time/status, diff, pace [, real time, nb mps]
		long bestTime = result.bestTime();
		for (RankedRunner runner : result.getRanking()) {
			RunnerRaceData data = runner.getRunnerData();
			writeHtml(
					data,
					Integer.toString(runner.getRank()),
					data.getResult().formatRacetime(),
					runner.formatDiffTime(bestTime),
					(paceComputable ? data.formatPace() : ""), //$NON-NLS-1$
					config.showPenalties,
					html);
		}
		emptyTr(html);
		for (RunnerRaceData runnerData : result.getUnrankedRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				writeHtml(
						runnerData,
						"", //$NON-NLS-1$
						runnerData.getResult().formatStatus(),
						"", //$NON-NLS-1$
						"", //$NON-NLS-1$
						config.showPenalties,
						html);
			} else if( config.showNC ) {
				writeHtml(
						runnerData,
						"NC", //$NON-NLS-1$
						runnerData.getResult().shortFormat(),
						"", //$NON-NLS-1$
						"", //$NON-NLS-1$
						config.showPenalties,
						html);
			}
		}
		if( config.showOthers ) {
			emptyTr(html);
			for (RunnerRaceData runnerData : result.getUnresolvedRunners()) {
				writeHtml(
						runnerData,
						"", //$NON-NLS-1$
						runnerData.getResult().formatStatus(),
						"", //$NON-NLS-1$
						"", //$NON-NLS-1$
						config.showPenalties,
						html);
			}			
		}
		html.close("table").nl(); //$NON-NLS-1$
		if( timestamp != null ) {
			html.nl().tag("p", Messages.getString("ResultExporter.LastUpdateLabel") + timestamp); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void writeHtml(RunnerRaceData runnerData, String rank, String timeOrStatus, String diffTime,
															String pace, boolean showPenalties, Html html) {
		html.openTr("runner"); //$NON-NLS-1$
		html.td(rank);
		html.td(runnerData.getRunner().getName());
		html.td(runnerData.getRunner().getClub().getName());
		html.td(runnerData.getRunner().getCategory().getName());
		html.td(timeOrStatus, "class=\"time\""); //$NON-NLS-1$
		html.td(diffTime, "class=\"diff\""); //$NON-NLS-1$
		if( ! pace.isEmpty() ){
			html.td(pace, "class=\"pace\""); //$NON-NLS-1$
		}
		if( showPenalties ){
			html.td(Integer.toString(runnerData.getResult().getNbMPs()), "class=\"right\""); //$NON-NLS-1$
			html.td(TimeManager.time(runnerData.realRaceTime()), "class=\"right\""); //$NON-NLS-1$
		}
		html.closeTr();
	}

	@Override
	public void generateOECsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		getService(SplitExporter.class).generateOECsvResult(config, false, writer);
	}

	@Override
	public void generateXMLResult(ResultConfig config, String filename)
			throws Exception {
		new SplitXmlExporter(geco()).generateXMLResult(buildResults(config), filename, false);		
	}
	
}
