package edu.kit.alicenlp.konkordanz;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.spi.DirObjectFactory;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.LexFileNameLexFileIdMap;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;

public class StaticDynamicClassifier implements IStanfordAnalyzer, INlpPrinter, IWordnetAnalyzer {
	static private StaticDynamicClassifier myinstance = null;
	private Dictionary dictionary;
	private StanfordCoreNLP mypipeline = null;
	
	private enum Classification {
		SetupDescription,
		ActionDescription,
		EventDescription,
		TimeDescription
	}
	
	private Classification classifySentence(IndexedWord root, SemanticGraph graph) throws JWNLException
	{
		String word = expandVerb(root, graph);
		IndexWord wnetw = dictionary.getIndexWord(POS.VERB, word);
		wnetw.sortSenses();
		List<Synset> senses = wnetw.getSenses();
		Synset mcs = senses.get(0); // most common sense
		switch ((int) (mcs.getLexFileNum())) {
		case 42: // stative
			// TODO: make sure this actually refers to a state; not a changing
			// state
			return Classification.SetupDescription;
			// break;
		case 39: // perception
			break;
		case 36: // creation
			break;
		default:
			break;
		}
		return Classification.ActionDescription;
	}

	public StaticDynamicClassifier() 
	{
		// this creates a wordnet dictionary
		setupWordNet();
		// this creates the corenlp pipeline
		setupCoreNLP();
		
		if (myinstance == null) {
			myinstance = this;
		}
	}

	public StaticDynamicClassifier(StanfordCoreNLP pipeline, Dictionary wordnet)
	{
		mypipeline = pipeline;
		dictionary = wordnet;
		if (myinstance == null) {
			myinstance = this;
		}
	}

	private Boolean analyzeLexicographerFileNamesForVerbs(String token) throws JWNLException {
		IndexWord word = dictionary.getIndexWord(POS.VERB, token);
		if (word == null) {
			word = dictionary.lookupIndexWord(POS.VERB, token);
			if (word == null) {
				// skip
				System.err.println("-- Cannot find word \"" + token + "\" in WordNet dictionary."); //$NON-NLS-1$ //$NON-NLS-2$
				System.err.println();
				return false;
			}
		}
		demonstrateLexicographerFileNames(word);
		return true;
	}

	protected Annotation annotate(String text) {
		// create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    mypipeline.annotate(document);
	    System.out.println("{annotation is now done}"); //$NON-NLS-1$
		return document;
	}

