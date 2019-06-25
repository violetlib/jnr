/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.StringTokenizer;

import org.jetbrains.annotations.*;

/**
  Platform dependent utilities used by the Java Native Rendering library.
*/

public class JNRPlatformUtils
{
    private static @Nullable String platformVersionString;
    private static int platformVersion = 0;

    /**
      Return an integer representing the platform version.

      @return the coded platform version, e.g. 101401 for 10.14.1.
    */

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
}
