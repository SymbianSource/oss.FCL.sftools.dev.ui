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
package com.nokia.tools.theme.s60.packaging.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.packaging.KeyPair;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.PackagingMessages;
import com.nokia.tools.packaging.PackagingPlugin;
import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.s60.packaging.SigningProcessor;

/**
 * Various utilities related to symbian packaging.
 */
public class SymbianUtil {
	/**
	 * Maximum key length
	 */
	public static final int MAX_KEY_LENGTH = 2048;
	/**
	 * Minimum key length
	 */
	public static final int MIN_KEY_LENGTH = 512;
	/**
	 * Minimum password length
	 */
	public static final int MIN_PASSWORD_LENGTH = 4;
	/**
	 * Default expiry days - one year
	 */
	public static final int DEFAULT_EXPIRY_DAYS = 365;
	/**
	 * UID3 for file format version <= 6.0
	 */
	public static final int S60_2x_UID3 = 0x10000419;
	/**
	 * UID1 for file format version > 6.0
	 */
	public static final int S60_3x_UID1 = 0x10201a7a;
	/**
	 * Default folder for storing the files.
	 */
	public static final String DEFAULT_INSTALL_FOLDER = "\\Data\\Others\\";

	/**
	 * CRC table
	 */
	public static final int[] CRC_TABLE = { 0x0000, 0x1021, 0x2042, 0x3063,
			0x4084, 0x50a5, 0x60c6, 0x70e7, 0x8108, 0x9129, 0xa14a, 0xb16b,
			0xc18c, 0xd1ad, 0xe1ce, 0xf1ef, 0x1231, 0x0210, 0x3273, 0x2252,
			0x52b5, 0x4294, 0x72f7, 0x62d6, 0x9339, 0x8318, 0xb37b, 0xa35a,
			0xd3bd, 0xc39c, 0xf3ff, 0xe3de, 0x2462, 0x3443, 0x0420, 0x1401,
			0x64e6, 0x74c7, 0x44a4, 0x5485, 0xa56a, 0xb54b, 0x8528, 0x9509,
			0xe5ee, 0xf5cf, 0xc5ac, 0xd58d, 0x3653, 0x2672, 0x1611, 0x0630,
			0x76d7, 0x66f6, 0x5695, 0x46b4, 0xb75b, 0xa77a, 0x9719, 0x8738,
			0xf7df, 0xe7fe, 0xd79d, 0xc7bc, 0x48c4, 0x58e5, 0x6886, 0x78a7,
			0x0840, 0x1861, 0x2802, 0x3823, 0xc9cc, 0xd9ed, 0xe98e, 0xf9af,
			0x8948, 0x9969, 0xa90a, 0xb92b, 0x5af5, 0x4ad4, 0x7ab7, 0x6a96,
			0x1a71, 0x0a50, 0x3a33, 0x2a12, 0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e,
			0x9b79, 0x8b58, 0xbb3b, 0xab1a, 0x6ca6, 0x7c87, 0x4ce4, 0x5cc5,
			0x2c22, 0x3c03, 0x0c60, 0x1c41, 0xedae, 0xfd8f, 0xcdec, 0xddcd,
			0xad2a, 0xbd0b, 0x8d68, 0x9d49, 0x7e97, 0x6eb6, 0x5ed5, 0x4ef4,
			0x3e13, 0x2e32, 0x1e51, 0x0e70, 0xff9f, 0xefbe, 0xdfdd, 0xcffc,
			0xbf1b, 0xaf3a, 0x9f59, 0x8f78, 0x9188, 0x81a9, 0xb1ca, 0xa1eb,
			0xd10c, 0xc12d, 0xf14e, 0xe16f, 0x1080, 0x00a1, 0x30c2, 0x20e3,
			0x5004, 0x4025, 0x7046, 0x6067, 0x83b9, 0x9398, 0xa3fb, 0xb3da,
			0xc33d, 0xd31c, 0xe37f, 0xf35e, 0x02b1, 0x1290, 0x22f3, 0x32d2,
			0x4235, 0x5214, 0x6277, 0x7256, 0xb5ea, 0xa5cb, 0x95a8, 0x8589,
			0xf56e, 0xe54f, 0xd52c, 0xc50d, 0x34e2, 0x24c3, 0x14a0, 0x0481,
			0x7466, 0x6447, 0x5424, 0x4405, 0xa7db, 0xb7fa, 0x8799, 0x97b8,
			0xe75f, 0xf77e, 0xc71d, 0xd73c, 0x26d3, 0x36f2, 0x0691, 0x16b0,
			0x6657, 0x7676, 0x4615, 0x5634, 0xd94c, 0xc96d, 0xf90e, 0xe92f,
			0x99c8, 0x89e9, 0xb98a, 0xa9ab, 0x5844, 0x4865, 0x7806, 0x6827,
			0x18c0, 0x08e1, 0x3882, 0x28a3, 0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e,
			0x8bf9, 0x9bd8, 0xabbb, 0xbb9a, 0x4a75, 0x5a54, 0x6a37, 0x7a16,
			0x0af1, 0x1ad0, 0x2ab3, 0x3a92, 0xfd2e, 0xed0f, 0xdd6c, 0xcd4d,
			0xbdaa, 0xad8b, 0x9de8, 0x8dc9, 0x7c26, 0x6c07, 0x5c64, 0x4c45,
			0x3ca2, 0x2c83, 0x1ce0, 0x0cc1, 0xef1f, 0xff3e, 0xcf5d, 0xdf7c,
			0xaf9b, 0xbfba, 0x8fd9, 0x9ff8, 0x6e17, 0x7e36, 0x4e55, 0x5e74,
			0x2e93, 0x3eb2, 0x0ed1, 0x1ef0 };

