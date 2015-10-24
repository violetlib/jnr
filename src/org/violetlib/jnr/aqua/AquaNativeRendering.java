/*
 * Copyright (c) 2015 Alan Snyder.
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

import org.violetlib.jnr.aqua.impl.AquaNativePainter;
import org.violetlib.jnr.aqua.impl.HybridAquaUIPainter;
import org.violetlib.jnr.aqua.impl.NativeSupport;

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

		AquaNativePainter viewPainter = null;
		AquaUIPainter coreUIPainter = null;
		AquaUIPainter jrsPainter = null;

		try {
			Class c = Class.forName("org.violetlib.jnr.aqua.impl.AugmentedAquaNativePainter");
			if (AquaNativePainter.class.isAssignableFrom(c)) {
				viewPainter = (AquaNativePainter) c.newInstance();
			}
		} catch (Exception ex) {
		}

		int jrsVersion = NativeSupport.getJavaRuntimeSupportMajorVersion();
		boolean useJRS = jrsVersion >= 15;

		try {
			Class c = Class.forName("org.violetlib.jnr.aqua.coreui.AugmentedCoreUIPainter");
			if (AquaUIPainter.class.isAssignableFrom(c)) {
				Constructor cons = c.getConstructor(Boolean.TYPE);
				coreUIPainter = (AquaUIPainter) cons.newInstance(useJRS);
			}
		} catch (Exception ex) {
		}

		if (useJRS) {
			try {
				Class c = Class.forName("org.violetlib.jnr.aqua.jrs.AugmentedJRSPainter");
				if (AquaUIPainter.class.isAssignableFrom(c)) {
					jrsPainter = (AquaUIPainter) c.newInstance();
				}
			} catch (Exception ex) {
			}
		}

		if (viewPainter != null && coreUIPainter != null) {
			preferredPainter = new HybridAquaUIPainter(viewPainter, coreUIPainter, jrsPainter);
		} else if (coreUIPainter != null) {
			preferredPainter = coreUIPainter;
		} else if (viewPainter != null) {
			preferredPainter = viewPainter;
		} else {
			preferredPainter = jrsPainter;	// last because it has the most limitations
		}
	}
}
