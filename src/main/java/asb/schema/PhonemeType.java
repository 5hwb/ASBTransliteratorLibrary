package asb.schema;

public class PhonemeType {

	private String name;
	private String[] extraTypes;
	
	public PhonemeType(String name) {
		this.name = name;
	}

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
