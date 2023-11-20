/*
 * Copyright (c) 2018-2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

#include "jnix.h"
#include "org_violetlib_jnr_aqua_impl_AquaUIPainterBase.h"
#include "AppearanceSupport.h"

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaUIPainterBase
 * Method:    nativeRegisterAppearance
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_aqua_impl_AquaUIPainterBase_nativeRegisterAppearance
  (JNIEnv *env, jclass cl, jstring jAppearanceName)
{
    jint result = -1;

    COCOA_ENTER();

    const jchar *appearanceNameChars = (*env)->GetStringChars(env, jAppearanceName, NULL);
    NSString *appearanceName = [NSString stringWithCharacters:(UniChar *)appearanceNameChars length:(*env)->GetStringLength(env, jAppearanceName)];
    result = registerAppearance(appearanceName);
    (*env)->ReleaseStringChars(env, jAppearanceName, appearanceNameChars);

    COCOA_EXIT();

    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaUIPainterBase
 * Method:    nativeSetAppearance
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaUIPainterBase_nativeSetAppearance
  (JNIEnv *env, jclass cl, jint jAppearanceID)
{
    COCOA_ENTER();

    setAppearance(jAppearanceID);

    COCOA_EXIT();
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaUIPainterBase
 * Method:    nativeInvalidateAppearances
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaUIPainterBase_nativeInvalidateAppearances
  (JNIEnv *env, jclass cl)
{
    COCOA_ENTER();

    updateCurrentAppearance();

    COCOA_EXIT();
}
