/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Properties;

import net.geco.basics.Announcer.StageListener;
import net.geco.basics.CsvWriter;
import net.geco.basics.GecoResources;
import net.geco.control.GecoControl;
import net.geco.control.results.ResultBuilder.ResultConfig;
import net.geco.control.results.context.ContextList;
import net.geco.control.results.context.GenericContext;
import net.geco.control.results.context.ResultContext;
import net.geco.control.results.context.RunnerContext;
import net.geco.control.results.context.StageContext;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;

import com.samskivert.mustache.Mustache;

/**
 * @author Simon Denier
 * @since Dec 1, 2010
 *
 */
public class ResultExporter extends AResultExporter implements StageListener {
	
	private File rankingTemplate;

	public ResultExporter(GecoControl gecoControl) {
		super(ResultExporter.class, gecoControl);
		geco().announcer().registerStageListener(this);
		changed(null, null);
	}

	@Override
	protected void exportHtmlFile(String filename, ResultConfig config, int refreshInterval)
			throws IOException {
		BufferedReader template = GecoResources.getSafeReaderFor(getRankingTemplate().getAbsolutePath());
		BufferedWriter writer = GecoResources.getSafeWriterFor(filename);
		buildHtmlResults(template, config, refreshInterval, writer, OutputType.FILE);
		writer.close();
		template.close();
	}

	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval, OutputType outputType) {
		Reader reader;
		StringWriter out = new StringWriter();
		try {
			switch (outputType) {
			case DISPLAY:
				// TODO I18N template headers
				reader = GecoResources.getResourceReader("/resources/formats/results_ranking_internal.mustache");
				break;
			case PRINTER:
			default:
				reader = GecoResources.getSafeReaderFor(getRankingTemplate().getAbsolutePath());
			}
			buildHtmlResults(reader, config, refreshInterval, out, outputType);
			reader.close();
		} catch (IOException e) {
			geco().logger().debug(e);
		}
		return out.toString();
	}

	protected void buildHtmlResults(Reader template, ResultConfig config, int refreshInterval,
			Writer out, OutputType outputType) {
		// TODO: lazy cache of template
		Mustache.compiler()
			.defaultValue("N/A")
			.compile(template)
			.execute(buildDataContext(config, refreshInterval, outputType), out);
	}

	protected Object buildDataContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		// TODO remove show empty/others from config
		boolean isSingleCourseResult = config.resultType != ResultType.CategoryResult;
		List<Result> results = buildResults(config);

		StageContext stageCtx = new StageContext(
				stage().getName(), isSingleCourseResult, config.showPenalties, refreshInterval, outputType);
		ContextList resultsCollection = stageCtx.createResultsCollection(results.size());
		mergeCustomStageProperties(stageCtx);

		for (Result result : results) {
			if( ! result.isEmpty() ) {
				long bestTime = result.bestTime();

				ResultContext resultCtx =
						resultsCollection.addContext(new ResultContext(result, isSingleCourseResult));
				ContextList rankingCollection = resultCtx.createRankedRunnersCollection();
				ContextList unrankedCollection = resultCtx.createUnrankedRunnersCollection();
				
				for (RankedRunner rankedRunner : result.getRanking()) {
					rankingCollection.add(RunnerContext.createRankedRunner(rankedRunner, bestTime));
				}

				for (RunnerRaceData data : result.getUnrankedRunners()) {
					Runner runner = data.getRunner();
					if( runner.isNC() ) {
						if( config.showNC ) {
							unrankedCollection.add(RunnerContext.createNCRunner(data));
						} // else nothing
					} else {
						unrankedCollection.add(RunnerContext.createUnrankedRunner(data));
					}
				}
			}
		}
		return stageCtx;
	}

	protected void mergeCustomStageProperties(GenericContext stageContext) {
		final String customPropertiesPath = stage().filepath("formats.prop");
		if( GecoResources.exists(customPropertiesPath) ) {
			Properties props = new Properties();
			try {
				props.load( GecoResources.getSafeReaderFor(customPropertiesPath) );
				stageContext.mergeProperties(props);
			} catch (IOException e) {
				geco().logger().debug(e);
			}
		}
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

	public File getRankingTemplate() {
		return rankingTemplate;
	}

	public void setRankingTemplate(File selectedFile) {
		rankingTemplate = selectedFile;
	}
	
	@Override
	public void changed(Stage previous, Stage current) {
		try {
			setRankingTemplate(new File( stage().getProperties().getProperty(rankingTemplateProperty()) ));
		} catch (NullPointerException e) {
			setRankingTemplate(new File("formats/results_ranking.mustache")); //$NON-NLS-1$
		}
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		if( getRankingTemplate().exists() ){
			properties.setProperty(rankingTemplateProperty(), getRankingTemplate().getAbsolutePath());
		}
	}

	@Override
	public void closing(Stage stage) {}
	
	public static String rankingTemplateProperty() {
		return "RankingTemplate"; //$NON-NLS-1$
	}

}
