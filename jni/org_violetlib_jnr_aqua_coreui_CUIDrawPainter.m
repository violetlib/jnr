/*
 * Copyright (c) 2015-2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

#import <CoreFoundation/CoreFoundation.h>
#import <CoreGraphics/CoreGraphics.h>
#import <Cocoa/Cocoa.h>
#import <JavaNativeFoundation.h>

#include "JNI.h"
#include "org_violetlib_jnr_aqua_coreui_CoreUIPainter.h"
#include "CoreUISupport.h"
#include "AppearanceSupport.h"

// This painter uses private methods of NSAppearance to perform Core UI rendering.

@interface NSAppearance (NSAppearancePrivate)
- (void)_drawInRect: (NSRect) rect context: (CGContextRef) context options: (CFDictionaryRef) options;
- (void)_createOrUpdateLayer: (CALayer **) layer options: (CFDictionaryRef) options;
@end

/*
 * Class:     org_violetlib_jnr_aqua_coreui_CoreUIPainter
 * Method:    nativePaint
 * Signature: ([IIIFF[Ljava/lang/Object;[J)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_coreui_CoreUIPainter_nativePaint
  (JNIEnv *env, jclass cl, jintArray data, jint w, jint h, jfloat xscale, jfloat yscale, jobjectArray args, jlongArray jLayerHolder)
{
    COCOA_ENTER(env);

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

    CFDictionaryRef d = CFDictionaryCreate(kCFAllocatorDefault, (const void **) keys, (const void **) values, argCount,
        &kCFTypeDictionaryKeyCallBacks, &kCFTypeDictionaryValueCallBacks);

    for (int i = 0; i < argCount; i++) {
        CFRelease(keys[i]);
        CFRelease(values[i]);
    }

    jboolean isCopy = JNI_FALSE;
    void *rawPixelData = (*env)->GetPrimitiveArrayCritical(env, data, &isCopy);
    if (rawPixelData) {
        CGColorSpaceRef colorspace = CGColorSpaceCreateDeviceRGB();
        CGContextRef cgRef = CGBitmapContextCreate(rawPixelData, w, h, 8, w * 4, colorspace, kCGImageAlphaPremultipliedFirst | kCGBitmapByteOrder32Host);
        CGColorSpaceRelease(colorspace);

        CGContextScaleCTM(cgRef, xscale, yscale);

        NSAppearance *app = configuredAppearance;

        if (app == nil) {
            id application = [NSApplication sharedApplication];
            if ([application respondsToSelector:@selector(appearance)]) {
                app = [application appearance];
            }
        }

        if (app == nil) {
            app = [NSAppearance currentAppearance];
        }

        if (jLayerHolder) {
            jlong *layerHolder = (*env)->GetLongArrayElements(env, jLayerHolder, NULL);
            CALayer **lv = (CALayer **) layerHolder;
            [app _createOrUpdateLayer: lv options: d];
            CALayer *layer = lv[0];
            if (layer) {
                [layer display];
                [layer renderInContext: cgRef];
            }
            (*env)->ReleaseLongArrayElements(env, jLayerHolder, layerHolder, 0);
        } else {
            NSRect bounds = NSMakeRect(0, 0, w / xscale, h / yscale);
            [app _drawInRect: bounds context: cgRef options: d];
        }

        (*env)->ReleasePrimitiveArrayCritical(env, data, rawPixelData, 0);
        CFRelease(cgRef);
    }

    CFRelease(d);

    COCOA_EXIT(env);
}
