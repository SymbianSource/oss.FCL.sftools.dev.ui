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

package com.nokia.tools.media.utils.timeline.impl;

public class ExecutionThread extends Thread {

	public static final String NAME = "ExecutionThread";

	public static final ExecutionThread INSTANCE = new ExecutionThread();

	int threadCount;

	static {
		INSTANCE.start();
	}

	Runnable execute = null;

	Object mutex = new Object();

	private ExecutionThread() {
		setName(NAME);
		setDaemon(true);
		setPriority(Thread.NORM_PRIORITY);
	}

	@Override
	public void run() {
		while (true) {
			synchronized (mutex) {
				try {
					if (execute == null) {
						mutex.wait();
					}
					if (execute != null) {
						try {
							Thread executionThread = new Thread(execute);
							executionThread.setDaemon(true);
							executionThread.setName(NAME + "_" + threadCount++);
							executionThread.start();
						} finally {
							execute = null;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void execute(Runnable execute) {
		synchronized (mutex) {
			this.execute = execute;
			mutex.notify();
		}
	}
}