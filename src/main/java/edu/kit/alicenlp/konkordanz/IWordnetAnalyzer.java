package edu.kit.alicenlp.konkordanz;

import net.sf.extjwnl.dictionary.Dictionary;

public interface IWordnetAnalyzer {

	/** Get this instances' WordNet dictionary
	 * @return the dictionary
	 */
	public abstract Dictionary getDictionary();

	/** Set this instances' WordNet dictionary
	 * @param dictionary the dictionary to set
	 */
	public abstract void setDictionary(Dictionary dictionary);

}