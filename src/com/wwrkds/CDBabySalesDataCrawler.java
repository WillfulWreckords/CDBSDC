package com.wwrkds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.varia.NullAppender;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.wwrkds.license.LicenseManager;

public class CDBabySalesDataCrawler extends Thread {

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

	private boolean doFtp = false, doXml = true, doXlsx = true, doHtml = true,
			doCsv = true;
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

	public CDBabySalesDataCrawler() {

		LicenseManager manager = new LicenseManager(
				com.wwrkds.CDBabySalesDataCrawler.class, "CDBSDC-ZIP",
				"http://willfulwreckords.com/Store/",
				"http://willfulwreckords.com/Software/license/");

	}

	public CDBabySalesDataCrawler(String u, String p, String d, String s) {

		LicenseManager manager = new LicenseManager(
				com.wwrkds.CDBabySalesDataCrawler.class, "CDBSDC-ZIP",
				"http://willfulwreckords.com/Store/",
				"http://willfulwreckords.com/Software/license/");

		this.username = u;
		this.password = p;
		this.outputDirectory = d;
		this.sheetTitle = s;
	}

	public CDBabySalesDataCrawler(String u, String p, String d, String s,
			int timeout) {

		LicenseManager manager = new LicenseManager(
				com.wwrkds.CDBabySalesDataCrawler.class, "CDBSDC-ZIP",
				"http://willfulwreckords.com/Store/",
				"http://willfulwreckords.com/Software/license/");

		this.username = u;
		this.password = p;
		this.outputDirectory = d;
		this.sheetTitle = s;
		this.timedelay = timeout;
	}

	private boolean doCrawl() {
		return this.isDoCsv() | this.isDoHtml() | this.isDoXlsx()
				| this.isDoXlsx();
	}

