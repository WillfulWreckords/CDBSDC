package com.wwrkds;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.varia.NullAppender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.seleniumhq.jetty9.util.Fields.Field;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wwrkds.datamodels.RankingOrder;
import com.wwrkds.datamodels.Row;
import com.wwrkds.datamodels.Table;
import com.wwrkds.datamodels.TimeStamp;

/**
 * This is the web crawler implementation class for crawling the CDBaby site and
 * downloading sales information. It is instantiated and called by the
 * CDBabySalesDataCrawlerUI class
 *
 * @author jonlareau, Willful Wreckords, LLC, willfulwreckords@gmail.com
 *
 */
public class CDBabySalesDataCrawler extends Thread {

	private static void clickButton(WebDriver driver, String buttonText) {
		WebElement parentElement = driver
				.findElement(By.partialLinkText(buttonText));
		Actions actionProvider = new Actions(driver);
		actionProvider.moveToElement(parentElement).perform();
		parentElement.click();
	}

	private static void clickButtonByClass(WebDriver driver,
			String buttonClassName) {
		WebElement parentElement = driver
				.findElement(By.className(buttonClassName));
		Actions actionProvider = new Actions(driver);
		actionProvider.moveToElement(parentElement).perform();
		parentElement.click();
	}

	/**
	 * Main function for running the crawler as a command line program. Use the
	 * "-h" option to print current help commands or see the {@see parseArgs}
	 * javadocs.
	 *
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

	public String toJson(){
		String ret = "{";
		for (java.lang.reflect.Field f : this.getClass().getDeclaredFields()){
			try {
				ret += "\""+f.getName()+"\":\""+ f.get(this) +"\"\n";
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ret += "}";
		return ret;
	}
	public String toString(){
		String ret = "{";
		for (java.lang.reflect.Field f : this.getClass().getDeclaredFields()){
			try {
				if (f.getName().matches("password")){
					ret += "\""+f.getName()+"\":\"************\"\n";
				}else{
					ret += "\""+f.getName()+"\":\""+ f.get(this) +"\"\n";
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ret += "}";
		return ret;
	}
	private boolean doFtp = false, doXml = false, doXlsx = true, doHtml = true,
			doCsv = true;
	private final boolean dosql = false;
	private String drivername = "firefox";
	private boolean exitAfterFinish = true;
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

	/**
	 * Default Constructor
	 */
	public CDBabySalesDataCrawler() {

	}

	/**
	 * Constructor
	 *
	 * @param u
	 *            username
	 * @param p
	 *            password
	 * @param d
	 *            output directory
	 * @param s
	 *            XLS sheet title default
	 */
	public CDBabySalesDataCrawler(String u, String p, String d, String s) {
		this.setUsername(u);
		this.setPassword(p);
		this.setOutputDirectory(d);
		this.setSheetTitle(s);
	}

	/**
	 * Constructor
	 *
	 * @param u
	 *            username
	 * @param p
	 *            password
	 * @param d
	 *            output directory
	 * @param s
	 *            XLS sheet title default
	 * @param t
	 *            timeout
	 */
	public CDBabySalesDataCrawler(String u, String p, String d, String s,
			int timeout) {
		this.setUsername(u);
		this.setPassword(p);
		this.setOutputDirectory(d);
		this.setSheetTitle(s);
		this.setTimedelay(timeout);
	}

	private boolean doCrawl() {
		return this.startpage.startsWith("http") && (this.isDoCsv()
				|| this.isDoHtml() || this.isDoXlsx() || this.isDoXlsx());
	}

	/**
	 * This is the function that dows the meat of the data collection / scapring
	 * from the CDBaby web content
	 *
	 * @return
	 */
	private Table getData() {

		Table completeData = new Table();

		if (this.doCrawl()) {
			// org.apache.log4j.BasicConfigurator.configure();
			org.apache.log4j.BasicConfigurator.configure(new NullAppender());

			// The Firefox driver supports javascript

			System.setProperty("webdriver.gecko.driver",
					"/Applications/CDBSDC.app/Contents/MacOS/geckodriver");

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
				}
			} catch (Exception e) {

			}

