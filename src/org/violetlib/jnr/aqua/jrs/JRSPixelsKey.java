/*
 * Changes copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

/*
 * Copyright (c) 2011, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.violetlib.jnr.aqua.jrs;

// copied from com.apple.laf.AquaPainter.AquaPixelsKey
// replaced GraphicsConfiguration with scaleFactor, removed bounds

import apple.laf.JRSUIState;
import org.violetlib.jnr.impl.ImageCache;

public class JRSPixelsKey implements ImageCache.PixelsKey {

     private final int pixelCount;
     private final int hash;

     // key parts
     private final int scaleFactor;
     private final int w;
     private final int h;
     private final JRSUIState state;

		JRSPixelsKey(final int scaleFactor,
								 final int w, final int h,
								 final JRSUIState state) {
			this.pixelCount = w * h;
			this.scaleFactor = scaleFactor;
			this.w = w;
			this.h = h;
			this.state = state;
			this.hash = hash();
		}

     @Override
     public int getPixelCount() {
         return pixelCount;
     }

     private int hash() {
         int hash = scaleFactor;
         hash = 31 * hash + w;
         hash = 31 * hash + h;
         hash = 31 * hash + state.hashCode();
         return hash;
     }

     @Override
     public int hashCode() {
         return hash;
     }

     @Override
     public boolean equals(Object obj) {
         if (obj instanceof JRSPixelsKey) {
					 JRSPixelsKey key = (JRSPixelsKey) obj;
             return scaleFactor == key.scaleFactor && w == key.w && h == key.h
                     && state.equals(key.state);
         }
         return false;
     }
 }
