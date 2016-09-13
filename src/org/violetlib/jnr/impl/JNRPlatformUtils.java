/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import org.jetbrains.annotations.*;

/**
	Platform dependent utilities used by the Java Native Rendering library.
*/

public class JNRPlatformUtils
{
	private static @Nullable String platformVersionString;
	private static int platformVersion = 0;

	public static synchronized int getPlatformVersion()
	{
		if (platformVersionString == null) {
			String osName = System.getProperty("os.name");
			String osVersion = System.getProperty("os.version");

			boolean isOSX = osName.equals("Mac OS X") || osName.equals("macOS");
			if (isOSX) {

				platformVersionString = osVersion;

				int n1 = 0;
				int n2 = 0;
				int n3 = 0;

				StringTokenizer st = new StringTokenizer(osVersion, ".");
				if (st.hasMoreTokens()) {
					n1 = parseComponent(st.nextToken());
					if (st.hasMoreTokens()) {
						n2 = parseComponent(st.nextToken());
						if (st.hasMoreTokens()) {
							n3 = parseComponent(st.nextToken());
						}
					}
				}

				platformVersion = n1 * 10000 + n2 * 100 + n3;
				return platformVersion;
			}
		}

		return platformVersion;
	}

	private static int parseComponent(@NotNull String s)
	{
		try {
			int n = Integer.parseInt(s);
			if (n > 0) {
				return n;
			}
		} catch (NumberFormatException ex) {
		}
		return 0;
	}

	public static @Nullable Graphics2D toGraphics2D(@NotNull Graphics g)
	{
		// Ideally should be able to extract a Graphics2D from a printer graphics
		try {
			return (Graphics2D) g;
		} catch (ClassCastException ex) {
			return null;
		}
	}

	private static final @NotNull WeakHashMap<Graphics,Integer> scaleMap = new WeakHashMap<>();

	public static int getScaleFactor(@NotNull Graphics g)
	{
		// A public API will be provided in JDK 9

		// Is it fair to assume that a graphics context always is associated with the same device,
		// in other words, they are not reused in some sneaky way?
		Integer n = scaleMap.get(g);
		if (n != null) {
			return n;
		}

		int scaleFactor;
		if (g instanceof Graphics2D) {
			Graphics2D gg = (Graphics2D) g;
			GraphicsConfiguration gc = gg.getDeviceConfiguration();
			scaleFactor = getScaleFactor(gc);
		} else {
			scaleFactor = 1;
		}

		scaleMap.put(g, scaleFactor);

		return scaleFactor;
	}

	public static int getScaleFactor(@NotNull GraphicsConfiguration gc)
	{
		GraphicsDevice device = gc.getDevice();
		Object scale = null;

		try {
			Field field = device.getClass().getDeclaredField("scale");
			if (field != null) {
				field.setAccessible(true);
				scale = field.get(device);
			}
		} catch (Exception ignore) {}

		if (scale instanceof Integer) {
			return (Integer) scale;
		}

		return 1;
	}

	public static @NotNull Image createMultiResolutionImage(int baseImageWidth, int baseImageHeight, @NotNull BufferedImage im)
	{
		return new JNR18MultiResolutionImage(baseImageWidth, baseImageHeight, im);
	}
}
