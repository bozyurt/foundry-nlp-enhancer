package edu.uth.clamp.nlp.configurable.param;




public class StringParam extends Param {

	public StringParam(String name, String value, String defaultValue,
			String description) {
		super(name, value, defaultValue, description);
		// TODO Auto-generated constructor stub
	}

	public String value() {
		return getValueStr();
	}
}
