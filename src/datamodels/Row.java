package datamodels;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author jonlareau
 * 
 */
public class Row implements Map<String, String> {
	private final LinkedHashMap<String, String> elements = new LinkedHashMap<String, String>();

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

	@Override
	public void clear() {
		this.elements.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.elements.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.elements.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return this.elements.entrySet();
	}

	@Override
	public String get(Object key) {
		return this.elements.get(key);
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
		val = val.replace("$", "");
		try {
			return Double.parseDouble(val);
		} catch (Exception e) {
			return 0;
		}
	}

	public boolean isDouble(String col) {
		String val = this.get(col);
		if (val == null || val.trim().isEmpty()) {
			return false;
		}
		val = val.trim();
		val = val.replace("$", "");
		try {
			Double.parseDouble(val);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean isEmpty() {
		return this.elements.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return this.elements.keySet();
	}

	@Override
	public String put(String key, String value) {
		return this.elements.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		this.elements.putAll(m);
	}

	@Override
	public String remove(Object key) {
		return this.elements.remove(key);
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

	@Override
	public int size() {
		return this.elements.size();
	}

	/**
	 * Convert to a JSON string
	 * 
	 * @return
	 */
	public String toJson() {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues()
				.serializeNulls().create();
		return gson.toJson(this);
	}

	@Override
	public Collection<String> values() {
		return this.elements.values();
	}
}
