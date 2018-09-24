//package ca.ipredict.controllers;

import java.io.*;
import java.util.Arrays;





import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.*;
import java.util.Locale;
import java.util.Set;

/**
 * Reads two files and checks whether answers are identical or not.
 * If answers are not identical, then it checks if any of the answers are correct according to the consequent provided
 */
public class answerValidator { 

	public static void main(String[] args) throws IOException {

			List<Integer> answers, consequents, answersBWT, consequentsBWT;
			
			answersBWT = new ArrayList<Integer>();
			consequentsBWT = new ArrayList<Integer>();
			answers = new ArrayList<Integer>();
			consequents = new ArrayList<Integer>();

			// The name of the file to open.
	        String fileName = "answers.consequents.Fifa.txt";

	        // This will reference one line at a time
	        String line = null;

	        try {
	            // FileReader reads text files in the default encoding.
	            FileReader fileReader = 
	                new FileReader(fileName);

	            // Always wrap FileReader in BufferedReader.
	            BufferedReader bufferedReader = 
	                new BufferedReader(fileReader);

	            while((line = bufferedReader.readLine()) != null) {
	                //System.out.println(line);
	                String[] item = line.split(" ");
	                answers.add(Integer.parseInt(item[0]));
	                consequents.add(Integer.parseInt(item[1]));
	                // System.out.println(item[0] + " " + item[1]);
	            }   

	            // Always close files.
	            bufferedReader.close();         
	        }
	        catch(FileNotFoundException ex) {
	            System.out.println(
	                "Unable to open file '" + 
	                fileName + "'");                
	        }
	        catch(IOException ex) {
	            System.out.println(
	                "Error reading file '" 
	                + fileName + "'");                  
	            // Or we could just do this: 
	            // ex.printStackTrace();
	        }

	        fileName = "BWT.answers.consequents.Fifa.txt";

	        // This will reference one line at a time
	        line = null;

	        try {
	            // FileReader reads text files in the default encoding.
	            FileReader fileReader = 
	                new FileReader(fileName);

	            // Always wrap FileReader in BufferedReader.
	            BufferedReader bufferedReader = 
	                new BufferedReader(fileReader);

	            while((line = bufferedReader.readLine()) != null) {
	                //System.out.println(line);
	                String[] item = line.split(" ");
	                answersBWT.add(Integer.parseInt(item[0]));
	                //consequentsBWT.add(Integer.parseInt(item[1]));
	                //System.out.println(item[0] + " " + item[1]);
	            }   

	            // Always close files.
	            bufferedReader.close();         
	        }
	        catch(FileNotFoundException ex) {
	            System.out.println(
	                "Unable to open file '" + 
	                fileName + "'");                
	        }
	        catch(IOException ex) {
	            System.out.println(
	                "Error reading file '" 
	                + fileName + "'");                  
	            // Or we could just do this: 
	            // ex.printStackTrace();
	        }

	        if (answers.size() != answersBWT.size()){System.out.println("Answers are missing fron one or the other file"); return;}

	        int counter = 0;

	        for (int i = 0; i < answersBWT.size(); i++){
	        	if (answersBWT.get(i).compareTo(consequents.get(i)) == 0) counter++;
	        	else if (answersBWT.get(i).compareTo(answers.get(i)) == 0) counter++;
	        	else if (answers.get(i).compareTo(consequents.get(i)) != 0) counter++;
	        	if (answers.get(i).compareTo(consequents.get(i)) == 0 && answersBWT.get(i).compareTo(answers.get(i)) != 0) System.out.println("Index: " + i);
	        }

	        System.out.println("BWT approach validity: " + ((float)counter / answersBWT.size()));

	}

}
