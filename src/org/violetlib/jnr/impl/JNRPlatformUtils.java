/*
 * Copyright (c) 2015-2021 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.jnr.impl;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.InputStreamReader;
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
                platformVersion = obtainSystemVersion(osVersion);
                return platformVersion;
            }
        }

        return platformVersion;
    }

    private static int obtainSystemVersion(@NotNull String s)
    {
        int version = parseSystemVersion(s);
        if (version / 100 == 1016) {
            // Liar, there is no macOS 10.16!
            int v = readSystemVersion();
            if (v > 0) {
                return v;
            }
        }
        return version;
    }

    private static int readSystemVersion()
    {
        String[] cmd = { "/usr/bin/sw_vers", "-productVersion" };
        String[] env = { "SYSTEM_VERSION_COMPAT=0" };
        String s = command("sw_vers", cmd, env);
        if (s != null) {
            return parseSystemVersion(s);
        }
        return 0;
    }

    private static @Nullable String command(@NotNull String name, String @NotNull [] command, String @Nullable [] env)
    {
        try {
            Process p = Runtime.getRuntime().exec(command, env);
            InputStreamReader stdout = new InputStreamReader(p.getInputStream());
            StringBuffer sb = new StringBuffer();
            while (true) {
                int n = stdout.read();
                if (n == -1 || n == 10) {
                    break;
                }
                sb.append((char) n);
            }
            return sb.toString();
        } catch (Throwable th) {
            System.err.println("Unable to run " + name + ": " + th);
            return null;
        }
    }

    private static int parseSystemVersion(@NotNull String s)
    {
        int version = 0;
        int count = 0;
        StringTokenizer st = new StringTokenizer(s, ".");
        while (st.hasMoreTokens()) {
            ++count;
            if (count > 3) {
                break;
            }
            String t = st.nextToken();
            try {
                int n = Integer.parseInt(t);
                if (n >= 0 && n < 100) {
                    version = version * 100 + n;
                } else {
                    return 0;
                }
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return count == 2 ? version * 100 : version;
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
