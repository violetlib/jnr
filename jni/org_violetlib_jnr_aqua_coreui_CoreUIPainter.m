/*
 * Copyright (c) 2015-2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

#import <CoreFoundation/CoreFoundation.h>
#import <CoreGraphics/CoreGraphics.h>
#import <Cocoa/Cocoa.h>
#import <JavaRuntimeSupport/JavaRuntimeSupport.h>

#include "jnix.h"
#include "org_violetlib_jnr_aqua_coreui_CoreUIPainter.h"
#include "AppearanceSupport.h"
#include "CoreUISupport.h"

// This painter uses the Java RuntimeSupport framework to perform Core UI rendering.

// Currently, we create a JRSUIControl for each painting request. The reason is that
// there is no API for resetting the parameters in a JRSUIControl. Reusing a JRSUIControl
// works only if all parameters of interest are set every time.

static JRSUIRendererRef renderer;

/*
 * Class:     org_violetlib_jnr_aqua_coreui_CoreUIPainter
 * Method:    nativeJRSPaint
 * Signature: ([IIIFF[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_coreui_CoreUIPainter_nativeJRSPaint
  (JNIEnv *env, jclass cl, jintArray data, jint w, jint h, jfloat xscale, jfloat yscale, jobjectArray args)
{
    COCOA_ENTER();

    jsize argsCount = (*env) -> GetArrayLength(env, args);
    jsize keyCount = argsCount / 2;

    CFTypeRef keys[50];
    CFTypeRef values[50];

    jsize argIndex = 0;
    jsize argCount = 0;

    for (int i = 0; i < keyCount; i++) {
        jobject jkey = (*env) -> GetObjectArrayElement(env, args, argIndex++);
        jobject jvalue = (*env) -> GetObjectArrayElement(env, args, argIndex++);
        if (!(*env)->IsSameObject(env, jvalue, NULL)) {
            CFTypeRef key = CopyCFTypeToJava(env, jkey);
            CFTypeRef value = CopyCFTypeToJava(env, jvalue);
            if (key == nil) {
                NSLog(@"Invalid CoreUI key");
                continue;
            }
            if (value == nil) {
                NSLog(@"Invalid CoreUI value");
                continue;
            }
        keys[argCount] = key;
        values[argCount] = value;
        argCount++;
        }
    }

    if (renderer == nil) {
        renderer = JRSUIRendererCreate();
    }

    JRSUIControlRef control = JRSUIControlCreate(NO);

    for (int i = 0; i < argCount; i++) {
        CFTypeRef key = keys[i];
        CFTypeRef value = values[i];
        JRSUIControlSetValueByKey(control, key, value);
        CFRelease(key);
        CFRelease(value);
    }

    jboolean isCopy = JNI_FALSE;
    void *rawPixelData = (*env)->GetPrimitiveArrayCritical(env, data, &isCopy);
    if (rawPixelData) {
        CGColorSpaceRef colorspace = CGColorSpaceCreateDeviceRGB();
        CGContextRef cgRef = CGBitmapContextCreate(rawPixelData, w, h, 8, w * 4, colorspace, kCGImageAlphaPremultipliedFirst | kCGBitmapByteOrder32Host);
        CGColorSpaceRelease(colorspace);

        CGContextScaleCTM(cgRef, xscale, yscale);
        NSRect bounds = NSMakeRect(0, 0, w / xscale, h / yscale);
        JRSUIControlDraw(renderer, control, cgRef, bounds);

        (*env)->ReleasePrimitiveArrayCritical(env, data, rawPixelData, 0);
        CFRelease(cgRef);
    }

    JRSUIControlRelease(control);

    COCOA_EXIT();
}
