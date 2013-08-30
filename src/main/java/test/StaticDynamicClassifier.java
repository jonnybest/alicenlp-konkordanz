package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.relationship.AsymmetricRelationship;
import net.sf.extjwnl.data.relationship.Relationship;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import net.sf.extjwnl.dictionary.Dictionary;
 





import java.io.FileInputStream;
import java.io.FileNotFoundException;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.util.CoreMap;

public class StaticDynamicClassifier {
	static private StaticDynamicClassifier myinstance = null;
	static private StanfordCoreNLP mypipeline = null;

	public static void analyze(String text) {	    
	    /* parse text with corenlp
	     * 
	     */
		StanfordCoreNLP pipeline;
		if (mypipeline == null) {			
		    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
		    Properties props = new Properties();
		    // nimm das wsj-bidirectional model, weil das bessere ergebnisse liefert
		    props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
		    // konfiguriere pipeline
		    props.put("annotators", "tokenize, ssplit, pos, lemma");
		    pipeline = new StanfordCoreNLP(props);	    
		    mypipeline = pipeline;
		}
		else {
			pipeline = mypipeline;
		}
	    
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    System.out.println("{annotation is now done}");

	    // get all distinct verbs as a list
	    List<String> verblist = new ArrayList<String>();
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);	    
		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// TODO: using verbs is probably a bad idea: use verb phrases instead? Or better yet: predicate (root) only?
				if (pos.startsWith("VB")) {
					verblist.add(token.get(LemmaAnnotation.class));
				}
			}
		}
	
		System.out.println("{have verb list with " + verblist.size() + " entries}");
		System.out.println(verblist);
	    
	    /* do word net stuff
	     * 
	     */
	    // set up properties file
	    String propsFile = "src/main/java/test/file_properties.xml";
	    FileInputStream properties = null;
	    try {
	    	properties = new FileInputStream(propsFile);
	    } catch (FileNotFoundException e1) {
	    	e1.printStackTrace();
	    }
	    
	    // create a dictionary and run the analytics
	    try {
	    	
	    	// run
	    	StaticDynamicClassifier myclassifier; //.go();
			if (myinstance == null) {
				//new style, instance dictionary
				Dictionary dictionary = Dictionary.getInstance(properties);
				myclassifier = new StaticDynamicClassifier(dictionary);
				myinstance = myclassifier;
			}
			else {
				myclassifier = myinstance;
			}
	    	for (String token : verblist) 
	    	{
				myclassifier.analyzeLexicographerFileNamesForVerbs(token);
			}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(-1);
	    }      
	    /* analyse lexnames with wordnet
	     * 
	     */
	}
	
	private void analyzeLexicographerFileNamesForVerbs(String token) throws JWNLException {
		IndexWord word = dictionary.getIndexWord(POS.VERB, token);
		demonstrateLexicographerFileNames(word);		
	}

	private IndexWord ACCOMPLISH;
    private IndexWord DOG;
    private IndexWord CAT;
    private IndexWord FUNNY;
    private IndexWord DROLL;
    private final String MORPH_PHRASE = "running-away";
    private final Dictionary dictionary;

    public StaticDynamicClassifier(Dictionary dictionary) throws JWNLException {
        this.dictionary = dictionary;
        ACCOMPLISH = dictionary.getIndexWord(POS.VERB, "accomplish");
        DOG = dictionary.getIndexWord(POS.NOUN, "dog");
        CAT = dictionary.lookupIndexWord(POS.NOUN, "cat");
        FUNNY = dictionary.lookupIndexWord(POS.ADJECTIVE, "funny");
        DROLL = dictionary.lookupIndexWord(POS.ADJECTIVE, "droll");
    }

    public void go() throws JWNLException, CloneNotSupportedException {
    	demonstrateLexicographerFileNames(DOG);
        demonstrateMorphologicalAnalysis(MORPH_PHRASE);
        demonstrateListOperation(ACCOMPLISH);
        demonstrateTreeOperation(DOG);
        demonstrateAsymmetricRelationshipOperation(DOG, CAT);
        demonstrateSymmetricRelationshipOperation(FUNNY, DROLL);
    }

	private void demonstrateLexicographerFileNames(IndexWord word) {
		word.sortSenses();
		List<Synset> senses = word.getSenses();
		System.out.println("The word " + word + " is filed under the following lexicographer file names: ");
		for (Synset synset : senses) {
			if (senses.indexOf(synset) > 2) {
				break;
			}
			System.out.print(synset.getLexFileName() + "[" + synset.getLexFileNum() + "] (" + word.getSynsetOffsets());
		}
		System.out.println();
	}

	private void demonstrateMorphologicalAnalysis(String phrase) throws JWNLException {
        // "running-away" is kind of a hard case because it involves
        // two words that are joined by a hyphen, and one of the words
        // is not stemmed. So we have to both remove the hyphen and stem
        // "running" before we get to an entry that is in WordNet
        System.out.println("Base form for \"" + phrase + "\": " +
                dictionary.lookupIndexWord(POS.VERB, phrase));
    }

    private void demonstrateListOperation(IndexWord word) throws JWNLException {
        // Get all of the hypernyms (parents) of the first sense of <var>word</var>
        PointerTargetNodeList hypernyms = PointerUtils.getDirectHypernyms(word.getSenses().get(0));
        System.out.println("Direct hypernyms of \"" + word.getLemma() + "\":");
        hypernyms.print();
    }

    private void demonstrateTreeOperation(IndexWord word) throws JWNLException {
        // Get all the hyponyms (children) of the first sense of <var>word</var>
        PointerTargetTree hyponyms = PointerUtils.getHyponymTree(word.getSenses().get(0));
        System.out.println("Hyponyms of \"" + word.getLemma() + "\":");
        hyponyms.print();
    }

    private void demonstrateAsymmetricRelationshipOperation(IndexWord start, IndexWord end) throws JWNLException, CloneNotSupportedException {
        // Try to find a relationship between the first sense of <var>start</var> and the first sense of <var>end</var>
        RelationshipList list = RelationshipFinder.findRelationships(start.getSenses().get(0), end.getSenses().get(0), PointerType.HYPERNYM);
        System.out.println("Hypernym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
        for (Object aList : list) {
            ((Relationship) aList).getNodeList().print();
        }
        System.out.println("Common Parent Index: " + ((AsymmetricRelationship) list.get(0)).getCommonParentIndex());
        System.out.println("Depth: " + list.get(0).getDepth());
    }

    private void demonstrateSymmetricRelationshipOperation(IndexWord start, IndexWord end) throws JWNLException, CloneNotSupportedException {
        // find all synonyms that <var>start</var> and <var>end</var> have in common
        RelationshipList list = RelationshipFinder.findRelationships(start.getSenses().get(0), end.getSenses().get(0), PointerType.SIMILAR_TO);
        System.out.println("Synonym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
        for (Object aList : list) {
            ((Relationship) aList).getNodeList().print();
        }
        System.out.println("Depth: " + list.get(0).getDepth());
    }

}
