/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.testfactory;

import net.geco.basics.TimeManager;
import net.geco.model.Factory;
import net.geco.model.Trace;
import net.geco.model.TraceData;
import net.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since Jul 23, 2013
 *
 */
public class TraceFactory {

	private static Factory factory = new POFactory();

	public static Trace trace(String code) {
		return factory.createTrace(code, TimeManager.NO_TIME);
	}
	
	public static Trace[] createTrace(String... codes) {
		Trace[] trace = new Trace[codes.length];
		for (int i = 0; i < trace.length; i++) {
			trace[i] = trace(codes[i]);
		}
		return trace;
	}

	public static TraceData createTraceData(String... codes) {
		TraceData traceData = factory.createTraceData();
		traceData.setTrace(createTrace(codes));
		return traceData; 
	}
	
}
