/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.aqua;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;

import org.jetbrains.annotations.*;

import org.violetlib.jnr.aqua.impl.HybridAquaUIPainter;
import org.violetlib.jnr.aqua.impl.NativeSupport;
import org.violetlib.jnr.impl.ImageCache;

/**
	The main entry point to the Aqua Native Rendering library.
*/

public class AquaNativeRendering
{
	private static boolean isInitialized;

	private static @Nullable AquaUIPainter preferredPainter;

	/**
		Create a native painter. The painter class is determined by the available implementations. The best available
		implementation in terms of performance and coverage is returned.

		@return the new painter.

		@throws UnsupportedOperationException if there are no available implementations.
	*/

	public static @NotNull AquaUIPainter createPainter()
		throws UnsupportedOperationException
	{
		if (!isInitialized) {

			findPainterClasses();
		}

		if (preferredPainter != null) {
			return preferredPainter.copy();
		}

		throw new UnsupportedOperationException("Unable to create a native painter");
	}

	public static void clearCache()
	{
		ImageCache.getInstance().flush();
	}

	private AquaNativeRendering()
	{
	}

	/**
		Return a string identifying the release of this library.
	*/
	public static @NotNull String getReleaseName() {
		return getStringResource("RELEASE.txt");
	}

	/**
		Return a string identifying the build of this library.
	*/
	public static @NotNull String getBuildID() {
		return getStringResource("BUILD.txt");
	}

	/**
		Return a string identifying the version of this library.
	*/
	public static @NotNull String getVersionString() {
		return "VAquaRendering: release " + getReleaseName() + " (build " + getBuildID() + ")";
	}

	/**
		Write to standard error a description of the version of this library.
	*/
	public static void showVersion() {
		System.err.println("VAquaRendering: release " + getReleaseName() + ", build " + getBuildID());
	}

	private static @NotNull String getStringResource(@NotNull String name)
	{
		InputStream s = AquaNativeRendering.class.getResourceAsStream(name);
		if (s != null) {
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(s));
				StringBuilder sb = new StringBuilder();
				for (; ; ) {
					int ch = r.read();
					if (ch < 0) {
						break;
					}
					sb.append((char) ch);
				}
				return sb.toString();
			} catch (IOException ex) {
			}
		}

		return "Unknown";
	}

	private static synchronized void findPainterClasses()
	{
		if (isInitialized) {
			return;
		}

		isInitialized = true;

		AquaUIPainter viewPainter = null;
		AquaUIPainter coreUIPainter = null;
		AquaUIPainter jrsPainter = null;

		{
			String name = "org.violetlib.jnr.aqua.impl.AugmentedAquaNativePainter";
			viewPainter = getPainter(name, null);
		}

		int jrsVersion = NativeSupport.getJavaRuntimeSupportMajorVersion();
		boolean useJRS = jrsVersion >= 15;

		{
			boolean useJRSToAccessCoreUI = useJRS;
			String name = "org.violetlib.jnr.aqua.coreui.AugmentedCoreUIPainter";
			coreUIPainter = getPainter(name, useJRSToAccessCoreUI);
		}

		if (useJRS) {
			String name = "org.violetlib.jnr.aqua.jrs.AugmentedJRSPainter";
			jrsPainter = getPainter(name, null);
		}

		if (viewPainter != null && coreUIPainter != null) {
			preferredPainter = new HybridAquaUIPainter(viewPainter, coreUIPainter, jrsPainter);
		} else if (coreUIPainter != null) {
			debug("Using Core UI painter as preferred painter");
			preferredPainter = coreUIPainter;
		} else if (viewPainter != null) {
			debug("Using NSView painter as preferred painter");
			preferredPainter = viewPainter;
		} else {
			debug("Using JRS painter as preferred painter");
			preferredPainter = jrsPainter;	// last because it has the most limitations
		}
	}

	protected static void debug(@NotNull String s)
	{
		if (false) {
			System.err.println(s);
		}
	}

	protected static @Nullable AquaUIPainter getPainter(@NotNull String name, @Nullable Boolean parameter)
	{
		Class c = getClass(name);
		if (c != null) {
			if (AquaUIPainter.class.isAssignableFrom(c)) {
				try {
					if (parameter != null) {
						Constructor cons = c.getConstructor(Boolean.TYPE);
						return (AquaUIPainter) cons.newInstance(parameter);
					} else {
						Constructor cons = c.getConstructor();
						return (AquaUIPainter) cons.newInstance();
					}
				} catch (Exception ex) {
					System.err.println("Unable to instantiate painter class: " + name);
					ex.printStackTrace();
				}
			} else {
				System.err.println("Painter class is not valid: " + name);
			}
		} else {
			debug("Painter class not found: " + name);
		}
		return null;
	}

	protected static @Nullable Class getClass(@NotNull String name)
	{
		ClassLoader loader = AquaNativeRendering.class.getClassLoader();
		try {
			return Class.forName(name, true, loader);
		} catch (ClassNotFoundException ex) {
			return null;
		}
	}
}
