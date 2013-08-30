/**
 * 
 */
package test;

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

		// StanfordWebsiteExample.analyze(text);
		
		StaticDynamicClassifier.analyze(text);
	    
	    System.out.println("{End of runtime.}");
	}

}
