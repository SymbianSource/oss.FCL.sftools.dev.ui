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
package com.nokia.tools.s60.internal.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.clipboard.ClipboardContentElement;
import com.nokia.tools.media.utils.clipboard.PasteHelper;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.s60.S60SkinnableEntityAdapter;

/**
 * PasteFolderUtils
*/
public class PasteFolderUtils {

	static String[] partNameTags = new String[] { "_side_l", "_side_r",
			"_side_t", "_side_b", "_corner_tl", "_corner_tr", "_corner_bl",
			"_corner_br", "_center" };

	/**
	 * Process folders and files and map file names to theme elements
	 * 
	 * @param theme
	 * @param possibleTarget
	 * 
	 * @param folder
	 * @return
	 */
	public static PasteInfo createPasteInfo(IContent theme, File[] files,
			IContentData possibleTarget) {

		Map<String, IContentData> id2cdMap = createId2ContentDataMap(theme);
		PasteInfo info = new PasteInfo(id2cdMap);

		/*
		 * if(possibleTarget != null && possibleTarget.getChildren().length >
		 * 2){ for(File f : files){ if(f != null){ String name = f.getName();
		 * name = name.substring(0,name.lastIndexOf("."));
		 * 
		 * if( i< possibleTarget.getChildren().length){ id2cdMap.put(name,
		 * possibleTarget.getChildren()[i++]); } } } }
		 */

		for (File file : files) {
			if (file != null) {
				if (file.isDirectory()) {
					processFolder(file, info);
				} else {
					processFile(file, info);

				}
			}
		}

		processResult(info);

		return info;
	}

