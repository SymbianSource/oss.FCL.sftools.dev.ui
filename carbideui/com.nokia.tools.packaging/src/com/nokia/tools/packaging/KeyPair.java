/*
* Copyright (c) 2006-2010 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
*/
package com.nokia.tools.packaging;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;

/**
 * This class models a keypair, i.e. a public key (certificate) and its private
 * key. The keypair has a name and password and they are stored in the
 * authorized areas so it's not easy for other people to retrieve sensitive
 * information.
 * 
 */
public class KeyPair {
	private static final String NAME = "name";
	private static final String PRIVATE_KEY_FILE = "private.key.file";
	private static final String CERTIFICATE_FILE = "certificate.file";
	private static final String PASSWORD = "password";
	private static final String SAVE_PASSWORD = "savepassword";
	private static final String DEFAULT = "default";

	private static final URL FAKE_URL;
	private static final String AUTH_SCHEME = "";
	private static final String REALM_KEY_PAIR = "keypairs.";
	private static final String REALM_KEY_PAIRS = "allkeypairs";

	static {
		URL temp = null;
		try {
			temp = new URL("http://com.nokia.tools.packaging");//$NON-NLS-1$ 
		} catch (MalformedURLException e) {
			
		}
		FAKE_URL = temp;
	}

	private String name;
	private String privateKeyFile;
	private String certificateFile;
	private String password;
	private boolean isDefault;
	private boolean savePassword = false;

	/**
	 * Creates a new keypair.
	 */
	public KeyPair() {
	}

	/**
	 * Creates a keypair using the information in the given map.
	 * 
	 * @param map the map contains the keypair information.
	 */
	private KeyPair(Map<String, String> map) {
		setName(map.get(NAME));
		setPrivateKeyFile(map.get(PRIVATE_KEY_FILE));
		setCertificateFile(map.get(CERTIFICATE_FILE));
		setPassword(map.get(PASSWORD));
		setSavePassword(new Boolean(map.get(SAVE_PASSWORD)));
		setDefault(new Boolean(map.get(DEFAULT)));
	}

