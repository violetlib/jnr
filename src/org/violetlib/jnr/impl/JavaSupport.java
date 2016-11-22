/*
 * Copyright (c) 2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.StringTokenizer;

import org.jetbrains.annotations.*;

/**
	Platform support that varies based on the Java version.
*/

public class JavaSupport
{
	public interface JavaSupportImpl
	{
		int getScaleFactor(@NotNull Graphics g);
		Image createMultiResolutionImage(int baseImageWidth, int baseImageHeight, @NotNull BufferedImage im);
	}

	private final static JavaSupportImpl impl = findImpl();

	public static int getScaleFactor(@NotNull Graphics g)
	{
		return impl.getScaleFactor(g);
	}

	public static @NotNull Image createMultiResolutionImage(int baseImageWidth, int baseImageHeight, @NotNull BufferedImage im)
	{
		return impl.createMultiResolutionImage(baseImageWidth, baseImageHeight, im);
	}

	private static JavaSupportImpl findImpl()
	{
		int version = obtainJavaVersion();
		System.err.println("Java version: " + version);
		String className;
		if (version >= 900000) {
			className = "Java9Support";
		} else {
			className = "Java8Support";
		}
		try {
			Class c = Class.forName("org.violetlib.jnr.impl." + className);
			return (JavaSupportImpl) c.getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			throw new UnsupportedOperationException("Unsupported Java version: " + version, ex);
		}
	}

	private static int obtainJavaVersion()
	{
		String s = System.getProperty("java.version");
		if (s.startsWith("1.")) {
			s = s.substring(2);
		}
		int version = 0;
		int tokenCount = 0;
		StringTokenizer st = new StringTokenizer(s, "._");
		try {
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.endsWith("-internal")) {
					token = token.substring(0, token.length() - 9);
				} else if (token.endsWith("-ea")) {
        	token = token.substring(0, token.length() - 3);
				}
				int n = Integer.parseInt(token);
				++tokenCount;
				int limit = tokenCount < 3 ? 100 : 1000;
				if (n < 0 || n >= limit) {
					return 0;
				}
				version = version * limit + n;
			}
		} catch (NumberFormatException ex) {
			return 0;
		}

		while (tokenCount < 3) {
			++tokenCount;
			int limit = tokenCount < 3 ? 100 : 1000;
			version = version * limit;
		}

		if (tokenCount != 3) {
			return 0;
		}
		return version;
	}
}
