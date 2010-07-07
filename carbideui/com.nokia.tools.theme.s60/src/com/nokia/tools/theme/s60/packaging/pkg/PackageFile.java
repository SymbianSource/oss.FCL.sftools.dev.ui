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
package com.nokia.tools.theme.s60.packaging.pkg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nokia.tools.packaging.PackagingPlugin;
import com.nokia.tools.resource.util.FileUtils;

/**
 * This class can be used to create and manipulate simple symbian pkgFile files.
 * Note: the methods in this class are not thread-safe.
 */
public class PackageFile {
	private static final String CHARSET = "UTF-8";
	private static final String LINE_SEPARATOR = "\r\n";
	private static final String PARSE_NAME = "parse";
	private static final Class[] PARSE_TYPE = { String.class };
	private static Method[] PARSERS;
	static {
		try {
			PARSERS = new Method[] {
					Comment.class.getMethod(PARSE_NAME, PARSE_TYPE),
					Dependency.class.getMethod(PARSE_NAME, PARSE_TYPE),
					BracketDependency.class.getMethod(PARSE_NAME, PARSE_TYPE),
					EmbeddedSis.class.getMethod(PARSE_NAME, PARSE_TYPE),
					EmptyLine.class.getMethod(PARSE_NAME, PARSE_TYPE),
					Header.class.getMethod(PARSE_NAME, PARSE_TYPE),
					InstallFile.class.getMethod(PARSE_NAME, PARSE_TYPE),
					Languages.class.getMethod(PARSE_NAME, PARSE_TYPE),
					LocalizedVendor.class.getMethod(PARSE_NAME, PARSE_TYPE),
					Signature.class.getMethod(PARSE_NAME, PARSE_TYPE),
					Vendor.class.getMethod(PARSE_NAME, PARSE_TYPE),
					VendorLogo.class.getMethod(PARSE_NAME, PARSE_TYPE),
					ConditionalStatement.class
							.getMethod(PARSE_NAME, PARSE_TYPE),
					OriginalStatement.class.getMethod(PARSE_NAME, PARSE_TYPE) };
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Wrapper> statements = new ArrayList<Wrapper>();

	/**
	 * This will replace the existing statement if the classes are matching,
	 * otherwise the statemnet will be appended to the package file.
	 * 
	 * @param statement
	 */
	public void setStatement(Object statement) {
		for (Wrapper wrapper : statements) {
			if (wrapper.statement.getClass() == statement.getClass()) {
				wrapper.statement = statement;
				return;
			}
		}
		addStatement(statement);
	}

	/**
	 * Finds the statement of the given class.
	 * 
	 * @param clazz the statement class.
	 * @return the statement matching the given class, null otherwise.
	 */
	public Object getStatement(Class clazz) {
		for (Wrapper wrapper : statements) {
			if (wrapper.statement.getClass() == clazz) {
				return wrapper.statement;
			}
		}
		return null;
	}

	/**
	 * Appends statement to the package file.
	 * 
	 * @param statement the statement to be appended.
	 */
	public void addStatement(Object statement) {
		statements.add(new Wrapper(statement));
	}

	/**
	 * Removes a statement from the package file.
	 * 
	 * @param statement a statement to be removed.
	 */
	public void removeStatement(Object statement) {
		for (Iterator<Wrapper> i = statements.iterator(); i.hasNext();) {
			if (i.next().statement == statement) {
				i.remove();
				break;
			}
		}
	}

	/**
	 * Inserts a statement to the package file.
	 * 
	 * @param statement a statement to be inserted.
	 */
	public void insertStatement(Object statement,Object toBeInserted) {
		int index = 0;
		for (Iterator<Wrapper> i = statements.iterator(); i.hasNext();) {
			if (i.next().statement == statement) {
				if(i.hasNext()&&i.hasNext())
				{
					statements.add(++index,new Wrapper(";logo"));
					statements.add(++index,new Wrapper(toBeInserted));				
				break;
				}
			}
			index++;
		}
	}

	public void insertStatementAfter(Object statement, Object toBeInserted) {
		int index = 0;
		ArrayList<Wrapper> temp = new ArrayList<Wrapper>(statements.size() +  1);
		Wrapper wrapper = null;
		for (Iterator<Wrapper> i = statements.iterator(); i.hasNext();) {
			wrapper = i.next();
			temp.add(wrapper);
			if (wrapper.statement == statement) {
				temp.add(new Wrapper(toBeInserted));
			}
			index++;
		}
		
		statements = temp;
	}

	/**
	 * Returns all statements of the specific class.
	 * 
	 * @param clazz the statement class.
	 * @return statements of the given class.
	 */
	public Object[] getStatements(Class clazz) {
		List<Object> list = new ArrayList<Object>();
		for (Wrapper wrapper : statements) {
			if (wrapper.statement.getClass() == clazz) {
				list.add(wrapper.statement);
			}
		}
		return list.toArray();
	}

	/**
	 * Loads content from the package file.
	 * 
	 * @param file the package file.
	 * @throws IOException if I/O error occurred.
	 */
	public void load(File file) throws IOException {
		load(new FileInputStream(file));
	}

	/**
	 * Loads content from the package file.
	 * 
	 * @param fileName name of the file.
	 * @throws IOException if I/O error occurred.
	 */
	public void load(String fileName) throws IOException {
		load(new FileInputStream(fileName));
	}

	/**
	 * Loads content from the input stream.
	 * 
	 * @param in the input stream to read from.
	 * @throws IOException if I/O error occurred.
	 */
	public void load(InputStream in) throws IOException {
		statements.clear();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in, CHARSET));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim().replaceAll(",\\s+", ",");
				Object statement = null;
				for (Method parser : PARSERS) {
					try {
						statement = parser.invoke(null, new Object[] { line });
						if (statement != null) {
							statements.add(new Wrapper(statement));
							break;
						}
					} catch (Exception e) {
						PackagingPlugin.error(e);
					}
				}
				if (statement instanceof OriginalStatement) {
					System.err.println("Can't parse: " + statement);
				}
			}
		} finally {
			FileUtils.close(reader);
		}
	}

	/**
	 * Saves the content to the file.
	 * 
	 * @param file the destination file.
	 * @throws IOException if I/O error occurred.
	 */
	public void save(File file) throws IOException {
		save(new FileOutputStream(file));
	}

	/**
	 * Saves the content to the file.
	 * 
	 * @param fileName the destination file name.
	 * @throws IOException if I/O error occurred.
	 */
	public void save(String fileName) throws IOException {
		save(new FileOutputStream(fileName));
	}

	/**
	 * Saves the content to the given stream.
	 * 
	 * @param out the output stream to write data.
	 * @throws IOException if I/O error occurred.
	 */
	public void save(OutputStream out) throws IOException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new OutputStreamWriter(out, CHARSET), true);
			for (Wrapper wrapper : statements) {
				writer.print(wrapper + LINE_SEPARATOR);
			}
			writer.flush();
		} finally {
			FileUtils.close(writer);
		}
	}

	class Wrapper {
		Object statement;

		/**
		 * Wraps a statement.
		 * 
		 * @param statement a statement to wrap.
		 */
		Wrapper(Object statement) {
			this.statement = statement;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return statement.toString();
		}
	}
}
