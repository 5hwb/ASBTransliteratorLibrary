package asb.mappings;

import java.util.HashMap;
import java.util.Map;

import asb.schema.PhonemeCounter;
import asb.schema.PhonemeType;

/**
 * Singleton class for storing mappings.
 * @author perry
 *
 */
public class Mappings {

	// Singleton instance of Mappings
	private static Mappings instance;
	
	/** Consonant counters */
	protected Map<String, PhonemeCounter> consoTypeToCounterMap;

	/** Reference hashmap for phoneme types */
	protected Map<String, PhonemeType> phonemeTypeReferenceMap;

	private Mappings() {
		this.phonemeTypeReferenceMap = new HashMap<String, PhonemeType>();
		this.consoTypeToCounterMap = new HashMap<String, PhonemeCounter>();
	}
	
	/**
	 * Get the Mappings instance.
	 * @return The Mappings instance
	 */
	public static Mappings getInstance() {
		if (instance == null) {
			instance = new Mappings();
		}
		return instance;
	}
	
	//////////////////////////////////////////////////
	// GETTERS
	//////////////////////////////////////////////////

	public static Map<String, PhonemeCounter> getConsoTypeToCounterMap() {
		return getInstance().consoTypeToCounterMap;
	}
	
	public static Map<String, PhonemeType> getPhonemeTypeReferenceMap() {
		return getInstance().phonemeTypeReferenceMap;
	}
	
}
