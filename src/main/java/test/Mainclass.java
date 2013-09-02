/**
 * 
 */
package test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.TreeCoreAnnotations.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.util.logging.NewlineLogFormatter;

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
		
		// texts go here:
		List<String> texts = new LinkedList<String>();

		// StanfordWebsiteExample.analyze(text);
		
		/*
		 * Load text from individual files
		 */
		File folder= new File("files/");
		File[] listOfFiles = folder.listFiles();
		CharBuffer buffer = CharBuffer.allocate(1024);

	    for (File item : listOfFiles) {	    	
	      if (item.isFile()) {
	    	  System.out.println("{Reading " + item.getName() + "}");
	        /*
	         * actual file loading 
	         */	    	  
	    	  StringBuffer strbuffer = new StringBuffer();
	    	  buffer.clear();
	    	  try {
				FileReader stream = new FileReader(item);
				int readbytes = 0;
				do {
					readbytes = stream.read(buffer);
					if (readbytes < 0) {
						break;
					}
					strbuffer.append(buffer.array(), 0, readbytes);
					buffer.clear();
				}
	    	  	while (readbytes > 0);
				stream.close();
				texts.add(strbuffer.toString());
				//System.out.println("{read: " +strbuffer.toString() + " }");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	    	  
	      } else if (item.isDirectory()) {
	        System.out.println("Skipped directory " + item.getName());
	      }
	    }
		
		
		/* 
		 * Run text through pipeline
		 */
	    StringBuffer alltexts = new StringBuffer();
	    for (String item : texts) {
			alltexts.append(item);
			alltexts.append(" ");
		}
	    StaticDynamicClassifier.analyze(alltexts.toString());
	    
	    /* old text-by text way
		for (String item : texts) {
			StaticDynamicClassifier.analyze(item);
			//System.out.println(item);
		}
		*/
		
		//StaticDynamicClassifier.analyze(text);
	    
	    System.out.println("{End of runtime.}");
	}

}
