/**
 * 
 */
package test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.TreeCoreAnnotations.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.*;

/**
 * @author Jonny
 *
 */
public class Mainclass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("{Begin runtime.}");
		// Add your text here!
		String text = "Frank told me about the first thing he learned in publishing: \"I was a young boy, when my supervisor took me aside,\" he "
				+ "said. \"She told me to write fast and keep my mouth shut.\" "
				+ "Frank shrugged and his face darkened.";
		
		// texts go here:
		List<String> texts = new LinkedList<String>();

		// StanfordWebsiteExample.analyze(text);
		
		/*
		 * Load text from individual files
		 */
		File folder= new File("files/");
		File[] listOfFiles = folder.listFiles();

	    for (File item : listOfFiles) {
	      if (item.isFile()) {
	        /*
	         * actual file loading 
	         */
	    	  try {
				FileInputStream stream = new FileInputStream(item);
				BufferedInputStream bufstr = new BufferedInputStream(stream);
				bufstr.toString();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	    	  
	      } else if (item.isDirectory()) {
	        System.out.println("Skipped directory " + item.getName());
	      }
	    }
		
		
		/* 
		 * Run text through pipeline
		 */
		for (String item : texts) {
			StaticDynamicClassifier.analyze(item);
			System.out.println(text);
		}
		
		//StaticDynamicClassifier.analyze(text);
	    
	    System.out.println("{End of runtime.}");
	}

}
