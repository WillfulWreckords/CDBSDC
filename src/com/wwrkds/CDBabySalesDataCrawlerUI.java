package com.wwrkds;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.wwrkds.license.LicenseManager;

public class CDBabySalesDataCrawlerUI {

	static class DirListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			int returnVal = CDBabySalesDataCrawlerUI.dirc
					.showOpenDialog(CDBabySalesDataCrawlerUI.frame
							.getContentPane());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = CDBabySalesDataCrawlerUI.dirc.getSelectedFile();
				if (!file.isDirectory()) {
					file = file.getParentFile();
				}
				CDBabySalesDataCrawlerUI.dirField.setText(file
						.getAbsolutePath());

				// This is where a real application would open the file.
				// log.append("Opening: " + file.getName() + "." + newline);
			} else {
				// log.append("Open command cancelled by user." + newline);
			}
		}
	}

	static class GoListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			// Get all the fields snd update local variables...
			CDBabySalesDataCrawlerUI.getGUIFieldData();

			CDBabySalesDataCrawlerUI.btn.setEnabled(false);

			CDBabySalesDataCrawlerUI.go();

			CDBabySalesDataCrawlerUI.btn.setEnabled(true);
		}
	}

	static JButton btn, fbtn;

	static JFileChooser dirc;

	static String directory = "", ftpDirectory = "", password = null,
			ftpPassword = null, timeout = "15", username = null,
			ftpUsername = null, ftpPort = "21", browser = "firefox",
			ftpServer = null;
	static boolean doauto = false;

	private static boolean docsv = false, doxml = false, doxlsx = false,
			dohtml = true, doftp = false;
	static JCheckBox docsvBox, dohtmlBox, doxlsxBox, doxmlBox, doftpBox;
	static JRadioButton firefoxButton, ieButton, chromeButton, safariButton;
	static JFrame frame;
	static ButtonGroup group;
	static JTextArea jta;
	static JScrollPane scrollPane;
	static JTextField userField, passField, timeoutField, dirField,
			ftpUserField, ftpPassField, ftpDirField, ftpServerField,
			ftpPortField;

	static String version = "v1.0.1";

	static void getGUIFieldData() {

		CDBabySalesDataCrawlerUI.username = CDBabySalesDataCrawlerUI.userField
				.getText();
		CDBabySalesDataCrawlerUI.password = CDBabySalesDataCrawlerUI.passField
				.getText();
		CDBabySalesDataCrawlerUI.directory = CDBabySalesDataCrawlerUI.dirField
				.getText();
		CDBabySalesDataCrawlerUI.timeout = CDBabySalesDataCrawlerUI.timeoutField
				.getText();
		CDBabySalesDataCrawlerUI.ftpUsername = CDBabySalesDataCrawlerUI.ftpUserField
				.getText();
		CDBabySalesDataCrawlerUI.ftpPassword = CDBabySalesDataCrawlerUI.ftpPassField
				.getText();
		CDBabySalesDataCrawlerUI.ftpDirectory = CDBabySalesDataCrawlerUI.ftpDirField
				.getText();
		CDBabySalesDataCrawlerUI.ftpPort = CDBabySalesDataCrawlerUI.ftpPortField
				.getText();
		CDBabySalesDataCrawlerUI.ftpServer = CDBabySalesDataCrawlerUI.ftpServerField
				.getText();

		CDBabySalesDataCrawlerUI.doxml = CDBabySalesDataCrawlerUI.doxmlBox
				.isSelected();
		CDBabySalesDataCrawlerUI.dohtml = CDBabySalesDataCrawlerUI.dohtmlBox
				.isSelected();
		CDBabySalesDataCrawlerUI.doxlsx = CDBabySalesDataCrawlerUI.doxlsxBox
				.isSelected();
		CDBabySalesDataCrawlerUI.docsv = CDBabySalesDataCrawlerUI.docsvBox
				.isSelected();
		CDBabySalesDataCrawlerUI.doftp = CDBabySalesDataCrawlerUI.doftpBox
				.isSelected();

		if (CDBabySalesDataCrawlerUI.firefoxButton.isSelected()) {
			CDBabySalesDataCrawlerUI.browser = "firefox";
		} else if (CDBabySalesDataCrawlerUI.chromeButton.isSelected()) {
			CDBabySalesDataCrawlerUI.browser = "chrome";
		} else if (CDBabySalesDataCrawlerUI.safariButton.isSelected()) {
			CDBabySalesDataCrawlerUI.browser = "safari";
		} else if (CDBabySalesDataCrawlerUI.ieButton.isSelected()) {
			CDBabySalesDataCrawlerUI.browser = "ie";
		}
	}

	static void getPrefs() {
		Class<?> cls = com.wwrkds.CDBabySalesDataCrawler.class;
		// User and system preferences
		Preferences userRoot = Preferences.userRoot();
		Preferences sysRoot = Preferences.systemRoot();
		Preferences userPrefs = userRoot.node(cls.getCanonicalName());
		CDBabySalesDataCrawlerUI.username = userPrefs.get("username",
				System.getProperty("user.name"));
		CDBabySalesDataCrawlerUI.password = userPrefs.get("password", "");
		CDBabySalesDataCrawlerUI.directory = userPrefs.get("directory",
				System.getProperty("user.home") + "/Documents/CDBSDC/");
		CDBabySalesDataCrawlerUI.timeout = userPrefs.get("timeout", "1");
		CDBabySalesDataCrawlerUI.ftpDirectory = userPrefs.get("ftpDir", "");
		CDBabySalesDataCrawlerUI.ftpUsername = userPrefs.get("ftpUser",
				System.getProperty("user.name"));
		CDBabySalesDataCrawlerUI.ftpPassword = userPrefs.get("ftpPass", "");
		CDBabySalesDataCrawlerUI.ftpPort = userPrefs.get("ftpPort", "21");
		CDBabySalesDataCrawlerUI.ftpServer = userPrefs.get("ftpServer", "");
		CDBabySalesDataCrawlerUI.docsv = Boolean.parseBoolean(userPrefs.get(
				"docsv", "false"));
		CDBabySalesDataCrawlerUI.doxml = Boolean.parseBoolean(userPrefs.get(
				"doxml", "false"));
		CDBabySalesDataCrawlerUI.dohtml = Boolean.parseBoolean(userPrefs.get(
				"dohtml", "true"));
		CDBabySalesDataCrawlerUI.doxlsx = Boolean.parseBoolean(userPrefs.get(
				"doxlsx", "false"));
		CDBabySalesDataCrawlerUI.doftp = Boolean.parseBoolean(userPrefs.get(
				"doftp", "false"));
		CDBabySalesDataCrawlerUI.browser = userPrefs.get("browser", "firefox");

	}

	static void go() {

		// Set the user preferences...
		CDBabySalesDataCrawlerUI.setPrefs();

		if (!CDBabySalesDataCrawlerUI.directory.endsWith(File.separator)) {
			CDBabySalesDataCrawlerUI.directory += File.separator;
		}

		if (CDBabySalesDataCrawlerUI.username != null
				&& !CDBabySalesDataCrawlerUI.username.isEmpty()
				&& CDBabySalesDataCrawlerUI.password != null
				&& !CDBabySalesDataCrawlerUI.password.isEmpty()) {
			System.out.print("Processing...\n");
			System.out.flush();

			String[] args2 = { "-u", CDBabySalesDataCrawlerUI.username, "-p",
					CDBabySalesDataCrawlerUI.password, "-o",
					CDBabySalesDataCrawlerUI.directory };

			int to = Integer.parseInt(CDBabySalesDataCrawlerUI.timeout) * 1000;
			CDBabySalesDataCrawler crawler = new CDBabySalesDataCrawler(
					CDBabySalesDataCrawlerUI.username,
					CDBabySalesDataCrawlerUI.password,
					CDBabySalesDataCrawlerUI.directory, "CD Baby Sales Data",
					to);
			crawler.setDocsv(CDBabySalesDataCrawlerUI.docsv);
			crawler.setDoxml(CDBabySalesDataCrawlerUI.doxml);
			crawler.setDohtml(CDBabySalesDataCrawlerUI.dohtml);
			crawler.setDoxlsx(CDBabySalesDataCrawlerUI.doxlsx);
			crawler.setDoFtp(CDBabySalesDataCrawlerUI.doftp);
			crawler.setFtpServer(CDBabySalesDataCrawlerUI.ftpServer);
			crawler.setFtpPort(CDBabySalesDataCrawlerUI.ftpPort);
			crawler.setFtpDirectory(CDBabySalesDataCrawlerUI.ftpDirectory);
			crawler.setFtpUsername(CDBabySalesDataCrawlerUI.ftpUsername);
			crawler.setFtpPassword(CDBabySalesDataCrawlerUI.ftpPassword);

			crawler.setDrivername(CDBabySalesDataCrawlerUI.browser);
			crawler.start();

		} else {
			System.out.print("Error: username and password must be defined.\n");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Check License...
		Class<?> cls = com.wwrkds.CDBabySalesDataCrawler.class;
		LicenseManager manager = new LicenseManager(cls, "CDBSDC-ZIP",
				"http://willfulwreckords.com/Store/",
				"http://willfulwreckords.com/Software/license/");

		boolean nogui = false;
		for (String arg : args) {
			if (arg.contentEquals("-nogui")) {
				nogui = true;
			}
		}

		if (nogui) {
			CDBabySalesDataCrawlerUI.getPrefs();
			CDBabySalesDataCrawlerUI.go();
		} else {
			CDBabySalesDataCrawlerUI.run();
		}
	}

	// Followings are The Methods that do the Redirect, you can simply Ignore
	// them.
	private static void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(byte[] b) throws IOException {
				this.write(b, 0, b.length);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				CDBabySalesDataCrawlerUI
						.updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(int b) throws IOException {
				CDBabySalesDataCrawlerUI.updateTextArea(String
						.valueOf((char) b));
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}

	public static void run() {

		// ////////////////////////////////////////////////////////////
		// Get System / User Preferences
		// ////////////////////////////////////////////////////////////
		CDBabySalesDataCrawlerUI.getPrefs();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		CDBabySalesDataCrawlerUI.frame = new JFrame("Sales Data Crawler");

		CDBabySalesDataCrawlerUI.jta = new JTextArea(22, 60);
		CDBabySalesDataCrawlerUI.jta.setLineWrap(true);
		CDBabySalesDataCrawlerUI.jta.setWrapStyleWord(true);
		CDBabySalesDataCrawlerUI.scrollPane = new JScrollPane(
				CDBabySalesDataCrawlerUI.jta);

		CDBabySalesDataCrawlerUI.frame.setSize(1024, 1024);
		CDBabySalesDataCrawlerUI.frame.getContentPane().setLayout(
				new BoxLayout(CDBabySalesDataCrawlerUI.frame.getContentPane(),
						BoxLayout.Y_AXIS));

		// /////////////////////////////////////////////////////////
		// Create the radio buttons.
		// /////////////////////////////////////////////////////////
		CDBabySalesDataCrawlerUI.firefoxButton = new JRadioButton("firefox");
		CDBabySalesDataCrawlerUI.ieButton = new JRadioButton("ie");
		CDBabySalesDataCrawlerUI.chromeButton = new JRadioButton("chrome");
		CDBabySalesDataCrawlerUI.safariButton = new JRadioButton("safari");

		/*
		 * // Check user prefs for which on should be selected String bwsr =
		 * userPrefs.get("browser", "firefox");
		 * CDBabySalesDataCrawlerUI.firefoxButton.setSelected(true);
		 * CDBabySalesDataCrawlerUI.chromeButton.setSelected(false);
		 * CDBabySalesDataCrawlerUI.ieButton.setSelected(false);
		 * CDBabySalesDataCrawlerUI.safariButton.setSelected(false); if
		 * (bwsr.contentEquals("firefox")) {
		 * CDBabySalesDataCrawlerUI.firefoxButton.setSelected(true); } if
		 * (bwsr.contentEquals("safari")) {
		 * CDBabySalesDataCrawlerUI.safariButton.setSelected(true); } if
		 * (bwsr.contentEquals("ie")) {
		 * CDBabySalesDataCrawlerUI.ieButton.setSelected(true); } if
		 * (bwsr.contentEquals("chrome")) {
		 * CDBabySalesDataCrawlerUI.chromeButton.setSelected(true); }
		 */

		// Group the radio buttons.
		CDBabySalesDataCrawlerUI.group = new ButtonGroup();
		CDBabySalesDataCrawlerUI.group
				.add(CDBabySalesDataCrawlerUI.firefoxButton);
		CDBabySalesDataCrawlerUI.group
				.add(CDBabySalesDataCrawlerUI.chromeButton);
		CDBabySalesDataCrawlerUI.group
				.add(CDBabySalesDataCrawlerUI.safariButton);
		CDBabySalesDataCrawlerUI.group.add(CDBabySalesDataCrawlerUI.ieButton);

		// /////////////////////////////////////////////////////////
		// Create the GO button
		// /////////////////////////////////////////////////////////
		CDBabySalesDataCrawlerUI.btn = new JButton("Get CDBaby Sales Data");
		CDBabySalesDataCrawlerUI.btn.addActionListener(new GoListener());
		CDBabySalesDataCrawlerUI.btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		CDBabySalesDataCrawlerUI.btn.setEnabled(true);

		CDBabySalesDataCrawlerUI.fbtn = new JButton("Choose Output Directory");
		CDBabySalesDataCrawlerUI.fbtn.addActionListener(new DirListener());
		CDBabySalesDataCrawlerUI.fbtn.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Create the username field
		CDBabySalesDataCrawlerUI.userField = new JTextField(15);
		CDBabySalesDataCrawlerUI.userField.setVisible(true);
		CDBabySalesDataCrawlerUI.userField.setEditable(true);
		CDBabySalesDataCrawlerUI.userField
				.setAlignmentX(Component.CENTER_ALIGNMENT);
		// CDBabySalesDataCrawlerUI.userField.setText(userPrefs.get("username",
		// System.getProperty("user.name")));

		// Create the password field
		CDBabySalesDataCrawlerUI.passField = new JPasswordField(15);
		CDBabySalesDataCrawlerUI.passField.setVisible(true);
		CDBabySalesDataCrawlerUI.passField.setEditable(true);
		CDBabySalesDataCrawlerUI.passField
				.setAlignmentX(Component.CENTER_ALIGNMENT);
		// CDBabySalesDataCrawlerUI.passField.setText(userPrefs
		// .get("password", ""));

		// Create the output directory field
		CDBabySalesDataCrawlerUI.dirc = new JFileChooser();
		CDBabySalesDataCrawlerUI.dirc
				.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		CDBabySalesDataCrawlerUI.dirc.setDialogType(JFileChooser.SAVE_DIALOG);
		CDBabySalesDataCrawlerUI.dirc.setApproveButtonText("Select");

		CDBabySalesDataCrawlerUI.dirField = new JTextField(15);
		CDBabySalesDataCrawlerUI.dirField.setVisible(false);
		CDBabySalesDataCrawlerUI.dirField.setEditable(false);
		CDBabySalesDataCrawlerUI.dirField
				.setAlignmentX(Component.CENTER_ALIGNMENT);
		// CDBabySalesDataCrawlerUI.dirField.setText(userPrefs.get("directory",
		// System.getProperty("user.home") + "/Documents/CDBSDC/"));

		// Create the timeout directory field
		CDBabySalesDataCrawlerUI.timeoutField = new JTextField(3);
		// CDBabySalesDataCrawlerUI.timeoutField.setText(userPrefs.get("timeout",
		// "1"));
		CDBabySalesDataCrawlerUI.timeoutField.setVisible(true);
		CDBabySalesDataCrawlerUI.timeoutField.setEditable(true);
		CDBabySalesDataCrawlerUI.timeoutField
				.setAlignmentX(Component.CENTER_ALIGNMENT);

		CDBabySalesDataCrawlerUI.doxmlBox = new JCheckBox();
		CDBabySalesDataCrawlerUI.doxmlBox.setVisible(true);
		// CDBabySalesDataCrawlerUI.doxmlBox.setSelected(Boolean
		// .parseBoolean(userPrefs.get("doxml", "false")));
		CDBabySalesDataCrawlerUI.doxmlBox
				.setAlignmentX(Component.CENTER_ALIGNMENT);

		CDBabySalesDataCrawlerUI.doxlsxBox = new JCheckBox();
		CDBabySalesDataCrawlerUI.doxlsxBox.setVisible(true);
		// CDBabySalesDataCrawlerUI.doxlsxBox.setSelected(Boolean
		// .parseBoolean(userPrefs.get("doxlsx", "false")));
		CDBabySalesDataCrawlerUI.doxlsxBox
				.setAlignmentX(Component.CENTER_ALIGNMENT);

		CDBabySalesDataCrawlerUI.docsvBox = new JCheckBox();
		CDBabySalesDataCrawlerUI.docsvBox.setVisible(true);
		// CDBabySalesDataCrawlerUI.docsvBox.setSelected(Boolean
		// .parseBoolean(userPrefs.get("docsv", "false")));
		CDBabySalesDataCrawlerUI.docsvBox
				.setAlignmentX(Component.CENTER_ALIGNMENT);

		CDBabySalesDataCrawlerUI.dohtmlBox = new JCheckBox();
		CDBabySalesDataCrawlerUI.dohtmlBox.setVisible(true);
		// CDBabySalesDataCrawlerUI.dohtmlBox.setSelected(Boolean
		// .parseBoolean(userPrefs.get("dohtml", "true")));
		CDBabySalesDataCrawlerUI.dohtmlBox
				.setAlignmentX(Component.CENTER_ALIGNMENT);

		// ////////////////////////////////////////////////////////////
		// FTP Settings
		// ////////////////////////////////////////////////////////////
		CDBabySalesDataCrawlerUI.ftpUserField = new JTextField(15);
		CDBabySalesDataCrawlerUI.ftpUserField.setVisible(true);
		CDBabySalesDataCrawlerUI.ftpUserField.setEditable(true);
		CDBabySalesDataCrawlerUI.ftpUserField
				.setAlignmentX(Component.CENTER_ALIGNMENT);
		// CDBabySalesDataCrawlerUI.ftpUserField.setText(userPrefs.get("ftpUser",
		// System.getProperty("user.name")));
		CDBabySalesDataCrawlerUI.ftpPassField = new JPasswordField(15);
		CDBabySalesDataCrawlerUI.ftpPassField.setVisible(true);
		CDBabySalesDataCrawlerUI.ftpPassField.setEditable(true);
		CDBabySalesDataCrawlerUI.ftpPassField
				.setAlignmentX(Component.CENTER_ALIGNMENT);
		// CDBabySalesDataCrawlerUI.ftpPassField.setText(userPrefs.get("ftpPass",
		// ""));
		CDBabySalesDataCrawlerUI.ftpServerField = new JTextField(15);
		CDBabySalesDataCrawlerUI.ftpServerField.setVisible(true);
		CDBabySalesDataCrawlerUI.ftpServerField.setEditable(true);
		CDBabySalesDataCrawlerUI.ftpServerField
				.setAlignmentX(Component.CENTER_ALIGNMENT);
		// CDBabySalesDataCrawlerUI.ftpServerField.setText(userPrefs.get(
		// "ftpServer", ""));
		CDBabySalesDataCrawlerUI.ftpPortField = new JTextField(4);
		// CDBabySalesDataCrawlerUI.ftpPortField.setText(userPrefs.get("ftpPort",
		// "21"));
		CDBabySalesDataCrawlerUI.ftpPortField.setVisible(true);
		CDBabySalesDataCrawlerUI.ftpPortField.setEditable(true);
		CDBabySalesDataCrawlerUI.ftpPortField
				.setAlignmentX(Component.CENTER_ALIGNMENT);
		CDBabySalesDataCrawlerUI.ftpDirField = new JTextField(15);
		CDBabySalesDataCrawlerUI.ftpDirField.setVisible(true);
		CDBabySalesDataCrawlerUI.ftpDirField.setEditable(true);
		CDBabySalesDataCrawlerUI.ftpDirField
				.setAlignmentX(Component.CENTER_ALIGNMENT);
		// CDBabySalesDataCrawlerUI.ftpDirField.setText(userPrefs
		// .get("ftpDir", ""));
		CDBabySalesDataCrawlerUI.doftpBox = new JCheckBox();
		CDBabySalesDataCrawlerUI.doftpBox.setVisible(true);
		// CDBabySalesDataCrawlerUI.doftpBox.setSelected(Boolean
		// .parseBoolean(userPrefs.get("doftp", "true")));
		CDBabySalesDataCrawlerUI.doftpBox
				.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Add components
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(
				CDBabySalesDataCrawlerUI.scrollPane);

		JPanel tf = new JPanel();
		tf.add(new JLabel("Use Browser: "));
		tf.add(CDBabySalesDataCrawlerUI.firefoxButton);
		tf.add(CDBabySalesDataCrawlerUI.chromeButton);
		tf.add(CDBabySalesDataCrawlerUI.safariButton);
		tf.add(CDBabySalesDataCrawlerUI.ieButton);
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(tf);

		JPanel uf = new JPanel();
		uf.add(new JLabel("Username: "));
		uf.add(CDBabySalesDataCrawlerUI.userField);
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(uf);

		JPanel pf = new JPanel();
		pf.add(new JLabel("Password: "));
		pf.add(CDBabySalesDataCrawlerUI.passField);
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(pf);

		JPanel of = new JPanel();
		of.add(CDBabySalesDataCrawlerUI.fbtn);
		of.add(CDBabySalesDataCrawlerUI.dirField);
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(of);

		tf = new JPanel();
		tf.add(new JLabel("Click Delay (s): "));
		tf.add(CDBabySalesDataCrawlerUI.timeoutField);
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(tf);

		tf = new JPanel();
		tf.add(new JLabel("Outputs: "));
		tf.add(CDBabySalesDataCrawlerUI.doxmlBox);
		tf.add(new JLabel("xml"));
		tf.add(CDBabySalesDataCrawlerUI.doxlsxBox);
		tf.add(new JLabel("xlsx"));
		tf.add(CDBabySalesDataCrawlerUI.docsvBox);
		tf.add(new JLabel("csv"));
		tf.add(CDBabySalesDataCrawlerUI.dohtmlBox);
		tf.add(new JLabel("html"));
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(tf);

		// ////////////////////////////////////////////////////////////
		// FTP
		// ////////////////////////////////////////////////////////////
		tf = new JPanel();
		tf.add(new JLabel("FTP Results to Remote Server: "));
		tf.add(CDBabySalesDataCrawlerUI.doftpBox);
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(tf);
		tf = new JPanel();
		tf.add(new JLabel("FTP Server: "));
		tf.add(CDBabySalesDataCrawlerUI.ftpServerField);
		tf.add(new JLabel("Port: "));
		tf.add(CDBabySalesDataCrawlerUI.ftpPortField);
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(tf);
		tf = new JPanel();
		tf.add(new JLabel("FTP Username: "));
		tf.add(CDBabySalesDataCrawlerUI.ftpUserField);
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(tf);
		tf = new JPanel();
		tf.add(new JLabel("FTP Password: "));
		tf.add(CDBabySalesDataCrawlerUI.ftpPassField);
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(tf);
		tf = new JPanel();
		tf.add(new JLabel("FTP Directory: "));
		tf.add(CDBabySalesDataCrawlerUI.ftpDirField);
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(tf);

		// ////////////////////////////////////////////////////////////
		// Set the gui fields to their defaults
		// ////////////////////////////////////////////////////////////
		CDBabySalesDataCrawlerUI.setGUIFieldData();

		// Finalize
		CDBabySalesDataCrawlerUI.frame.getContentPane().add(
				CDBabySalesDataCrawlerUI.btn);

		CDBabySalesDataCrawlerUI.frame
				.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		CDBabySalesDataCrawlerUI.frame.pack();
		CDBabySalesDataCrawlerUI.frame.setVisible(true);

		// Redirect system streams...
		CDBabySalesDataCrawlerUI.redirectSystemStreams();

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
		CDBabySalesDataCrawlerUI.scrollPane.getVerticalScrollBar().setValue(0);
	}

	static void setGUIFieldData() {
		CDBabySalesDataCrawlerUI.doxmlBox
				.setSelected(CDBabySalesDataCrawlerUI.doxml);
		CDBabySalesDataCrawlerUI.dohtmlBox
				.setSelected(CDBabySalesDataCrawlerUI.dohtml);
		CDBabySalesDataCrawlerUI.doftpBox
				.setSelected(CDBabySalesDataCrawlerUI.doftp);
		CDBabySalesDataCrawlerUI.doxlsxBox
				.setSelected(CDBabySalesDataCrawlerUI.doxlsx);
		CDBabySalesDataCrawlerUI.docsvBox
				.setSelected(CDBabySalesDataCrawlerUI.docsv);

		CDBabySalesDataCrawlerUI.userField
				.setText(CDBabySalesDataCrawlerUI.username);
		CDBabySalesDataCrawlerUI.passField
				.setText(CDBabySalesDataCrawlerUI.password);
		CDBabySalesDataCrawlerUI.dirField
				.setText(CDBabySalesDataCrawlerUI.directory);
		CDBabySalesDataCrawlerUI.dirc.setCurrentDirectory(new File(
				CDBabySalesDataCrawlerUI.directory));
		CDBabySalesDataCrawlerUI.timeoutField
				.setText(CDBabySalesDataCrawlerUI.timeout);
		CDBabySalesDataCrawlerUI.ftpUserField
				.setText(CDBabySalesDataCrawlerUI.ftpUsername);
		CDBabySalesDataCrawlerUI.ftpPassField
				.setText(CDBabySalesDataCrawlerUI.ftpPassword);
		CDBabySalesDataCrawlerUI.ftpServerField
				.setText(CDBabySalesDataCrawlerUI.ftpServer);
		CDBabySalesDataCrawlerUI.ftpPortField
				.setText(CDBabySalesDataCrawlerUI.ftpPort);
		CDBabySalesDataCrawlerUI.ftpDirField
				.setText(CDBabySalesDataCrawlerUI.ftpDirectory);

		String bwsr = CDBabySalesDataCrawlerUI.browser;
		CDBabySalesDataCrawlerUI.firefoxButton.setSelected(true);
		CDBabySalesDataCrawlerUI.chromeButton.setSelected(false);
		CDBabySalesDataCrawlerUI.ieButton.setSelected(false);
		CDBabySalesDataCrawlerUI.safariButton.setSelected(false);
		if (bwsr.contentEquals("firefox")) {
			CDBabySalesDataCrawlerUI.firefoxButton.setSelected(true);
		}
		if (bwsr.contentEquals("safari")) {
			CDBabySalesDataCrawlerUI.safariButton.setSelected(true);
		}
		if (bwsr.contentEquals("ie")) {
			CDBabySalesDataCrawlerUI.ieButton.setSelected(true);
		}
		if (bwsr.contentEquals("chrome")) {
			CDBabySalesDataCrawlerUI.chromeButton.setSelected(true);
		}
	}

	static void setPrefs() {
		Class<?> cls = com.wwrkds.CDBabySalesDataCrawler.class;
		// User and system preferences
		Preferences userRoot = Preferences.userRoot();
		Preferences userPrefs = userRoot.node(cls.getCanonicalName());
		userPrefs.put("username", CDBabySalesDataCrawlerUI.username);
		userPrefs.put("password", CDBabySalesDataCrawlerUI.password);
		userPrefs.put("directory", CDBabySalesDataCrawlerUI.directory);
		userPrefs.put("timeout", CDBabySalesDataCrawlerUI.timeout);
		userPrefs.put("ftpDir", CDBabySalesDataCrawlerUI.ftpDirectory);
		userPrefs.put("ftpUser", CDBabySalesDataCrawlerUI.ftpUsername);
		userPrefs.put("ftpPass", CDBabySalesDataCrawlerUI.ftpPassword);
		userPrefs.put("ftpPort", CDBabySalesDataCrawlerUI.ftpPort);
		userPrefs.put("ftpServer", CDBabySalesDataCrawlerUI.ftpServer);
		userPrefs.put("docsv", "" + CDBabySalesDataCrawlerUI.docsv);
		userPrefs.put("doxml", "" + CDBabySalesDataCrawlerUI.doxml);
		userPrefs.put("dohtml", "" + CDBabySalesDataCrawlerUI.dohtml);
		userPrefs.put("doxlsx", "" + CDBabySalesDataCrawlerUI.doxlsx);
		userPrefs.put("doftp", "" + CDBabySalesDataCrawlerUI.doftp);
		userPrefs.put("browser", CDBabySalesDataCrawlerUI.browser);
	}

	// The following codes set where the text get redirected. In this case,
	// jTextArea1
	private static void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				CDBabySalesDataCrawlerUI.jta.append(text);
			}
		});
	}

}
