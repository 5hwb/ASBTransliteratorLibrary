package asb.schema;

/**
 * Contains phoneme types and their 'extra types' (more specific subclasses of the main type).
 * @author perry
 *
 */
public class PhonemeType {

	private String name;         // Main phoneme type name
	private String[] extraTypes; // Names of extra types (subclasses of the main type)
	
	public PhonemeType(String name) {
		this.name = name;
	}
	
	//////////////////////////////////////////////////
	// GETTER AND SETTER METHODS
	//////////////////////////////////////////////////

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] extraTypes() {
		return extraTypes;
	}

	public void setExtraTypes(String[] extraTypes) {
		this.extraTypes = extraTypes;
	}
}
