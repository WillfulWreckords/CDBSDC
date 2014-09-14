package datamodels;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author jonlareau
 * 
 */
public class Row extends LinkedHashMap<String, String> {
	private final Set<String> labels = new LinkedHashSet<String>();

	public Row() {
		super();
	}

	/**
	 * Constructor will convert all inputs to Strings using their toString()
	 * method.
	 * 
	 * @param data
	 */
	@SafeVarargs
	public <B, C> Row(Entry<B, C>... data) {
		super();
		for (Entry<B, C> entry : data) {
			this.put(entry.getKey().toString(), entry.getValue().toString());
		}
	}

	/**
	 * Constructor will convert all inputs to Strings using their toString()
	 * method.
	 * 
	 * @param data
	 */
	public <B, C> Row(Map<B, C> data) {
		super();
		for (Entry<B, C> entry : data.entrySet()) {
			this.put(entry.getKey().toString(), entry.getValue().toString());
		}
	}

	/**
	 * Constructor will convert all inputs to Strings using their toString()
	 * method.
	 * 
	 * @param rowLabels
	 * @param data
	 */
	@SafeVarargs
	public <A, B, C> Row(Set<A> rowLabels, Entry<B, C>... data) {
		super();
		for (Entry<B, C> entry : data) {
			this.put(entry.getKey().toString(), entry.getValue().toString());
		}
		for (A a : rowLabels) {
			this.labels.add(a.toString());
		}
	}

	/**
	 * Constructor will convert all inputs to Strings using their toString()
	 * method.
	 * 
	 * @param rowLabels
	 * @param data
	 */
	public <A, B, C> Row(Set<A> rowLabels, Map<B, C> data) {
		super();
		for (Entry<B, C> entry : data.entrySet()) {
			this.put(entry.getKey().toString(), entry.getValue().toString());
		}
		for (A a : rowLabels) {
			this.labels.add(a.toString());
		}
	}

	/**
	 * Create a new row containing only those columns specified (if they exist)
	 * 
	 * @param cols
	 * @return
	 */
	public Row get(String... cols) {
		Row ret = new Row();
		for (String col : cols) {
			if (this.containsKey(col)) {
				ret.put(col, this.get(col));
			}
		}
		return ret;
	}

	/**
	 * Get the parsed + converted value of the given column. returns 0 if column
	 * does not exist or is empty. Will remove any leading '$' symbols from
	 * currency Strings.
	 * 
	 * @param col
	 * @return
	 */
	public double getDouble(String col) {
		String val = this.get(col);
		if (val == null || val.trim().isEmpty()) {
			return 0;
		}
		val = val.trim();
		if (val.matches("-?$?\\d*\\.?\\d+")) {
			val = val.replace("$", "");
			return Double.parseDouble(this.get(col));
		} else {
			return 0;
		}
	}

	public Set<String> getLabels() {
		return this.labels;
	}

	/**
	 * Does this Row satisfy the given JavaScript filter string? Operates by
	 * first replacing any instances of column names prepended with "$" in
	 * filter with their corresponding values, then evaluating the javascript
	 * string.
	 * 
	 * @param filter
	 * @return
	 */
	public boolean satisfies(String filter) {
		try {
			for (Entry<String, String> entry : this.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				filter = filter.replaceAll("$" + key + "", value + "");
			}
			// create a script engine manager
			ScriptEngineManager factory = new ScriptEngineManager();
			// create a JavaScript engine
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			// evaluate JavaScript code from String
			Object obj = engine.eval(filter);
			return (boolean) obj;
		} catch (ScriptException e) {
			return false;
		}
	}
}