			if (driver != null) {
				// driver.manage().timeouts().implicitlyWait(10,
				// TimeUnit.SECONDS);
				driver.manage().timeouts().pageLoadTimeout(10,
						TimeUnit.SECONDS);
				driver.manage().timeouts().setScriptTimeout(10,
						TimeUnit.SECONDS);

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e3) {

				}

				((JavascriptExecutor) driver).executeScript("window.focus();");

				// String mainWindow = driver.getWindowHandle();

				// driver.switchTo().window(mainWindow);

				// driver.manage().window().maximize();

				try {
					// driver.manage().deleteAllCookies();

					// Go to the CDBaby artist login page
					driver.get(this.startpage);

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e3) {

					}

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

					try {
						Thread.sleep(this.timedelay);
					} catch (InterruptedException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}

					try {
						CDBabySalesDataCrawler.clickButton(driver,
								"Looks correct!");
					} catch (Exception ex) {

					}

					try {
						Thread.sleep(this.timedelay);
					} catch (InterruptedException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}

					driver.get(
							"https://members.cdbaby.com/Accounting/AccountOverview.aspx");

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

					List<String> urls = new ArrayList<String>();
					urls.add(
							"https://members.cdbaby.com/Accounting/DigitalArtistOverview.aspx");
					urls.add(
							"https://members.cdbaby.com/Accounting/SyncArtistOverview.aspx");
					urls.add(
							"https://members.cdbaby.com/Accounting/TotalCDBSales-Physical.aspx");
					urls.add(
							"https://members.cdbaby.com/Accounting/TotalCDBSales-Digital.aspx");
					Set<String> seen = new LinkedHashSet<String>();
					for (String url : urls) {
						seen.addAll(this.scrapeDataPages(driver, url));
					}

					Table data = new Table();

