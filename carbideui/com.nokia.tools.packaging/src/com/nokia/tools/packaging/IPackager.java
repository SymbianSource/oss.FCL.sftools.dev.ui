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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.core.IPlatform;

/**
 * Interface for the packager that is responsible for packaging the provided
 * content into a specific package. The packager consists of a chain of
 * processors, where each processor will take the input from the previous
 * processor or original content if it's the first processor in the chain,
 * process the input according to its specific logic, generate the output and
 * finally the packager will set the output to be input of the next processor
 * and the processing continues until all registered processors finish the work.
 * The packaging process will fail if any of the processor throws the
 * {@link PackagingException}.
 * 
 */
public interface IPackager {
	int TOTAL_WORK = 100;

	/**
	 * Adds a processor to the chain.
	 * 
	 * @param processor the processor to be added.
	 */
	void addProcessor(IPackagingProcessor processor);

	/**
	 * Removes the specific processor from the chain.
	 * 
	 * @param processor the processor to be removed.
	 */
	void removeProcessor(IPackagingProcessor processor);

	/**
	 * @return all packaging processors
	 */
	IPackagingProcessor[] getProcessors();

	/**
	 * Builds the package by using the provided context.
	 * 
	 * @param context the packaging context that contains the information needed
	 *            during the packaging process.
	 * @return the final package.
	 * @throws PackagingException if the packaging processor fails.
	 */
	Object buildPackage(PackagingContext context) throws PackagingException;

	/**
	 * Builds the package by using the provided context.
	 * 
	 * @param context the packaging context that contains the information needed
	 *            during the packaging process.
	 * @param monitor the progress monitor, can be null.
	 * @return the final package.
	 * @throws PackagingException if the packaging processor fails.
	 */
	Object buildPackage(PackagingContext context, IProgressMonitor monitor)
			throws PackagingException;

	/**
	 * Tests whether the packager supports the given platform.
	 * 
	 * @param platform the platform to test.
	 * @return true if the platform is supported, false otherwise.
	 */
	boolean supportPlatform(IPlatform platform);

	/**
	 * String that is displayed outside. Several device platforms could have the
	 * same public name for packaging.
	 * 
	 * @param platform
	 * @return
	 */
	String getPlatformPackagingName(IPlatform platform);

	/**
	 * List of target platforms suported by packager
	 * 
	 * @return
	 */
	public IPlatform[] getSupportedPlatforms();

	/**
	 * Stub implementation of the {@link IPackage} interface.
	 * 
	 * @author shji
	 * @version $Revision: 1.6 $ $Date: 2010/04/21 14:45:53 $
	 */
	public abstract class Packager implements IPackager {
		protected List<IPackagingProcessor> processors = new ArrayList<IPackagingProcessor>();

		/**
		 * Returns true if the package signing is required for the given
		 * platform.
		 * 
		 * @param platform the platform to test.
		 * @return true if signing is required, false otherwise.
		 */
		public static boolean isSigningRequired(IPlatform platform) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.packaging.IPackager#getPlatformPackagingName(com.nokia.tools.platform.core.IPlatform)
		 */
		public String getPlatformPackagingName(IPlatform platform) {
			return platform.getName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.packaging.IPackager#supportPlatform(com.nokia.tools.platform.core.IPlatform)
		 */
		public boolean supportPlatform(IPlatform platform) {
			for (IPlatform p : getSupportedPlatforms()) {
				if (p.getId().equals(platform.getId())) {
					return true;
				}
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.packaging.IPackager#addProcessor(com.nokia.tools.packaging.IPackagingProcessor)
		 */
		public void addProcessor(IPackagingProcessor processor) {
			processors.add(processor);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.packaging.IPackager#getProcessors()
		 */
		public IPackagingProcessor[] getProcessors() {
			return processors
					.toArray(new IPackagingProcessor[processors.size()]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.packaging.IPackager#buildPackage(com.nokia.tools.packaging.PackagingContext)
		 */
		public Object buildPackage(PackagingContext context)
				throws PackagingException {
			return buildPackage(context, new NullProgressMonitor());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.packaging.IPackager#buildPackage(com.nokia.tools.packaging.PackagingContext,
		 *      org.eclipse.core.runtime.IProgressMonitor)
		 */
		public Object buildPackage(PackagingContext context,
				IProgressMonitor monitor) throws PackagingException {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			int slice = processors.isEmpty() ? TOTAL_WORK : TOTAL_WORK
					/ processors.size();
			for (IPackagingProcessor processor : processors) {
				Object output = processor.process(context);
				context.setInput(output);
				monitor.worked(slice);
			}
			return context.getOutput();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.packaging.IPackager#removeProcessor(com.nokia.tools.packaging.IPackagingProcessor)
		 */
		public void removeProcessor(IPackagingProcessor processor) {
			processors.remove(processor);
		}
	}
}
