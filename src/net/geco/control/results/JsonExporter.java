/**
 * Copyright (c) 2015 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

import net.geco.basics.GecoResources;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.iojson.JSONSerializer;
import net.geco.model.iojson.JacksonSerializer;

/**
 * @author Simon Denier
 * @since 21 mars 2015
 *
 */
public class JsonExporter {

	Stage stage;
	
	public JsonExporter(Stage stage) {
		this.stage = stage;
	}

	public void generateJson(String filename, List<Result> results) throws IOException {
		BufferedWriter writer = GecoResources.getSafeWriterFor(filename);
		exportResultsToJson(results, new JacksonSerializer(writer, true));
		writer.close();
	}

	public void exportResultsToJson(List<Result> results, JSONSerializer json) throws IOException {
		json.startObject();
		exportGeneralData(json);
		json.startArrayField("results");
		for (Result result : results) {
			exportResult(result, json);
		}
		json.endArray();
		json.endObject();
		json.close();
	}

	public void exportGeneralData(JSONSerializer json) throws IOException {
		long stamp = System.currentTimeMillis();
		long lastTime = stamp + TimeZone.getDefault().getOffset(stamp);
		json.field("lastTime", lastTime);
		json.field("name", stage.getName());
	}

	public void exportResult(Result result, JSONSerializer json) throws IOException {
		json.startObject();
		json.field("name", result.getIdentifier());
		json.field("finishCount", result.nbFinishedRunners());
		json.field("presentCount", result.nbPresentRunners());
		json.startArrayField("rankedRunners");
		for (RankedRunner rankedRunner : result.getRanking()) {
			String rank = Integer.toString(rankedRunner.getRank());
			exportRunner(rankedRunner.getRunnerData(), rank, json);
		}
		json.endArray();
		json.startArrayField("unrankedRunners");
		for (RunnerRaceData runnerData : result.getUnrankedRunners()) {
			exportRunner(runnerData, "", json);
		}
		json.endArray();
		json.endObject();
	}

	public void exportRunner(RunnerRaceData runnerData, String rank,
			JSONSerializer json) throws IOException {
		Runner runner = runnerData.getRunner();
		json.startObject();
		json.field("id", runner.getStartId());
		json.field("firstName", runner.getFirstname());
		json.field("lastName", runner.getLastname());
		json.field("category", runner.getCategory().getName());
		json.field("club", runner.getClub().getName());
		json.field("finishTime", runnerData.getResultTime());
		json.field("totalPenalties", runnerData.getResult().getTimePenalty() +
									 runnerData.getResult().getManualTimePenalty());
		json.field("readTime", runnerData.getReadtime().getTime());
		json.field("status", runnerData.getStatus().toString());
		json.field("rank", rank);
		json.field("nc", runner.isNC());
		json.endObject();
	}

}