	/**
	 * Creates ClipboardContentElement structure for PasteContentData action
	 */
	public static ClipboardContentElement createContentElement(
			IContentData elData, File[] files) {

		if (elData != null && files.length != 0) {
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) elData
					.getAdapter(ISkinnableEntityAdapter.class);
			if (skAdapter != null) {
				try {
					INamingAdapter nma = (INamingAdapter) skAdapter
							.getAdapter(INamingAdapter.class);

					String elName = nma == null ? elData.getName() : nma
							.getName();
					// if (!skAdapter.isNinePiece()) {
					if (!skAdapter.isMultiPiece()) {
						String caption = getGroupName(elData.getParent())
								+ "\\" + elName;
						return new ClipboardContentElement(
								Arrays.asList(files), 1, elData.getId(),
								caption);
					} else {
						
						String caption = getGroupName(elData.getParent())
								+ "\\" + elName;
						return new ClipboardContentElement(
								Arrays.asList(files), 1, elData.getId(),
								caption, IMediaConstants.NINE_PIECE_COPY_INFO);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private static Map<String, IContentData> createId2ContentDataMap(
			IContent theme) {
		IContentData[] children = theme.getAllChildren();
		Map<String, IContentData> id2cdMap = new HashMap<String, IContentData>();
		for (IContentData data : children) {
			String id = data.getId();
			if (id != null) {
				id.replace(' ', '_');
				id2cdMap.put(id, data);
			}
		}
		return id2cdMap;
	}

	/**
	 * returns full 'path' name to given group/category.
	 * 
	 * @param elData
	 * @return
	 */
	public static Object getGroupName(IContentData elData) {
		StringBuffer result = new StringBuffer();
		String origName = elData.getName();
		while (elData.getParent() != null) {
			result.insert(0, elData.getName());
			if (elData.getParent().getParent() != null)
				result.insert(0, "\\");
			elData = elData.getParent();
			if (elData.getName().equals(origName))
				elData = elData.getParent();
		}
		return result.toString();
	}

	/**
	 * walks though all files which was resolved and rearanges them to the right
	 * order for IContentData which use more files (animations, layers,
	 * nine-piece elements, ...). So the result will be that PasteInfo structure
	 * will have Map of resolved files in right order and which really contains
	 * files that could be pasted=CTRL-V (processed) correctly.
	 * 
	 * @param info
	 *            in/out parameter for rearranging files of IContentData's
	 */
	private static void processResult(PasteInfo info) {
		Set<Entry<IContentData, List<File>>> entries = info.resolvedFiles
				.entrySet();
		List<IContentData> toRemove = new ArrayList<IContentData>();
		for (Entry<IContentData, List<File>> entry : entries) {
			IContentData data = entry.getKey();
			List<File> files = entry.getValue();

			for (File file : files.toArray(new File[0])) {
				String name2 = removeNumbersAtEnd(file.getName());
				if (name2.endsWith("_i")) {
					files.remove(file);
				}
			}

			ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);

			if (adapter instanceof S60SkinnableEntityAdapter) {

				if (adapter == null || adapter.isColour()) { 
					
					info.unresolvedFiles.addAll(files);
					toRemove.add(data);
					continue;
				}

				if (files.size() > 1) {// rearanging is needed for IContentData
					// which use more files
					// (animations, layers, nine-piece elements, ...)
					IImageAdapter imgAdapter = (IImageAdapter) data
							.getAdapter(IImageAdapter.class);

					if (imgAdapter.isAnimated()) {
						
						List<File> newFiles = new ArrayList<File>();
						Map<File, File> newFileMasks = new HashMap<File, File>();
						for (File file : files.toArray(new File[0])) {
							if (!isMaskFile(file)) {
								File maskFile = findMaskFile(file, files);
								newFiles.add(file);
								newFileMasks.put(file, maskFile);
								files.remove(file);
								files.remove(maskFile);
							}
						}

						// update unresolved files
						info.unresolvedFiles.addAll(files);

						// sort files in correct order
						Collections.sort(newFiles, new Comparator<File>() {
							public int compare(File o1, File o2) {
								long f1 = getFrameNumber(o1);
								long f2 = getFrameNumber(o2);

								return (f1 < f2) ? -1 : (f1 > f2 ? 1 : 0);
							}

							private long getFrameNumber(File o1) {
								String name = removeExtension(o1.getName());
								if (removeNumbersAtEnd(name).endsWith("_")) {
									// second, third, ... frame
									name = name.substring(0, name.length() - 1);
									String nums = getNumbersAtEnd(name);
									if (nums.length() > 0) {
										return Long.parseLong(nums);
									} else {
										return 0L;
									}
								} else {
									// first frame sometime doesn't have "_1"
									// suffix
									// so place it at begining
									return -1L;
								}
							}
						});

						// re add sorted files
						files.clear();
						for (File file : newFiles) {
							files.add(file);
							File mask = newFileMasks.get(file);
							if (mask != null) {
								files.add(mask);
							}
						}

						continue;
					}

					IImage img = imgAdapter.getImage(true);
					int noOfPic = 0;
					boolean isMultiPiece = true;

					try {
						noOfPic = img.getPartInstances().size();
						if (noOfPic != 0)
							isMultiPiece = true;
						else
							isMultiPiece = false;
					} catch (Exception e) {
						isMultiPiece = false;
					}
					/*
					 * if(img.supportsElevenPiece()){ noOfPic = 11; } else if
					 * (img.supportsNinePiece()) { noOfPic = 9; } else if
					 * (img.supportsThreePiece()) { noOfPic = 3; } else {
					 * isMultiPiece = false; }
					 */

					if (isMultiPiece) {// it's
						// nine-piece
						// element
						// nine piece, sort pieces according to center, t, b, l,
						// r,
						// ... identificators

						List<String> partNames = new ArrayList<String>(noOfPic);
						partNames.addAll(Arrays.asList(partNameTags));

						File[] nineFiles = new File[noOfPic];
						File[] nineMasks = new File[noOfPic];
						boolean empty = true;
						for (File file : files.toArray(new File[0])) {
							for (int i = 0; i < partNameTags.length; i++) {
								if (!isMaskFile(file)) {
									String partName = partNameTags[i];
									if (file.getName().toLowerCase().indexOf(
											partName) > 0) {
										nineFiles[i] = file;
										files.remove(file);
										empty = false;
										break;
									}
								} else {
									String partName = partNameTags[i];
									if (file.getName().toLowerCase().indexOf(
											partName) > 0) {
										nineMasks[i] = file;
										files.remove(file);
										empty = false;
										break;
									}
								}
							}
						}

						// update unresolved files
						// info.unresolvedFiles.addAll(files);

						// re add sorted files
						files.clear();
						if (!empty) {
							files.addAll(Arrays.asList(nineFiles));
							files.addAll(Arrays.asList(nineMasks));
						}

						continue;
					}

					// This is special case where only one image file and mask
					// file
					// are correct.
					// But there is more than one file of image and/or it's
					// mask. We
					// need to find the correct one.
					// So we'll choose that one with greatest number as it's
					// name's
					// prefix or without number at all.
					// more than one file, but no animation, no nine piece...
					// use image and mask with max number suffix
					// image without number suffix have maximum priority
					long maxnum = -1;
					long maxmasknum = -1;
					File maxfile = null;
					File maxmaskfile = null;
					for (File file : files) {
						if (!isMaskFile(file)) {
							String name = removeExtension(file.getName());
							String number = getNumbersAtEnd(name);
							if (number.length() > 0) {
								long num = Long.parseLong(number);
								if (num > maxnum) {
									maxnum = num;
									maxfile = file;
								}
							} else {
								long num = Long.MAX_VALUE;
								if (num > maxnum) {
									maxnum = num;
									maxfile = file;
								}
							}
						} else {
							String name = removeExtension(file.getName());
							String number = getNumbersAtEnd(name);
							if (number.length() > 0) {
								long num = Long.parseLong(number);
								if (num > maxmasknum) {
									maxmasknum = num;
									maxmaskfile = file;
								}
							} else {
								long num = Long.MAX_VALUE;
								if (num > maxmasknum) {
									maxmasknum = num;
									maxmaskfile = file;
								}
							}
						}
					}

					if (maxfile != null) {
						files.remove(maxfile);
					}

					if (maxmaskfile != null) {
						files.remove(maxmaskfile);
					}

					// update unresolved files
					// info.unresolvedFiles.addAll(files);

					files.clear();

					if (maxfile != null) {
						files.add(maxfile);
					}

					if (maxmaskfile != null) {
						files.add(maxmaskfile);
					}
				}
			}
		}

	}

	private static boolean isMaskFile(File file) {
		String plainName = getPlainName(file.getName()).toLowerCase();
		boolean mask = plainName.endsWith("_mask_soft")
				|| plainName.endsWith("_mask_hard")
				|| plainName.endsWith("_mask");
		return mask;
	}

	/**
	 * Finds mask file for a image file (bitmap, png or jpg, ...)
	 * 
	 * @param forImage
	 *            file for which we search of its mask
	 * @param files
	 *            list of files where we do searching
	 * @return found mask file or <code>null</code> if mask file wasn't found.
	 */
	private static File findMaskFile(File forImage, List<File> files) {
		for (File file : files) {
			if (isMaskFile(file)) {
				String plainName = getPlainName(file.getName()).toLowerCase();
				String maskPrefix = plainName.substring(0, plainName
						.lastIndexOf("_mask"));
				if (removeExtension(forImage.getName()).toLowerCase().equals(
						maskPrefix)) {
					return file;
				}
				if (getPlainName(forImage.getName()).toLowerCase().equals(
						maskPrefix)) {
					return file;
				}
			}
		}
		return null;
	}

	private static String getPlainName(String name) {
		String plainName = removeExtension(name);
		plainName = removeNumbersAtEnd(plainName);
		if (plainName.endsWith("_")) {
			plainName = plainName.substring(0, plainName.length() - 1);
		}
		return plainName;
	}

	private static String removeExtension(String name) {
		if (name.lastIndexOf('.') > 0) {
			name = name.substring(0, name.lastIndexOf('.'));
		}
		return name;
	}

	private static String removeNumbersAtEnd(String name) {
		StringBuffer sb = new StringBuffer();
		boolean digit = true;
		for (int i = name.length() - 1; i >= 0; i--) {
			char ch = name.charAt(i);
			if (digit && Character.isDigit(ch)) {
				continue;
			} else {
				digit = false;
			}
			sb.insert(0, ch);
		}
		return sb.toString();
	}

	private static String getNumbersAtEnd(String name) {
		StringBuffer sb = new StringBuffer();
		boolean digit = true;
		for (int i = name.length() - 1; i >= 0; i--) {
			char ch = name.charAt(i);
			if (digit && Character.isDigit(ch)) {
				sb.insert(0, ch);
			} else {
				digit = false;
			}
		}
		return sb.toString();
	}

	private static void processFolder(File folder, PasteInfo info) {
		if (!folder.isDirectory()) {
			return;
		}

		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				processFolder(file, info);
			} else {
				processFile(file, info);
			}
		}
	}

	/**
	 * For one file it tries to find IContentData which belongs to this file
	 * (identified by its name)
	 * 
	 * @param file
	 *            processed file to which we try to find its content data
	 * @param info
	 *            structure containing all content data of current theme mapped
	 *            by its ids. Next it holds list for unresolved files and Map
	 *            for resolved files in form <IContentData, List<File>>
	 */
	private static void processFile(File file, PasteInfo info) {
		if (PasteHelper.isParameterUsableAsImage(file)) {
			String name = file.getName();
			if (name.lastIndexOf('.') > 0) {
				name = name.substring(0, name.lastIndexOf('.'));
			}

			name = name.replace(' ', '_');

			// remove nine piece part name
			/*
			 * for (int i = 0; i < partNameTags.length; i++) { if
			 * (name.toLowerCase().indexOf(partNameTags[i]) > 0) { name =
			 * name.replaceAll(partNameTags[i], ""); break; } }
			 */

			IContentData themeData = null;

			while (name.length() > 0) {
				IContentData data = info.id2cdMap.get(name);
				if (data != null) {
					themeData = data;
					break;
				}

				name = name.substring(0, name.length() - 1);
			}

			/*
			 * Do not just compare by appending _anim we may have to copy the
			 * file if (themeData == null) { // try append "anim" suffix and
			 * retry
			 * 
			 * name = file.getName(); if (name.lastIndexOf('.') > 0) { name =
			 * name.substring(0, name.lastIndexOf('.')); }
			 * 
			 * name = name.replace(' ', '_');
			 * 
			 * while (name.length() > 0) { IContentData data =
			 * info.id2cdMap.get(name + "_anim"); if (data != null) { themeData
			 * = data; break; }
			 * 
			 * name = name.substring(0, name.length() - 1); } }
			 */

			if (themeData == null) {
				info.unresolvedFiles.add(file);

			} else {
				List<File> files = info.resolvedFiles.get(themeData);
				if (files == null) {
					files = new ArrayList<File>();
					info.resolvedFiles.put(themeData, files);
				}

				files.add(file);
			}
		}
	}

	public static class PasteInfo {
		Map<String, IContentData> id2cdMap;

		Map<IContentData, List<File>> resolvedFiles = new HashMap<IContentData, List<File>>();

		List<File> unresolvedFiles = new ArrayList<File>();

		public PasteInfo(Map<String, IContentData> id2cdMap2) {
			this.id2cdMap = id2cdMap2;
		}

		public Map<IContentData, List<File>> getResolvedFiles() {
			return resolvedFiles;
		}

		public List<File> getUnresolvedFiles() {
			return unresolvedFiles;
		}
	}

}
