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

package com.nokia.tools.startuptip;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.nokia.tools.startuptip.preferences.StartupTipPreferences;

/**
 * Instance of this class is used to get next tips, from the tip root folder,
 * selected randomly and previous tips. It also checks the preferences if some
 * category has been set then it selects tips from that category.
 * 
 */
public class TipSelector {

	private String tipRootFolder;
	private ArrayList<String> tipUrls = new ArrayList<String>();
	private List<String> tipsShown;
	private ArrayList<String> nextPreviousList = new ArrayList<String>();
	private int nextPrevPosition = -1;
	private Random randomNumberGenerator;

	public TipSelector() {
		tipRootFolder = StartupTipsUtil.getStartupTipsRootFolder();
		randomNumberGenerator = new Random();
		tipsShown = StartupTipPreferences.getInstance().getTipsShownList();
		nextPrevPosition = nextPreviousList.size() - 1;
		loadTips();
	}

	public boolean previousAvailable() {
		if (nextPrevPosition <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * Returns tip shown previously.
	 * 
	 * @return the tip shown previously or null if nothing had been shown.
	 */
	public String getPreviousTip() {
		String tip = null;
		if (nextPreviousList.size() > 0 && nextPrevPosition > 0) {
			tip = nextPreviousList.get(--nextPrevPosition);
		}

		return tip;
	}

	/**
	 * Gets randomly selected next tip.
	 * 
	 * @return next tip
	 */
	public String getNextTip() {
		String nextTip = null;
		if (++nextPrevPosition < nextPreviousList.size()) {
			nextTip = nextPreviousList.get(nextPrevPosition);
		} else {
			nextTip = getRandomTip();

			tipsShown.add(nextTip);
			nextPreviousList.add(nextTip);
			nextPrevPosition = nextPreviousList.size() - 1;

			StartupTipPreferences.getInstance().setTipsShownList(tipsShown);
		}
		return nextTip;
	}

	private String getRandomTip() {
		if (tipUrls.size() == 0 && tipsShown.size() > 1) {
			// tipUrls.addAll(tipsShown);
			tipsShown.clear();
			loadTips();
			nextPrevPosition = -1;
		}
		String randomTip = null;
		if (tipUrls.size() > 0) {
			randomTip = tipUrls.remove(randomNumberGenerator.nextInt(tipUrls
					.size()));
		}
		return randomTip;
	}

	private void loadTips() {
		tipUrls.clear();
		String category = StartupTipPreferences.getInstance().getCategory();
		File loadTipsFrm = null;
		if (category.equals("All")) {
			loadTipsFrm = new File(tipRootFolder);
		} else {
			loadTipsFrm = new File(tipRootFolder, category);
		}
		populateTipUrls(loadTipsFrm);
		tipUrls.removeAll(tipsShown);
	}

	private void populateTipUrls(File tipFolder) {
		try {
			File[] list = tipFolder.listFiles();
			for (File file : list) {
				if (file.isDirectory()) {
					populateTipUrls(file);
				} else if (file.getName().endsWith(".html")
						|| file.getName().endsWith(".htm")) {
					tipUrls.add(file.toURL().toString());
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
