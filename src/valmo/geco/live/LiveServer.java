/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JOptionPane;

import valmo.geco.control.Control;
import valmo.geco.control.GecoControl;
import valmo.geco.control.RunnerControl;
import valmo.geco.control.RunnerCreationException;
import valmo.geco.model.Messages;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.Status;
import valmo.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Sep 6, 2010
 *
 */
public class LiveServer extends Control {

	private Socket clientSocket;
	private Thread liveThread;
	private LiveServerMulti serverMulti;
	private RunnerControl runnerControl;

	public LiveServer(GecoControl gecoControl, Socket clientSocket, LiveServerMulti serverMulti) throws IOException {
		super(gecoControl);
		runnerControl = new RunnerControl(gecoControl);
		this.clientSocket = clientSocket;
		this.serverMulti = serverMulti;
	}
	
	public LiveServer start() {
		liveThread = new Thread() {
			public void run() {
				System.out.println("start"); //$NON-NLS-1$
				try {
					BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			        String inputLine = null;
			        
			        while (!isInterrupted() && (inputLine = clientInput.readLine()) != null) {
			        	processData(inputLine);
			        	if( inputLine.equals("Bye") ) { //$NON-NLS-1$
			        		JOptionPane.showMessageDialog(
			        						null,
			        						Messages.liveGet("LiveServer.ConnectionTerminatedMessage"), //$NON-NLS-1$
			        						Messages.liveGet("LiveServer.ConnectionTerminatedTitle"), //$NON-NLS-1$
			        						JOptionPane.INFORMATION_MESSAGE);
			        		break;
			        	}
			        }
			        if( inputLine==null ) {
			        	JOptionPane.showMessageDialog(
			        					null,
			        					Messages.liveGet("LiveServer.ConnectionLostMessage"), //$NON-NLS-1$
			        					Messages.liveGet("LiveServer.ConnectionLostTitle"), //$NON-NLS-1$
			        					JOptionPane.WARNING_MESSAGE);
			        }
			        close();
				} catch (IOException e) {
					System.out.println("Live thread stopped"); //$NON-NLS-1$
				}
				System.out.println("done"); //$NON-NLS-1$
			}
		};
		
		liveThread.start();
		return this;
	}
	
	private void close() {
        try {
        	System.out.print("closing"); //$NON-NLS-1$
			clientSocket.close();
			System.out.println(" - closed"); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverMulti.terminated(this);
	}

	public void stop() {
		liveThread.interrupt();
		close();
	}

	private String processData(String input) {
		if( input.equals("Hi") || //$NON-NLS-1$
			input.equals("Idle") || //$NON-NLS-1$
			input.equals("Bye") ) //$NON-NLS-1$
			return input;
		return processData(input.split(",")); //$NON-NLS-1$
	}
	
	private String processData(String[] data) {
		// chip, start, last, first, cat, club, course, status, racetime, mps, penalties, trace...
		Runner runner = registry().findRunnerByEcard(data[0]);
		if( runner==null || Integer.parseInt(data[1])!=runner.getStartnumber() ) {
			System.err.println("creation " + data[0]); //$NON-NLS-1$
			try {
				runner = runnerControl.createAnonymousRunner(registry().findCourse(data[6]));
				runnerControl.validateChipnumber(runner, data[0]);
				runnerControl.validateStartnumber(runner, data[1]);
				runnerControl.validateLastname(runner, data[2]);
				runnerControl.validateFirstname(runner, data[3]);
				runnerControl.validateCategory(runner, data[4]);
				runnerControl.validateClub(runner, data[5]);
				serverMulti.announceNewData();
			} catch (RunnerCreationException e) {
				e.printStackTrace();
			}
		} else { // check that course is consistent
			if( !runner.getCourse().getName().equals(data[6]) ) {
				System.err.println("inconsistent course " + data[6]); //$NON-NLS-1$
				runnerControl.validateCourse(registry().findRunnerData(runner), data[6]);
			}
		}
		
		RunnerResult result = factory().createRunnerResult();
		result.setStatus(Status.valueOf(data[7]));
		result.setRacetime(Long.parseLong(data[8]));
		result.setNbMPs(Integer.parseInt(data[9]));
		result.setTimePenalty(Long.parseLong(data[10]));
		result.setTrace(createTraceFrom(Arrays.copyOfRange(data, 11, data.length)));
		
		RunnerRaceData runnerData = registry().findRunnerData(runner);
		runnerData.setResult(result);
		serverMulti.announceData(runnerData);
		return "ok"; //$NON-NLS-1$
	}

	/**
	 * @param copyOfRange
	 * @return
	 */
	private Trace[] createTraceFrom(String[] trace) {
		Trace[] trace2 = new Trace[trace.length];
		for (int i = 0; i < trace.length; i++) {
			trace2[i] = factory().createTrace(trace[i], new Date(0));
		}
		return trace2;
	}
	
}