	/**
	 * No instantiation.
	 */
	private SymbianUtil() {
	}

	/**
	 * Executes the command with the command line arguments in the runtime
	 * directory of the specific plugin.
	 * 
	 * @param executable the executable to be invoked.
	 * @param commandList the command line arguments.
	 * @throws PackagingException if command execution failed.
	 */
	public static void exec(Plugin plugin, String executable,
			List<String> commandList, File workingDir)
			throws PackagingException {
		exec(plugin, executable, commandList, workingDir, false);
	}

	/**
	 * Executes the command with the command line arguments in the runtime
	 * directory of the specific plugin.
	 * 
	 * @param executable the executable to be invoked.
	 * @param commandList the command line arguments.
	 * @throws PackagingException if command execution failed.
	 */
	public static void exec(Plugin plugin, String executable,
			List<String> commandList, File workingDir,
			boolean checkErrorInOutput) throws PackagingException {
		commandList.add(0, SymbianUtil.getRuntimeDir(plugin) + File.separator
				+ executable);

		Process process = null;
		StringBuilder sb = new StringBuilder();
		sb.append("Command:");
		for (String cmd : commandList) {
			sb.append(" " + cmd);
		}
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		StringBuilder message = new StringBuilder();
		try {
			System.err.println(commandList);
			ProcessBuilder builder = new ProcessBuilder(commandList);
			builder.redirectErrorStream(true);
			builder.directory(workingDir);
			process = builder.start();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				message.append(line + System.getProperty("line.separator"));
			}

			process.waitFor();
		} catch (Exception e) {
			if (process != null) {
				process.destroy();
			}
			throw new PackagingException(e);
		}

