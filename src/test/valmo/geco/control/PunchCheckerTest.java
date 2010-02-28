/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.valmo.geco.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import valmo.geco.control.PunchChecker;
import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;
import valmo.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class PunchCheckerTest {
	
	private Factory factory;
	private PunchChecker checker;
	
	private Course course;
	private RunnerRaceData data;

	@Before
	public void setUp() {
		factory = new POFactory();
		checker = new PunchChecker(factory);
		course = factory.createCourse();
		data = factory.createRunnerRaceData();
		Runner runner = factory.createRunner();
		runner.setCourse(course);
		data.setRunner(runner);
	}
	
	/**
	 * @param course2
	 */
	public void createSimpleCourse(Course course) {
		course.setCodes(new int[] { 121, 122, 34, 33, 45});
	}
	
	public void createButterflyCourse(Course course) {
		course.setCodes(new int[] { 121, 122, 121, 123, 121, 45});
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
	
	
	@Test
	public void testSimpleCourseOK() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(34), punch(33), punch(45),
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}

	@Test
	public void testSimpleCourseOKWithLoopBack() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(33), punch(45), punch(34), punch(33), punch(45),
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}


	@Test
	public void testSimpleCourseMP() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(34), punch(33), punch(45),
			});
		checker.check(data);
		assertEquals(Status.MP, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}

	@Test
	public void testSimpleCourseInvertPunch() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(122), punch(121), punch(34), punch(33), punch(45),
			});
		checker.check(data);
		assertEquals(Status.MP, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}

	@Test
	public void testSimpleCourseReplacePunch() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(34), punch(33), punch(46),
			});
		checker.check(data);
		assertEquals(Status.MP, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}

	
	@Test
	public void testCourseWithButterlyOK() {
		createButterflyCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(121), punch(123), punch(121), punch(45), 
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}

	@Test
	public void testCourseWithButterlyOKDoublePunch() {
		createButterflyCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(121), punch(121), punch(123), punch(121), punch(45), 
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}

	
	@Test
	public void testCourseWithButterflyMP() {
		createButterflyCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(123), punch(121), punch(45), 
			});
		checker.check(data);
		assertEquals(Status.MP, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}
	
	@Test
	public void testCourseWithButterflyMPInvertLoop() {
		createButterflyCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(123), punch(121), punch(122), punch(121), punch(45), 
			});
		checker.check(data);
		assertEquals(Status.MP, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}

	 
}
