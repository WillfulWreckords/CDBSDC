package com.wwrkds.datamodels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.LegendPosition;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.PieChart;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.Slice;
import com.googlecode.charts4j.XYLine;

public class Table implements List<Row> {

	public static synchronized <U, T> String sum(Collection<Map<U, T>> rows,
			U col) {

		String output = "";

		// Print rows
		for (Map<U, T> map : rows) {
			String value = map.get(col) + "";
			try {
				double old = output.isEmpty() ? 0.0 : Double
						.parseDouble(output);
				double val = Double.parseDouble(value.replace("$", "").trim());
				output = old + val + "";
			} catch (Exception e) {
				String n = output.trim() + " " + value;
				output = n.trim();

			}
		}
		return output;
	}

	public static synchronized <U, T> Collection<Map<String, String>> sum(
			Map<String, Collection<Map<U, T>>> groupedRows, U col) {
		Map<String, String> res = new HashMap<String, String>();
		for (Entry<String, Collection<Map<U, T>>> entry : groupedRows
				.entrySet()) {
			String group = entry.getKey();
			Collection<Map<U, T>> rows = entry.getValue();
			String value = Table.sum(rows, col);

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

	public static Map<String, Double> sum(Map<String, Table> in, String col) {
		Map<String, Double> ret = new HashMap<String, Double>();
		for (Entry<String, Table> entry : in.entrySet()) {
			ret.put(entry.getKey(), entry.getValue().sum(col));
		}
		return ret;
	}

	public static Table toTable(Collection<Map<String, String>> rows) {
		Table ret = new Table();
		for (Map<String, String> row : rows) {
			ret.add(new Row(row));
		}
		return ret;
	}

	private final Map<String, String> attributes = new HashMap<String, String>();

	private transient Color[] colors = { Color.PURPLE, Color.ORANGE, Color.RED,
			Color.YELLOW, Color.GREEN, Color.BLUE, Color.INDIGO, Color.VIOLET,
			Color.TEAL, Color.MAGENTA };

	private transient int height = 300, width = 800;

	private final ArrayList<Row> rows = new ArrayList<Row>();

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

	@Override
	public void add(int index, Row element) {
		this.rows.add(index, element);
	}

	@Override
	public boolean add(Row e) {
		return this.rows.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends Row> c) {
		return this.rows.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Row> c) {
		return this.rows.addAll(index, c);
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

	@Override
	public void clear() {
		this.rows.clear();
	}

	@Override
	public boolean contains(Object o) {
		return this.rows.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.rows.containsAll(c);
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

	@Override
	public Row get(int index) {
		return this.rows.get(index);
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

	public String getAccumXYPlotTable(String X, String Y, String... groupBy) {
		String ret = "";
		Table table = this;

		try {
			Table nt = new Table();
			nt.attributes.put("border", "1");
			Map<String, Table> series = table.groupBy(groupBy);
			for (Entry<String, Table> entry : series.entrySet()) {
				Table stable = entry.getValue();
				String key = entry.getKey();
				double total = stable.sum(Y);
				Row row = new Row();
				row.put("Total", String.format("%.4f", total));
				row.put("Group", key);
				nt.add(row);
			}
			return nt.toHtmlTableString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return ret;
		}
	}

	/**
	 * Gets a Google url for plotting the accumulated totals of y vs x
	 * stratified by the provided groups
	 * 
	 * @param X
	 * @param Y
	 * @param groupBy
	 * @return
	 */
	public String getAccumXYPlotUrl(String X, String Y, String... groupBy) {
		String ret = "";
		ArrayList<XYLine> lines = new ArrayList<XYLine>();
		Table table = this;

		double ymax = this.max(Y);
		double ymin = this.min(Y);
		double xmax = this.max(X);
		double xmin = this.min(X);

		Table sumTable = table.sumBy(X, Y);
		ymax = sumTable.max(Y);
		ymin = sumTable.min(Y);

		xmin = xmin - 0.01 * xmin;
		xmax = xmax + 0.01 * xmax;

		try {

			// Accumulated totals...
			Map<Double, Double> xy = new HashMap<Double, Double>();
			for (Row row : table) {
				double a = row.getDouble(X);
				a = (a - xmin) / (xmax - xmin) * 100;
				xy.put(a, 0.0);
			}

			Map<String, Table> series = table.groupBy(groupBy);

			int i = 0;
			double stotal = 0;
			for (Entry<String, Table> entry : series.entrySet()) {
				Table stable = entry.getValue();

				// Accumulate counts in each X bin
				stable = stable.sumBy(X, Y);

				double total = 0;
				for (Row row : stable) {
					double a = row.getDouble(X);
					double b = row.getDouble(Y);
					total += b;
					stotal += b;
					a = (a - xmin) / (xmax - xmin) * 100;
					b = (b - ymin) / (ymax - ymin) * 100;
					if (xy.containsKey(a)) {
						xy.put(a, xy.get(a) + b);
					} else {
						xy.put(a, b);
					}
				}

				ArrayList<Double> x = new ArrayList<Double>();
				ArrayList<Double> y = new ArrayList<Double>();
				for (Entry<Double, Double> xyentry : xy.entrySet()) {
					x.add(xyentry.getKey());
					y.add(xyentry.getValue());
				}

				XYLine line = Plots.newXYLine(Data.newData(x), Data.newData(y));
				line.setColor(this.colors[i % this.colors.length]);
				line.setFillAreaColor(this.colors[i % this.colors.length]);
				i++;
				line.setLegend(entry.getKey());
				lines.add(line);
			}
			Collections.reverse(lines);

			LineChart chart = GCharts.newLineChart(lines);
			chart.setSize(this.width, this.height);
			// chart.setTitle(stotal + "", Color.BLACK, 16);
			chart.setLegendPosition(LegendPosition.BOTTOM);

			// Defining axis info and styles
			// AxisStyle axisStyle = AxisStyle.newAxisStyle(Color.WHITE, 12,
			// AxisTextAlignment.CENTER);
			AxisLabels yAxis = AxisLabelsFactory.newNumericRangeAxisLabels(
					ymin, ymax);
			// yAxis.setAxisStyle(axisStyle);
			// AxisLabels xAxis1 =
			// AxisLabelsFactory.newAxisLabels(Arrays.asList("Fed Chiefs:","Burns","Volcker","Greenspan","Bernanke"),
			// Arrays.asList(5,18,39,55,92));
			// xAxis1.setAxisStyle(axisStyle);
			TimeStamp start = new TimeStamp(TimeStamp.ms1970ToJDN((long) xmin));
			TimeStamp end = new TimeStamp(TimeStamp.ms1970ToJDN((long) xmax));
			double nd = (end.getJulianDateNumber() - start
					.getJulianDateNumber()) / 10;
			ArrayList<String> labels = new ArrayList<String>();
			for (double jdn = start.getJulianDateNumber(); jdn < end
					.getJulianDateNumber(); jdn += nd) {
				labels.add(TimeStamp.getDateString(jdn));
			}
			labels.add(end.getDateString());

			AxisLabels xAxis = AxisLabelsFactory.newAxisLabels(labels);
			// xAxis2.setAxisStyle(axisStyle);
			// AxisLabels xAxis3 = AxisLabelsFactory.newAxisLabels("Year",
			// 50.0);
			// xAxis3.setAxisStyle(AxisStyle.newAxisStyle(WHITE, 14,
			// AxisTextAlignment.CENTER));

			// Adding axis info to chart.
			chart.addYAxisLabels(yAxis);
			chart.addXAxisLabels(xAxis);
			// chart.addXAxisLabels(xAxis2);
			// chart.addXAxisLabels(xAxis3);
			chart.setGrid(10, 10, 5, 0);

			// Defining background and chart fills.
			// chart.setBackgroundFill(Fills.newSolidFill(BLACK));
			// chart.setAreaFill(Fills.newSolidFill(Color.newColor("708090")));

			String url = chart.toURLString();

			return url;
		} catch (Exception ex) {
			ex.printStackTrace();
			return ret;
		}
	}

	public Map<String, String> getAttributes() {
		return this.attributes;
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

	public Color[] getColors() {
		return this.colors;
	}

	public int getHeight() {
		return this.height;
	}

	public String getPiePlotTable(String X, String Y) {

		Map<String, Double> res = new HashMap<String, Double>();
		double total = 0;
		for (Row row : this) {
			String Xval = row.get(X);
			double v = 1.0;
			if (row.isDouble(Y)) {
				v = row.getDouble(Y);
			}

			total += v;
			if (res.containsKey(Xval)) {
				res.put(Xval, v + res.get(Xval));
			} else {
				res.put(Xval, v);
			}
		}
		Table nt = new Table();
		nt.attributes.put("border", "1");
		for (Entry<String, Double> entry : res.entrySet()) {
			String key = entry.getKey();
			String value = String.format("%.4f", entry.getValue());
			Row row = new Row();
			row.put("Total", value);
			row.put("Group", key);
			nt.add(row);
		}
		return nt.toHtmlTableString();
	}

	/**
	 * Create a Google charts url for a pie graph of the distribution of unique
	 * entries in the given column
	 * 
	 * @param X
	 * @return
	 */
	public String getPiePlotUrl(String X, String Y) {
		Map<String, Double> res = new HashMap<String, Double>();
		double total = 0;
		for (Row row : this) {
			String Xval = row.get(X);
			double v = 1.0;
			if (row.isDouble(Y)) {
				v = row.getDouble(Y);
			}

			total += v;
			if (res.containsKey(Xval)) {
				res.put(Xval, v + res.get(Xval));
			} else {
				res.put(Xval, v);
			}
		}
		ArrayList<Slice> slices = new ArrayList<Slice>();

		int si = 0;
		for (Entry<String, Double> entry : res.entrySet()) {
			String str = entry.getKey();
			double x = entry.getValue();
			int v = (int) (x / total * 100);
			Color c = this.colors[si++ % this.colors.length];
			Slice s = Slice.newSlice(v, c, str);
			slices.add(s);
		}

		PieChart chart = GCharts.newPieChart(slices);
		// chart.setTitle(X + " " + total, Color.BLACK, 16);
		chart.setSize(this.width, this.height);
		chart.setThreeD(true);
		chart.setLegendPosition(LegendPosition.BOTTOM);
		String url = chart.toURLString();
		return url;
	}

	public String getPlotTable(String... args) {
		int type = 0;
		ArrayList<String> args2 = new ArrayList<String>();
		for (String arg : args) {
			String key = arg;
			if (key.toUpperCase().matches("TYPE=PIE")) {
				type = 1;
			} else {
				args2.add(key);
			}
		}
		args = args2.toArray(new String[args2.size()]);

		if (args.length > 2) {
			String[] args1 = new String[args.length - 2];
			for (int i = 2; i < args.length; i++) {
				args1[i - 2] = args[i];
			}
			return this.getAccumXYPlotTable(args[0], args[1], args1);
		} else if (args.length == 2) {
			if (type == 1) {
				return this.getPiePlotTable(args[0], args[1]);
			} else {
				return this.getAccumXYPlotTable(args[0], args[1]);
			}
		} else {
			return "";
		}
	}

	public String getPlotUrl(String... args) {
		int type = 0;
		ArrayList<String> args2 = new ArrayList<String>();
		for (String arg : args) {
			String key = arg;
			if (key.toUpperCase().matches("TYPE=PIE")) {
				type = 1;
			} else {
				args2.add(key);
			}
		}
		args = args2.toArray(new String[args2.size()]);

		if (args.length > 2) {
			String[] args1 = new String[args.length - 2];
			for (int i = 2; i < args.length; i++) {
				args1[i - 2] = args[i];
			}
			return this.getAccumXYPlotUrl(args[0], args[1], args1);
		} else if (args.length == 2) {
			if (type == 1) {
				return this.getPiePlotUrl(args[0], args[1]);
			} else {
				return this.getAccumXYPlotUrl(args[0], args[1]);
			}
		} else {
			return "";
		}
	}

	public int getWidth() {
		return this.width;
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
			if (cols != null) {
				for (String key : cols) {
					if (row.get(key) != null) {
						group += row.get(key) + " ";
					}
				}
			}
			group = group.trim();
			if (!groups.containsKey(group)) {
				groups.put(group, new Table());
			}
			groups.get(group).add(row);
		}
		return groups;
	}

	@Override
	public int indexOf(Object o) {
		return this.rows.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return this.rows.isEmpty();
	}

	@Override
	public Iterator<Row> iterator() {
		return this.rows.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return this.rows.lastIndexOf(o);
	}

	@Override
	public ListIterator<Row> listIterator() {
		return this.rows.listIterator();
	}

	@Override
	public ListIterator<Row> listIterator(int index) {
		return this.rows.listIterator(index);
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
			} catch (Exception e) {

			}
		}
		return output;
	}

	@Override
	public Row remove(int index) {
		return this.rows.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		return this.rows.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return this.rows.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.rows.retainAll(c);
	}

	@Override
	public Row set(int index, Row element) {
		return this.rows.set(index, element);
	}

	public void setColors(Color[] colors) {
		this.colors = colors;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int size() {
		return this.rows.size();
	}

	/**
	 * Sort the Table entries by the specified columns
	 * 
	 * @param rankingOrder
	 * @param cols
	 * @return
	 */
	public Table sortBy(RankingOrder rankingOrder, String... cols) {
		if (this.size() < 2 || cols == null || cols.length == 0) {
			Table ret = new Table();
			ret.addAll(this);
			return ret;
		}

		if (cols.length > 1) {
			Table ret = this;
			for (String col : cols) {
				ret = ret.sortBy(rankingOrder, col);
			}
			return ret;
		}

		// Check if the requested column can be converted into a double?
		boolean isNumeric = true;
		if (isNumeric) {
			Row row = this.get(0);
			for (String col : cols) {
				if (row.containsKey(col)) {
					try {
						double d = Double.parseDouble(row.get(col));
						isNumeric &= true;
					} catch (Exception e) {
						isNumeric = false;
					}
				}
			}
		}

		if (isNumeric) {
			ArrayList<Double> sortDoubles = new ArrayList<Double>();
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
				sortDoubles.add(Double.parseDouble(str));
			}

			Sort.BubbleSort(sortDoubles, rows, rankingOrder);

			return new Table(rows);
		} else {

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

	@Override
	public List<Row> subList(int fromIndex, int toIndex) {
		return this.rows.subList(fromIndex, toIndex);
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
	 * Sum the data in the given groupings
	 * 
	 * @param X
	 *            The column to group by
	 * @param Ys
	 *            The columns to accumulate
	 * @return
	 */
	public Table sumBy(String X, String... Ys) {
		Map<String, Table> grouped = this.groupBy(X);
		Table ret = new Table();
		for (Entry<String, Table> entry : grouped.entrySet()) {
			String group = entry.getKey();
			Table gt = entry.getValue();
			Row row = new Row();
			row.put(X, group);
			for (String Y : Ys) {
				double sum = gt.sum(Y);
				row.put(Y, sum + "");
			}
			ret.add(row);
		}
		return ret;
	}

	@Override
	public Object[] toArray() {
		return this.rows.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.rows.toArray(a);
	}

	/**
	 * Returns fully defined HTML content including headers, title, summary
	 * plots etc...
	 * 
	 * @param title
	 * @param plotVars
	 * @return
	 */
	public String toHtml(String title, String[]... plotVars) {

		StringBuilder sb = new StringBuilder();

		sb.append("<html>\n");

		if (title != null && !title.isEmpty()) {
			sb.append("  <title>" + title.replace(".html", "") + "</title>\n");
		}

		sb.append("  <head>\n");
		// sb.append("      <script src=\"http://willfulwreckords.com/Software/scripts/flot/excanvas.min.js\"></script>");
		// sb.append("      <script src=\"http://code.jquery.com/jquery-1.10.1.min.js\"></script>");
		// sb.append("      <script src=\"http://willfulwreckords.com/Software/scripts/flot/jquery.flot.min.js\"></script>");
		// sb.append("      <script src=\"http://willfulwreckords.com/Software/scripts/sdc/sdc.js\"></script>");
		// sb.append("      <link rel=\"stylesheet\" type=\"text/css\" href=\"http://willfulwreckords.com/Software/scripts/sdc/sdc.css\">");
		sb.append("  </head>\n");

		sb.append("  <body>\n");
		sb.append("     <p>Report Generated @ " + new TimeStamp().toString()
				+ "</p>\n");

		if (plotVars != null) {
			for (String[] args : plotVars) {
				String url = this.getPlotUrl(args);
				if (!url.isEmpty()) {
					if (args.length == 1) {
						sb.append("     <h2>" + args[0] + "</h2>\n");
					} else if (args.length >= 2) {
						String str = args[0];
						String str2 = "";
						for (int k = 2; k < args.length; k++) {
							if (!args[k].contains("=")) {
								str2 += " " + args[k];
							}
						}
						str2 = str2.trim();
						if (!str2.isEmpty()) {
							str += " by " + str2;
						}
						sb.append("     <h2>" + args[1] + " vs. " + str
								+ "</h2>\n");
					}
					sb.append("     <img src=" + url + ">\n");
					sb.append(this.getPlotTable(args));
				}
			}
		}

		sb.append("     <div id=\"rawData\">\n");
		sb.append(this.toHtmlTableString());
		sb.append("    </div>\n");

		sb.append("  </body>\n");
		sb.append("</html>\n");

		return sb.toString();
	}

	/**
	 * Returns a simple HTML table representing this object
	 * 
	 * @return
	 */
	public String toHtmlTableString() {

		StringBuilder sb = new StringBuilder();
		String attr = "";
		for (Entry<String, String> entry : this.attributes.entrySet()) {
			String key = entry.getKey().trim();
			String value = entry.getValue().trim();
			if (!value.startsWith("\"")) {
				value = "\"" + value;
			}
			if (!value.endsWith("\"")) {
				value = value + "\"";
			}
			attr += " " + key + "=" + value;
		}
		sb.append("    <table" + attr + ">\n");

		Set<String> headers = this.getColLabels();

		// Print header information
		sb.append("      <thead><tr>\n");
		for (String header : headers) {
			// out.print(header.replace(",", "") + ",\t");
			sb.append("        <th>" + header + "</th>\n");
		}
		sb.append("      </tr></thead>\n");

		// Print rows
		sb.append("      <tbody>\n");
		for (Row row : this) {
			sb.append("          <tr>\n");
			for (String header : headers) {
				String str = row.get(header) + "";
				if (row.get(header) == null) {
					str = " ";
				}
				// out.print(str.replace(",", "") + ",\t");
				sb.append("        <td class=\"" + header + "\">" + str
						+ "</td>");
			}
			sb.append("          </tr>\n");
			sb.append("\n");
		}
		sb.append("      </tbody>\n");
		sb.append("    </table>\n");
		return sb.toString();
	}

	/**
	 * Convert to a JSON string
	 * 
	 * @return
	 */
	public String toJson() {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues()
				.serializeNulls().create();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"attributes\":" + gson.toJson(this.attributes) + ", ");
		sb.append("\"rows\":{");

		for (int i = 0; i < this.size(); i++) {
			Row row = this.get(i);
			if (i == this.size() - 1) {
				sb.append(row.toJson());
			} else {
				sb.append(row.toJson() + ", ");
			}
		}

		sb.append("}");
		return sb.toString();
	}

	public void writeCSV(String filename) {
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			PrintStream out = new PrintStream(file);

			Set<String> headers = this.getColLabels();

			// Print header information
			String rstr = "";
			for (String header : headers) {
				// out.print(header.replace(",", "") + ",\t");
				rstr += header.replace(",", "").trim() + ", ";
			}
			rstr = rstr.trim();
			out.println(rstr.substring(0, rstr.length() - 1));

			// Print rows
			for (Row row : this) {
				rstr = "";
				for (String header : headers) {
					String vstr = row.get(header);
					if (vstr == null) {
						vstr = " ";
					}
					rstr += vstr.replace(",", "").trim() + ", ";
				}
				rstr = rstr.trim();
				out.println(rstr.substring(0, rstr.length() - 1));
			}
			out.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}
	}

	public void writeHTML(String filename, String[]... plotVars) {
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			PrintStream out = new PrintStream(file);

			out.println(this.toHtml(file.getName(), plotVars));

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeXLSX(String filename, String title) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		if (title == null) {
			title = "Data";
		}
		XSSFSheet sheet = workbook.createSheet(title);
		XSSFCreationHelper createHelper = workbook.getCreationHelper();
		int rownum = 0;

		Set<String> headers = this.getColLabels();

		// Print header information
		XSSFRow row = sheet.createRow(rownum++);
		int cellnum = 0;
		for (String header : headers) {
			Cell cell = row.createCell(cellnum++);

			if (header.toString().contains("$")) {
				cell.setCellValue(new Double(header.toString().replace("$", "")));
				cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			} else {
				cell.setCellValue(header.toString());
			}

			// out.print(header.replace(",", "") + ",\t");
		}
		// out.println();

		// Print rows
		for (Row map : this) {
			row = sheet.createRow(rownum++);
			cellnum = 0;
			for (String header : headers) {
				Cell cell = row.createCell(cellnum++);
				String str = map.get(header) + "";
				if (map.get(header) == null) {
					str = " ";
				}

				if (str.contains("$")) {
					cell.setCellValue(new Double(str.replace("$", "")));
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				} else if (str.trim().matches("(\\d{1,2}/){2}\\d{4}")) {
					CellStyle cellStyle = workbook.createCellStyle();
					cellStyle.setDataFormat(createHelper.createDataFormat()
							.getFormat("m/d/yyyy"));
					try {
						Date date = new java.text.SimpleDateFormat("MM/d/yyyy",
								Locale.ENGLISH).parse(str);
						cell.setCellValue(date);
						cell.setCellStyle(cellStyle);
					} catch (ParseException e) {
						cell.setCellValue(str);
					}

				} else if (str.trim().matches("(\\d{1,2}/){1}\\d{4}")) {
					CellStyle cellStyle = workbook.createCellStyle();
					cellStyle.setDataFormat(createHelper.createDataFormat()
							.getFormat("m/yyyy"));
					try {
						Date date = new java.text.SimpleDateFormat("MM/yyyy",
								Locale.ENGLISH).parse(str);
						cell.setCellValue(date);
						cell.setCellStyle(cellStyle);
					} catch (ParseException e) {
						cell.setCellValue(str);
					}

				} else {
					cell.setCellValue(str);
				}
				// out.print(str.replace(",", "") + ",\t");
			}
			// out.println();
		}

		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	public void writeXML(String filename) {
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			PrintStream out = new PrintStream(file);
			out.println("<xml>");

			String attr = "";
			for (Entry<String, String> entry : this.attributes.entrySet()) {
				String key = entry.getKey().trim();
				String value = entry.getValue().trim();
				if (!value.startsWith("\"")) {
					value = "\"" + value;
				}
				if (!value.endsWith("\"")) {
					value = value + "\"";
				}
				attr += " " + key + "=" + value;
			}
			out.println("    <table" + attr + ">");

			Set<String> headers = this.getColLabels();

			// Print header information
			out.println("      <thead><tr>");
			for (String header : headers) {
				// out.print(header.replace(",", "") + ",\t");
				out.println("        <th>" + header + "</th>");
			}
			out.println("      </tr></thead>");

			// Print rows
			out.println("      <tbody>");
			for (Row row : this) {
				out.println("          <tr>");
				for (String header : headers) {
					String str = row.get(header) + "";
					if (row.get(header) == null) {
						str = " ";
					}
					// out.print(str.replace(",", "") + ",\t");
					out.print("        <td class=\"" + header + "\">" + str
							+ "</td>");
				}
				out.println("          </tr>");
				out.println();
			}
			out.println("      </tbody>");
			out.println("    </table>");
			out.println("</xml>");
			// workbook.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}
	}
}