					int current = 0;
					for (String src : seen) {
						try {
							current++;
							File rawsource = new File(this.getOutputDirectory()
									+ "raw" + File.separator + "Page" + current
									+ ".raw.html");
							rawsource.getParentFile().mkdirs();
							PrintStream ps = new PrintStream(rawsource);
							ps.println(src);
							ps.close();

							System.out.println(">>\t\tpage # " + current);

							// org.w3c.dom.Document docw3c =
							// loadXMLFromString(src);
						} catch (Exception ex) {
							ex.printStackTrace();
						}

						try {
							Document doc = Jsoup.parse(src);
							Elements els1 = doc
									.select("div.section-title-953 h2");
							String revenueType = "";
							for (Element el : els1) {
								revenueType = el.text();
							}

							Elements elements = doc
									.select("table.data-table thead tr th");
							List<String> headers = new ArrayList<String>();
							for (Element el : elements) {
								headers.add(el.text().replace(", ", ""));
							}

							elements = doc.select("table.data-table tbody tr");
							for (Element rowel : elements) {
								if (rowel.text()
										.matches("(?is).*PAGE TOTAL.*")) {
									continue;
								}
								Row row = new Row();

								// Add revenue type field
								row.put("REVENUETYPE",
										revenueType.toUpperCase());

								// Default partner value
								row.put("USER",
										this.getUsername().toUpperCase());

								// Default partner value
								row.put("PARTNER", "CDBaby");

								// Error checking and formatting of date
								// data
								int i = 0;
								for (Element el : rowel.select("td")) {
									try {
										String hdr = headers.get(i++);

										String val = el.text().trim();

										if (hdr.toLowerCase().matches("sales")
												|| hdr.toLowerCase()
														.matches("date")
												|| hdr.toLowerCase()
														.matches("report")) {
											val = val.replace("Jan ", "1/");
											val = val.replace("Feb ", "2/");
											val = val.replace("Mar ", "3/");
											val = val.replace("Apr ", "4/");
											val = val.replace("May ", "5/");
											val = val.replace("Jun ", "6/");
											val = val.replace("Jul ", "7/");
											val = val.replace("Aug ", "8/");
											val = val.replace("Sep ", "9/");
											val = val.replace("Oct ", "10/");
											val = val.replace("Nov ", "11/");
											val = val.replace("Dec ", "12/");
											val = val.replace(", ", "/");
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
									String dstr = row.get("SALES").trim();
									dstr = dstr.replace("/", "/01/");
									row.put("SALEDATE", dstr);
								}
								if (row.get("SALEDATE") != null) {
									String dstr = row.get("SALEDATE");

									Matcher m = Pattern
											.compile("(\\d+)/(\\d+)/(\\d+)")
											.matcher(dstr);
									double yyyy = 0, mm = 0, dd = 0;
									if (m.find()) {
										yyyy = Double.parseDouble(m.group(3));
										mm = Double.parseDouble(m.group(1));
										dd = Double.parseDouble(m.group(2));
										TimeStamp date = new TimeStamp(yyyy, mm,
												dd);
										row.put("SALEDATE-MS1970",
												(long) date.toMS1970() + "");
									}
								}

								completeData.add(row);
								data.add(row);

							}
						} catch (Exception ex) {
							ex.printStackTrace();
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
		} else {
			try {
				File f = new File(this.getStartpage());
				if (f.exists() && f.getName().endsWith(".csv")) {
					// Load completeData from CSV
					Scanner scanner = new Scanner(f);
					ArrayList<String> headers = new ArrayList<String>();
					while (scanner.hasNextLine()) {
						String[] tokens = scanner.nextLine().split("\\s*,\\s*");
						if (headers.isEmpty()) {
							for (String token : tokens) {
								headers.add(token);
							}
						} else {
							Row row = new Row();
							for (int i = 0; i < tokens.length; i++) {
								String key = headers.get(i);
								String value = tokens[i];
								row.put(key, value);
							}
							completeData.add(row);
						}
					}
					scanner.close();

				} else if (f.exists() && f.getName().endsWith(".json")) {
					// Load from JSON
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return completeData;
	}

	/**
	 * Gets the output directory prefix
	 *
	 * @return
	 */
	public String getDirPrefix() {
		return this.getOutputDirectory();
	}

	/**
	 * Gets the selenium webdriver name
	 *
	 * @return
	 */
	public String getDrivername() {
		return this.drivername;
	}

	/**
	 * Gets the ftp directory variable
	 *
	 * @return
	 */
	public String getFtpDirectory() {
		return this.ftpDirectory;
	}

	/**
	 * Gets the FTP password
	 *
	 * @return
	 */
	public String getFtpPassword() {
		return this.ftpPassword;
	}

	/**
	 * gets the FTP port
	 *
	 * @return
	 */
	public String getFtpPort() {
		return this.ftpPort;
	}

	/**
	 * Gets the FTP server
	 *
	 * @return
	 */
	public String getFtpServer() {
		return this.ftpServer;
	}

	/**
	 * Gets the FTP username
	 *
	 * @return
	 */
	public String getFtpUsername() {
		return this.ftpUsername;
	}

	/**
	 * Gets the output directory
	 *
	 * @return
	 */
	public String getOutputDirectory() {
		return this.outputDirectory;
	}

	/**
	 * Gets the CDBaby password
	 *
	 * @return
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Gets the XLS Sheet title variable
	 *
	 * @return
	 */
	public String getSheetTitle() {
		return this.sheetTitle;
	}

	/**
	 * Gets the CDBaby start page url
	 *
	 * @return
	 */
	public String getStartpage() {
		return this.startpage;
	}

	/**
	 * gets the time delay between clicks
	 *
	 * @return
	 */
	public int getTimedelay() {
		return this.timedelay;
	}

	/**
	 * Gets the CDBaby username
	 *
	 * @return
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 *
	 * @return
	 */
	public boolean isDoCsv() {
		return this.doCsv;
	}

	/**
	 *
	 * @return
	 */
	public boolean isDoFtp() {
		return this.doFtp;
	}

	/**
	 *
	 * @return
	 */
	public boolean isDoHtml() {
		return this.doHtml;
	}

	/**
	 *
	 * @return
	 */
	public boolean isDoXlsx() {
		return this.doXlsx;
	}

	/**
	 *
	 * @return
	 */
	public boolean isDoXml() {
		return this.doXml;
	}

	public boolean isExitAfterFinish() {
		return this.exitAfterFinish;
	}

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
	 * <td>optional</td>
	 * <td>-dohtml [true|false]</td>
	 * <td></td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-doxml [true|false]</td>
	 * <td></td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-docsv [true|false]</td>
	 * <td></td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-doxlsx [true|false]</td>
	 * <td></td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-doftp [true|false]</td>
	 * <td></td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-ftpusername string</td>
	 * <td>string = The FTP username to use</td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-ftppassword string</td>
	 * <td>string = The FTP password to use</td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-ftpserver string</td>
	 * <td>string = The FTP server url to use</td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-ftpport string</td>
	 * <td>string = The FTP port to use</td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-ftpdirectory string</td>
	 * <td>string = The FTP directory to use</td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-driver [(firefox) | chrome | safari | ie]</td>
	 * <td>string = The WEBDriver to use to use</td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-sheeetitle string</td>
	 * <td>string = The Title of the xlsx sheet to use</td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-startpage string</td>
	 * <td>string = The url of the CDBaby login page</td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
	 * <td>-clickdelay string</td>
	 * <td>string = The amount of click delay to use</td>
	 * </tr>
	 *
	 * <tr>
	 * <td>optional</td>
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
				this.setDoHtml(Boolean.parseBoolean(args[++i]));
			} else if (args[i].toLowerCase().contentEquals("-doxml")) {
				this.setDoXml(Boolean.parseBoolean(args[++i]));
			} else if (args[i].toLowerCase().contentEquals("-doxlsx")) {
				this.setDoXlsx(Boolean.parseBoolean(args[++i]));
			} else if (args[i].toLowerCase().contentEquals("-docsv")) {
				this.setDoCsv(Boolean.parseBoolean(args[++i]));
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

	/**
	 * Prints the help information to system.out
	 */
	public void printHelp() {
		System.out.print(
				"*********************************************************************************************************************\n");
		System.out.print(
				"*********************************************************************************************************************\n");
		System.out.print("CD Baby Sales Data Crawler ("
				+ CDBabySalesDataCrawlerUI.getVersion() + "):\n");
		System.out.print(
				"\tWritten by: Jonathan J. Lareau - Willful Wreckords, LLC\n\twww.willfulwreckords.com\n\n");
		System.out.print(
				"DISCLAIMER:\n\tTHIS SOFTWARE IS PROVIDED \"AS IS\" AND WITHOUT ANY EXPRESSED OR IMPLIED WARRANTIES.\n");
		System.out.print(
				"*********************************************************************************************************************\n");
		System.out.print(
				"*********************************************************************************************************************\n");
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
		System.out.println(
				"\t-driver [firefox | chrome | safari | ie] : sets the browser to use.  The selected browser must have a \"standard\" install on your system.");
		System.out.println(
				"\t-outdir <local output directory> : sets local output directory");
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
		System.out.println(
				"\t-ftpdirectory <FTP directory> : sets FTP directory");
		System.out.println(
				"\t-startpage <url> : defaults to https://members.cdbaby.com/Login.aspx");
		System.out.println("\t-sheettitle <string> : sets xlxs sheet title");
		System.out.println(
				"\t-clickdelay <integer seconds> : added delay between crawler clicks for stability");
		System.out.println("\t-h : prints this help message");
	}

	@Override
	public void run() {

		System.out.println("Running CDBaby Sales Data Crawler:");
		System.out.println(this);
		System.out.println("");
		
		Table completeData = this.getData();

		System.out.println("Writing output ... ");

		boolean alsoWriteToCurrentDir = false;
		String currentDirName = null;
		if (!this.getOutputDirectory().matches("(?si).*current.*")) {
			currentDirName = new File(this.getOutputDirectory()).getParentFile()
					.getParentFile().getParentFile().getParentFile()
					.getAbsolutePath() + File.separator + "current"
					+ File.separator;
			alsoWriteToCurrentDir = true;
		}
		Table sumTotals = null;
		try {
			sumTotals = completeData.sumBy("ALBUM", "PAYABLE", "QTY.")
					.sortBy(RankingOrder.DESCENDING, "PAYABLE");
			this.writeOutputs(this.getOutputDirectory() + "AlbumTotals",
					sumTotals);
			if (alsoWriteToCurrentDir) {
				this.writeOutputs(currentDirName + "AlbumTotals", sumTotals);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			sumTotals = completeData.sumBy("PARTNER", "PAYABLE", "QTY.")
					.sortBy(RankingOrder.DESCENDING, "PAYABLE");
			this.writeOutputs(this.getOutputDirectory() + "PartnerTotals",
					sumTotals);
			if (alsoWriteToCurrentDir) {
				this.writeOutputs(currentDirName + "PartnerTotals", sumTotals);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {

			sumTotals = completeData.sumBy("SONG", "PAYABLE", "QTY.")
					.sortBy(RankingOrder.DESCENDING, "PAYABLE");
			this.writeOutputs(this.getOutputDirectory() + "SongTotals",
					sumTotals);
			if (alsoWriteToCurrentDir) {
				this.writeOutputs(currentDirName + "SongTotals", sumTotals);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {

			sumTotals = completeData.sumBy("ARTIST", "PAYABLE", "QTY.")
					.sortBy(RankingOrder.DESCENDING, "PAYABLE");
			this.writeOutputs(this.getOutputDirectory() + "ArtistTotals",
					sumTotals);
			if (alsoWriteToCurrentDir) {
				this.writeOutputs(currentDirName + "ArtistTotals", sumTotals);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String[] totals = { "SALEDATE-MS1970", "PAYABLE" };
		String[] byArtist = { "SALEDATE-MS1970", "PAYABLE", "ARTIST" };
		String[] byAlbum = { "SALEDATE-MS1970", "PAYABLE", "ALBUM" };
		// String[] byPartner = { "SALEDATE-MS1970", "PAYABLE", "PARTNER" };
		String[] artists = { "ARTIST", "PAYABLE", "TYPE=PIE" };
		String[] albums = { "ALBUM", "PAYABLE", "TYPE=PIE" };
		String[] partners = { "PARTNER", "PAYABLE", "TYPE=PIE" };
		try {

			this.writeOutputs(this.getOutputDirectory() + "Complete",
					completeData, artists, albums, partners, totals, byArtist,
					byAlbum);
			if (alsoWriteToCurrentDir) {
				this.writeOutputs(currentDirName + "Complete", completeData,
						artists, albums, partners, totals, byArtist, byAlbum);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Map<String, Table> grouped = null;
		try {

			grouped = completeData.groupBy("PARTNER");
			for (Entry<String, Table> entry : grouped.entrySet()) {
				this.writeOutputs(this.getOutputDirectory() + "byPartner"
						+ File.separator + entry.getKey(), entry.getValue(),
						totals);
				if (alsoWriteToCurrentDir) {
					this.writeOutputs(currentDirName + "byPartner"
							+ File.separator + entry.getKey(), entry.getValue(),
							totals);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			grouped = completeData.groupBy("ALBUM");
			for (Entry<String, Table> entry : grouped.entrySet()) {
				this.writeOutputs(this.getOutputDirectory() + "byAlbum"
						+ File.separator + entry.getKey(), entry.getValue(),
						totals);
				if (alsoWriteToCurrentDir) {
					this.writeOutputs(currentDirName + "byAlbum"
							+ File.separator + entry.getKey(), entry.getValue(),
							totals);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {

			grouped = completeData.groupBy("ARTIST");
			for (Entry<String, Table> entry : grouped.entrySet()) {
				this.writeOutputs(this.getOutputDirectory() + "byArtist"
						+ File.separator + entry.getKey(), entry.getValue(),
						totals);
				if (alsoWriteToCurrentDir) {
					this.writeOutputs(currentDirName + "byArtist"
							+ File.separator + entry.getKey(), entry.getValue(),
							totals);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {

			grouped = completeData.groupBy("ALBUM", "ARTIST", "SONG");
			for (Entry<String, Table> entry : grouped.entrySet()) {
				this.writeOutputs(this.getOutputDirectory() + "bySong"
						+ File.separator + entry.getKey(), entry.getValue(),
						totals);
				if (alsoWriteToCurrentDir) {
					this.writeOutputs(currentDirName + "bySong" + File.separator
							+ entry.getKey(), entry.getValue(), totals);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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

		if (this.exitAfterFinish) {
			System.out.println("Exiting Application.");
			System.exit(0);
		}
	}

	private Collection<String> scrapeDataPages(WebDriver driver, String url) {

		driver.get(url);

		try {
			Thread.sleep(1000);

			((JavascriptExecutor) driver)
					.executeScript("javascript:window.scrollTo(0, 0);");

			Thread.sleep(1000);
		} catch (Exception ex) {

		}

		Set<String> ret = new LinkedHashSet<String>();

		boolean set500 = false;

		try {
			Set<String> visited = new LinkedHashSet<String>();
			while (true) {
				List<WebElement> data = driver.findElements(
						By.cssSelector("table.data-table tr.hover-row"));
				if (data == null || data.isEmpty()) {
					if (!set500) {
						try {
							// Thread.sleep(this.timedelay);
							List<WebElement> allOptions = driver
									.findElements(By.tagName("option"));
							for (WebElement option : allOptions) {
								// System.out.println(String.format("Value is:
								// %s",
								// option.getAttribute("value")));
								if (option.getText().contains("500")) {
									option.click();
									set500 = true;
									Thread.sleep(500);
									break;
								}
							}

						} catch (Exception ex3) {

						}
					}

					ret.addAll(this.scrapeRows(driver));

					break;
				} else {
					Iterator<WebElement> it = data.iterator();
					while (it.hasNext()) {
						WebElement tr = it.next();
						String text = tr.getText();
						if (visited.contains(text)) {
							it.remove();
						}
					}
					if (data.isEmpty()) {
						break;
					}
					for (WebElement tr : data) {
						String text = tr.getText();
						if (!visited.contains(text)) {
							visited.add(tr.getText());
							tr.findElement(By.partialLinkText("view sales"))
									.click();

							Thread.sleep(3000);

							ret.addAll(this.scrapeRows(driver));

							driver.get(url);

							break;
						}
					}
				}
			}
		} catch (Exception ex) {

		}

		return ret;
	}

	private Collection<String> scrapeRows(WebDriver driver) {
		Set<String> list = new LinkedHashSet<String>();

		try {
			List<WebElement> allOptions = driver
					.findElements(By.tagName("option"));
			for (WebElement option : allOptions) {
				boolean is500 = option.getText().contains("500");
				boolean isSelected = option.isSelected();
				if (is500 && !isSelected) {
					option.click();
					Thread.sleep(5000);
					break;
				}
			}
		} catch (Exception ex3) {

		}

		try {
			Thread.sleep(1000);

			((JavascriptExecutor) driver)
					.executeScript("javascript:window.scrollTo(0, 0);");

			Thread.sleep(1000);
		} catch (Exception ex) {

		}
		// List<WebElement> chk = driver.findElements(By.cssSelector("table"));
		// if (chk == null || chk.isEmpty()) {
		// } else {
		String src = driver.getPageSource();
		list.add(src);
		// }

		/*
		 * List<WebElement> hrs = driver.findElements(By
		 * .cssSelector("table.data-table thead tr th")); List<String> headers =
		 * new ArrayList<String>(); for (WebElement hr : hrs) {
		 * headers.add(hr.getText()); } if (!headers.isEmpty()) {
		 * list.add(headers); }
		 *
		 * List<WebElement> rows = driver.findElements(By
		 * .cssSelector("table.data-table tbody tr.light-row")); for (WebElement
		 * row : rows) { List<WebElement> cols =
		 * row.findElements(By.cssSelector("td")); List<String> r = new
		 * ArrayList<String>(); for (WebElement col : cols) {
		 * r.add(col.getText()); } if (!r.isEmpty()) { list.add(r); } } rows =
		 * driver.findElements(By
		 * .cssSelector("table.data-table tbody tr.dark-row")); for (WebElement
		 * row : rows) { List<WebElement> cols =
		 * row.findElements(By.cssSelector("td")); List<String> r = new
		 * ArrayList<String>(); for (WebElement col : cols) {
		 * r.add(col.getText()); } if (!r.isEmpty()) { list.add(r); } }
		 */

		try {

			Thread.sleep(1000);

			((JavascriptExecutor) driver).executeScript(
					"javascript:window.scrollTo(0, document.body.scrollHeight);");

			Thread.sleep(1000);

			((JavascriptExecutor) driver)
					.executeScript("javascript:window.scrollBy(0,-500)");
			Thread.sleep(1000);

			WebElement nxt = driver.findElement(By.cssSelector(
					"table.pagination tbody tr td a.next-link:not(.aspNetDisabled)"));

			String js = "javascript:__doPostBack('ctl00$centerColumn$rptDDSales$ctl11$lnkPageNext','')";

			// WebElement nxt = driver.findElement(By.partialLinkText("Next"));

			// WebElement actv = driver.findElement(By
			// .cssSelector("div.page-numbers table tbody td a.active"));
			// String s1 = actv.getText();

			if (nxt != null) {

				nxt.click();

				Thread.sleep(1000);

				// WebElement actv2 = driver
				// .findElement(By
				// .cssSelector("div.page-numbers table tbody td a.active"));

				// String s2 = actv2.getText();
				// if (!s1.matches(s2)) {
				list.addAll(this.scrapeRows(driver));
				// }
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
		return list;

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
		System.out.println(
				"\tConnected to " + this.ftpServer + " on " + this.ftpPort);

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

	/**
	 *
	 * @param doCsv
	 */
	public void setDoCsv(boolean doCsv) {
		this.doCsv = doCsv;
	}

	/**
	 *
	 * @param doFtp
	 */
	public void setDoFtp(boolean doFtp) {
		this.doFtp = doFtp;
	}

	/**
	 *
	 * @param doHtml
	 */
	public void setDoHtml(boolean doHtml) {
		this.doHtml = doHtml;
	}

	/**
	 *
	 * @param doXlsx
	 */
	public void setDoXlsx(boolean doXlsx) {
		this.doXlsx = doXlsx;
	}

	/**
	 *
	 * @param doXml
	 */
	public void setDoXml(boolean doXml) {
		this.doXml = doXml;
	}

	/**
	 *
	 * @param drivername
	 */
	public void setDrivername(String drivername) {
		this.drivername = drivername;
	}

	public void setExitAfterFinish(boolean exitAfterFinish) {
		this.exitAfterFinish = exitAfterFinish;
	}

	/**
	 *
	 * @param ftpDirectory
	 */
	public void setFtpDirectory(String ftpDirectory) {
		this.ftpDirectory = ftpDirectory;
	}

	/**
	 *
	 * @param ftpPassword
	 */
	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}

	/**
	 *
	 * @param ftpPort
	 */
	public void setFtpPort(String ftpPort) {
		this.ftpPort = ftpPort;
	}

	/**
	 *
	 * @param ftpServer
	 */
	public void setFtpServer(String ftpServer) {
		this.ftpServer = ftpServer;
	}

	/**
	 *
	 * @param ftpUsername
	 */
	public void setFtpUsername(String ftpUsername) {
		this.ftpUsername = ftpUsername;
	}

	/**
	 *
	 * @param dirPrefix
	 */
	public void setOutputDirectory(String dirPrefix) {
		this.outputDirectory = dirPrefix;
	}

	/**
	 *
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 *
	 * @param sheetTitle
	 */
	public void setSheetTitle(String sheetTitle) {
		this.sheetTitle = sheetTitle;
	}

	/**
	 *
	 * @param startpage
	 */
	public void setStartpage(String startpage) {
		this.startpage = startpage;
	}

	/**
	 *
	 * @param timedelay
	 */
	public void setTimedelay(int timedelay) {
		this.timedelay = timedelay;
	}

	/**
	 *
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 *
	 * @param filename
	 * @param table
	 * @param plotArgs
	 */
	private void writeOutputs(String filename, Table table,
			String[]... plotArgs) {
		File file = new File(filename);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		System.out.print(">>Writing outputs for " + file.getName() + "\n");
		System.out.flush();

		if (this.doXml) {
			table.writeXML(filename + ".xml");
		}

		if (this.doCsv) {
			table.writeCSV(filename + ".csv");
		}

		if (this.doXlsx) {
			// table.writeXLSX(filename + ".xlsx", "sheet1");
		}

		if (this.dosql) {
			// TODO: FINISH THIS SECTION....
		}

		if (this.doHtml) {
			table.writeHTML(filename + ".html", plotArgs);
		}
	}

}
