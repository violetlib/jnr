/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

#import <CoreFoundation/CoreFoundation.h>
#import <CoreGraphics/CoreGraphics.h>
#import <Cocoa/Cocoa.h>
#import <JavaNativeFoundation.h>

#include "org_violetlib_jnr_aqua_coreui_CoreUIPainter.h"
#include "JNI.h"

// This painter uses a private method of NSAppearance to perform Core UI rendering.

#include "CoreUISupport.h"

@interface NSAppearance (NSAppearancePrivate)
- (void)_drawInRect: (NSRect) rect context: (CGContextRef) context options: (CFDictionaryRef) options;
@end

/*
 * Class:     org_violetlib_jnr_aqua_coreui_CoreUIPainter
 * Method:    nativePaint
 * Signature: ([IIIFF[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_coreui_CoreUIPainter_nativePaint
  (JNIEnv *env, jclass cl, jintArray data, jint w, jint h, jfloat xscale, jfloat yscale, jobjectArray args)
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

		NSAppearance *app = [NSAppearance currentAppearance];
		NSRect bounds = NSMakeRect(0, 0, w / xscale, h / yscale);

		[app _drawInRect: bounds context: cgRef options: d];

		(*env)->ReleasePrimitiveArrayCritical(env, data, rawPixelData, 0);
		CFRelease(cgRef);
	}

	CFRelease(d);

	COCOA_EXIT(env);
}
