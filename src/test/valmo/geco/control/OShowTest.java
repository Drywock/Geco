/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.valmo.geco.control;


import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import valmo.geco.control.PenaltyChecker;
import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;
import valmo.geco.model.impl.POFactory;


/**
 * @author Simon Denier
 * @since Jan 2, 2009
 *
 */
public class OShowTest {

	private PenaltyChecker checker;
	private Factory factory;
	private Course course;
	private RunnerRaceData data;

	@Before
	public void setUp() {
		factory = new POFactory();
		checker = new PenaltyChecker(factory);
		checker.setMPPenalty(30000);
		checker.setMPLimit(5);
		course = factory.createCourse();
		data = factory.createRunnerRaceData();
		Runner runner = factory.createRunner();
		runner.setCourse(course);
		data.setRunner(runner);
	}
	
	public Punch punch(Date time, int code) {
		Punch punch = factory.createPunch();
		punch.setTime(time);
		punch.setCode(code);
		return punch;
	}
	
	public Punch punch(int code) {
		return punch(new Date(), code);
	}
	
	public Punch[] punches(int[] codes) {
		Punch[] punches = new Punch[codes.length];
		for (int i = 0; i < codes.length; i++) {
			punches[i] = punch(codes[i]);
		}
		return punches;
	}
	
	int[] pauline = new int[] {
			131,136,142,147,144,135,131,158,160,161,164,131,154,153,131,159,181,104,195,189,107,124,102,189,188,185,189,183,179,189,199,121,109,189,180,175,174,173,172,169,129,200,
	};
	int[] okpauline = new int[] {
			131, 136, 142, 131, 147, 144, 135, 131, 158, 160, 161, 164, 131, 154, 153, 131, 159, 181, 104, 195, 189, 107, 124, 102, 189, 188, 185, 189, 183, 179, 189, 199, 121, 109, 189, 180, 175, 174, 173, 172, 169, 129, 200
	};
	int[] lola = new int[] {
			131,154,154,152,131,142,136,142,131,147,144,135,131,158,161,164,131,159,181,104,195,189,199,121,109,189,107,124,102,189,188,185,189,183,179,189,180,175,174,173,172,169,129,200,
	};
	int[] oklola = new int[] {
			131, 154, 153, 131, 136, 142, 131, 147, 144, 135, 131, 158, 161, 164, 131, 159, 181, 104, 195, 189, 199, 121, 109, 189, 107, 124, 102, 189, 188, 185, 189, 183, 179, 189, 180, 175, 174, 173, 172, 169, 129, 200
	};
	int[] amelie = new int[] {
			131,147,144,135,131,158,161,164,131,154,153,131,136,142,131,159,181,104,195,189,188,185,189,183,179,189,199,121,109,189,107,124,102,189,180,175,174,173,172,169,129,200,
	};
	int[] okamelie = new int[] {
			131,147,144,135,131,158,161,164,131,154,153,131,136,142,131,159,181,104,195,189,188,185,189,183,179,189,199,121,109,189,107,124,102,189,180,175,174,173,172,169,129,200,
	};
	int[] misa = new int[] {
			131,158,161,164,131,153,154,153,131,136,142,131,147,144,135,131,159,181,104,195,189,183,179,189,199,121,109,189,107,124,102,189,188,185,189,180,175,174,173,172,169,129,200,
	};
	int[] okmisa = new int[] {
			131, 158, 161, 164, 131, 154, 153, 131, 136, 142, 131, 147, 144, 135, 131, 159, 181, 104, 195, 189, 183, 179, 189, 199, 121, 109, 189, 107, 124, 102, 189, 188, 185, 189, 180, 175, 174, 173, 172, 169, 129, 200
	};

	@Test
	public void testAmelieRace() {
		course.setCodes(okamelie);
		data.setPunches(punches(amelie));
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(20000000));
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		System.out.println("\nAmelie");
		checker.explainTrace(okamelie, data.getPunches(), false);
	}

	@Test
	public void testMisaRace() {
		course.setCodes(okmisa);
		data.setPunches(punches(misa));
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(20000000));
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		System.out.println("\nMisa");
		checker.explainTrace(okmisa, data.getPunches(), false);
	}

	@Test
	public void testLolaRace() {
		course.setCodes(oklola);
		data.setPunches(punches(lola));
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(20000000));
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		System.out.println("\nLola");
		checker.explainTrace(oklola, data.getPunches(), false);
	}

	@Test
	public void testPaulineRace() {
		course.setCodes(okpauline);
		data.setPunches(punches(pauline));
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(20000000));
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		System.out.println("\nPauline");
		checker.explainTrace(okpauline, data.getPunches(), false);
	}

}
