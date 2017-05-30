package ca.ipredict.database;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import ca.ipredict.predictor.profile.Profile;

public class DatabaseHelper {

	/**
	 * Path to the datasets directory
	 */
	private String basePath;
	
	//Data sets
	public static enum Format{BMS, KOSARAK, FIFA, MSNBC, SIGN, CANADARM1, CANADARM2, SNAKE, BIBLE_CHAR, BIBLE_WORD, KORAN_WORD, LEVIATHAN_WORD, CUSTOM,
									QUEST200,
									QUEST400,
									QUEST600,
									QUEST800,
									QUEST600_05,
									QUEST600_01,
									QUEST600_15,
									QUEST600_20,
									QUEST600_25,
									QUEST600_30,
									QUEST5_1200,
									QUEST10_600,
									QUEST20_300,
									QUEST40_150};
	
	//Database
	private SequenceDatabase database;

	/**
	 * Main constructor, instantiate an empty database
	 */
	public DatabaseHelper(String basePath) {
		this.basePath = basePath;
		this.database = new SequenceDatabase();
	}
	
	/**
	 * Return an instance of the database
	 * @return
	 */
	public SequenceDatabase getDatabase() {
		return database;
	}
	
	
	public void loadDataset(String fileName, int maxCount) {
		
		//Clearing the database
		if(database == null) {
			database = new SequenceDatabase();
		}
		else {
			database.clear();
		}
		
		//Tries to guess the format if it is a predefined dataset
		try {
		
			Format datasetFormat = Format.valueOf(fileName);
			loadPredefinedDataset(datasetFormat, maxCount);
		
		} catch(IllegalArgumentException e) {
			loadCustomDataset(fileName, maxCount);
		}

		
		//Shuffling the database
		Collections.shuffle(database.getSequences());
	}
	
	private void loadCustomDataset(String fileName, int maxCount) {
		try {
			
			database.loadFileCustomFormat(fileToPath(fileName), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
			
		} catch (IOException e) {
			System.out.println("Could not load dataset, IOExeption");
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads a predefined dataset -- see full list in DatabaseHelper.Format
	 */
	private void loadPredefinedDataset(Format format, int maxCount) {
				
		//Loading the specified dataset (according to the format)
		try {
			switch(format) {
			case BMS:
				database.loadFileBMSFormat(fileToPath("BMS.dat"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case KOSARAK:
				database.loadFileCustomFormat(fileToPath("kosarak.dat"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case FIFA:
				database.loadFileFIFAFormat(fileToPath("FIFA_large.dat"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case MSNBC:
				database.loadFileMsnbsFormat(fileToPath("msnbc.seq"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case SIGN:
				database.loadFileSignLanguage(fileToPath("sign_language.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case CANADARM1:
				database.loadFileSPMFFormat(fileToPath("Canadarm1_actions.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case CANADARM2:
				database.loadFileSPMFFormat(fileToPath("Canadarm2_states.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case SNAKE:
				database.loadSnakeDataset(fileToPath("snake.dat"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case BIBLE_CHAR:
				database.loadFileLargeTextFormatAsCharacter(fileToPath("Bible.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case BIBLE_WORD:
				database.loadFileLargeTextFormatAsWords(fileToPath("Bible.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"), true);
				break;
			case KORAN_WORD:
				database.loadFileLargeTextFormatAsWords(fileToPath("koran.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"), false);
				break;
			case LEVIATHAN_WORD:
				database.loadFileLargeTextFormatAsWords(fileToPath("leviathan.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"), false);
				break;
			case QUEST200:
				database.loadFileSPMFFormat(fileToPath("spmf/data.ncust_200.tlen_1.nitems_1.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST400:
				database.loadFileSPMFFormat(fileToPath("spmf/data.ncust_400.tlen_1.nitems_1.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST600:
				database.loadFileSPMFFormat(fileToPath("spmf/data.ncust_600.tlen_1.nitems_1.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST800:
				database.loadFileSPMFFormat(fileToPath("spmf/data.ncust_800.tlen_1.nitems_1.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST600_05:
				database.loadFileSPMFFormat(fileToPath("spmf/var_sigma/data.slen_10.ncust_600.tlen_1.nitems_0.5.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST600_01:
				database.loadFileSPMFFormat(fileToPath("spmf/var_sigma/data.slen_10.ncust_600.tlen_1.nitems_1.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST600_15:
				database.loadFileSPMFFormat(fileToPath("spmf/var_sigma/data.slen_10.ncust_600.tlen_1.nitems_1.5.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST600_20:
				database.loadFileSPMFFormat(fileToPath("spmf/var_sigma/data.slen_10.ncust_600.tlen_1.nitems_2.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST600_25:
				database.loadFileSPMFFormat(fileToPath("spmf/var_sigma/data.slen_10.ncust_600.tlen_1.nitems_2.5.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST600_30:
				database.loadFileSPMFFormat(fileToPath("spmf/var_sigma/data.slen_10.ncust_600.tlen_1.nitems_3.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST5_1200:
				database.loadFileSPMFFormat(fileToPath("spmf/var_length/data.slen_5.ncust_1200.tlen_1.nitems_1.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST10_600:
				database.loadFileSPMFFormat(fileToPath("spmf/var_length/data.slen_10.ncust_600.tlen_1.nitems_1.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST20_300:
				database.loadFileSPMFFormat(fileToPath("spmf/var_length/data.slen_20.ncust_300.tlen_1.nitems_1.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			case QUEST40_150:
				database.loadFileSPMFFormat(fileToPath("spmf/var_length/data.slen_40.ncust_150.tlen_1.nitems_1.txt"), maxCount, Profile.paramInt("sequenceMinSize"), Profile.paramInt("sequenceMaxSize"));
				break;
			default:
				System.out.println("Could not load dataset, unknown format.");
			}
		
		} catch (IOException e) {
			System.out.println("Could not load dataset, IOExeption");
			e.printStackTrace();
		}
	}
	
	
	/** 
	 * Return the path for the specified data set file
	 * @param filename Name of the data set file
	 * @throws UnsupportedEncodingException 
	 */
	public String fileToPath(String filename) throws UnsupportedEncodingException {
		return basePath + File.separator + filename;
	}
	
}
