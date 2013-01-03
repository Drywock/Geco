/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iojson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.geco.basics.GecoResources;
import net.geco.control.Checker;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.HeatSet;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.Trace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

/**
 * @author Simon Denier
 * @since Dec 26, 2012
 *
 */
public class PersistentStore {

	public static final String JSON_SCHEMA_VERSION = "2.0";
	
	private static final boolean DEBUG = true;
	
	/*
	 * TODO
	 * - build an adapter/bridge to jackson writer
	 * - build an adapter/bridge to json reader
	 */
	
	private IdMap idMap;

	public PersistentStore() {
		idMap = new IdMap();
	}
	
	public int idFor(Object o) {
		return idMap.idFor(o);
	}
	
	public void storeData(Stage stage) {
		try {
			String datafile = "store.json";
			BufferedWriter writer = GecoResources.getSafeWriterFor(stage.getBaseDir() + GecoResources.sep + datafile);

			JSONWriter json = new JSONWriter(writer);
			json.object()
				.key(K.VERSION).value(JSON_SCHEMA_VERSION);
			
			json.key(K.STAGE).object();
			json.key(K.NAME).value(stage.getName())
				.key(K.BASEDIR).value(stage.getBaseDir())
				.key(K.ZEROHOUR).value(stage.getZeroHour())
				.endObject();
			
			json.key(K.PROPERTIES).object().endObject();
			
			json.key(K.COURSES).array();
			for (Course c : stage.registry().getCourses()) {
				json.object()
					.key(K.NAME).value(c.getName())
					.key(K.ID).value(idFor(c))
					.key(K.LENGTH).value(c.getLength())
					.key(K.CLIMB).value(c.getClimb())
					.key(K.CODES).array();
				for (int code : c.getCodes()) {
					json.value(code);
				}
				json.endArray()
					.endObject();
				
			}
			json.endArray();
			
			json.key(K.CATEGORIES).array();
			for (Category c : stage.registry().getCategories()) {
				json.object()
					.key(K.NAME).value(c.getShortname())
					.key(K.ID).value(idFor(c))
					.key(K.LONG).value(c.getLongname());
				if( c.getCourse() != null ){
					json.key(K.COURSE).value(idFor(c.getCourse()));
				}
				json.endObject();
								
			}
			json.endArray();
			
			json.key(K.CLUBS).array();
			for (Club c : stage.registry().getClubs()) {
				json.object()
					.key(K.NAME).value(c.getName())
					.key(K.ID).value(idFor(c))
					.key(K.SHORT).value(c.getShortname())
					.endObject();
						
			}
			json.endArray();
			
			json.key(K.HEATSETS).array();
			for (HeatSet h : stage.registry().getHeatSets()) {
				json.object()
					.key(K.NAME).value(h.getName())
					.key(K.RANK).value(h.getQualifyingRank())
					.key(K.TYPE).value(h.getSetType())
					.key(K.HEATS).array().endArray()
					.key(K.POOLS).array().endArray()
					.endObject();
						
			}
			json.endArray();
			
			json.key(K.RUNNERS_DATA).array();
			for (RunnerRaceData runnerData : stage.registry().getRunnersData()) {
				json.array();
				Runner runner = runnerData.getRunner();
				json.object()
					.key(K.START_ID).value(runner.getStartId())
					.key(K.FIRST).value(runner.getFirstname())
					.key(K.LAST).value(runner.getLastname())
					.key(K.ECARD).value(runner.getEcard())
					.key(K.CLUB).value(idFor(runner.getClub()))
					.key(K.CAT).value(idFor(runner.getCategory()))
					.key(K.COURSE).value(idFor(runner.getCourse()))
					.key(K.START).value(runner.getRegisteredStarttime().getTime());
				if( runner.getArchiveId() != null ){
					json.key(K.ARK).value(runner.getArchiveId());
				}
				if( runner.rentedEcard() ){
					json.key(K.RENT).value(true);
				}
				if( runner.isNC() ){
					json.key(K.NC).value(true);
				}
				json.endObject();
				
				json.object()
					.key(K.START).value(runnerData.getStarttime().getTime())
					.key(K.FINISH).value(runnerData.getFinishtime().getTime())
					.key(K.ERASE).value(runnerData.getErasetime().getTime())
					.key(K.CHECK).value(runnerData.getControltime().getTime())
					.key(K.READ).value(runnerData.getReadtime().getTime())
					.key(K.PUNCHES).array();
				for (Punch punch : runnerData.getPunches()) {
					json.value(punch.getCode()).value(punch.getTime().getTime());
				}
				json.endArray().endObject();
				
				RunnerResult result = runnerData.getResult();
				json.object()
					.key(K.TIME).value(result.getRacetime())
					.key(K.STATUS).value(result.getStatus())
					.key(K.MPS).value(result.getNbMPs())
					.key(K.PENALTY).value(result.getTimePenalty());
				
				JSONArray jTrace = new JSONArray();
				JSONArray jNeutralized = new JSONArray();
				for (int i = 0; i < result.getTrace().length; i++) {
					Trace trace = result.getTrace()[i];
					jTrace.put(trace.getCode()).put(trace.getTime().getTime());
					if( trace.isNeutralized() ){
						jNeutralized.put(i);
					}
				}
				json.key(K.TRACE).value(jTrace);
				json.key(K.NEUTRALIZED).value(jNeutralized);
				json.endObject().endArray();
				
			}
			json.endArray();
			json.key(K.MAXID).value(idMap.maxId());
			json.endObject();
			writer.close();
			
			backupData(stage.getBaseDir(), datafile, "store.zip");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void backupData(String basedir, String datafile, String backupname) throws IOException {
		ZipOutputStream zipStream = 
				new ZipOutputStream(new FileOutputStream(basedir + GecoResources.sep + backupname));
		writeZipEntry(zipStream, datafile, basedir);	
		zipStream.close();
	}

	private void writeZipEntry(ZipOutputStream zipStream, String filename, String basedir)
			throws IOException, FileNotFoundException {
		ZipEntry zipEntry = new ZipEntry(filename);
		zipStream.putNextEntry(zipEntry);
		byte[] buffer = new byte[4096];
		BufferedInputStream inputStream =
						new BufferedInputStream(new FileInputStream(basedir + GecoResources.sep + filename));
		int len;
		while( (len = inputStream.read(buffer)) != -1 ) {
			zipStream.write(buffer, 0, len);
		}
		inputStream.close();
		zipStream.closeEntry();
	}

	public Stage loadData(String baseDir, Factory factory, Checker checker) {
		Stage newStage = factory.createStage();
		try {
			BufferedReader reader = GecoResources.getSafeReaderFor(baseDir + GecoResources.sep + "store.json");
			JSONObject store = new JSONObject(new JSONTokener(reader));
			
			// TODO
//			loadStageProperties(store, newStage, baseDir);
			importDataIntoRegistry(store, newStage, factory);
			// REMOVE???
//			checker.postInitialize(newStage); // post initialization
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newStage;
	}

	private void importDataIntoRegistry(JSONObject store, Stage newStage, Factory factory) throws JSONException {
		Registry registry = new Registry();
		newStage.setRegistry(registry);

		RefMap refMap = new RefMap(store.getInt(K.MAXID) + 1);
			
		JSONArray courses = store.getJSONArray(K.COURSES);
		for (int i = 0; i < courses.length(); i++) {
			JSONObject c = courses.getJSONObject(i);
			Course course = factory.createCourse();
			course.setName(c.getString(K.NAME));
			course.setLength(c.getInt(K.LENGTH));
			course.setClimb(c.getInt(K.CLIMB));
			JSONArray codes = c.getJSONArray(K.CODES);
			int[] codez = new int[codes.length()];
			for (int j = 0; j < codes.length(); j++) {
				codez[j] = codes.getInt(j);
			}
			course.setCodes(codez);
			refMap.put(c.getInt(K.ID), course);
			registry.addCourse(course);
		}
		registry.ensureAutoCourse(factory);

		JSONArray categories = store.getJSONArray(K.CATEGORIES);
		for (int i = 0; i < categories.length(); i++) {
			JSONObject c = categories.getJSONObject(i);
			Category category = factory.createCategory();
			category.setName(c.getString(K.NAME));
			category.setLongname(c.getString(K.LONG));
			category.setCourse((Course) refMap.get(c.optInt(K.COURSE))); // ref[0] = null
			refMap.put(c.getInt(K.ID), category);
			registry.addCategory(category);
		}

		JSONArray clubs = store.getJSONArray(K.CLUBS);
		for (int i = 0; i < clubs.length(); i++) {
			JSONObject c = clubs.getJSONObject(i);
			Club club = factory.createClub();
			club.setName(c.getString(K.NAME));
			club.setShortname(c.getString(K.SHORT));
			refMap.put(c.getInt(K.ID), club);
			registry.addClub(club);
		}

		store.getJSONArray(K.HEATSETS); // TODO

		JSONArray runnersData = store.getJSONArray(K.RUNNERS_DATA);
		for (int i = 0; i < runnersData.length(); i++) {
			JSONArray runnerData = runnersData.getJSONArray(i);

			JSONObject r = runnerData.getJSONObject(0);
			Runner runner = factory.createRunner();
			runner.setStartId(r.getInt(K.START_ID));
			runner.setFirstname(r.getString(K.FIRST));
			runner.setLastname(r.getString(K.LAST));
			runner.setEcard(r.getString(K.ECARD));
			runner.setClub((Club) refMap.get(r.getInt(K.CLUB)));
			runner.setCategory((Category) refMap.get(r.getInt(K.CAT)));
			runner.setCourse((Course) refMap.get(r.getInt(K.COURSE)));
			runner.setRegisteredStarttime(new Date(r.getLong(K.START)));
			runner.setArchiveId((Integer) r.opt(K.ARK));
			// TODO: nc, rented
			registry.addRunner(runner);

			JSONObject d = runnerData.getJSONObject(1);
			RunnerRaceData ecardData = factory.createRunnerRaceData();
			ecardData.setRunner(runner);
			ecardData.setStarttime(new Date(d.getLong(K.START)));
			ecardData.setFinishtime(new Date(d.getLong(K.FINISH)));
			ecardData.setErasetime(new Date(d.getLong(K.ERASE)));
			ecardData.setControltime(new Date(d.getLong(K.CHECK)));
			ecardData.setReadtime(new Date(d.getLong(K.READ)));
			JSONArray p = d.getJSONArray(K.PUNCHES);
			Punch[] punches = new Punch[p.length() / 2];
			ecardData.setPunches(punches);
			for (int j = 0; j < punches.length; j++) {
				punches[j] = factory.createPunch();
				punches[j].setCode(p.getInt(2 * j));
				punches[j].setTime(new Date(p.getLong(2 * j + 1)));
			}
			registry.addRunnerData(ecardData);

			JSONObject res = runnerData.getJSONObject(2);
			RunnerResult result = factory.createRunnerResult();
			result.setRacetime(res.getLong(K.TIME));
			result.setStatus(Status.valueOf(res.getString(K.STATUS)));
			result.setNbMPs(res.getInt(K.MPS));
			result.setTimePenalty(res.getLong(K.PENALTY));
			JSONArray t = res.getJSONArray(K.TRACE);
			Trace[] trace = new Trace[t.length() / 2];
			result.setTrace(trace);
			for (int j = 0; j < trace.length; j++) {
				trace[j] = factory.createTrace(t.getString(2 * j),
						new Date(t.getLong(2 * j + 1)));
			}
			JSONArray neut = res.getJSONArray(K.NEUTRALIZED);
			for (int j = 0; j < neut.length(); j++) {
				trace[neut.getInt(j)].setNeutralized(true);
			}
			ecardData.setResult(result);
		}
			
	}
	
	public static class K {
		
		static {
			if( DEBUG ) {
				START_ID = "startid";
				FIRST = "first";
				LAST = "last";
				ECARD = "ecard";
				CLUB = "club";
				CAT = "cat";
				COURSE = "course";
				ARK = "ark";
				RENT = "rent";
				NC = "nc";

				TIME = "time";
				STATUS = "status";
				MPS = "mps";
				PENALTY = "penalty";
				TRACE = "trace";
				NEUTRALIZED = "neut";
			
				START = "start";
				FINISH = "finish";
				ERASE = "erase";
				READ = "read";
				CHECK = "check";
				PUNCHES = "punches";
			} else {
				START_ID = "i";
				FIRST = "f";
				LAST = "l";
				ECARD = "e";
				CLUB = "u";
				CAT = "t";
				COURSE = "c";
				ARK = "a";
				RENT = "r";
				NC = "n";

				TIME = "t";
				STATUS = "s";
				MPS = "m";
				PENALTY = "p";
				TRACE = "r";
				NEUTRALIZED = "n";
			
				START = "s";
				FINISH = "f";
				ERASE = "e";
				READ = "r";
				CHECK = "c";
				PUNCHES = "p";
			}
		}

		private static final String ID = "id";;
		private static final String MAXID = "maxid";;
		private static final String NAME = "name";
		private static final String VERSION = "version";

		private static final String STAGE = "stage";
		private static final String BASEDIR = "basedir";
		private static final String ZEROHOUR = "zerohour";

		private static final String PROPERTIES = "properties";

		private static final String COURSES = "courses";
		private static final String LENGTH = "length";
		private static final String CLIMB = "climb";
		private static final String CODES = "codes";

		private static final String CATEGORIES = "categories";;
		private static final String LONG = "long";
		private static final String CLUBS = "clubs";
		private static final String SHORT = "short";

		private static final String HEATSETS = "heatsets";
		private static final String RANK = "rank";
		private static final String TYPE = "type";
		private static final String HEATS = "heats";
		private static final String POOLS = "pools";

		private static final String RUNNERS_DATA = "runnersData";

		private static final String START_ID;
		private static final String FIRST;
		private static final String LAST;
		private static final String ECARD;
		private static final String CLUB;
		private static final String CAT;
		private static final String COURSE;
		private static final String ARK;
		private static final String NC;
		private static final String RENT;

		private static final String START;
		private static final String FINISH;
		private static final String ERASE;
		private static final String CHECK;
		private static final String READ;
		private static final String PUNCHES;

		private static final String TIME;
		private static final String STATUS;
		private static final String MPS;
		private static final String PENALTY;
		private static final String TRACE;
		private static final String NEUTRALIZED;
	}
	
}