		if (process.exitValue() != 0) {
			PackagingException e = new PackagingException(MessageFormat.format(
					PackagingMessages.Error_commandExecutionFailed,
					new Object[] { executable }));
			sb.append(message);
			e.setDetails(sb.toString());
			throw e;
		}
		if (checkErrorInOutput && message.indexOf("Error") >= 0) {
			throw new PackagingException(message.toString());
		}
	}

	/**
	 * Returns the runtime directory containing the platform specific binary
	 * files.
	 * 
	 * @param plugin the target plugin.
	 * @return the runtime directory.
	 */
	public static String getRuntimeDir(Plugin plugin) {
		return getRootDir(plugin) + File.separator + "runtime" + File.separator
				+ Platform.getOS();
	}

	/**
	 * @return the runtime directory of this packaging plugin.
	 */
	public static String getRuntimeDir() {
		return getRuntimeDir(PackagingPlugin.getDefault());
	}

	/**
	 * Returns the plugin root directory or the current directory if the the
	 * plugin root directory cannot be resolved.
	 * 
	 * @param plugin the target plugin.
	 * @return the root directory of the specific plugin.
	 */
	public static String getRootDir(Plugin plugin) {
		try {
			URL url = FileUtils.getURL(plugin, "");
			return new File(FileLocator.resolve(url).getFile())
					.getAbsolutePath();
		} catch (Exception e) {
			UtilsPlugin.error(e);
			return ".";
		}
	}

	/**
	 * Tests if the combination of the certificate file, private key file and
	 * passphrase is valid.
	 * 
	 * @param certificateFile absolute path of the certificate file.
	 * @param privateKeyFile absolute path of the private key file.
	 * @param passphrase the passphrase.
	 * @throws PackagingException if any of the files or passphrase is not
	 *             correct.
	 */
	public static void testKey(String certificateFile, String privateKeyFile,
			String passphrase, PackagingContext passedContext) throws PackagingException {
		String workingDir = null;
		try {
			URL url = Platform.find(PackagingPlugin.getDefault().getBundle(),
					new Path(""));
			workingDir = new File(Platform.resolve(url).getFile())
					.getAbsolutePath()
					+ File.separator + "data";
			String testSisFile = workingDir + File.separator + "test.txt";
			PackagingContext context = new PackagingContext();
			context.setInput(testSisFile);
			context.setAttribute(PackagingAttribute.platform.name(),
					passedContext.getAttribute(PackagingAttribute.platform.name()));
			context.setAttribute(PackagingAttribute.signPackage.name(), "true");
			context.setAttribute(PackagingAttribute.keepInputAfterSigning
					.name(), "true");
			context.setAttribute(PackagingAttribute.sisFile.name(), testSisFile
					+ "x");
			context.setAttribute(PackagingAttribute.workingDir.name(),
					workingDir);
			context.setAttribute(PackagingAttribute.certificateFile.name(),
					certificateFile);
			context.setAttribute(PackagingAttribute.privateKeyFile.name(),
					privateKeyFile);
			context.setAttribute(PackagingAttribute.passphrase.name(),
					passphrase);

			context.setAttribute(PackagingAttribute.primaryModelId.name(), passedContext.getAttribute(PackagingAttribute.primaryModelId.name()));
			context.setAttribute(PackagingAttribute.secondaryModelId.name(), passedContext.getAttribute(PackagingAttribute.secondaryModelId.name()));
			new SigningProcessor().process(context);
		} catch (PackagingException e) {
			UtilsPlugin.error(e);
			throw e;
		}catch (IOException e) {
			UtilsPlugin.error(e);
		}finally {
			if (workingDir != null) {
				File outputFile = new File(workingDir + File.separator
						+ "test.sisx");
				if (outputFile.exists()) {
					outputFile.delete();
				}
				outputFile = new File(workingDir + File.separator
						+ "test.sis.tmp");
				if (outputFile.exists()) {
					outputFile.delete();
				}
			}
		}
	}

	/**
	 * Reads the UID from the SIS file.
	 * 
	 * @param fileName the name of the SIS file.
	 * @param platform the platform
	 * @return the UID.
	 * @throws IOException if file reading failed.
	 * @throws PackagingException if the SIS file is not valid.
	 */
	public static int readUID(String fileName)
			throws IOException, PackagingException {
		return readUID(new FileInputStream(fileName));
	}

	/**
	 * Reads the UID from the SIS file.
	 * 
	 * @param file the SIS file.
	 * @param platform the platform
	 * @return the UID.
	 * @throws IOException if file reading failed.
	 * @throws PackagingException if the SIS file is not valid.
	 */
	public static int readUID(File file)
			throws IOException, PackagingException {
		return readUID(new FileInputStream(file));
	}

	/**
	 * Reads the UID from the stream which contains the SIS package data. This
	 * will check both the SIS file format correctness for the target platform
	 * and also compares the checksum of the UIDs. If either the format is not
	 * correct or the checksum is wrong, the {@link PackagingException} will be
	 * thrown.
	 * 
	 * @param in the input stream.
	 * @param platform the platform
	 * @return the UID.
	 * @throws IOException if stream reading failed.
	 * @throws PackagingException if the SIS file is not valid.
	 */
	public static int readUID(InputStream in)
			throws IOException, PackagingException {
		try {
			byte[] buf = new byte[in.available()];
			int read, offset = 0, left = buf.length;
			while ((read = in.read(buf, offset, left)) > 0) {
				offset += read;
				left = buf.length - offset;
			}
			if (buf.length < 16) {
				throw new PackagingException(
						PackagingMessages.Error_sisFileFormat);
			}

			int uid1 = toUID(buf, 0);
		
			int uid3 = toUID(buf, 8);
			int uid4 = toUID(buf, 12);

			// UID 4 is a checksum calculated from the UID1+UID2+UID3. The
			// least significant 16 bits are given by the CRC16 of the bytes at
			// even offsets from the start of the file, and the most significant
			// 16 bits are given by the CRC16 of the bytes at odd offsets from
			// the start of the file.
			// every even bytes
			byte[] b1 = new byte[6];
			// every odd bytes
			byte[] b2 = new byte[6];

			for (int i = 0; i < 6; i++) {
				b1[i] = buf[i * 2];
				b2[i] = buf[i * 2 + 1];
			}

			int crc1 = crc(b1) & 0xffff;
			int crc2 = crc(b2) & 0xffff;

			int uidChecksum = (crc2 << 16) | crc1;
			if (uid4 != uidChecksum) {
				throw new PackagingException(
						PackagingMessages.Error_sisFileFormat);
			}

			boolean is3xUid = uid1 == S60_3x_UID1 || uid3 != S60_2x_UID3;

			if (is3xUid) {
				return uid3;
			}
			return uid1;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Reads 4 bytes from the buffer from the given offset.
	 * 
	 * @param buf the buf containing the uid data.
	 * @param offset the offset to the beginning of the uid data.
	 * @return the uid.
	 */
	public static int toUID(byte[] buf, int offset) {
		return ((buf[offset + 3] & 0xff) << 24)
				| ((buf[offset + 2] & 0xff) << 16)
				| ((buf[offset + 1] & 0xff) << 8) | (buf[offset] & 0xff);
	}

	/**
	 * Computes the checksum of the given byte array.
	 * 
	 * @param buf the byte array.
	 * @return the computed checksum.
	 */
	public static int crc(byte[] buf) {
		int crc = 0;
		for (int i = 0; i < buf.length; i++) {
			crc = (crc << 8) ^ CRC_TABLE[((crc >> 8) ^ buf[i]) & 0xff];
		}
		return crc;
	}

	/**
	 * Generates a self-signed certificate. The private key file will be
	 * generated if it doesn't exist.
	 * 
	 * @param names the distinguished names.
	 * @param keyLength desired key length
	 * @param keyPair the keypair that holds the private/public key and password
	 *            information.
	 * @throws PackagingException if error occurred in creating the keys.
	 */
	public static void generateKeyPair(DistinguishedNames names, int keyLength,
			KeyPair keyPair) throws PackagingException {
		generateKeyPair(names, keyLength, 0, keyPair.getPassword(), new File(
				keyPair.getPrivateKeyFile()), new File(keyPair
				.getCertificateFile()));
	}

	/**
	 * Generates a self-signed certificate. The private key file will be
	 * generated if it doesn't exist.
	 * 
	 * @param names the distinguished names.
	 * @param keyLength desired key length
	 * @param keyPair the keypair that holds the private/public key and password
	 *            information.
	 * @throws PackagingException if error occurred in creating the keys.
	 */
	public static void generateKeyPair(DistinguishedNames names, int keyLength,
			int expDays, KeyPair keyPair) throws PackagingException {
		generateKeyPair(names, keyLength, expDays, keyPair.getPassword(),
				new File(keyPair.getPrivateKeyFile()), new File(keyPair
						.getCertificateFile()));
	}

	private static String read(BufferedReader in, int length)
			throws IOException {
		char[] buf = new char[length];
		int read = 0;
		while (read < buf.length) {
			read += in.read(buf, read, buf.length - read);
		}
		return new String(buf);
	}

	/**
	 * Generates a self-signed certificate. The private key file will be
	 * generated if it doesn't exist.
	 * 
	 * @param names the distinguished names.
	 * @param keyLength desired key length
	 * @param password the password, can be null.
	 * @param privateKey the private key file.
	 * @param certificate the public key file.
	 * @throws PackagingException if error occurred in creating the keys.
	 */
	public static void generateKeyPair(DistinguishedNames names, int keyLength,
			String password, File privateKey, File certificate)
			throws PackagingException {
		generateKeyPair(names, keyLength, 0, password, privateKey, certificate);
	}

	/**
	 * Generates a self-signed certificate. The private key file will be
	 * generated if it doesn't exist.
	 * 
	 * @param names the distinguished names.
	 * @param keyLength desired key length
	 * @param password the password, can be null.
	 * @param privateKey the private key file.
	 * @param certificate the public key file.
	 * @throws PackagingException if error occurred in creating the keys.
	 */
	public static void generateKeyPair(DistinguishedNames names, int keyLength,
			int expDays, String password, File privateKey, File certificate)
			throws PackagingException {
		if (keyLength < MIN_KEY_LENGTH || keyLength > MAX_KEY_LENGTH
				|| keyLength % 8 != 0) {
			throw new IllegalArgumentException("The key length is not valid: "
					+ keyLength);
		}
		if (password != null) {
			password = password.trim();
			if (password.length() == 0) {
				password = null;
			}
		}
		if (password != null && password.length() < MIN_PASSWORD_LENGTH) {
			throw new IllegalArgumentException(
					"The password must be at least 4 characters");
		}
		List<String> commandList = new ArrayList<String>();
		commandList.add("-cert");
		if (password != null && password.length() > 0) {
			commandList.add("-password");
			commandList.add(password);
		}
		if (expDays > 0) {
			commandList.add("-expdays");
			commandList.add(Integer.toString(expDays));
		}
		commandList.add("-len");
		commandList.add(Integer.toString(keyLength));
		commandList.add("-dname");
		commandList.add(names.toString());
		commandList.add(privateKey.getAbsolutePath());
		commandList.add(certificate.getAbsolutePath());

		String executable = "makekeys.exe";

		if (password != null && password.length() > 0) {
			exec(PackagingPlugin.getDefault(), executable, commandList, null,
					true);
		} else {
			commandList.add(0, SymbianUtil.getRuntimeDir(PackagingPlugin
					.getDefault())
					+ File.separator + executable);
			if (privateKey.exists()) {
				// use the existing key to sign a new certificate, we provide a
				// faked password otherwise the process will hang, strange that
				// stdout/stderr hang but still there is output from the command
				// line, verified with the command prompt with redirecting and
				// also java stream reading with multi-threading
			
				commandList.add(1, "-password");
				commandList.add(2, "myfakedpassword");
			}

			Process process = null;
			StringBuilder sb = new StringBuilder();
			sb.append("Command:");
			for (String cmd : commandList) {
				sb.append(" " + cmd);
			}
			sb.append(System.getProperty("line.separator"));
			sb.append(System.getProperty("line.separator"));
			try {
				System.err.println(commandList);
				ProcessBuilder builder = new ProcessBuilder(commandList);
				builder.redirectErrorStream(true);
				process = builder.start();
				PrintWriter out = new PrintWriter(process.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				String input = "Enter private key passphrase:";
				String str = read(in, 5);

				if (input.startsWith(str)) {
					// private key is password-protected and no password
					// is
					// provided
					System.out.println(input);
					process.destroy();
					throw new PackagingException(MessageFormat.format(
							PackagingMessages.Error_PasswordNeeded,
							new Object[] { privateKey }));
				}

				// reads waring message first
				String line = in.readLine();
				System.out.println(str + line);

				StringBuilder message = new StringBuilder();
				if (!(str + line).contains("Existing private key will be used")) {
					String question = "Do you want to use a password (y/n)? ";
					read(in, question.length());
					System.out.println(question);
					// answering "no"
					out.println("n");
					out.flush();
				}
				while ((line = in.readLine()) != null) {
					System.out.println(line);
					message.append(line + System.getProperty("line.separator"));
				}

				process.waitFor();
				if (process.exitValue() != 0) {
					PackagingException e = new PackagingException(
							MessageFormat
									.format(
											PackagingMessages.Error_commandExecutionFailed,
											new Object[] { executable }));
					sb.append(message);
					e.setDetails(sb.toString());
					throw e;
				}
				if (message.indexOf("Error") >= 0) {
					throw new PackagingException(message.toString());
				}
			} catch (PackagingException e) {
				throw e;
			} catch (Exception e) {
				throw new PackagingException(e);
			}
		}
	}
}
