/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;

import valmo.geco.core.Announcer.StageListener;
import valmo.geco.core.Messages;
import valmo.geco.model.Archive;
import valmo.geco.model.ArchiveRunner;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
import valmo.geco.model.Runner;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class ArchiveManager extends OEImporter implements StageListener {
	
	private Archive archive;
	
	private File archiveFile;
	
	public ArchiveManager(GecoControl gecoControl) {
		super(ArchiveManager.class, gecoControl);
		geco().announcer().registerStageListener(this);
		changed(null, null);
	}
	
	public Archive archive() throws IOException {
		if( archive==null ) {
			if( archiveFile==null ) {
				archive = new Archive();
			} else {
				try {
					loadArchiveFrom(archiveFile);
				} catch (IOException e) {
					archive = new Archive();
					throw e;
				}
			}
		}
		return this.archive;
	}
	
	public void loadArchiveFrom(File archiveFile) throws IOException {
		this.archiveFile = archiveFile;
		archive = new Archive();
		super.loadArchiveFrom(archiveFile);
	}
	
	public String getArchiveName() {
		return ( archiveFile==null )? " " : archiveFile.getName(); //$NON-NLS-1$
	}
	
	public String archiveLastModified() {
		return ( archiveFile==null )? "" :  //$NON-NLS-1$
									DateFormat.getDateInstance().format(new Date(archiveFile.lastModified()));
	}
	
	@Override
	protected void importRunnerRecord(String[] record) {
//		[0-5] Ident. base de données;Puce;Nom;Prénom;Né;S;
//		[6-9] N° club;Nom;Ville;Nat;
//		[10-15] N° cat.;Court;Long;Num1;Num2;Num3;
//		E_Mail;Texte1;Texte2;Texte3;Adr. nom;Rue;Ligne2;Code Post.;Ville;Tél.;Fax;E-mail;Id/Club;Louée
		Club club = ensureClubInArchive(record[7], record[8]);
		Category cat = ensureCategoryInArchive(record[11], record[12]);
		importRunner(record, club, cat);
	}

	private void importRunner(String[] record, Club club, Category cat) {
//		[0-5] Ident. base de données;Puce;Nom;Prénom;Né;S;
		ArchiveRunner runner = geco().factory().createArchiveRunner();
		runner.setArchiveId(new Integer(record[0]));
		runner.setChipnumber(record[1]);
		runner.setLastname(trimQuotes(record[2]));
		runner.setFirstname(trimQuotes(record[3]));
		runner.setBirthYear(record[4]);
		runner.setSex(record[5]);
		runner.setClub(club);
		runner.setCategory(cat);
		archive.addRunner(runner);
	}


	private Club ensureClubInArchive(String shortName, String longName) {
		Club club = archive.findClub(longName);
		if( club==null ) {
			club = geco().factory().createClub();
			club.setName(trimQuotes(longName));
			club.setShortname(shortName);
			archive.addClub(club);
		}
		return club;
	}

	private Category ensureCategoryInArchive(String shortName, String longName) {
		Category cat = archive.findCategory(shortName);
		if( cat==null ) {
			cat = geco().factory().createCategory();
			cat.setShortname(shortName);
			cat.setLongname(longName);
			archive.addCategory(cat);
		}
		return cat;
	}
	
	
	public Runner findAndCreateRunner(String ecard, Course course) {
		try {
			ArchiveRunner arkRunner = archive().findRunner(ecard);
			if( arkRunner==null ){
				return null;
			}
			return createRunner(arkRunner, course);			
		} catch (IOException e) {
			geco().log(e.getLocalizedMessage());
			return null;
		}
	}
	
	public Runner insertRunner(ArchiveRunner arkRunner) {
		Category rCat = ensureCategoryInRegistry(arkRunner.getCategory());
		Course course = registry().getDefaultCourseOrAnyFor(rCat);
		Runner runner = createRunner(arkRunner, course);
		runnerControl().registerNewRunner(runner);
		return runner;
	}
	
	private Category ensureCategoryInRegistry(Category category) {
		return ensureCategoryInRegistry(category.getName(), category.getLongname());
	}

	private Runner createRunner(ArchiveRunner arkRunner, Course course) {
		Club club = arkRunner.getClub();
		Club rClub = ensureClubInRegistry(club.getName(), club.getShortname());
		Category category = arkRunner.getCategory();
		Category rCat = ensureCategoryInRegistry(category.getName(), category.getLongname());
		String ecard = arkRunner.getChipnumber();
		if( ecard.equals("") ){ //$NON-NLS-1$
			geco().log(Messages.getString("ArchiveManager.NoMatchingEcardWarning") + arkRunner.getName()); //$NON-NLS-1$
			ecard = runnerControl().newUniqueChipnumber();
			// TODO: an e-card is required for the registry, however it would be good to get past that REQ
			// part of the move to startnumber as id
		}
		Runner runner = runnerControl().buildBasicRunner(ecard); // ensure unique ecard
		runner.setArchiveId(arkRunner.getArchiveId());
		runner.setFirstname(arkRunner.getFirstname());
		runner.setLastname(arkRunner.getLastname());
		runner.setClub(rClub);
		runner.setCategory(rCat);
		runner.setCourse(course);
		return runner;
	}
	
	@Override
	public void changed(Stage previous, Stage current) {
		archive = null; // discard old archive
		try {
			this.archiveFile = new File( stage().getProperties().getProperty(archiveFileProperty()) );
		} catch (NullPointerException e) {
			this.archiveFile = null;
		}
	}
	@Override
	public void saving(Stage stage, Properties properties) {
		if( archiveFile!=null ) {
			properties.setProperty(archiveFileProperty(), archiveFile.getAbsolutePath());
		}
	}
	@Override
	public void closing(Stage stage) {	}
	
	public static String archiveFileProperty() {
		return "ArchiveFile"; //$NON-NLS-1$
	}
	
}