	private void demonstrateLexicographerFileNames(IndexWord word) {
		word.sortSenses();
		List<Synset> senses = word.getSenses();
		System.out.print("                                    to " + word.getLemma() + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		for (Synset synset : senses) {
			if (senses.indexOf(synset) > 2) {
				break;
			}
			System.out.print(synset.getLexFileName() + "(" + synset.getLexFileNum() + ") "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		System.out.println();
	}

	private IndexedWord getDeterminer(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nl)p.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DeterminerGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}

	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IWordnetAnalyzer#getDictionary()
	 */
	@Override
	public Dictionary getDictionary() {
		return dictionary;
	}
	
	private IndexedWord getDirectObject(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DirectObjectGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}
	
	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IStanfordAnalyzer#getPipeline()
	 */
	@Override
	public StanfordCoreNLP getPipeline() {
		return mypipeline;
	}

	private CoreLabel getPrepMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PrepositionalModifierGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}
	
	protected Boolean hasWordNetEntry(String verb) throws JWNLException {
		IndexWord word = dictionary.getIndexWord(POS.VERB, verb);
		if (word == null) {
			word = dictionary.lookupIndexWord(POS.VERB, verb);
			if (word == null || !word.getLemma().equals(verb)) {
				// skip
				//System.err.println("-- Cannot find word \"" + verb + "\" in WordNet dictionary."); //$NON-NLS-1$ //$NON-NLS-2$
				//System.err.println();
				return false;
			}
			else 
				return true;
		}
		else
			return true;
	}
	
	/** Finds whole word to multi-word verbs like phrasal verbs
	 * @param graph The sentence this word occurs in
	 * @param word The word to find parts for
	 * @return The whole verb (in base form) as it exists in WordNet 
	 * @throws JWNLException
	 */
	protected String expandVerb(IndexedWord word, SemanticGraph graph)
			throws JWNLException {
		String lemma = word.lemma();
		if (hasParticle(word, graph)) {
			String particle = null;
			particle = getParticle(word, graph).word();
			System.err.println(particle);
			String combinedword = lemma + " " + particle;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;							
			}
		}
		else if(hasPrepMod(word, graph)) {
			String prepmod = null;
			prepmod = getPrepMod(word, graph).word();
			System.err.println(prepmod);
			String combinedword = lemma + " " + prepmod;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;							
			}
		}
		else if(hasDirectObjectNP(word, graph)) {
			String dirobstr = null;
			IndexedWord direObj = null;
			direObj = getDirectObject(word, graph);
			CoreLabel det = getDeterminer(direObj, graph);
			if (det != null) {
				dirobstr = det.word() + " " + direObj.word();
			}
			else {
				dirobstr = direObj.word();
			}
			System.err.println(direObj);
			String combinedword = lemma + " " + dirobstr;
			if (hasWordNetEntry(combinedword)) {
				lemma = combinedword;
			}
		}
		return lemma;
	}

	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.INlpPrinter#print(java.lang.String)
	 */
	@Override
	public void print(String text) {		
	    /* parse text with corenlp
	     * 
	     */
	    
		// annotate text
	    Annotation document = annotate(text);

	    // get all distinct verbs as a list
	    SortedMap<String,SortedSet<String>> verblist = new TreeMap<String, SortedSet<String>>();
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);	    
		for (CoreMap sentence : sentences) {
			//printTaggedSentence(sentence); // debug output
			
			// traversing the words in the current sentence
			SemanticGraph graph = sentence.get(CollapsedDependenciesAnnotation.class);
				
			try {
				IndexedWord word = graph.getFirstRoot();
				String pos = word.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				if (!pos.startsWith("VB")) {
					continue;
				}
				System.out.println(graph.getRoots());
				if(word != null) {
					String lemma = expandVerb(word, graph);

					SortedSet<String> otherse = verblist.get(lemma);
					if (otherse == null) {
						otherse = new TreeSet<String>();
						verblist.put(lemma, otherse);
					}
					otherse.add(concordance(sentence.toString(), word.word()));						
					nop();				
				}
			} catch (RuntimeException e) { // because Stanford doesn't declare proper exceptions	
				if (e.getMessage() == null) {
					System.err.println("Unknown problem: " + e);
				}
				else if (!e.getMessage().contains("No roots in graph")) {
					e.printStackTrace();
					throw(e);	
				}
				else {
					if (sentence != null) {
						System.err.println(" --no root: "+ sentence.get(TextAnnotation.class));					
					}
				}
			} catch (JWNLException e) {
				// lookup went awry
				e.printStackTrace();
			}
		}
	
		System.out.println("{have verb list with " + verblist.size() + " entries}"); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(verblist.keySet());
		System.out.println();
	    
	    /* do word net stuff
	     * 
	     */

	    /* analyse lexnames with wordnet
	     * 
	     */
	    printLexnamesAndKwic(verblist);
	}

	/**
	 * @param verblist
	 */
	private void printLexnamesAndKwic(
			SortedMap<String, SortedSet<String>> verblist) {
		try {
			for (String token : verblist.keySet()) 
			{
				Boolean found = myinstance.analyzeLexicographerFileNamesForVerbs(token);
				if (found) {
					for (String item : verblist.get(token)) {
						System.out.println(item.toString());
					}
				}
				else {
					System.err.println(verblist.get(token).first());
				}
				System.out.println();
			}
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IWordnetAnalyzer#setDictionary(net.sf.extjwnl.dictionary.Dictionary)
	 */
	@Override
	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	/* (non-Javadoc)
	 * @see edu.kit.alicenlp.konkordanz.IStanfordAnalyzer#setPipeline(edu.stanford.nlp.pipeline.StanfordCoreNLP)
	 */
	@Override
	public void setPipeline(StanfordCoreNLP mypipeline) {
		this.mypipeline = mypipeline;
	} 

	/**
	 * 
	 */
	protected void setupCoreNLP() {
		StanfordCoreNLP pipeline;
		if (mypipeline == null) {			
		    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
		    Properties props = new Properties();
		    // alternativ: wsj-bidirectional 
		    try {
				props.put("pos.model", Settings.getString("settings.pos-model-tagger")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception e) {
				e.printStackTrace();
			}
		    // konfiguriere pipeline
		    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse"); //$NON-NLS-1$ //$NON-NLS-2$
		    pipeline = new StanfordCoreNLP(props);	    
		    mypipeline = pipeline;
		}
		else {
			pipeline = mypipeline;
		}
	}

	/**
	 * 
	 */
	protected void setupWordNet() {
		// set up properties file
	    String propsFile = Settings.getString("settings.wordnet-config-xml"); //$NON-NLS-1$
	    FileInputStream properties = null;
	    try {
	    	properties = new FileInputStream(propsFile);
	    } catch (FileNotFoundException e1) {
	    	e1.printStackTrace();
	    }
	    
	    // create a dictionary and run the analytics
	    try {
	    	
	    	// run
	    	if (dictionary == null) {
				//new style, instance dictionary
				dictionary = Dictionary.getInstance(properties);
			}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.exit(-1);
	    }
	}

	private static String concordance(String sentence, String word) {
		int lastindex = 100;
		int alignby = 39;
		String sestring = sentence;
		String wordstring = word;
		int indexof = sestring.indexOf(wordstring);
		if (indexof < alignby) {
			// implementiere den fall dass das wort zu weit links liegt
			// f�ge (alignby - indexof) leerzeichen links ein
			int offset = alignby - indexof;
			String aligner = ""; //$NON-NLS-1$
			for (int i = 0; i < offset; i++) {
				aligner += " "; //$NON-NLS-1$
			}
			sestring = aligner + sestring;
		}
		else if (alignby < indexof) {
			// implementiere den fall dass das wort zu weit rechts liegt
			int offset = indexof - alignby;
			sestring = sestring.substring(offset);
		}
		// zeichen hintenraus l�schen.
		if (sestring.length() > lastindex) {
			sestring = sestring.substring(0, lastindex);
		}
		return sestring;
	}

	/** Get the current instance
	 * @return the current Instance
	 */
	public static INlpPrinter getInstance() {
		return myinstance;
	}
	
	protected static Boolean is1stPerson(IndexedWord root, SemanticGraph graph)
	{
		
		return false;
	}
	
	protected static IndexedWord getParticle(IndexedWord word, SemanticGraph graph)
	{
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PhrasalVerbParticleGRAnnotation.class);
		return graph.getChildWithReln(word, reln);
	}
	
	private static boolean hasAdverbMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.AdverbialModifierGRAnnotation.class);
		return graph.hasChildWithReln(word, reln);
	}

	private static boolean hasDirectObjectNP(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.DirectObjectGRAnnotation.class);
		if (graph.hasChildWithReln(word, reln)) {
			String pos = graph.getChildWithReln(word, reln).get(PartOfSpeechAnnotation.class);
			if (pos.equalsIgnoreCase("NN")) {
				return true;
			}
		}
		return false;
	}

	protected static Boolean hasParticle(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PhrasalVerbParticleGRAnnotation.class);
		return graph.hasChildWithReln(word, reln);
	}
	
    private static boolean hasPrepMod(IndexedWord word, SemanticGraph graph) {
		GrammaticalRelation reln = edu.stanford.nlp.trees.GrammaticalRelation.getRelation(edu.stanford.nlp.trees.EnglishGrammaticalRelations.PrepositionalModifierGRAnnotation.class);
		return graph.hasChildWithReln(word, reln);
	}
    
    private static void nop() {
		// nop		
	}

	private static void printTaggedSentence(CoreMap sentence) {
		for (CoreLabel item : sentence.get(TokensAnnotation.class)) {
			System.out.print(item.originalText() + "/"+ item.get(PartOfSpeechAnnotation.class) + " "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		System.out.println();
	}
	
	/** Set the instance for this class
	 * @param myinstance the myinstance to set
	 */
	public static void setInstance(StaticDynamicClassifier myinstance) {
		StaticDynamicClassifier.myinstance = myinstance;
	}
	

}