	/**
	 * Extracts the information from this keypair and put to a map.
	 * 
	 * @return the map containing the extracted information.
	 */
	private Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>(4);
		map.put(NAME, getName());
		map.put(PRIVATE_KEY_FILE, getPrivateKeyFile());
		map.put(CERTIFICATE_FILE, getCertificateFile());
		map.put(PASSWORD, getPassword());
		map.put(SAVE_PASSWORD,  new Boolean(isSavePassword()).toString());
		map.put(DEFAULT, new Boolean(isDefault).toString());
		return map;
	}

	/**
	 * Finds keypair with the given name.
	 * 
	 * @param name name of the keypair, which is case sensitive.
	 * @return the keypair if exists, otherwise null.
	 * @throws PackagingException if error occurred while retrieving keypair
	 *         information.
	 */
	public static KeyPair getKeyPair(String name) throws PackagingException {
		try {
			Map<String, String> keyPairs = Platform.getAuthorizationInfo(
					FAKE_URL, REALM_KEY_PAIRS, AUTH_SCHEME);
			if (keyPairs != null && keyPairs.containsKey(name)) { // exists
				Map<String, String> map = Platform.getAuthorizationInfo(
						FAKE_URL, REALM_KEY_PAIR + name, AUTH_SCHEME);
				if (map != null) {
					return new KeyPair(map);
				}
			}
			return null;
		} catch (Exception e) {
			throw new PackagingException(e);
		}
	}

	/**
	 * Finds the default keypair.
	 * 
	 * @return the default keypair or null if there is no such keypair exists.
	 * @throws PackagingException if error occurred while retrieving keypair
	 *         information.
	 */
	public static KeyPair getDefaultKeyPair() throws PackagingException {
		for (KeyPair keyPair : getKeyPairs()) {
			if (keyPair.isDefault()) {
				return keyPair;
			}
		}
		return null;
	}

	/**
	 * Finds all keypairs.
	 * 
	 * @return all keypairs.
	 * @throws PackagingException if error occurred while retrieving keypair
	 *         information.
	 */
	public static KeyPair[] getKeyPairs() throws PackagingException {
		try {
			Map<String, String> keyPairs = Platform.getAuthorizationInfo(
					FAKE_URL, REALM_KEY_PAIRS, AUTH_SCHEME);
			if (keyPairs == null) {
				return new KeyPair[0];
			}
			List<KeyPair> list = new ArrayList<KeyPair>(keyPairs.size());
			for (String name : keyPairs.keySet()) {
				Map<String, String> map = Platform.getAuthorizationInfo(
						FAKE_URL, REALM_KEY_PAIR + name, AUTH_SCHEME);
				if (map != null) {
					list.add(new KeyPair(map));
				}
			}
			return list.toArray(new KeyPair[list.size()]);
		} catch (Exception e) {
			throw new PackagingException(e);
		}
	}

	/**
	 * Saves the keypair information to the persistent storage.
	 * 
	 * @throws PackagingException if information saving failed.
	 */
	public void save() throws PackagingException {
		if (name == null) {
			throw new NullPointerException("The name cannot be null.");
		}
		if (privateKeyFile == null) {
			throw new NullPointerException(
					"The private key file cannot be null.");
		}
		if (certificateFile == null) {
			throw new NullPointerException(
					"The certificate file cannot be null.");
		}
		if (isDefault) {
			// updates all other keypairs
			for (KeyPair keyPair : getKeyPairs()) {
				if (!name.equals(keyPair.getName())) {
					keyPair.setDefault(false);
					keyPair.save();
				}
			}
		}

		try {
			Map<String, String> keyPairs = Platform.getAuthorizationInfo(
					FAKE_URL, REALM_KEY_PAIRS, AUTH_SCHEME);
			if (keyPairs == null) {
				keyPairs = new HashMap<String, String>(1);
			}
			if (!keyPairs.containsKey(name)) { // new keypair
				keyPairs.put(name, name);
				Platform.addAuthorizationInfo(FAKE_URL, REALM_KEY_PAIRS,
						AUTH_SCHEME, keyPairs);
			}

			Platform.addAuthorizationInfo(FAKE_URL, REALM_KEY_PAIR + name,
					AUTH_SCHEME, toMap());
		} catch (Exception e) {
			throw new PackagingException(e);
		}
	}

	/**
	 * Deletes this keypair.
	 * 
	 * @throws PackagingException if deletion failed.
	 */
	public void delete() throws PackagingException {
		delete(name);
	}

	/**
	 * Deletes the keypair with the given name.
	 * 
	 * @param name name of the keypair to be deleted.
	 * @throws PackagingException if deletion failed.
	 */
	public static void delete(String name) throws PackagingException {
		if (name == null) {
			throw new NullPointerException("The name cannot be null.");
		}

		try {
			Map<String, String> keyPairs = Platform.getAuthorizationInfo(
					FAKE_URL, REALM_KEY_PAIRS, AUTH_SCHEME);
			if (keyPairs != null && keyPairs.remove(name) != null) {
				// removed from the list
				Platform.addAuthorizationInfo(FAKE_URL, REALM_KEY_PAIRS,
						AUTH_SCHEME, keyPairs);
			}

			Platform.flushAuthorizationInfo(FAKE_URL, REALM_KEY_PAIR + name,
					AUTH_SCHEME);
		} catch (Exception e) {
			throw new PackagingException(e);
		}
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the certificateFile.
	 */
	public String getCertificateFile() {
		return certificateFile;
	}

	/**
	 * @param certificateFile The certificateFile to set.
	 */
	public void setCertificateFile(String certificateKeyFile) {
		this.certificateFile = certificateKeyFile;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return Returns the privateKeyFile.
	 */
	public String getPrivateKeyFile() {
		return privateKeyFile;
	}

	/**
	 * @param privateKeyFile The privateKeyFile to set.
	 */
	public void setPrivateKeyFile(String privateKeyFile) {
		this.privateKeyFile = privateKeyFile;
	}

	/**
	 * @return Returns the isDefault.
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * @param isDefault The isDefault to set.
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public void setSavePassword(boolean b) {
		this.savePassword  = b;
	}
	
	public boolean isSavePassword() {
		return savePassword;
	}
	
	
}
