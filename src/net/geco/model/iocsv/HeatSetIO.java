/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iocsv;

import net.geco.model.Factory;
import net.geco.model.HeatSet;
import net.geco.model.Pool;
import net.geco.model.Registry;
import net.geco.model.ResultType;


/**
 * Line format:
 * heatset name, heatset type, qualifyingRank, nbHeats, heatname1, heatname2, ..., set1, set2, ...
 * 
 * @author Simon Denier
 * @since Jun 23, 2009
 *
 */
public class HeatSetIO extends AbstractIO<HeatSet> {
	
	public static String sourceFilename() {
		return "Heatsets.csv"; //$NON-NLS-1$
	}

	public HeatSetIO(Factory factory, CsvReader reader, CsvWriter writer,
			Registry registry) {
		super(factory, reader, writer, registry);
	}

	@Override
	public String[] exportTData(HeatSet heatset) {
		Integer nbHeats = heatset.getNbHeats();
		String[] values = new String[4 + nbHeats + heatset.getSelectedPools().length];
		values[0] = heatset.getName();
		values[1] = heatset.getSetType().name();
		values[2] = heatset.getQualifyingRank().toString();
		values[3] = nbHeats.toString();
		for (int i = 0; i < nbHeats; i++) {
			values[4 + i] = heatset.getHeatNames()[i]; 
		}
		for (int i = 0; i < heatset.getSelectedPools().length; i++) {
			values[4 + nbHeats + i] = heatset.getSelectedPools()[i].getName(); 
		}
		return values;
	}

	@Override
	public HeatSet importTData(String[] record) {
		HeatSet heatSet = this.factory.createHeatSet();
		heatSet.setName(record[0]);
		heatSet.setSetType(ResultType.valueOf(record[1]));
		heatSet.setQualifyingRank(new Integer(record[2]));
		int nbHeats = new Integer(record[3]);
		String[] heatNames = new String[nbHeats];
		for (int i = 0; i < nbHeats; i++) {
			heatNames[i] = record[4 + i];
		}
		heatSet.setHeatNames(heatNames);
		int nbSets = record.length - nbHeats - 4;
		Pool[] selectedSets = new Pool[nbSets];
		if( heatSet.isCourseType() ) {
			for (int i = 0; i < selectedSets.length; i++) {
				selectedSets[i] = registry.findCourse(record[4 + nbHeats + i]);
			}
		} else {
			for (int i = 0; i < selectedSets.length; i++) {
				selectedSets[i] = registry.findCategory(record[4 + nbHeats + i]);
			}			
		}
		heatSet.setSelectedPools(selectedSets);
		return heatSet;
	}

	@Override
	public void register(HeatSet heatset, Registry registry) {
		registry.addHeatSet(heatset);
	}

}
