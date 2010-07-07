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

package com.nokia.tools.theme.s60.editing;

import java.awt.image.RenderedImage;
import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;

import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.ITimeLineTreeContentProvider;
import com.nokia.tools.media.utils.timeline.ITreeTimeLineDataProvider;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.media.utils.timeline.cp.IControlPointModel;
import com.nokia.tools.media.utils.timeline.cp.IControlPointMovingListener;
import com.nokia.tools.media.utils.timeline.impl.TimeLineNode;
import com.nokia.tools.media.utils.timeline.impl.TimeLineRow;
import com.nokia.tools.media.utils.timeline.impl.cp.ControlPointModel;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.theme.AnimatedThemeGraphic;
import com.nokia.tools.platform.theme.AnimatedThemeGraphicCache;
import com.nokia.tools.platform.theme.ColourGraphic;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.SoundGraphic;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeGraphicInterface;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.theme.s60.editing.providers.BaseTimeLineProvider;
import com.nokia.tools.theme.s60.editing.providers.TimeLineTreeContentProvider;
import com.nokia.tools.theme.s60.model.MorphedGraphic;
import com.nokia.tools.theme.s60.model.S60Theme;


public class EditableAnimatedEntity extends EditableEntityImage implements
		IAnimatedImage {

	protected ITimeLineRow timeLineRow;

	protected Map<ThemeGraphic, IAnimationFrame> gr2ImgMap = new HashMap<ThemeGraphic, IAnimationFrame>();

	protected AnimatedImageControlPointModel controlPointModel;

	protected long defaultAnimateTime = Long
			.parseLong(ThemeTag.ATTR_ANIMATE_DEFAULT_TIME);

	
	public EditableAnimatedEntity(SkinnableEntity se, int width, int height)
			throws ThemeException {
		super(se, se.getAnimatedObject1(), null, width, height);
	}

	public EditableAnimatedEntity(SkinnableEntity se) throws ThemeException {
		super(se, se.getAnimatedObject1(), null, 0, 0);
	}

	public IAnimationFrame[] getAnimationFrames() {
		List<IAnimationFrame> animationImages = new ArrayList<IAnimationFrame>();

		List<ThemeGraphic> themeGraphicsList = null;

		ThemeGraphic tg = getThemeGraphics();

		if (tg instanceof AnimatedThemeGraphic) {
			themeGraphicsList = ((AnimatedThemeGraphic) tg).getThemeGraphics();
		} else if (tg instanceof AnimatedThemeGraphicCache) {
			themeGraphicsList = ((AnimatedThemeGraphicCache) tg)
					.getThemeGraphics();
		}

		if (themeGraphicsList != null) {

			for (ThemeGraphic graphic : themeGraphicsList) {
				if (gr2ImgMap.containsKey(graphic)) {
					animationImages.add(gr2ImgMap.get(graphic));
				} else {
					IAnimationFrame animImg = new EditableAnimationFrame(this,
							graphic);

					animationImages.add(animImg);
					gr2ImgMap.put(graphic, animImg);
				}
			}

			return (IAnimationFrame[]) animationImages
					.toArray(new IAnimationFrame[animationImages.size()]);
		}

		return new IAnimationFrame[0];
	}

	@Override
	public boolean canBeAnimated() {
		return true;
	}

	@Override
	public boolean isAnimatedFor(TimingModel timingType) {
		// frame animations don't work on animation timing models
		return false;
	}

	@Override
	public boolean isAnimated() {
		return getAnimationFrames().length > 1;
	}

	public IAnimationFrame createNewAnimationFrame(String imagePath) {
		File newFile = null;
		newFile = new File(imagePath);

		ThemeGraphic tg = prepareThemeGraphic(getEntity(), newFile
				.getAbsolutePath(), "" + getDefaultAnimateTime(), true);

		tg.setAttribute(ThemeTag.ATTR_STATUS, ThemeTag.ATTR_VALUE_ACTUAL);

		EditableAnimationFrame animImg = new EditableAnimationFrame(this, tg);

		gr2ImgMap.put(tg, animImg);

		((AnimatedImageControlPointModel) getControlPointModel())
				.addControlPoint(animImg);

		// fire structure change event
		propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null, null);

		return animImg;
	}

	public IAnimationFrame createNewAnimationFrame() {
		File newFile = null;
		try {
			newFile = File.createTempFile("img", ".svg");
			File emptySvg = FileUtils.getFile(FileUtils.getURL(
					PlatformCorePlugin.getDefault(), "image/transparent.svg"));
			FileUtils.copyFile(emptySvg, newFile);
			return createNewAnimationFrame(newFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (newFile != null) {
				newFile.delete();
			}
		}
		return null;
	}

	public void addAnimationFrame(IAnimationFrame frame) {
		if (!(frame instanceof EditableAnimationFrame)) {
			throw new UnsupportedOperationException();
		}

		EditableAnimationFrame fImage = (EditableAnimationFrame) frame;

		ThemeGraphic tg = getThemeGraphics();

		if (tg instanceof AnimatedThemeGraphic) {
			((AnimatedThemeGraphic) tg).addThemeGraphic(fImage.graphic);
			gr2ImgMap.put(fImage.graphic, fImage);
			((AnimatedImageControlPointModel) getControlPointModel())
					.addControlPoint(fImage);

			// fire structure change event
			propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null, null);

		} else if (tg instanceof AnimatedThemeGraphicCache) {
			((AnimatedThemeGraphicCache) tg).addThemeGraphic(fImage.graphic);
			gr2ImgMap.put(fImage.graphic, fImage);
			((AnimatedImageControlPointModel) getControlPointModel())
					.addControlPoint(fImage);

			// fire structure change event
			propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null, null);
		}
	}

	public void removeAnimationFrame(IAnimationFrame image) {
		EditableAnimationFrame fImage = (EditableAnimationFrame) image;

		ThemeGraphic tg = getThemeGraphics();

		try {
			if (tg instanceof AnimatedThemeGraphic) {
				List themeGraphics = ((AnimatedThemeGraphic) tg)
						.getThemeGraphics();
				int idx = 0;
				for (Object object : themeGraphics) {
					if (object == fImage.graphic) {
						((AnimatedThemeGraphic) tg).removeThemeGraphic(idx);
						gr2ImgMap.remove(fImage.graphic);
						((AnimatedImageControlPointModel) getControlPointModel())
								.silentRemoveControlPoint(fImage);

						// fire structure change event
						propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE,
								null, null);

						break;
					}
					idx++;
				}
			} else if (tg instanceof AnimatedThemeGraphicCache) {
				List themeGraphics = ((AnimatedThemeGraphic) tg)
						.getThemeGraphics();
				int idx = 0;
				for (Object object : themeGraphics) {
					if (object == fImage.graphic) {
						((AnimatedThemeGraphicCache) tg)
								.removeThemeGraphic(idx);
						gr2ImgMap.remove(fImage.graphic);
						((AnimatedImageControlPointModel) getControlPointModel())
								.silentRemoveControlPoint(fImage);

						// fire structure change event
						propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE,
								null, null);

						break;
					}
					idx++;
				}
			}
		} catch (ThemeException e) {
			e.printStackTrace();
		}
	}

	public void moveAnimationFrame(IAnimationFrame image, int newPosition) {
		moveAnimationFrame(image, newPosition, false);
	}

	public void moveAnimationFrame(IAnimationFrame image, int newPosition,
			boolean silent) {
		EditableAnimationFrame fImage = (EditableAnimationFrame) image;
		String seqNo = fImage.graphic.getAttribute(ThemeTag.ATTR_ANIMATE_SEQNO);
		List<String> seqNos = new ArrayList<String>();
		seqNos.add(seqNo);

		ThemeGraphic tg = getThemeGraphics();

		if (tg instanceof AnimatedThemeGraphic) {
			((AnimatedThemeGraphic) tg).rearrangeThemeGraphics(seqNos,
					newPosition);

			if (!silent) {
				// fire structure change event
				propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null,
						null);
			}

		} else if (tg instanceof AnimatedThemeGraphicCache) {
			((AnimatedThemeGraphicCache) tg).rearrangeThemeGraphics(seqNos,
					newPosition);

			if (!silent) {
				// fire structure change event
				propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null,
						null);
			}
		}
	}

	@Override
	public ITreeTimeLineDataProvider getRealTimeTimeModelTimeLineProvider() {
		return new RealTimeTreeDataProvider();
	}

	@Override
	public ITreeTimeLineDataProvider getRelativeTimeModelTimeLineProvider(
			TimeSpan span) {
		return new RealTimeTreeDataProvider();
	}

	protected ThemeGraphic prepareThemeGraphic(SkinnableEntity se,
			String fileName, String time, boolean add) {

		try {
			ThemeGraphic tg = null;

			if (se.isEntityType().equals(ThemeTag.ELEMENT_COLOUR))
				tg = new ColourGraphic(se);
			else if (se.isEntityType().equals(ThemeTag.ELEMENT_SOUND)
					|| se.isEntityType().equals(ThemeTag.ELEMENT_EMBED_FILE))
				tg = new SoundGraphic(se);
			else if ((se.getToolBox() != null)
					&& (se.getToolBox().multipleLayersSupport)) {
				tg = new MorphedGraphic(se);
			} else
				tg = new ThemeGraphic(se);

			ImageLayer il = new ImageLayer(tg);
			il.setAttribute(ThemeTag.ATTR_ENTITY_Y,
					ThemeTag.ATTR_ENTITY_Y_DEFAULT);
			il.setAttribute(ThemeTag.ATTR_IMAGE_X,
					ThemeTag.ATTR_IMAGE_X_DEFAULT);
			il.setAttribute(ThemeTag.ATTR_STRETCH,
					ThemeTag.ATTR_STRETCH_DEFAULT);
			il.setAttribute(ThemeTag.ATTR_ANGLE, ThemeTag.ATTR_ANGLE_DEFAULT);
			il.setAttribute(ThemeTag.ATTR_STATUS, ThemeTag.ATTR_VALUE_DRAFT);
			il.setAttribute(ThemeTag.ATTR_IMAGE_Y,
					ThemeTag.ATTR_IMAGE_Y_DEFAULT);
			il.setAttribute(ThemeTag.ATTR_ENTITY_X,
					ThemeTag.ATTR_ENTITY_X_DEFAULT);
			il.setAttribute(ThemeTag.ATTR_COLOURDEPTH,
					ThemeTag.ATTR_VALUE_COLOUR_DEPTH_DEFAULT);
			il.setAttribute(ThemeTag.ATTR_NAME, "layer0");

			if (add) {
				Object animObj = getThemeGraphics();
				if (animObj != null) {
					String newFileName = getNewFileName(getEntity()
							.getIdentifier(), "."
							+ new Path(fileName).getFileExtension());

					FileUtils.copyFile(fileName, newFileName);

					il.setAttribute(ThemeTag.FILE_NAME, new File(newFileName)
							.toString());

					tg.setAttribute(ThemeTag.ATTR_ANIMATE_TIME, time);
					tg.setAttribute(ThemeTag.ATTR_TYPE, ThemeTag.ATTR_BMPANIM);
					tg.setAttribute(ThemeTag.UNIQUE_ID,
							getUniqueID(((ThemeGraphicInterface) animObj)
									.getThemeGraphics()));

					tg.setImageLayers(il);
					((ThemeGraphicInterface) animObj).addThemeGraphic(tg);
				}
			} else {
				tg.setImageLayers(il);
			}

			return tg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getNewFileName(String prefix, String fileType)
			throws ThemeException {
		prefix = prefix.replace('%', '_');
		String newFileName = prefix + fileType;
		File newFile = new File(getWorkDir(), newFileName);

		int i = 0;
		while (newFile.exists()) {
			i++;
			newFileName = prefix + i + fileType;
			newFile = new File(getWorkDir(), newFileName);
		}

		return newFile.getAbsolutePath().toString();
	}

	public File getWorkDir() {
		S60Theme theme = (S60Theme) getEntity().getRoot();
		String themeName = theme.getAttributeValue("name1");
		File tmpDir = new File(FileUtils.getTemporaryDirectory(), themeName);
		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
			tmpDir.deleteOnExit();
		}
		return tmpDir;
	}

	public File getThemeDir() {
		File themeDir = new File(((S60Theme) getEntity().getRoot())
				.getThemeDir());
		return themeDir;
	}

	/**
	 * To get unique id for a ThemeGraphic in a Animated skinnableentity
	 * 
	 * @return string uniqueid
	 */
	private static String getUniqueID(List tgList) {
		int id = 0;
		if (tgList != null) {
			for (int i = 0; i < tgList.size(); i++) {
				String uid = ((ThemeGraphic) tgList.get(i))
						.getAttribute(ThemeTag.UNIQUE_ID);
				if (Integer.parseInt(uid) > id)
					id = Integer.parseInt(uid);
			}
		}
		id = id + 1;
		return id + "";
	}

	/**
	 * @return
	 */
	private ITimeLineTreeContentProvider getTreeContentProvider() {

		ITimeLineTreeContentProvider p = new ITimeLineTreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof TreeRoot) {
					return ((TreeRoot) parentElement).getChildren();
				}
				return TimeLineTreeContentProvider.EMPTY;
			}

			public Object getParent(Object element) {
				return null;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof TreeRoot) {
					return ((TreeRoot) element).getChildren().length > 0;
				}
				return false;
			}

			/*
			 * (non-Javadoc) Returns roots for tree.
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

			public boolean isSelected(Object element) {
				return true;
			}

		};
		return p;
	}

	class RealTimeTreeDataProvider extends BaseTimeLineProvider {

		public RealTimeTreeDataProvider() {
			super((getAnimationDuration() / 1000 + 1) * 1000);
		}

		public ITimeLineTreeContentProvider getTreeContentProvider() {
			return EditableAnimatedEntity.this.getTreeContentProvider();
		}

		public ILabelProvider getTreeLabelProvider() {
			return new LabelProvider() {
				public String getText(Object element) {
					if (element instanceof EditableAnimatedEntity) {
						return ((EditableAnimatedEntity) element).getEntity()
								.getName();
					}
					return "BAD!";
				}
			};
		}

		public int getMajorGridInterval() {
			return 100;
		}

		public int getMinorGridInterval() {
			return 20;
		}

		public int getClockIncrement() {
			return 20;
		}

		public int getClockTimePerIncrement() {
			return 20;
		}

		public Object getInput() {
			return new TreeRoot();
		}

		public ITimeLineRow getRowForTreeElement(Object object) {
			if (object instanceof IAnimatedImage) {
				return getTimeLineRow();
			}
			return null;
		}

		public boolean getClockAutorepeat() {
			return true;
		}

	}

	public ITimeLineRow getTimeLineRow() {
		if (timeLineRow == null) {
			timeLineRow = new TimeLineRow(this);
			timeLineRow.addNode(new AnimatedImageTimeLineNode(timeLineRow));
		}
		return timeLineRow;
	}

	@Override
	public long getAnimationDuration() {
		long overallTime = 0;
		if (getAnimationFrames().length > 1) {
			Vector times = getTimeDelaysForAnimate();
			for (Object object : times) {
				Integer time = (Integer) object;
				overallTime += time;
			}
		}
		return overallTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicEntityImage#getAggregateImage(com.nokia.tools.media.utils.layers.TimingModel,
	 *      long, boolean)
	 */
	@Override
	public RenderedImage getAggregateImage(TimingModel timing, long time,
			boolean preview) {
		IAnimationFrame image = getAnimationFrame(time);

		if (image != null) {
			return image.getImage();
		} else {
			return null;
		}
	}

	public IAnimationFrame getAnimationFrame(long time) {
		long overallTime = 0;
		IAnimationFrame[] images = getAnimationFrames();

		if (images.length == 0) {
			return null;
		}

		Vector times = getTimeDelaysForAnimate();
		int idx = 0;
		for (Object object : times) {
			overallTime += (Integer) object;
			if (overallTime > time) {
				break;
			} else {
				idx++;
			}
		}

		idx = Math.max(0, Math.min(idx, images.length - 1));

		return images[idx];
	}

	public Vector getTimeDelaysForAnimate() {
		Vector timeVector = new Vector();
		List list = null;
		Integer integer = null;
		try {
			ThemeGraphic atg = getThemeGraphics();
			if (atg instanceof AnimatedThemeGraphic) {
				list = ((AnimatedThemeGraphic) atg).getThemeGraphics();
			} else if (atg instanceof AnimatedThemeGraphicCache) {
				list = ((AnimatedThemeGraphicCache) atg).getThemeGraphics();
			}

			if (list.size() != 0) {
				for (int i = 0; i < list.size(); i++) {
					ThemeGraphic tg = (ThemeGraphic) list.get(i);
					String string = tg.getAttribute(ThemeTag.ATTR_ANIMATE_TIME);
					if (string == null)
						string = ThemeTag.ATTR_ANIMATE_DEFAULT_TIME;
					integer = new Integer(string);
					timeVector.add(integer);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return timeVector;
	}

	public IControlPointModel getControlPointModel() {
		if (controlPointModel == null) {
			controlPointModel = new AnimatedImageControlPointModel(this);
		}
		return controlPointModel;
	}

	class TreeRoot {
		Object[] getChildren() {
			return new Object[] { EditableAnimatedEntity.this };
		}
	}

	protected void markForceTimes() {
		IAnimationFrame[] images = getAnimationFrames();
		for (IAnimationFrame image : images) {
			((EditableAnimationFrame) image).markForceTime();
		}
	}

	protected void sortImagesByForceTime(boolean updateAnimationTimes) {
		List<IAnimationFrame> images = new ArrayList<IAnimationFrame>();

		Collections.addAll(images, getAnimationFrames());
		Collections.sort(images, new Comparator<IAnimationFrame>() {
			public int compare(IAnimationFrame o1, IAnimationFrame o2) {
				return (int) (((EditableAnimationFrame) o1).getForceTime() - ((EditableAnimationFrame) o2)
						.getForceTime());
			}
		});

		// update sequence numbers
		ThemeGraphic atg = getThemeGraphics();
		if (atg instanceof AnimatedThemeGraphic) {
			((AnimatedThemeGraphic) atg).remove();

		} else if (atg instanceof AnimatedThemeGraphicCache) {
			((AnimatedThemeGraphicCache) atg).remove();
		}

		int idx = 0;
		for (IAnimationFrame image : images) {
			ThemeGraphic graphic = ((EditableAnimationFrame) image).graphic;

			if (updateAnimationTimes) {
				IAnimationFrame nextImage = null;
				if (idx + 1 < images.size()) {
					nextImage = images.get(idx + 1);
				}

				if (nextImage != null) {
					long animTime = ((EditableAnimationFrame) nextImage)
							.getForceTime()
							- ((EditableAnimationFrame) image).getForceTime();
					animTime = Math.max(10, animTime);
					graphic.setAttribute(ThemeTag.ATTR_ANIMATE_TIME, ""
							+ animTime);
				}
			}

			if (atg instanceof AnimatedThemeGraphic) {
				((AnimatedThemeGraphic) atg).addThemeGraphic(graphic);

			} else if (atg instanceof AnimatedThemeGraphicCache) {
				((AnimatedThemeGraphicCache) atg).addThemeGraphic(graphic);
			}
			idx++;
		}
	}

	public long getDefaultAnimateTime() {
		return defaultAnimateTime;
	}

	public void setDefaultAnimateTime(long defaultAnimateTime) {
		this.defaultAnimateTime = defaultAnimateTime;
	}

	public void setAnimateTime(IAnimationFrame frame, long time) {
		ThemeGraphic graphic = ((EditableAnimationFrame) frame).graphic;
		graphic.setAttribute(ThemeTag.ATTR_ANIMATE_TIME, "" + time);

		propsup.firePropertyChange(PROPERTY_STATE, null, null);
	}

	@Override
	public ThemeGraphic getThemeGraphics(boolean makePathsAbsolute)
			throws Exception {
		return makePathsAbsolute ? getSavedThemeGraphics(true) : super
				.getThemeGraphics(makePathsAbsolute);
	}

	@Override
	public ThemeGraphic getSavedThemeGraphics(boolean forceAbsolute)
			throws Exception {
		String identifier = getEntity().getIdentifier().replace("%", "_");

		File themeDir = new File(((S60Theme) getEntity().getRoot())
				.getThemeDir());

		S60Theme theme = (S60Theme) getEntity().getRoot();
		String themeName = theme.getAttributeValue("name1");
		String tempDir = FileUtils.getTemporaryDirectory() + File.separator
				+ themeName;

		File tmpDir = new File(tempDir, ""
				+ Math.abs(SecureRandom.getInstance("SHA1PRNG").nextInt()));
		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
			tmpDir.deleteOnExit();
		}

		IPath themePath = new Path(themeDir.getAbsolutePath());
		IPath workPath = new Path(getWorkDir().getAbsolutePath());

		Map<File, File> stage1Map = new HashMap<File, File>();

		IAnimationFrame[] frames = getAnimationFrames();
		int idx = 0;
		for (IAnimationFrame frame : frames) {
			File imageFile = frame.getImageFile();
			File maskFile = frame.getMaskFile();

			File destImageFile = null;

			if (imageFile != null) {
			
				destImageFile = new File(tmpDir, identifier
						+ "_"
						+ (idx + 1)
						+ imageFile.getAbsolutePath().substring(
								imageFile.getAbsolutePath().lastIndexOf('.')));
				
			}

			File destMaskFile = null;

			if (maskFile != null) {
				
				destMaskFile = new File(
						tmpDir,
						identifier
								+ "_"
								+ (idx + 1)
								+ (getEntity().getToolBox().SoftMask ? ThemeTag.SOFTMASK_FILE
										: ThemeTag.MASK_FILE)
								+ maskFile.getAbsolutePath().substring(
										maskFile.getAbsolutePath().lastIndexOf(
												'.')));
				
			}

			if (imageFile != null) {
				stage1Map.put(imageFile, destImageFile);
			}

			if (maskFile != null) {
				stage1Map.put(maskFile, destMaskFile);
			}

			idx++;
		}

		// stage1
		for (File file : stage1Map.keySet()) {
			File destFile = stage1Map.get(file);
			if (!file.equals(destFile)) {
				if (destFile.exists()) {
					destFile.delete();
				}
				if (file.exists())
					FileUtils.copyFile(file.getAbsolutePath(), destFile
							.getAbsolutePath());
				destFile.deleteOnExit();
			}
		}

		// stage2
		for (IAnimationFrame frame : frames) {
			File imageFile = frame.getImageFile();
			File maskFile = frame.getMaskFile();

			if (imageFile != null) {
				File destFile = stage1Map.get(imageFile);
				if (!imageFile.equals(destFile)) {
					((IImageHolder) frame).setImageFile(destFile);
					if (workPath.isPrefixOf(new Path(imageFile
							.getAbsolutePath()))) {
						imageFile.deleteOnExit();
					}
				}
			}

			if (maskFile != null) {
				File destFile = stage1Map.get(maskFile);
				if (!maskFile.equals(destFile)) {
					((IImageHolder) frame).setMaskFile(destFile);
					if (workPath
							.isPrefixOf(new Path(maskFile.getAbsolutePath()))) {
						maskFile.deleteOnExit();
					}
				}
			}
		}

		// refresh eclipse resources
		IPath destPath = new Path(getWorkDir().toString());
		IContainer[] cont = ResourcesPlugin.getWorkspace().getRoot()
				.findContainersForLocation(destPath);
		if (cont.length > 0) {
			try {
				cont[0].refreshLocal(IResource.DEPTH_INFINITE,
						new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return super.getSavedThemeGraphics(forceAbsolute);
	}

	class AnimatedImageTimeLineNode extends TimeLineNode {

		public AnimatedImageTimeLineNode(ITimeLineRow row) {
			super(row, 0, getAnimationDuration());
			setControlPointModel(EditableAnimatedEntity.this
					.getControlPointModel());
		}

		@Override
		public long getEndTime() {
			return getAnimationDuration();
		}

		@Override
		public long getStartTime() {
			return 0;
		}

	}

	class AnimatedImageControlPointModel extends ControlPointModel implements
			IControlPointMovingListener {

		protected Map<IControlPoint, UndoableMoveCommand> moveCommands = new HashMap<IControlPoint, UndoableMoveCommand>();

		public AnimatedImageControlPointModel(EditableAnimatedEntity entity) {
			super();
			IAnimationFrame[] images = entity.getAnimationFrames();
			for (IAnimationFrame image : images) {
				silentAddControlPoint((EditableAnimationFrame) image);
			}
		}

		public void silentAddControlPoint(IControlPoint cp) {
			synchronized (controlPoints) {
				controlPoints.add(cp);
			}
		}

		@Override
		public IControlPoint createControlPoint(long time) {
			final IAnimationFrame img = getAnimationFrame(time);

			final IAnimationFrame newImage = EditableAnimatedEntity.this
					.createNewAnimationFrame();

			if (img != null) {
				EditableAnimatedEntity.this.moveAnimationFrame(newImage, img
						.getSeqNo() + 1);
			}

			Command command = new Command(Messages.CreateCP_label) {
				@Override
				public boolean canExecute() {
					return newImage != null;
				}

				@Override
				public boolean canUndo() {
					return newImage != null;
				}

				@Override
				public void execute() {
				}

				@Override
				public void undo() {
					removeAnimationFrame(newImage);
				}

				@Override
				public void redo() {
					addAnimationFrame(newImage);
					if (img != null) {
						EditableAnimatedEntity.this.moveAnimationFrame(
								newImage, img.getSeqNo() + 1);
					}
				}
			};

			CommandStack commandStack = getCommandStack();
			if (commandStack != null) {
				commandStack.execute(command);
			} else {
				if (command.canExecute()) {
					command.execute();
				}
			}

			return (IControlPoint) newImage;
		}

		@Override
		public void moveControlPoint(IControlPoint movedControlPoint, long time) {

			int seqNo = ((EditableAnimationFrame) movedControlPoint).getSeqNo();

			if (seqNo == 0) {
				return;
			}

			IAnimationFrame[] images = getAnimationFrames();

			IAnimationFrame prev = images[seqNo - 1];

			if (time - ((IControlPoint) prev).getTime() < IAnimationFrame.MINIMUM_ANIMATE_TIME) {
				return;
			}

			markForceTimes();

			((EditableAnimationFrame) movedControlPoint).setForceTime(time);

			for (IAnimationFrame image : images) {
				if (image.getSeqNo() > seqNo) {
					((EditableAnimationFrame) image)
							.setForceTime(((EditableAnimationFrame) image)
									.getForceTime()
									+ time - movedControlPoint.getTime());
				}
			}

			sortImagesByForceTime(true);

			notifyControlPointMoved(movedControlPoint);
		}

		public void addControlPoint(IControlPoint cp) {
			synchronized (controlPoints) {
				controlPoints.add(cp);
			}
			notifyControlPointCreated(cp);
		}

		@Override
		public void removeControlPoint(IControlPoint cp) {
			final IAnimationFrame frame = ((IAnimationFrame) cp);

			Command command = new Command(Messages.RemoveCP_label) {
				@Override
				public boolean canExecute() {
					return frame != null;
				}

				@Override
				public boolean canUndo() {
					return frame != null;
				}

				@Override
				public void execute() {
					redo();
				}

				@Override
				public void undo() {
					int oldSeqNo = frame.getSeqNo();
					addAnimationFrame(frame);
					EditableAnimatedEntity.this.moveAnimationFrame(frame,
							oldSeqNo);
				}

				@Override
				public void redo() {
					removeAnimationFrame(frame);
					notifyControlPointRemoved((IControlPoint) frame);
				}
			};

			CommandStack commandStack = getCommandStack();
			if (commandStack != null) {
				commandStack.execute(command);
			} else {
				if (command.canExecute()) {
					command.execute();
				}
			}
		}

		protected CommandStack getCommandStack() {
			return (CommandStack) EclipseUtils.getActiveSafeEditor()
					.getAdapter(CommandStack.class);
		}

		public void moveStarted(IControlPoint cp) {
			UndoableMoveCommand command = new UndoableMoveCommand(cp);
			moveCommands.put(cp, command);
		}

		public void moveInProgress(IControlPoint cp) {

		}

		public void moveFinished(IControlPoint cp) {
			UndoableMoveCommand command = moveCommands.get(cp);
			if (command != null) {
				command.newTime = cp.getTime();

				if (!command.oldTime.equals(command.newTime)) {
					CommandStack commandStack = getCommandStack();
					if (commandStack != null) {
						commandStack.execute(command);
					} else {
						if (command.canExecute()) {
							command.execute();
						}
					}
				}

				moveCommands.remove(cp);
			}
		}
	}

	private class UndoableMoveCommand extends Command {
		private IControlPoint moved;

		private Long oldTime, newTime;

		private UndoableMoveCommand(IControlPoint cp) {
			super(Messages.MoveCP_label);
			this.moved = cp;
			this.oldTime = cp.getTime();
		}

		public boolean canExecute() {
			return moved != null && oldTime != null && newTime != null;
		};

		public boolean canUndo() {
			return moved != null && oldTime != null;
		};

		public void execute() {
		};

		public void redo() {
			if (newTime != null) {
				getControlPointModel().moveControlPoint(moved, newTime);
				propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null,
						null);
			}
		};

		public void undo() {
			if (oldTime != null) {
				getControlPointModel().moveControlPoint(moved, oldTime);
				propsup.firePropertyChange(PROPERTY_STRUCTURE_CHANGE, null,
						null);
			}
		};
	}

	@Override
	protected void resolveImages(Theme theme, String tmpDir, String themeDir,
			boolean forceAbsolute, boolean replaceFilenameWithId) {
		// turn off
	}

}
