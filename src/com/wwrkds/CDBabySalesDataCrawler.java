package com.wwrkds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.varia.NullAppender;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class CDBabySalesDataCrawler extends Thread {

	public class WWRKDSRow<U, T> extends LinkedHashMap<U, T> {
		private Set<String> labels = new LinkedHashSet<String>();

		public WWRKDSRow(Map<U, T> data) {
			super();
			for (Entry<U, T> entry : data.entrySet()) {
				this.put(entry.getKey(), entry.getValue());
			}

		}

		public Set<String> getLabels() {
			return this.labels;
		}

		public void setLabels(Set<String> labels) {
			this.labels = labels;
		}
	}

	public class WWRKDSTable<U, T> extends ArrayList<WWRKDSRow<U, T>> {

		public WWRKDSTable(Collection<Map<U, T>> rows) {
			super();
			for (Map<U, T> row : rows) {
				WWRKDSRow<U, T> r = new WWRKDSRow<U, T>(row);
				this.add(r);
			}
		}

		public Set<U> getColLabels() {
			Set<U> ret = new HashSet<U>();
			for (WWRKDSRow<U, T> row : this) {
				for (U key : row.keySet()) {
					ret.add(key);
				}
			}
			return ret;
		}

		public String toHtml() {
			String str = "<table border=1>\n";

			str += "  <thead>";
			str += "    <tr>\n";
			str += "      <th></th>\n";
			Set<U> cols = this.getColLabels();
			for (U col : cols) {
				str += "      <th>" + col + "</th>\n";
			}
			str += "    </tr>\n";
			str += "  </thead>";

			str += "  <tbody>\n";
			for (WWRKDSRow<U, T> row : this) {
				str += "    <tr>\n";

				str += "      <td>";
				String lbls = "";
				for (String label : row.getLabels()) {
					lbls += " " + label;
				}
				str += lbls.trim() + "</td>\n";

				for (U col : cols) {
					T c = row.get(col);
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
	}

	public static synchronized <U, T> Collection<Map<U, T>> filter(
			Collection<Map<U, T>> rows, String filter) {
		List<Map<U, T>> ret = new ArrayList<Map<U, T>>();
		for (Map<U, T> row : rows) {
			if (CDBabySalesDataCrawler.satisfies(row, filter)) {
				ret.add(row);
			}
		}
		return ret;
	}

	@SafeVarargs
	public static synchronized <U, T> Map<String, Collection<Map<U, T>>> groupBy(
			Collection<Map<U, T>> rows, U... cols) {
		Map<String, Collection<Map<U, T>>> groups = new HashMap<String, Collection<Map<U, T>>>();
		for (Map<U, T> row : rows) {
			String group = "";
			for (U key : cols) {
				if (row.get(key) != null) {
					group += row.get(key) + "";
				}
			}
			if (!groups.containsKey(group)) {
				groups.put(group, new ArrayList<Map<U, T>>());
			}
			groups.get(group).add(row);
		}
		return groups;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			CDBabySalesDataCrawler crawler = new CDBabySalesDataCrawler();
			crawler.parseArgs(args);
			crawler.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out
					.println("There was an error when trying to run Crawler.");
		}
	}

	/*
	 * @SafeVarargs public static synchronized <U, T> Map<String, String> sum(
	 * Collection<Map<U, T>> rows, U y, U... groupBy) { Set<U> gbs = new
	 * LinkedHashSet<U>(); gbs.addAll(Arrays.asList(groupBy));
	 * 
	 * Map<String, String> output = new TreeMap<String, String>();
	 * 
	 * // Print rows for (Map<U, T> map : rows) { String group = ""; for (U g :
	 * gbs) { T t = map.get(g); if (t != null) { group += t + " "; } } group =
	 * group.trim();
	 * 
	 * String value = map.get(y) + ""; value = value.replace("$", "").trim(); if
	 * (output.containsKey(group)) { try { double old =
	 * Double.parseDouble(output.get(group)); double val =
	 * Double.parseDouble(value); output.put(group, old + val + ""); } catch
	 * (Exception e) { if (!output.get(group).trim().contains(value)) { String n
	 * = output.get(group).trim() + " " + value; output.put(group, n.trim()); }
	 * } } else { output.put(group, value.trim()); } } return output; }
	 */

	public static synchronized <U, T> boolean satisfies(Map<U, T> row,
			String filter) {
		for (Entry<U, T> entry : row.entrySet()) {
			U key = entry.getKey();
			T value = entry.getValue();

			filter = filter.replaceAll(key + "", value + "");
		}
		// create a script engine manager
		ScriptEngineManager factory = new ScriptEngineManager();
		// create a JavaScript engine
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		// evaluate JavaScript code from String
		try {
			Object obj = engine.eval(filter);
			return (boolean) obj;
		} catch (ScriptException e) {
			return false;
		}
	}

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

	public static synchronized void writeCSV(String filename, List<String> rows) {
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			PrintStream out = new PrintStream(file);
			for (String rowdata : rows) {
				out.println(rowdata);
			}
			// workbook.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}
	}

	public static synchronized <U, T> void writeCSVRows(String filename,
			Collection<Map<U, T>> rows) {
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			PrintStream out = new PrintStream(file);

			Set<U> headers = new HashSet<U>();

			// Collect header information
			for (Map<U, T> map : rows) {
				headers.addAll(map.keySet());
			}

			// Print header information
			for (U header : headers) {
				out.print(header.toString().replace(",", "") + ",\t");
			}
			out.println();

			// Print rows
			for (Map<U, T> map : rows) {
				for (U header : headers) {
					String str = map.get(header) + "";
					if (map.get(header) == null) {
						str = " ";
					}
					out.print(str.replace(",", "") + ",\t");
				}
				out.println();
			}
			out.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}
	}

	public static synchronized void writeHTML(String filename, List<String> rows) {
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();

			PrintStream out = new PrintStream(file);
			out.println("<html>");
			out.println("  <title>" + file.getName().replace(".html", "")
					+ "</title>");
			out.println("  <head>");
			out.println("      <script src=\"http://willfulwreckords.com/Software/scripts/flot/excanvas.min.js\"></script>");
			out.println("      <script src=\"http://code.jquery.com/jquery-1.10.1.min.js\"></script>");
			out.println("      <script src=\"http://willfulwreckords.com/Software/scripts/flot/jquery.flot.min.js\"></script>");
			out.println("      <script src=\"http://willfulwreckords.com/Software/scripts/sdc/sdc.js\"></script>");
			out.println("      <link rel=\"stylesheet\" type=\"text/css\" href=\"http://willfulwreckords.com/Software/scripts/sdc/sdc.css\">");
			out.println("  </head>");
			out.println("  <body>");
			out.println("    <table class='dataTable'>");

			String headerrowdata = rows.get(0);
			String[] hobj = headerrowdata.split(",");
			out.println("      <thead><tr>");
			for (String s : hobj) {
				out.println("        <th>" + s.trim() + "</th>");
			}
			out.println("      </tr></thead>");

			out.println("      <tbody>");
			for (int ri = 1; ri < rows.size(); ri++) {
				String rowdata = rows.get(ri);
				String[] obj = rowdata.split(",");
				out.println("      <tr>");
				for (int si = 0; si < obj.length; si++) {
					String s = obj[si];
					String c = hobj[si];
					out.print("        <td class=\"" + c.trim() + "\">"
							+ s.trim() + "</td>");
				}
				out.println("      </tr>");
			}
			out.println("      </tbody>");
			out.println("    </table>");
			out.println("  </body>");
			out.println("</html>");
			// workbook.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}
	}

	public static synchronized <U, T> void writeHTMLRows(String filename,
			Collection<Map<U, T>> rows) {
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();

			PrintStream out = new PrintStream(file);
			out.println("<html>");
			out.println("  <title>" + file.getName().replace(".html", "")
					+ "</title>");
			out.println("  <head>");
			out.println("      <script src=\"http://willfulwreckords.com/Software/scripts/flot/excanvas.min.js\"></script>");
			out.println("      <script src=\"http://code.jquery.com/jquery-1.10.1.min.js\"></script>");
			out.println("      <script src=\"http://willfulwreckords.com/Software/scripts/flot/jquery.flot.min.js\"></script>");
			out.println("      <script src=\"http://willfulwreckords.com/Software/scripts/sdc/sdc.js\"></script>");
			out.println("      <link rel=\"stylesheet\" type=\"text/css\" href=\"http://willfulwreckords.com/Software/scripts/sdc/sdc.css\">");
			out.println("  </head>");
			out.println("  <body>");
			out.println("    <table class='dataTable'>");

			Set<U> headers = new HashSet<U>();

			// Collect header information
			for (Map<U, T> map : rows) {
				headers.addAll(map.keySet());
			}

			// Print header information
			out.println("      <thead><tr>");
			for (U header : headers) {
				// out.print(header.replace(",", "") + ",\t");
				out.println("        <th>" + header + "</th>");
			}
			out.println("      </tr></thead>");

			// Print rows
			out.println("      <tbody>");
			for (Map<U, T> map : rows) {
				out.println("          <tr>");
				for (U header : headers) {
					String str = map.get(header) + "";
					if (map.get(header) == null) {
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
			out.println("  </body>");
			out.println("</html>");
			// workbook.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}
	}

	public static synchronized void writeStringToFile(String filename,
			String text) {
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			PrintStream out = new PrintStream(file);
			// for (String rowdata : rows) {
			out.print(text);
			// }
			// workbook.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}
	}

	public static synchronized void writeXLSX(String filename, String title,
			List<String> rows) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		if (title == null) {
			title = "Data";
		}
		XSSFSheet sheet = workbook.createSheet(title);
		int rownum = 0;
		for (String rowdata : rows) {
			Row row = sheet.createRow(rownum++);
			String[] cols = rowdata.split(",");
			int cellnum = 0;
			for (String obj : cols) {
				Cell cell = row.createCell(cellnum++);
				if (obj.contains("$")) {
					cell.setCellValue(new Double(obj.replace("$", "")));
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				} else {
					cell.setCellValue(obj);
				}
			}
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

	public static synchronized <U, T> void writeXLSXRows(String filename,
			String title, Collection<Map<U, T>> rows) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		if (title == null) {
			title = "Data";
		}
		XSSFSheet sheet = workbook.createSheet(title);
		XSSFCreationHelper createHelper = workbook.getCreationHelper();
		int rownum = 0;

		Set<U> headers = new HashSet<U>();

		// Collect header information
		for (Map<U, T> map : rows) {
			headers.addAll(map.keySet());
		}

		// Print header information
		Row row = sheet.createRow(rownum++);
		int cellnum = 0;
		for (U header : headers) {
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
		for (Map<U, T> map : rows) {
			row = sheet.createRow(rownum++);
			cellnum = 0;
			for (U header : headers) {
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

	public static synchronized void writeXML(String filename, List<String> rows) {
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			PrintStream out = new PrintStream(file);
			out.println("<xml>");
			out.println("    <table>");
			boolean headers = true;
			for (String rowdata : rows) {
				String[] obj = rowdata.split(",");
				out.println("      <tr>");
				for (String s : obj) {
					if (headers) {
						out.println("        <th>" + s + "</th>");
					} else {
						out.println("        <td>" + s + "</td>");
					}
				}
				out.println("      </tr>");
				headers = false;
			}
			out.println("    </table>");
			out.println("</xml>");
			// workbook.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}
	}

	public static synchronized <U, T> void writeXMLRows(String filename,
			Collection<Map<U, T>> rows) {
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			PrintStream out = new PrintStream(file);
			out.println("<xml>");
			out.println("    <table class='dataTable'>");

			Set<U> headers = new HashSet<U>();

			// Collect header information
			for (Map<U, T> map : rows) {
				headers.addAll(map.keySet());
			}

			// Print header information
			out.println("      <thead><tr>");
			for (U header : headers) {
				// out.print(header.replace(",", "") + ",\t");
				out.println("        <th>" + header + "</th>");
			}
			out.println("      </tr></thead>");

			// Print rows
			out.println("      <tbody>");
			for (Map<U, T> map : rows) {
				out.println("          <tr>");
				for (U header : headers) {
					String str = map.get(header) + "";
					if (map.get(header) == null) {
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

	private boolean doFtp = false, doXml = false, doXlsx = false,
			doHtml = false, doCsv = true;
	private final boolean dosql = false;
	private String drivername = "firefox";
	private String ftpDirectory = null;
	private String ftpPassword = null;
	private String ftpPort = null;
	private String ftpServer = null;
	private String ftpUsername = null;
	private String outputDirectory = null;
	private String password = null;
	private String sheetTitle = "CDBaby Sales Data";
	private String startpage = "https://members.cdbaby.com/Login.aspx";

	private int timedelay = 15000;
	private String username = null;

	private CDBabySalesDataCrawler() {
		// LicenseManager manager = new LicenseManager(
		// com.wwrkds.CDBabySalesDataCrawler.class, "CDBSDC-ZIP",
		// "http://willfulwreckords.com/Store/",
		// "http://willfulwreckords.com/Software/license/");

	}

	public CDBabySalesDataCrawler(String u, String p, String d, String s) {
		this.setUsername(u);
		this.setPassword(p);
		this.setOutputDirectory(d);
		this.setSheetTitle(s);
	}

	public CDBabySalesDataCrawler(String u, String p, String d, String s,
			int timeout) {
		this.setUsername(u);
		this.setPassword(p);
		this.setOutputDirectory(d);
		this.setSheetTitle(s);
		this.setTimedelay(timeout);
	}

	private boolean doCrawl() {
		return this.isDoCsv() | this.isDoHtml() | this.isDoXlsx()
				| this.isDoXlsx();
	}

	public String getDirPrefix() {
		return this.getOutputDirectory();
	}

	public String getDrivername() {
		return this.drivername;
	}

	public String getFtpDirectory() {
		return this.ftpDirectory;
	}

	public String getFtpPassword() {
		return this.ftpPassword;
	}

	public String getFtpPort() {
		return this.ftpPort;
	}

	public String getFtpServer() {
		return this.ftpServer;
	}

	public String getFtpUsername() {
		return this.ftpUsername;
	}

	public String getOutputDirectory() {
		return this.outputDirectory;
	}

	public String getPassword() {
		return this.password;
	}

	public String getSheetTitle() {
		return this.sheetTitle;
	}

	public String getStartpage() {
		return this.startpage;
	}

	public int getTimedelay() {
		return this.timedelay;
	}

	public String getUsername() {
		return this.username;
	}

	public boolean isDoCsv() {
		return this.doCsv;
	}

	public boolean isDoFtp() {
		return this.doFtp;
	}

	public boolean isDoHtml() {
		return this.doHtml;
	}

	public boolean isDoXlsx() {
		return this.doXlsx;
	}

	public boolean isDoXml() {
		return this.doXml;
	}

	// public org.w3c.dom.Document loadXMLFromString(String xml) throws
	// Exception {
	// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	// factory.setNamespaceAware(true);
	// DocumentBuilder builder = factory.newDocumentBuilder();

	// builder.parse(new InputSource(new StringReader(xml)));
	// return builder.parse(new ByteArrayInputStream(xml.getBytes()));
	// }

	/**
	 * <p>
	 * Accepted arguments
	 * </p>
	 * <table border=1>
	 * <tr>
	 * <td>required</td>
	 * <td>[-username | -u] string</td>
	 * <td>string = The CD Baby Account username to use</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>required</td>
	 * <td>[-password | -p] string</td>
	 * <td>string = The CD Baby Account Password to use</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>required</td>
	 * <td>-outdir string</td>
	 * <td>string = The output directory to place completed files</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-dohtml [true|false]</td>
	 * <td></td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-doxml [true|false]</td>
	 * <td></td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-docsv [true|false]</td>
	 * <td></td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-doxlsx [true|false]</td>
	 * <td></td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-doftp [true|false]</td>
	 * <td></td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-ftpusername string</td>
	 * <td>string = The FTP username to use</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-ftppassword string</td>
	 * <td>string = The FTP password to use</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-ftpserver string</td>
	 * <td>string = The FTP server url to use</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-ftpport string</td>
	 * <td>string = The FTP port to use</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-ftpdirectory string</td>
	 * <td>string = The FTP directory to use</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-driver [(firefox) | chrome | safari | ie]</td>
	 * <td>string = The WEBDriver to use to use</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-sheeetitle string</td>
	 * <td>string = The Title of the xlsx sheet to use</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-startpage string</td>
	 * <td>string = The url of the CDBaby login page</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-clickdelay string</td>
	 * <td>string = The amount of click delay to use</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td>-h</td>
	 * <td>Print help information</td>
	 * </tr>
	 * 
	 * </table>
	 * 
	 * @param args
	 */
	public void parseArgs(String[] args) {

		for (int i = 0; i < args.length; i++) {
			if (args[i].toLowerCase().contentEquals("-username")
					|| args[i].toLowerCase().contentEquals("-u")) {
				this.setUsername(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-password")
					|| args[i].toLowerCase().contentEquals("-p")) {
				this.setPassword(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-outdir")) {
				this.setOutputDirectory(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-dohtml")) {
				this.setDohtml(Boolean.parseBoolean(args[++i]));
			} else if (args[i].toLowerCase().contentEquals("-doxml")) {
				this.setDoxml(Boolean.parseBoolean(args[++i]));
			} else if (args[i].toLowerCase().contentEquals("-doxlsx")) {
				this.setDoxlsx(Boolean.parseBoolean(args[++i]));
			} else if (args[i].toLowerCase().contentEquals("-docsv")) {
				this.setDocsv(Boolean.parseBoolean(args[++i]));
			} else if (args[i].toLowerCase().contentEquals("-doftp")) {
				this.setDoFtp(Boolean.parseBoolean(args[++i]));
			} else if (args[i].toLowerCase().contentEquals("-ftpusername")) {
				this.setFtpUsername(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-ftppassword")) {
				this.setFtpPassword(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-ftpserver")) {
				this.setFtpServer(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-ftpport")) {
				this.setFtpPort(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-ftpdirectory")) {
				this.setFtpDirectory(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-driver")) {
				this.setDrivername(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-sheettitle")) {
				this.setSheetTitle(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-startpage")) {
				this.setStartpage(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-clickdelay")) {
				this.setTimedelay(Integer.parseInt(args[++i]));
			} else if (args[i].toLowerCase().contentEquals("-h")) {
				this.printHelp();
			}
		}
	}

	public void printHelp() {
		System.out
				.print("*********************************************************************************************************************\n");
		System.out
				.print("*********************************************************************************************************************\n");
		System.out.print("CD Baby Sales Data Crawler ("
				+ CDBabySalesDataCrawlerUI.getVersion() + "):\n");
		System.out
				.print("\tWritten by: Jonathan J. Lareau - Willful Wreckords, LLC\n\twww.willfulwreckords.com\n\n");
		System.out
				.print("DISCLAIMER:\n\tTHIS SOFTWARE IS PROVIDED \"AS IS\" AND WITHOUT ANY EXPRESSED OR IMPLIED WARRANTIES.\n");
		System.out
				.print("*********************************************************************************************************************\n");
		System.out
				.print("*********************************************************************************************************************\n");
		System.out.print("\n");

		System.out
				.print("PRE_REQUISITES: This software requires that a HTML5 compliant version of Mozilla Firefox, Chrome, IE or Safari "
						+ "be installed on your system with a \"standard\" vendor install.\n\n");
		System.out
				.print("This program is a web-crawler that automates the process of logging "
						+ "into your CD Baby account and "
						+ "scraping your sales data for each project "
						+ "listing that is available in the Accounting Overview pages.\n\n");
		System.out
				.print("A new browser window will be opened so the crawler can operate, which "
						+ "will take a few moments to launch.  Once launched please do not click "
						+ "inside or make adjustments to this new browser window while the crawler "
						+ "is running. When the crawler completes it will automatically close this window. \n\n");

		System.out.println("Parameters:");
		System.out.println("\t-username <CDBaby username> : sets username");
		System.out.println("\t-password <CDBaby password> : sets password");
		System.out
				.println("\t-driver [firefox | chrome | safari | ie] : sets the browser to use.  The selected browser must have a \"standard\" install on your system.");
		System.out
				.println("\t-outdir <local output directory> : sets local output directory");
		System.out.println("\t-dohtml [true | false]");
		System.out.println("\t-doxml [true | false]");
		System.out.println("\t-doxlsx [true | false]");
		System.out.println("\t-docsv [true | false]");
		System.out.println("\t-doftp [true | false]");
		System.out.println("\t-ftpusername <FTP username> : sets ftp username");
		System.out.println("\t-ftppassword <FTP password> : sets ftp password");
		System.out
				.println("\t-ftpserver <FTP server URL> : sets FTP server url");
		System.out
				.println("\t-ftpport <FTP port number> : sets ftp port number");
		System.out
				.println("\t-ftpdirectory <FTP directory> : sets FTP directory");
		System.out
				.println("\t-startpage <url> : defaults to https://members.cdbaby.com/Login.aspx");
		System.out.println("\t-sheettitle <string> : sets xlxs sheet title");
		System.out
				.println("\t-clickdelay <integer seconds> : added delay between crawler clicks for stability");
		System.out.println("\t-h : prints this help message");
	}

	@Override
	public void run() {

		if (this.doCrawl()) {
			// org.apache.log4j.BasicConfigurator.configure();
			org.apache.log4j.BasicConfigurator.configure(new NullAppender());

			// The Firefox driver supports javascript
			WebDriver driver = null;
			try {
				if (this.drivername.trim().toLowerCase()
						.contentEquals("firefox")) {
					driver = new FirefoxDriver();
				} else if (this.drivername.trim().toLowerCase()
						.contentEquals("ie")) {
					driver = new org.openqa.selenium.ie.InternetExplorerDriver();
				} else if (this.drivername.trim().toLowerCase()
						.contentEquals("chrome")) {
					driver = new org.openqa.selenium.chrome.ChromeDriver();
				} else if (this.drivername.trim().toLowerCase()
						.contentEquals("safari")) {
					driver = new org.openqa.selenium.safari.SafariDriver();
				} else if (this.drivername.trim().toLowerCase()
						.contentEquals("htmlunit")) {
					driver = new org.openqa.selenium.htmlunit.HtmlUnitDriver();
				}
			} catch (Exception e) {

			}

			if (driver != null) {
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				driver.manage().timeouts()
						.pageLoadTimeout(10, TimeUnit.SECONDS);
				driver.manage().timeouts()
						.setScriptTimeout(10, TimeUnit.SECONDS);

				try {
					driver.manage().deleteAllCookies();

					// Go to the CDBaby artist login page
					driver.get(this.startpage);

					WebElement query;

					// Enter username
					String input_id = null;
					int version = 2;
					if (version == 1) {
						input_id = "ctl00_centerColumn_txtClientUsername";
					} else if (version == 2) {
						input_id = "ctl00_MainContent_txtClientUsername";
					}
					query = driver.findElement(By.id(input_id));
					query.clear();
					query.sendKeys(this.username);

					// Enter password
					if (version == 1) {
						input_id = "ctl00_centerColumn_txtClientPassword";
					} else if (version == 2) {
						input_id = "ctl00_MainContent_txtClientPassword";
					}
					query = driver.findElement(By.id(input_id));
					query.clear();
					query.sendKeys(this.password);

					// Click login button
					if (version == 1) {
						input_id = "ctl00_centerColumn_btnLoginArtist";
					} else if (version == 2) {
						input_id = "ctl00_MainContent_btnLoginArtist";
					}
					query = driver.findElement(By.id(input_id));
					query.click();

					// try {
					// Thread.sleep(this.timedelay);
					// } catch (InterruptedException e3) {
					// // TODO Auto-generated catch block
					// e3.printStackTrace();
					// }

					query = driver.findElement(By
							.partialLinkText("Accounting overview"));
					query.click();

					String overviewsrc = driver.getPageSource();
					try {
						File rawovsource = new File(this.getOutputDirectory()
								+ "raw" + File.separator + "Overview.raw.html");
						rawovsource.getParentFile().mkdirs();
						PrintStream ps = new PrintStream(rawovsource);
						ps.println(overviewsrc);
						ps.close();
						// org.w3c.dom.Document docw3c =
						// loadXMLFromString(src);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					// try {
					// Thread.sleep(this.timedelay);
					// } catch (InterruptedException e2) {
					// // TODO Auto-generated catch block
					// e2.printStackTrace();
					// }

					ArrayList<Integer> visited = new ArrayList<Integer>();
					List<Map<String, String>> completeData = new ArrayList<Map<String, String>>();
					while (true) {

						String albumTitle = "";

						List<WebElement> dataTables = driver
								.findElements(By
										.cssSelector("table.data-table-embed tr.light-row"));
						WebElement q = null;
						int vi = 0;
						for (WebElement dt : dataTables) {
							String text = dt.getText().trim()
									.replaceAll("\\n\\x0B\\f\\r", "");
							// System.out.println(text);
							if (!text.contains("Total Sales")
									&& !visited.contains(vi)) {
								q = dt;
								// Get the project name...
								albumTitle = text.substring(0,
										text.indexOf("$") - 1).trim();
								// Replace invalid file name elements
								albumTitle = albumTitle
										.replaceAll(
												"[\\\\\\/\\:\\,\\!\\@\\#\\$\\%\\^&\\+\\*\\(\\)]",
												"");
								albumTitle = albumTitle.trim();
								albumTitle = albumTitle.replace(" ", "-");
								visited.add(vi);
								System.out.print(">>" + text + "\n");
								break;
							}
							vi++;
						}

						if (q != null) {
							WebElement link = q.findElement(By
									.partialLinkText("view sales"));
							link.click();

							try {
								// Thread.sleep(this.timedelay);
								WebElement select = driver.findElement(By
										.tagName("select"));
								List<WebElement> allOptions = select
										.findElements(By.tagName("option"));
								for (WebElement option : allOptions) {
									// System.out.println(String.format("Value is: %s",
									// option.getAttribute("value")));
									if (option.getText().contains("500")) {
										option.click();
									}
								}
							} catch (Exception ex3) {

							}

							// try {
							// Thread.sleep(this.timedelay);
							// } catch (InterruptedException e2) {
							// // TODO Auto-generated catch block
							// e2.printStackTrace();
							// }

							String revenueType = "UNKNOWN";
							try {
								revenueType = " "
										+ driver.findElement(
												By.cssSelector("div.section-title-953 h2"))
												.getText();
								revenueType = revenueType.replace("(", "")
										.replace(")", "");

							} catch (Exception ex4) {

							}
							revenueType = revenueType.trim();
							revenueType = revenueType.replace(" ", "-");

							System.out.print(">>\tCollecting table data\n");
							System.out.flush();

							Set<String> seen = new HashSet<String>();
							while (true) {

								// try {
								// Thread.sleep(this.timedelay * 2);
								// } catch (InterruptedException e1) {
								// // TODO Auto-generated catch block
								// e1.printStackTrace();
								// }

								// Store the raw page source
								String src = driver.getPageSource();

								Pattern p = Pattern
										.compile("(?is)<table class=\"data-table.*?</table>");
								Matcher m = p.matcher(src);
								while (m.find()) {
									src = m.group();
								}

								System.out
										.println("\t\tGetting page source (table hashcode= "
												+ src.hashCode() + ")");

								seen.add(src);

								try {

									WebElement we = driver.findElement(By
											.cssSelector("a.next-link"));
									we.click();
									try {
										Thread.sleep(this.timedelay);
									} catch (InterruptedException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								} catch (Exception e) {
									// e.printStackTrace();
									break;
								}
							}

							List<Map<String, String>> data = new ArrayList<Map<String, String>>();

							int current = 0;
							for (String src : seen) {
								try {
									current++;
									File rawsource = new File(
											this.getOutputDirectory() + "raw"
													+ File.separator
													+ revenueType
													+ File.separator
													+ albumTitle
													+ File.separator + "Page"
													+ current + ".raw.html");
									rawsource.getParentFile().mkdirs();
									PrintStream ps = new PrintStream(rawsource);
									ps.println(src);
									ps.close();

									System.out.println(">>\t\tpage # "
											+ current);

									// org.w3c.dom.Document docw3c =
									// loadXMLFromString(src);
								} catch (Exception ex) {
									ex.printStackTrace();
								}

								try {
									Document doc = Jsoup.parse(src);

									Elements elements = doc
											.select("table.data-table thead tr th");
									List<String> headers = new ArrayList<String>();
									for (Element el : elements) {
										headers.add(el.text().replace(", ", ""));
									}

									elements = doc
											.select("table.data-table tbody tr");
									for (Element rowel : elements) {
										if (rowel.text().matches(
												"(?is).*PAGE TOTAL.*")) {
											continue;
										}
										Map<String, String> row = new HashMap<String, String>();

										// Add revenue type field
										row.put("REVENUETYPE",
												revenueType.toUpperCase());

										// Default partner value
										row.put("USER", this.getUsername()
												.toUpperCase());

										// Default partner value
										row.put("PARTNER", "CDBaby");

										// Error checking and formatting of date
										// data
										int i = 0;
										for (Element el : rowel.select("td")) {
											try {
												String hdr = headers.get(i++);

												String val = el.text().trim();

												if (hdr.toLowerCase().matches(
														"sales")
														|| hdr.toLowerCase()
																.matches("date")
														|| hdr.toLowerCase()
																.matches(
																		"report")) {
													val = val.replace("Jan ",
															"1/");
													val = val.replace("Feb ",
															"2/");
													val = val.replace("Mar ",
															"3/");
													val = val.replace("Apr ",
															"4/");
													val = val.replace("May ",
															"5/");
													val = val.replace("Jun ",
															"6/");
													val = val.replace("Jul ",
															"7/");
													val = val.replace("Aug ",
															"8/");
													val = val.replace("Sep ",
															"9/");
													val = val.replace("Oct ",
															"10/");
													val = val.replace("Nov ",
															"11/");
													val = val.replace("Dec ",
															"12/");
													val = val
															.replace(", ", "/");
												}
												row.put(hdr.toUpperCase(), val);
											} catch (Exception ex2) {
												ex2.printStackTrace();
											}
										}

										// Cleanup and extra formatting
										if (row.get("DATE") != null) {
											row.put("SALEDATE", row.get("DATE"));
										}
										if (row.get("SALES") != null) {
											String dstr = row.get("SALES")
													.trim();
											dstr = dstr.replace("/", "/01/");
											row.put("SALEDATE", dstr);
										}
										if (row.get("SALEDATE") != null) {
											String dstr = row.get("SALEDATE");

											Matcher m = Pattern.compile(
													"(\\d+)/(\\d+)/(\\d+)")
													.matcher(dstr);
											double yyyy = 0, mm = 0, dd = 0;
											if (m.find()) {
												yyyy = Double.parseDouble(m
														.group(3));
												mm = Double.parseDouble(m
														.group(1));
												dd = Double.parseDouble(m
														.group(2));
												TimeStamp date = new TimeStamp(
														yyyy, mm, dd);
												row.put("SALEDATE-MS1970",
														(long) date.toMS1970()
																+ "");
											}
										}

										if (row.get("REVENUETYPE").matches(
												"DIGITAL-DISTRIBUTION-SALES")
												&& row.get("TYPE") != null
												&& row.get("TYPE").matches(
														"(?i)Sync - Y")) {
											// No-OP to manage their
											// duplication of data...
										} else {
											completeData.add(row);
											data.add(row);
										}
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}

							// System.out.println("");
							this.writeOutputs(
									this.getOutputDirectory() + albumTitle
											+ File.separator + revenueType,
									data);

							// driver.get(arg0)
							driver.findElement(By.partialLinkText("overview"))
									.click();
							// try {
							// Thread.sleep(this.timedelay);
							// } catch (InterruptedException e) {
							// // TODO Auto-generated catch block
							// e.printStackTrace();
							// }
						} else {
							break;
						}
					}

					if (this.dosql) {
						// FINISH THIS SECTION....
					}

					System.out.println("this is a test");

					this.writeOutputs(this.getOutputDirectory() + "Complete",
							completeData);

					this.writeOutputs(this.getOutputDirectory() + "ByPartner",
							CDBabySalesDataCrawler.sum(CDBabySalesDataCrawler
									.groupBy(completeData, "PARTNER"),
									"PAYABLE"));

					this.writeOutputs(this.getOutputDirectory() + "ByAlbum",
							CDBabySalesDataCrawler.sum(CDBabySalesDataCrawler
									.groupBy(completeData, "ALBUM"), "PAYABLE"));

					this.writeOutputs(
							this.getOutputDirectory() + "ByArtist",
							CDBabySalesDataCrawler.sum(CDBabySalesDataCrawler
									.groupBy(completeData, "ARTIST"), "PAYABLE"));

					this.writeOutputs(this.getOutputDirectory() + "BySong",
							CDBabySalesDataCrawler.sum(CDBabySalesDataCrawler
									.groupBy(completeData, "ALBUM", "ARTIST",
											"SONG"), "PAYABLE"));

					this.writeOutputs(this.getOutputDirectory() + "BySaleDate",
							CDBabySalesDataCrawler.sum(CDBabySalesDataCrawler
									.groupBy(completeData, "SALEDATE"),
									"PAYABLE"));

					/*
					 * //
					 * ///////////////////////////////////////////////////////
					 * ////// // Now lets compute some totals... //
					 * /////////////
					 * ////////////////////////////////////////////////
					 * Map<String, Double> albums = new HashMap<String,
					 * Double>(); Map<String, Double> partners = new
					 * HashMap<String, Double>(); Map<String, Double> artists =
					 * new HashMap<String, Double>(); Map<String, Double> songs
					 * = new HashMap<String, Double>(); Map<String, Double>
					 * dates = new HashMap<String, Double>();
					 * 
					 * for (Map<String, String> row : els.values()) { try {
					 * String date = row.get("DATE"); if (date == null ||
					 * date.isEmpty()) { date = row.get("SALES"); } // int year
					 * = //
					 * Integer.parseInt(date.substring(date.length()-4,date.
					 * length())); double payable = Double.parseDouble(row
					 * .get("PAYABLE")); String albumTitle =
					 * row.get("albumTitle"); String partner =
					 * row.get("PARTNER"); String song = row.get("SONG"); String
					 * artist = row.get("ARTIST");
					 * 
					 * if (albums.containsKey(albumTitle)) {
					 * albums.put(albumTitle, payable + albums.get(albumTitle));
					 * } else { albums.put(albumTitle, payable); }
					 * 
					 * if (partners.containsKey(partner)) {
					 * partners.put(partner, payable + partners.get(partner)); }
					 * else { partners.put(partner, payable); }
					 * 
					 * if (artists.containsKey(artist)) { artists.put(artist,
					 * payable + artists.get(partner)); } else {
					 * artists.put(artist, payable); }
					 * 
					 * if (songs.containsKey(song)) { songs.put(song, payable +
					 * songs.get(song)); } else { songs.put(song, payable); }
					 * 
					 * if (dates.containsKey(date)) { dates.put(date, payable +
					 * songs.get(date)); } else { dates.put(date, payable); }
					 * 
					 * } catch (Exception oops) {
					 * 
					 * } }
					 * 
					 * 
					 * this.writeOutputs( this.getOutputDirectory() +
					 * "albumTotals", albums);
					 * this.writeOutputs(this.getOutputDirectory() +
					 * "artistTotals", artists);
					 * this.writeOutputs(this.getOutputDirectory() +
					 * "partnerTotals", partners);
					 * this.writeOutputs(this.getOutputDirectory() +
					 * "songTotals", songs);
					 * this.writeOutputs(this.getOutputDirectory() +
					 * "dateTotals", dates);
					 */

				} catch (Exception ex) {
					System.out.println("Exception Caught ... parsing aborted.");
				}

				System.out.print("Crawling Completed\n");
				driver.quit();

			} else {
				System.out.print("Could not instantiate web driver object");
			}
		}
		if (this.isDoFtp()) {
			for (int i = 0; i < 3; i++) {
				try {
					System.out
							.print("Attempting to Transmit Results via FTP\n");
					this.sendFTP();
					System.out.println("FTP Transmission Complete\n");
					break;
				} catch (Exception e) {
					System.out.print("Error performing FTP transmission\n");
					e.printStackTrace();
				}
			}
		}
		System.out.println("Crawling Completed.");
	}

	private void sendFiles(File dir, FTPClient ftp) throws Exception {
		String cd = ftp.printWorkingDirectory();

		for (File f : dir.listFiles()) {
			if (f.getName().startsWith(".")) {
				continue;
			}
			if (f.isDirectory()) {
				boolean exists = ftp.changeWorkingDirectory(f.getName());
				if (!exists) {
					ftp.makeDirectory(f.getName());
					exists = ftp.changeWorkingDirectory(f.getName());
					if (!exists) {
						throw new Exception("Couldn't make directory");
					}
				}

				this.sendFiles(f, ftp);

				ftp.changeToParentDirectory();

			} else {
				InputStream input = new FileInputStream(f);
				ftp.storeFile(f.getName(), input);
				input.close();
				System.out.println("\tUploaded " + cd + "/" + f.getName());
			}
		}
	}

	private void sendFTP() throws Exception {

		FTPClient ftp = new FTPClient();

		// Per:
		// http://malsserver.blogspot.com/2009/08/ftpconnectionclosedexception-connection.html
		// http://getfundas746.blogspot.in/2012/12/orgapachecommonsnetftpftpconnectionclos.html
		ftp.enterLocalPassiveMode();

		FTPClientConfig config = new FTPClientConfig();
		// change required options
		ftp.configure(config);

		int reply;
		ftp.connect(this.ftpServer, Integer.parseInt(this.ftpPort));
		System.out.println("\tConnected to " + this.ftpServer + " on "
				+ this.ftpPort);

		// After connection attempt, you should check the reply code to
		// verify success.
		reply = ftp.getReplyCode();

		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			System.err.println("\tFTP server refused connection.");
			// System.exit(1);
			return;
		}

		boolean in = ftp.login(this.ftpUsername, this.ftpPassword);
		if (!in) {
			ftp.logout();
			System.err.println("\tInvalid username or password.");
			return;
		}

		System.out.println("\tRemote system is " + ftp.getSystemType());

		boolean binaryTransfer = false;
		if (binaryTransfer) {
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
		} else {
			// in theory this should not be necessary as servers should
			// default to ASCII but they don't all do so - see NET-500
			ftp.setFileType(FTP.ASCII_FILE_TYPE);
		}

		// Use passive mode as default because most of us are
		// behind firewalls these days.
		boolean localActive = false;
		if (localActive) {
			ftp.enterLocalActiveMode();
		} else {
			ftp.enterLocalPassiveMode();
		}

		// Make sure directory is formatted properly...
		if (this.ftpDirectory == null || this.ftpDirectory.isEmpty()) {
			this.ftpDirectory = "";
		}

		// Make sure correct seperator format...
		this.ftpDirectory = this.ftpDirectory.replace(File.separator, "/");

		// Make sure trailing slash exists
		if (!this.ftpDirectory.endsWith("/")) {
			this.ftpDirectory += "/";
		}

		File dir = new File(this.getOutputDirectory());
		ftp.changeWorkingDirectory(this.ftpDirectory);

		this.sendFiles(dir, ftp);
		ftp.noop(); // check that control connection is working OK

		ftp.logout();

	}

	public void setDocsv(boolean docsv) {
		this.doCsv = docsv;
	}

	public void setDoCsv(boolean doCsv) {
		this.doCsv = doCsv;
	}

	public void setDoFtp(boolean doFtp) {
		this.doFtp = doFtp;
	}

	public void setDohtml(boolean dohtml) {
		this.doHtml = dohtml;
	}

	public void setDoHtml(boolean doHtml) {
		this.doHtml = doHtml;
	}

	public void setDoxlsx(boolean doxlsx) {
		this.doXlsx = doxlsx;
	}

	public void setDoXlsx(boolean doXlsx) {
		this.doXlsx = doXlsx;
	}

	public void setDoxml(boolean doxml) {
		this.doXml = doxml;
	}

	public void setDoXml(boolean doXml) {
		this.doXml = doXml;
	}

	public void setDrivername(String drivername) {
		this.drivername = drivername;
	}

	public void setFtpDirectory(String ftpDirectory) {
		this.ftpDirectory = ftpDirectory;
	}

	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}

	public void setFtpPort(String ftpPort) {
		this.ftpPort = ftpPort;
	}

	public void setFtpServer(String ftpServer) {
		this.ftpServer = ftpServer;
	}

	public void setFtpUsername(String ftpUsername) {
		this.ftpUsername = ftpUsername;
	}

	public void setOutputDirectory(String dirPrefix) {
		this.outputDirectory = dirPrefix;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSheetTitle(String sheetTitle) {
		this.sheetTitle = sheetTitle;
	}

	public void setStartpage(String startpage) {
		this.startpage = startpage;
	}

	public void setTimedelay(int timedelay) {
		this.timedelay = timedelay;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public <U, T> void writeOutputs(String filename, Collection<Map<U, T>> rows) {
		System.out.print(">>Writing outputs for "
				+ new File(filename).getName() + "\n");
		System.out.flush();

		if (this.doXml) {
			CDBabySalesDataCrawler.writeXMLRows(filename + ".xml", rows);
		}

		if (this.doHtml) {
			CDBabySalesDataCrawler.writeHTMLRows(filename + ".html", rows);
		}

		if (this.doCsv) {
			CDBabySalesDataCrawler.writeCSVRows(filename + ".csv", rows);
		}

		if (this.doXlsx) {
			CDBabySalesDataCrawler.writeXLSXRows(filename + ".xlsx",
					"Complete", rows);
		}
	}

	public <U, T> void writeOutputs(String filename, Collection<U> x,
			Collection<T> y) {
		this.writeOutputs(filename, x.toArray(), y.toArray());
	}

	public <U, T> void writeOutputs(String filename, Map<U, T> map) {
		ArrayList<U> keys = new ArrayList<U>();
		ArrayList<T> values = new ArrayList<T>();
		for (Entry<U, T> entry : map.entrySet()) {
			keys.add(entry.getKey());
			values.add(entry.getValue());
		}
		this.writeOutputs(filename, keys, values);
	}

	public <U, T> void writeOutputs(String filename, U[] x, T[] y) {
		List<Map<U, T>> data = new ArrayList<Map<U, T>>();
		Map<U, T> map = new HashMap<U, T>();
		for (int i = 0; i < x.length; i++) {
			map.put(x[i], y[i]);
		}
		data.add(map);
		this.writeOutputs(filename, data);
	}

}