	public String getDirPrefix() {
		return this.outputDirectory;
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

	public void parseArgs(String[] args) {

		for (int i = 0; i < args.length; i++) {
			if (args[i].toLowerCase().contentEquals("-username")) {
				this.setUsername(args[++i]);
			} else if (args[i].toLowerCase().contentEquals("-password")) {
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
				+ CDBabySalesDataCrawlerUI.version + "):\n");
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
				} else if (this.drivername.trim().toLowerCase()
						.contentEquals("iphone")) {
					driver = new org.openqa.selenium.iphone.IPhoneDriver();
				} else if (this.drivername.trim().toLowerCase()
						.contentEquals("android")) {
					driver = new org.openqa.selenium.android.AndroidDriver();
				}
			} catch (Exception e) {

			}

			if (driver != null) {
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

				try {
					driver.manage().deleteAllCookies();

					// Go to the CDBaby artist login page
					driver.get(this.startpage);

					WebElement query;

					// Enter username
					query = driver.findElement(By
							.id("ctl00_centerColumn_txtClientUsername"));
					query.clear();
					query.sendKeys(this.username);

					// Enter password
					query = driver.findElement(By
							.id("ctl00_centerColumn_txtClientPassword"));
					query.clear();
					query.sendKeys(this.password);

					// Click login button
					query = driver.findElement(By
							.id("ctl00_centerColumn_btnLoginArtist"));
					query.click();

					try {
						Thread.sleep(this.timedelay);
					} catch (InterruptedException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}

					query = driver.findElement(By
							.partialLinkText("Accounting overview"));
					query.click();

					try {
						Thread.sleep(this.timedelay);
					} catch (InterruptedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

					ArrayList<Integer> visited = new ArrayList<Integer>();
					while (true) {

						String albumTitle = "";
						// File file = new File(s+".csv");
						// PrintStream out= new PrintStream(file);
						// System.setOut(out);

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
								// Repace invalid file name elements
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

							// driver.findElement(By.cssSelector("select")).click();
							// driver.findElement(By.cssSelector("option[value=\"500\"]")).click();

							try {
								Thread.sleep(this.timedelay);
								WebElement select = driver.findElement(By
										.tagName("select"));
								List<WebElement> allOptions = select
										.findElements(By.tagName("option"));
								for (WebElement option : allOptions) {
									// System.out.println(String.format("Value is: %s",
									// option.getAttribute("value")));
									if (option.getText().contains("100")) {
										option.click();
									}
								}
							} catch (Exception ex3) {

							}

							try {
								Thread.sleep(this.timedelay);
							} catch (InterruptedException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
							List<String> data = new ArrayList<String>();
							Integer old = 0;

							System.out.print(">>\tCollecting table data\n");
							System.out.flush();

							List<String> headers = new ArrayList<String>();
							List<WebElement> welist = driver.findElements(By
									.cssSelector("table.data-table th"));
							for (WebElement we : welist) {
								headers.add(we.getText());
							}
							data.add(headers.toString().replace("[", "")
									.replace("]", ""));

							while (true) {
								try {
									Thread.sleep(this.timedelay);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

								Integer current = 1;
								try {
									current = Integer
											.parseInt(driver
													.findElement(
															By.cssSelector("div.page-numbers a.active"))
													.getText());
								} catch (Exception ex1) {

								}
								if (old == current) {
									break;
								}

								System.out.println(">>\t\tpage # " + current);

								// Get the table selectors
								List<WebElement> datarows = driver
										.findElements(By
												.cssSelector("table.data-table tbody tr td"));
								List<String> tdtext = new ArrayList<String>();

								for (WebElement datarow : datarows) {
									tdtext.add(datarow.getText().replace(", ",
											""));
								}

								String row = "";
								int k = 0;

								int N = headers.size();
								for (String str : tdtext) {

									// Make sure we strip any internal commas
									str = str.replace(",", "");

									row += str + " ";
									k++;
									if (k < N) {
										row += ", ";
									} else {
										// System.out.println(row);
										data.add(row);
										row = "";
										k = 0;
									}
								}

								try {
									old = Integer
											.parseInt(driver
													.findElement(
															By.cssSelector("div.page-numbers a.active"))
													.getText());
									WebElement we = driver.findElement(By
											.partialLinkText("Next"));
									we.click();

								} catch (Exception e) {
									break;
								}
							}

							// System.out.println("");

							String revenueType = "";
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

							if (this.doXml) {
								// Write to XML file ...
								System.out.print(">>Writing XML\n");
								System.out.flush();
								CDBabySalesDataCrawler.writeXML(
										this.outputDirectory + albumTitle
												+ File.separator + revenueType
												+ ".xml", data);
							}

							if (this.dosql) {
								// FINISH THIS SECTION....
								String sql = "";

								sql += "CREATE TABLE IF NOT EXISTS `cdb_project` ("
										+ "cdb_project_id NOT NULL auto_increment"
										+ "`project_artist` varchar(255), `project_title` varchar(255));\n";
								sql += "INSERT INTO `cdb_project` (";

								String tableName = "cdb_"
										+ revenueType.replace("-", "_")
												.toLowerCase();
								String[] h = data.get(0).split(",");

								// sql +=
								// "DROP TABLE IF EXISTS '"+tableName+"';\n";
								if (revenueType
										.matches("DIGITAL.+DISTRIBUTION.+SALES")
										&& h.length == 11) {

									sql += "CREATE TABLE IF NOT EXISTS `"
											+ tableName
											+ "` ("
											+ ""
											+ tableName
											+ "_id NOT NULL auto_increment"
											+ "`report` varchar(32), `sales` varchar(32), `partner` varchar(255), "
											+ "`artist` varchar(255), `album` varchar(255), `song` varchar(255), "
											+ "`cover` varchar(255), `type` varchar(255), `qty` varchar(32), "
											+ "`unit` varchar(255), `payable` varchar(255));\n";
									for (int row = 1; row < data.size(); row++) {
										String[] contents = data.get(row)
												.split(",");
										sql += "INSERT INTO `"
												+ tableName
												+ "` ("
												+ "`report`, `sales`, `partner`, "
												+ "`artist`, `album`, `song`, "
												+ "`cover`, `type`, `qty`, "
												+ "`unit`, `payable`) VALUES (";
										int k = 0;
										for (String c : contents) {
											if (k++ > 0) {
												sql += ", ";
											}
											sql += c;
										}
										sql += ");\n";
									}
								}
							}
							if (this.doHtml) {
								// Write to HTML file ...
								System.out.print(">>Writing HTML\n");
								System.out.flush();
								CDBabySalesDataCrawler.writeHTML(
										this.outputDirectory + albumTitle
												+ File.separator + revenueType
												+ ".html", data);
							}
							if (this.doCsv) {
								// Write to CSV file ...
								System.out.print(">>Writing CSV\n");
								System.out.flush();
								CDBabySalesDataCrawler.writeCSV(
										this.outputDirectory + albumTitle
												+ File.separator + revenueType
												+ ".csv", data);
							}
							if (this.doXlsx) {
								// Write to XLSX file ...
								System.out.print(">>Writing XLSX\n");
								System.out.flush();
								CDBabySalesDataCrawler.writeXLSX(
										this.outputDirectory + albumTitle
												+ File.separator + revenueType
												+ ".xlsx", this.sheetTitle,
										data);
							}

							// driver.get(arg0)
							driver.findElement(By.partialLinkText("overview"))
									.click();
							try {
								Thread.sleep(this.timedelay);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							break;
						}
					}
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
			System.out.print("Transmitting Results via FTP\n");
			try {
				this.sendFTP();
				System.out.println("FTP Transmission Complete\n");
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				System.out.print("Error performing FTP transmission\n");
				e.printStackTrace();
			}
		}
	}

	private void sendFiles(File dir, FTPClient ftp) {
		try {
			String cd = ftp.printWorkingDirectory();

			for (File f : dir.listFiles()) {
				if (f.getName().startsWith(".")) {
					continue;
				}
				if (f.isDirectory()) {
					try {
						boolean exists = ftp
								.changeWorkingDirectory(f.getName());
						if (!exists) {
							ftp.makeDirectory(f.getName());
							exists = ftp.changeWorkingDirectory(f.getName());
							if (!exists) {
								throw new Exception("Couldn't make directory");
							}
						}

						this.sendFiles(f, ftp);

						ftp.changeToParentDirectory();

					} catch (Exception e) {

					}

				} else {
					InputStream input = null;
					try {
						input = new FileInputStream(f);
						ftp.storeFile(f.getName(), input);
						input.close();
						System.out
								.println("Uploaded " + cd + "/" + f.getName());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendFTP() throws NumberFormatException, SocketException,
			IOException {

		FTPClient ftp = new FTPClient();
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

		File dir = new File(this.outputDirectory);
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

}
