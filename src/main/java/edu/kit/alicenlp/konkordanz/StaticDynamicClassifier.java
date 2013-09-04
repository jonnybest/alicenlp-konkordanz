package edu.kit.alicenlp.konkordanz;

import java.util.*;

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
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.*;
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
		    // alternativ: wsj-bidirectional 
		    //props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger");
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
	    SortedMap<String,SortedSet<String>> verblist = new TreeMap<String, SortedSet<String>>();
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);	    
		for (CoreMap sentence : sentences) {
			printTaggedSentence(sentence);
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// TODO: using verbs is probably a bad idea: use verb phrases instead? Or better yet: predicate (root) only? No. Root is a bad idea.
				if (pos.startsWith("VB")) {
					String word = token.lemma();
					if(word != null) {
						SortedSet<String> otherse = verblist.get(word);
						if (otherse == null) {
							otherse = new TreeSet<String>();
							verblist.put(word, otherse);
						}
						otherse.add(concordance(sentence, token));						
						nop();
					}
				}
			}
		}
	
		System.out.println("{have verb list with " + verblist.size() + " entries}");
		System.out.println(verblist.keySet());
		System.out.println();
	    
	    /* do word net stuff
	     * 
	     */
	    // set up properties file
	    String propsFile = "src/main/java/edu.kit.alicenlp.konkordanz/file_properties.xml";
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
	    	for (String token : verblist.keySet()) 
	    	{
				Boolean found = myclassifier.analyzeLexicographerFileNamesForVerbs(token);
				if (found) {
					for (String item : verblist.get(token)) {
						System.out.println(item.toString());
					}
				}
				System.out.println();
			}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(-1);
	    }      
	    /* analyse lexnames with wordnet
	     * 
	     */
	}
	
	private static void nop() {
		// nop		
	}

	private static void printTaggedSentence(CoreMap sentence) {
		for (CoreLabel item : sentence.get(TokensAnnotation.class)) {
			System.out.print(item.originalText() + "/"+ item.get(PartOfSpeechAnnotation.class) + " ");
		}
		System.out.println();
	}

	private static String concordance(CoreMap sentence, CoreLabel word) {
		int lastindex = 100;
		int alignby = 39;
		String sestring = sentence.toString();
		String wordstring = word.toString();
		int indexof = sestring.indexOf(wordstring);
		if (indexof < alignby) {
			// TODO: implementiere den fall dass das wort zu weit links liegt
			// füge (alignby - indexof) leerzeichen links ein
			int offset = alignby - indexof;
			String aligner = "";
			for (int i = 0; i < offset; i++) {
				aligner += " ";
			}
			sestring = aligner + sestring;
		}
		else if (alignby < indexof) {
			// TODO: implementiere den fall dass das wort zu weit rechts liegt
			int offset = indexof - alignby;
			sestring = sestring.substring(offset);
		}
		// TODO: zeichen hintenraus löschen.
		if (sestring.length() > lastindex) {
			sestring = sestring.substring(0, lastindex);
		}
		return sestring;
	}

	private Boolean analyzeLexicographerFileNamesForVerbs(String token) throws JWNLException {
		IndexWord word = dictionary.getIndexWord(POS.VERB, token);
		if (word == null) {
			word = dictionary.lookupIndexWord(POS.VERB, token);
			if (word == null) {
				// skip
				System.err.println("-- Cannot find word \"" + token + "\" in WordNet dictionary.");
				System.err.println();
				return false;
			}
		}
		demonstrateLexicographerFileNames(word);
		return true;
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
		System.out.print("                                    to " + word.getLemma() + ": ");
		for (Synset synset : senses) {
			if (senses.indexOf(synset) > 2) {
				break;
			}
			System.out.print(synset.getLexFileName() + "(" + synset.getLexFileNum() + ") ");
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
