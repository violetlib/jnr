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

#import <JavaRuntimeSupport/JavaRuntimeSupport.h>

#import "org_violetlib_jnr_impl_jrs_JRSUIControl.h"
#import "org_violetlib_jnr_impl_jrs_JRSUIConstants_DoubleValue.h"
#import "org_violetlib_jnr_impl_jrs_JRSUIConstants_Hit.h"

#import "JRSUIConstantSync.h"

#ifndef jlong_to_ptr
#define jlong_to_ptr(a) ((void *)(uintptr_t)(a))
#endif

#ifndef ptr_to_jlong
#define ptr_to_jlong(a) ((jlong)(uintptr_t)(a))
#endif

static JRSUIRendererRef gRenderer;

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    initNativeJRSUI
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_initNativeJRSUI
(JNIEnv *env, jclass clazz)
{
    BOOL coherent = _InitializeJRSProperties();
    if (!coherent) return org_violetlib_jnr_impl_jrs_JRSUIControl_INCOHERENT;

    gRenderer = JRSUIRendererCreate();
    if (gRenderer == NULL) return org_violetlib_jnr_impl_jrs_JRSUIControl_NULL_PTR;

    return org_violetlib_jnr_impl_jrs_JRSUIControl_SUCCESS;
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    getPtrOfBuffer
 * Signature: (Ljava/nio/ByteBuffer;)J
 */
JNIEXPORT jlong JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_getPtrOfBuffer
(JNIEnv *env, jclass clazz, jobject byteBuffer)
{
    char *byteBufferPtr = (*env)->GetDirectBufferAddress(env, byteBuffer);
    if (byteBufferPtr == NULL) return 0L;
    return ptr_to_jlong(byteBufferPtr); // GC
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    getCFDictionary
 * Signature: (Z)J
 */
JNIEXPORT jlong JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_getCFDictionary
(JNIEnv *env, jclass clazz, jboolean isFlipped)
{
    return ptr_to_jlong(JRSUIControlCreate(isFlipped));
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    disposeCFDictionary
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_disposeCFDictionary
(JNIEnv *env, jclass clazz, jlong controlPtr)
{
    void *ptr = jlong_to_ptr(controlPtr);
    if (!ptr) return;
    JRSUIControlRelease((JRSUIControlRef)ptr);
}


static inline void *getValueFor
(jbyte code, UInt8 *changeBuffer, size_t *dataSizePtr)
{
    switch (code)
    {
        case org_violetlib_jnr_impl_jrs_JRSUIConstants_DoubleValue_TYPE_CODE:
            *dataSizePtr = sizeof(jdouble);
            jdouble doubleValue = (*(jdouble *)changeBuffer);
            return (void *)CFNumberCreate(kCFAllocatorDefault, kCFNumberDoubleType, &doubleValue);
    }

    return NULL;
}

static inline jint syncChangesToControl
(JRSUIControlRef control, UInt8 *changeBuffer)
{
    UInt8 *endOfBuffer = changeBuffer + org_violetlib_jnr_impl_jrs_JRSUIControl_NIO_BUFFER_SIZE;

    while (changeBuffer < endOfBuffer)
    {
        // dereference the pointer to the constant that was stored as a jlong in the byte buffer
        CFStringRef key = (CFStringRef)jlong_to_ptr(*((jlong *)changeBuffer));
        if (key == NULL) return org_violetlib_jnr_impl_jrs_JRSUIControl_SUCCESS;
        changeBuffer += sizeof(jlong);

        jbyte code = *((jbyte *)changeBuffer);
        changeBuffer += sizeof(jbyte);

        size_t dataSize;
        void *value = (void *)getValueFor(code, changeBuffer, &dataSize);
        if (value == NULL) {
            NSLog(@"null pointer for %@ for value %d", key, (int)code);

            return org_violetlib_jnr_impl_jrs_JRSUIControl_NULL_PTR;
        }

        changeBuffer += dataSize;
        JRSUIControlSetValueByKey(control, key, value);
        CFRelease(value);
    }

    return org_violetlib_jnr_impl_jrs_JRSUIControl_SUCCESS;
}

static inline jint doSyncChanges
(JNIEnv *env, jlong controlPtr, jlong byteBufferPtr)
{
    JRSUIControlRef control = (JRSUIControlRef)jlong_to_ptr(controlPtr);
    UInt8 *changeBuffer = (UInt8 *)jlong_to_ptr(byteBufferPtr);

    return syncChangesToControl(control, changeBuffer);
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    syncChanges
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_syncChanges
(JNIEnv *env, jclass clazz, jlong controlPtr, jlong byteBufferPtr)
{
    return doSyncChanges(env, controlPtr, byteBufferPtr);
}

static inline jint doPaintCGContext(CGContextRef cgRef, jlong controlPtr, jlong oldProperties, jlong newProperties, jdouble x, jdouble y, jdouble w, jdouble h)
{
    JRSUIControlRef control = (JRSUIControlRef)jlong_to_ptr(controlPtr);
    _SyncEncodedProperties(control, oldProperties, newProperties);
    CGRect bounds = CGRectMake(x, y, w, h);
    JRSUIControlDraw(gRenderer, control, cgRef, bounds);
    return 0;
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    paintToCGContext
 * Signature: (JJJJDDDD)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_paintToCGContext
(JNIEnv *env, jclass clazz, jlong cgContextPtr, jlong controlPtr, jlong oldProperties, jlong newProperties, jdouble x, jdouble y, jdouble w, jdouble h)
{
    return doPaintCGContext((CGContextRef)jlong_to_ptr(cgContextPtr), controlPtr, oldProperties, newProperties, x, y, w, h);
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    paintChangesToCGContext
 * Signature: (JJJJDDDDJ)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_paintChangesToCGContext
(JNIEnv *env, jclass clazz, jlong cgContextPtr, jlong controlPtr, jlong oldProperties, jlong newProperties, jdouble x, jdouble y, jdouble w, jdouble h, jlong changes)
{
    int syncStatus = doSyncChanges(env, controlPtr, changes);
    if (syncStatus != org_violetlib_jnr_impl_jrs_JRSUIControl_SUCCESS) return syncStatus;

    return doPaintCGContext((CGContextRef)jlong_to_ptr(cgContextPtr), controlPtr, oldProperties, newProperties, x, y, w, h);
}

static inline jint doPaintImage
(JNIEnv *env, jlong controlPtr, jlong oldProperties, jlong newProperties, jintArray data, jint imgW, jint imgH, jdouble x, jdouble y, jdouble w, jdouble h)
{
    jboolean isCopy = JNI_FALSE;
    void *rawPixelData = (*env)->GetPrimitiveArrayCritical(env, data, &isCopy);
    if (!rawPixelData) return org_violetlib_jnr_impl_jrs_JRSUIControl_NULL_PTR;

    CGColorSpaceRef colorspace = CGColorSpaceCreateDeviceRGB();
    CGContextRef cgRef = CGBitmapContextCreate(rawPixelData, imgW, imgH, 8, imgW * 4, colorspace, kCGImageAlphaPremultipliedFirst | kCGBitmapByteOrder32Host);
    CGColorSpaceRelease(colorspace);
    CGContextScaleCTM(cgRef, imgW/(w + x + x) , imgH/(h + y + y));

    jint status = doPaintCGContext(cgRef, controlPtr, oldProperties, newProperties, x, y, w, h);
    CGContextRelease(cgRef);

    (*env)->ReleasePrimitiveArrayCritical(env, data, rawPixelData, 0);

    return status == noErr ? org_violetlib_jnr_impl_jrs_JRSUIControl_SUCCESS : status;
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    paintImage
 * Signature: ([IIIJJJDDDD)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_paintImage
(JNIEnv *env, jclass clazz, jintArray data, jint imgW, jint imgH, jlong controlPtr, jlong oldProperties, jlong newProperties, jdouble x, jdouble y, jdouble w, jdouble h)
{
    return doPaintImage(env, controlPtr, oldProperties, newProperties, data, imgW, imgH, x, y, w, h);
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    paintChangesImage
 * Signature: ([IIIJJJDDDDJ)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_paintChangesImage
(JNIEnv *env, jclass clazz, jintArray data, jint imgW, jint imgH, jlong controlPtr, jlong oldProperties, jlong newProperties, jdouble x, jdouble y, jdouble w, jdouble h, jlong changes)
{
    int syncStatus = doSyncChanges(env, controlPtr, changes);
    if (syncStatus != org_violetlib_jnr_impl_jrs_JRSUIControl_SUCCESS) return syncStatus;

    return doPaintImage(env, controlPtr, oldProperties, newProperties, data, imgW, imgH, x, y, w, h);
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    getNativeHitPart
 * Signature: (JJJDDDDDD)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_getNativeHitPart
(JNIEnv *env, jclass clazz, jlong controlPtr, jlong oldProperties, jlong newProperties, jdouble x, jdouble y, jdouble w, jdouble h, jdouble pointX, jdouble pointY)
{
    JRSUIControlRef control = (JRSUIControlRef)jlong_to_ptr(controlPtr);
    _SyncEncodedProperties(control, oldProperties, newProperties);

    CGRect bounds = CGRectMake(x, y, w, h);
    CGPoint point = CGPointMake(pointX, pointY);

    return JRSUIControlGetHitPart(gRenderer, control, bounds, point);
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIUtils_ScrollBar
 * Method:    shouldUseScrollToClick
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIUtils_00024ScrollBar_shouldUseScrollToClick
(JNIEnv *env, jclass clazz)
{
    return JRSUIControlShouldScrollToClick();
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    getNativePartBounds
 * Signature: ([DJJJDDDDI)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_getNativePartBounds
(JNIEnv *env, jclass clazz, jdoubleArray rectArray, jlong controlPtr, jlong oldProperties, jlong newProperties, jdouble x, jdouble y, jdouble w, jdouble h, jint part)
{
    JRSUIControlRef control = (JRSUIControlRef)jlong_to_ptr(controlPtr);
    _SyncEncodedProperties(control, oldProperties, newProperties);

    CGRect frame = CGRectMake(x, y, w, h);
    CGRect partBounds = JRSUIControlGetScrollBarPartBounds(control, frame, part);

    jdouble *rect = (*env)->GetPrimitiveArrayCritical(env, rectArray, NULL);
    rect[0] = partBounds.origin.x;
    rect[1] = partBounds.origin.y;
    rect[2] = partBounds.size.width;
    rect[3] = partBounds.size.height;
    (*env)->ReleasePrimitiveArrayCritical(env, rectArray, rect, 0);
}

/*
 * Class:     org_violetlib_jnr_impl_jrs_JRSUIControl
 * Method:    getNativeScrollBarOffsetChange
 * Signature: (JJJDDDDIII)D
 */
JNIEXPORT jdouble JNICALL Java_org_violetlib_jnr_impl_jrs_JRSUIControl_getNativeScrollBarOffsetChange
(JNIEnv *env, jclass clazz, jlong controlPtr, jlong oldProperties, jlong newProperties, jdouble x, jdouble y, jdouble w, jdouble h, jint offset, jint visibleAmount, jint extent)
{
    JRSUIControlRef control = (JRSUIControlRef)jlong_to_ptr(controlPtr);
    _SyncEncodedProperties(control, oldProperties, newProperties);

    CGRect frame = CGRectMake(x, y, w, h);
    return (jdouble)JRSUIControlGetScrollBarOffsetFor(control, frame, offset, visibleAmount, extent);
}
