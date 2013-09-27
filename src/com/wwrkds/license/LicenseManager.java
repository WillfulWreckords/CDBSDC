package com.wwrkds.license;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

/**
 * This is a very simplified version of a License Management utility. Your java
 * classes need only to simply instantiate a manager in order to perform the
 * checks. If any action needs to take place the manager will open the
 * appropriate dialog boxes as needed.
 * 
 * This assumes that there is a server side registration / validation service
 * running. The server side validation script will receive 3 URL parameters:
 * $product, $uuid, and $email. Upon success the validation script should return
 * the MD5 hash of the $uuid and $product variables.
 * 
 * Example PHP server-side validation script:
 * 
 * $email = $_REQUEST['email'];
 * 
 * $uuid = $_REQUEST['uuid'];
 * 
 * $product = $_REQUEST['product']; $success = true;
 * 
 * //TODO: Insert logic to check if user email has already purchased a valid
 * license
 * 
 * if ($success)
 * 
 * echo md5($uuid . $product);
 * 
 * else
 * 
 * echo "false";
 * 
 * 
 * @author jonlareau - willfulwreckords, LLC - contact@willfulwreckords.com
 * 
 */
public class LicenseManager {
	private static String md5(String input) {

		String md5 = null;

		if (null == input) {
			return null;
		}

		try {

			// Create MessageDigest object for MD5
			MessageDigest digest = MessageDigest.getInstance("MD5");

			// Update input string in message digest
			digest.update(input.getBytes(), 0, input.length());

			// Converts message digest value in base 16 (hex)
			md5 = new BigInteger(1, digest.digest()).toString(16);

		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}
		return md5;
	}

	/**
	 * Creates a new License Manager and performs the needed validation checks
	 * for the given Class.
	 * 
	 * @param cls
	 *            The Class that needs to be validated against.
	 * @param product
	 *            The $product key that will be sent to the server.
	 * @param purchaseURL
	 *            A URL that can be used to purchase a license for your product.
	 *            You must be able to correlate email addesses and prodict ids
	 *            with licenses on your server.
	 * @param registerURL
	 *            Registration / Validation script URL
	 */
	public LicenseManager(Class<?> cls, String product, String purchaseURL,
			String registerURL) {

		Preferences userRoot = Preferences.userRoot();
		Preferences sysRoot = Preferences.systemRoot();
		Preferences userPrefs = userRoot.node(cls.getCanonicalName());

		// try {
		// // userPrefs.clear();
		// } catch (BackingStoreException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		String uid = System.getProperty("user.name");
		System.out.println(uid);

		String uuid = userPrefs.get("uuid", uid + "");
		String license = userPrefs.get("license", System.currentTimeMillis()
				+ "");
		long currt = System.currentTimeMillis();
		String key = uuid + "" + product;

		String hash = LicenseManager.md5(key);
		if (license.contentEquals(hash)) {
			// LIC is valid
		} else {
			String text = "";
			boolean trial = false;
			long d = 0;
			try {
				// Otherwise we need to get a new license
				long initt = Long.parseLong(license);
				long a = (long) (1000.0 * 60.0 * 60.0 * 24.0 * 30.0);
				long b = currt - initt;
				d = (long) ((a - b) / (1000.0 * 60.0 * 60.0 * 24.0));
				trial = currt - initt < a;
			} catch (Exception e) {

			}
			if (trial) {
				// 30 day trial valid
				text += "You're currently using a trial license \nwhich will expire in "
						+ d + " days. \n\n";
				text += "To purchase a license please go to:\n\n" + purchaseURL
						+ "\n\n";
				text += "If you've already purchased a license, \n"
						+ "please enter the email address associated \n"
						+ "with your purchase below:\n";
				String s = JOptionPane.showInputDialog(null, text,
						"Buy/Register License", JOptionPane.PLAIN_MESSAGE);

				// If a string was returned, use the result to register
				if (s != null && s.length() > 0) {
					URL url;
					String ret = "N/A";
					try {
						url = new URL(registerURL + "?email=" + s + "&uuid="
								+ uuid + "&product=" + product);
						ret = org.apache.commons.io.IOUtils.toString(url);

					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
					System.out.println(ret);
					if (!ret.contentEquals(hash)) {// custom title, error icon
						JOptionPane.showMessageDialog(null, "No License Found",
								"Error", JOptionPane.ERROR_MESSAGE);

					} else {
						userPrefs.put("license", hash);
						// default title and icon
						JOptionPane.showMessageDialog(null, "Success!");
					}
				}

			} else {

				text += "You're license is currently expired, \n"
						+ "unregistered, incorrect or invalid.\n\n";
				text += "To purchase a license please go to:\n\n" + purchaseURL
						+ "\n\n";
				text += "If you've already purchased a license, \n"
						+ "please enter the email address associated \n"
						+ "with your purchase below:\n";
				String s = JOptionPane.showInputDialog(null, text,
						"Buy/Register License", JOptionPane.PLAIN_MESSAGE);

				// If a string was returned, use the result to register
				if (s != null && s.length() > 0) {
					URL url;
					String ret = "N/A";
					try {
						url = new URL(registerURL + "?email=" + s + "&uuid="
								+ uuid + "&product=" + product);
						ret = org.apache.commons.io.IOUtils.toString(url);

					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
					if (!ret.contentEquals(hash)) {
						JOptionPane.showMessageDialog(null, "No License Found",
								"Error", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					} else {
						userPrefs.put("license", hash);
						// default title and icon
						JOptionPane.showMessageDialog(null, "Success!");
					}
				}
			}
		}
	}

}
