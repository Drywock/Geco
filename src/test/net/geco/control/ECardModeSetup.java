/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.PunchRecordData;
import org.martin.sireader.server.IResultData;
import org.mockito.Mock;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class ECardModeSetup extends MockControlSetup {

	@Mock protected IResultData<PunchObject, PunchRecordData> card;
	protected RunnerRaceData fullRunnerData;
	protected RunnerRaceData danglingRunnerData;

	public void setUpMockCardData() {
		setUpCardPunches(card);
		fullRunnerData = createFullRunnerData();
		danglingRunnerData = factory.createRunnerRaceData();
	}

	protected void setUpCardPunches(IResultData<PunchObject, PunchRecordData> card) {
		ArrayList<PunchObject> punchArray = new ArrayList<PunchObject>();
		punchArray.add(new PunchObject(31, 2000));
		punchArray.get(0).evaluateTime(0);
		when(card.getPunches()).thenReturn(punchArray);
		when(card.getClearTime()).thenReturn(15000l);
		when(card.getCheckTime()).thenReturn(20000l);
		when(card.getStartTime()).thenReturn(25000l);
		when(card.getFinishTime()).thenReturn(30000l);
	}

	protected RunnerRaceData createFullRunnerData() {
		Runner runner = factory.createRunner();
		runner.setStartId(1);
		runner.setEcard("999");
		runner.setCourse(factory.createCourse());

		RunnerRaceData runnerData = factory.createRunnerRaceData();
		runnerData.setRunner(runner);
		runnerData.setResult(factory.createRunnerResult());
		return runnerData;
	}

	protected void checkCardData(IResultData<PunchObject, PunchRecordData> card, RunnerRaceData data) {
		assertEquals(new Date(card.getClearTime()), data.getErasetime());
		assertEquals(new Date(card.getCheckTime()), data.getControltime());
		assertEquals(new Date(card.getStartTime()), data.getStarttime());
		assertEquals(new Date(card.getFinishTime()), data.getFinishtime());

		ArrayList<PunchObject> punchObjects = card.getPunches();
		Punch[] punches = data.getPunches();
		assertEquals(punchObjects.size(), punches.length);
		for (int i = 0; i < punches.length; i++) {
			PunchObject punchObject = punchObjects.get(i);
			assertEquals(punchObject.getCode(), punches[i].getCode());
			assertEquals(new Date(punchObject.getTime()), punches[i].getTime());			
		}
	}

}
