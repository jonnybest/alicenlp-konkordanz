package test;

import java.util.Properties;

import edu.stanford.nlp.pipeline.*;

public class StaticDynamicClassifier {

	public static void analyze(String text) {
		
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    // nimm das wsj-bidirectional model, weil das bessere ergebnisse liefert
	    props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
	    // konfiguriere pipeline
	    props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);	    
	    
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
		
	}

}
