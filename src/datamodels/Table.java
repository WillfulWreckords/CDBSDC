package datamodels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wwrkds.CDBabySalesDataCrawler;

public class Table extends ArrayList<Row> {

	public static synchronized <U, T> Collection<Map<String, String>> sum(
			Map<String, Collection<Map<U, T>>> groupedRows, U col) {
		Map<String, String> res = new HashMap<String, String>();
		for (Entry<String, Collection<Map<U, T>>> entry : groupedRows
				.entrySet()) {
			String group = entry.getKey();
			Collection<Map<U, T>> rows = entry.getValue();
			String value = CDBabySalesDataCrawler.sum(rows, col);

			if (res.containsKey(group)) {
				try {
					double old = Double.parseDouble(res.get(group));
					double val = Double.parseDouble(value);
					res.put(group, old + val + "");
				} catch (Exception e) {
					if (!res.get(group).trim().contains(value)) {
						String n = res.get(group).trim() + " " + value;
						res.put(group, n.trim());
					}
				}
			} else {
				res.put(group, value.trim());
			}
		}

		List<Map<String, String>> out = new ArrayList<Map<String, String>>();
		out.add(res);
		return out;
	}

	private String name = "";

	public Table() {
		super();
	}

	public Table(Collection<Row> rows) {
		super();
		this.addAll(rows);
	}

	public Table(Row... rows) {
		super();
		for (Row row : rows) {
			this.add(row);
		}
	}

	/**
	 * Perform the computation on the elements of the given column if they can
	 * be converted to a number, ignored otherwise
	 * 
	 * @param col
	 * @return
	 */
	public double avg(String col) {
		double output = 0;
		double cnt = 0;

		for (Row row : this) {
			if (row.containsKey(col)) {
				try {
					double val = row.getDouble(col);
					output += val;
					cnt++;
				} catch (Exception e) {

				}
			}
		}
		return output / cnt;
	}

	/**
	 * Filter the table using the supplied javaScript filter string
	 * 
	 * @param filter
	 * @return
	 */
	public Table filter(String filter) {
		Table ret = new Table();
		Iterator<Row> it = this.iterator();
		while (it.hasNext()) {
			Row row = it.next();
			if (row.satisfies(filter)) {
				ret.add(row);
			}
		}
		return ret;
	}

	/**
	 * Gets a new table with only the specified columns
	 * 
	 * @param cols
	 * @return
	 */
	public Table get(String... cols) {
		Table ret = new Table();
		for (Row row : this) {
			ret.add(row.get(cols));
		}
		return ret;
	}

	/**
	 * Gets the set of unique column labels contained in this Table
	 * 
	 * @return
	 */
	public Set<String> getColLabels() {
		Set<String> ret = new HashSet<String>();
		for (Row row : this) {
			for (String key : row.keySet()) {
				ret.add(key);
			}
		}
		return ret;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Group by unique entries in the given columns
	 * 
	 * @param cols
	 * @return
	 */
	public Map<String, Table> groupBy(String... cols) {
		Map<String, Table> groups = new LinkedHashMap<String, Table>();
		for (Row row : this) {
			String group = "";
			for (String key : cols) {
				if (row.get(key) != null) {
					group += row.get(key) + "";
				}
			}
			if (!groups.containsKey(group)) {
				groups.put(group, new Table());
			}
			groups.get(group).add(row);
		}
		return groups;
	}

	/**
	 * Compute the max over a given column
	 * 
	 * @param col
	 * @return
	 */
	public double max(String col) {

		double output = Double.NEGATIVE_INFINITY;

		for (Row row : this) {
			try {
				double val = row.getDouble(col);
				output = output < val ? val : output;
				output += val;
			} catch (Exception e) {

			}
		}
		return output;
	}

	/**
	 * Compute the min over a given column
	 * 
	 * @param col
	 * @return
	 */
	public double min(String col) {

		double output = Double.POSITIVE_INFINITY;

		for (Row row : this) {
			try {
				double val = row.getDouble(col);
				output = output > val ? val : output;
				output += val;
			} catch (Exception e) {

			}
		}
		return output;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sort the Table entries by the specified columns
	 * 
	 * @param rankingOrder
	 * @param cols
	 * @return
	 */
	public Table sortBy(RankingOrder rankingOrder, String... cols) {
		ArrayList<String> sortStrings = new ArrayList<String>();
		ArrayList<Row> rows = new ArrayList<Row>();
		for (Row row : this) {
			rows.add(row);
			String str = "";
			for (String col : cols) {
				if (row.containsKey(col)) {
					str += " " + row.get(col);
				}
			}
			str = str.trim();
			sortStrings.add(str);
		}
		Sort.BubbleSort(sortStrings, rows, rankingOrder);

		return new Table(rows);
	}

	/**
	 * Sort the Table by the specified columns
	 * 
	 * @param cols
	 * @return
	 */
	public Table sortBy(String... cols) {
		ArrayList<String> sortStrings = new ArrayList<String>();
		ArrayList<Row> rows = new ArrayList<Row>();
		for (Row row : this) {
			rows.add(row);
			String str = "";
			for (String col : cols) {
				if (row.containsKey(col)) {
					str += " " + row.get(col);
				}
			}
			str = str.trim();
			sortStrings.add(str);
		}
		Sort.BubbleSort(sortStrings, rows, RankingOrder.ASCENDING);

		return new Table(rows);
	}

	/**
	 * Perform the computation on the elements of the given column if they can
	 * be converted to a number, ignored otherwise
	 * 
	 * @param col
	 * @return
	 */
	public double sum(String col) {

		double output = 0;

		for (Row row : this) {
			try {
				double val = row.getDouble(col);
				output += val;
			} catch (Exception e) {

			}
		}
		return output;
	}

	/**
	 * Convert to an XML/HTML table element
	 * 
	 * @return
	 */
	public String toHtml() {
		String str = "<table border=1>\n";

		str += "  <thead>";
		str += "    <tr>\n";
		str += "      <th></th>\n";
		Set<String> cols = this.getColLabels();
		for (String col : cols) {
			str += "      <th>" + col + "</th>\n";
		}
		str += "    </tr>\n";
		str += "  </thead>";

		str += "  <tbody>\n";
		int count = 1;
		for (Row row : this) {
			str += "    <tr>\n";

			str += "      <td>";
			String lbls = count++ + "";
			for (String label : row.getLabels()) {
				lbls += " " + label;
			}
			str += lbls.trim() + "</td>\n";

			for (String col : cols) {
				String c = row.get(col);
				if (c != null) {
					str += "      <td>" + c + "</td>\n";
				} else {
					str += "      <td></td>\n";
				}
			}

			str += "    </tr>\n";
		}
		str += "  </tbody>\n";
		str += "</table>\n";

		return str;
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
}
