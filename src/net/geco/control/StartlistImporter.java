/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.util.Date;

import net.geco.basics.TimeManager;
import net.geco.basics.Util;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Messages;
import net.geco.model.Runner;



/**
 * @author Simon Denier
 * @since Nov 17, 2010
 *
 */
public class StartlistImporter extends OEImporter {
	
	public StartlistImporter(GecoControl gecoControl) {
		super(StartlistImporter.class, gecoControl);
	}

	@Override
	public void importRunnerRecord(String[] record) {
		// [0-4] N° dép.;Puce;Ident. base de données;Nom;Prénom;
		// [5-12] Né;S;Plage;nc;Départ;Arrivée;Temps;Evaluation;
		// [13-19] N° club;Nom;Ville;Nat;N° cat.;Court;Long;
		// Num1;Num2;Num3;Text1;Text2;Text3;Adr. nom;Rue;Ligne2;Code Post.;Ville;Tél.;Fax;E-mail;Id/Club;Louée;Engagement;Payé
		// ;1061511;10869;DENIER;Simon;80;H;;;00:46:00;;;;5906;5906NO;VALMO;France;11;H21A;H21A
		
		try {
			String ecard = record[1];
			String lastName = trimQuotes(record[3]);
			String firstName = trimQuotes(record[4]);
			if( ecard.equals("") ){ //$NON-NLS-1$
				geco().log(Messages.getString("StartlistImporter.NoEcardWarning") + firstName + " " + lastName); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			Runner runner = runnerControl().buildBasicRunner(ecard); // ensure unique id and ecard
			if( ! record[0].equals("") ){ //$NON-NLS-1$
				Integer startId = Integer.valueOf(record[0]);
				if( registry().findRunnerById(startId)!=null ){
					geco().log(Messages.getString("StartlistImporter.ExistingStartIdMessage1") + startId); //$NON-NLS-1$
					geco().log(Messages.getString("StartlistImporter.ExistingStartIdMessage2") + runner.getStartId()); //$NON-NLS-1$
				} else {
					runner.setStartId(startId);
				}
			}
			if( ! record[2].equals("") ){ //$NON-NLS-1$
				runner.setArchiveId(Integer.valueOf(record[2]));			
			}
			runner.setLastname(lastName);
			runner.setFirstname(firstName);
			runner.setNC(record[8].equals("X")); //$NON-NLS-1$
			
			Date relativeTime = TimeManager.safeParse(record[9]); // ! Time since zero hour
			runner.setRegisteredStarttime( TimeManager.absoluteTime(relativeTime, stage().getZeroHour()) );

			Club club = stageControl().ensureClubInRegistry(record[15], record[14]);
			Category cat = stageControl().ensureCategoryInRegistry(record[18], record[19]);
			runner.setClub(club);
			runner.setCategory(cat);
			runner.setCourse(registry().getDefaultCourseOrAutoFor(cat));
			
			runnerControl().registerNewRunner(runner);	
		} catch (Exception e) {
			geco().announcer().log("Error during import", false);
			geco().announcer().log(e.toString(), false);
			geco().announcer().log(Util.join(record, ";", new StringBuilder()), false);
		}
	}

	
}
