package edu.kit.alicenlp.konkordanz;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public interface IStanfordAnalyzer {

	/** Get this instances' Stanford CoreNLP pipeline
	 * @return the mypipeline
	 */
	public abstract StanfordCoreNLP getPipeline();

	/** Set this instances' Stanford CoreNLP pipeline
	 * @param mypipeline the mypipeline to set
	 */
	public abstract void setPipeline(StanfordCoreNLP mypipeline);

}