/*
 * Copyright (c) 2015 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

static jclass integerClass;
static jclass longClass;
static jclass stringClass;
static jclass booleanClass;
static jclass doubleClass;

static jmethodID integerValueMethod;
static jmethodID longValueMethod;
static jmethodID booleanValueMethod;
static jmethodID doubleValueMethod;

static void throwException(JNIEnv *env)
{
    jclass exceptionClass;
    jmethodID constructor;
    jthrowable ex;

    exceptionClass = (*env)->FindClass(env, "java/lang/UnsupportedOperationException");
    if (exceptionClass == NULL) {
        return;
    }

    constructor = (*env)->GetMethodID(env, exceptionClass, "<init>", "()V");
    if (constructor == NULL) {
        return;
    }

    ex = (jthrowable) (*env)->NewObject(env, exceptionClass, constructor);
    if (ex != NULL) {
        (*env)->Throw(env, ex);
    }
}

static CFTypeRef CopyCFTypeToJava(JNIEnv *env, jobject obj)
{
	if (integerClass == 0) {
		integerClass = (*env) -> NewGlobalRef(env, (*env) -> FindClass(env, "java/lang/Integer"));
		integerValueMethod = (*env) -> GetMethodID(env, integerClass, "intValue", "()I");
		longClass = (*env) -> NewGlobalRef(env, (*env) -> FindClass(env, "java/lang/Long"));
		longValueMethod = (*env) -> GetMethodID(env, longClass, "longValue", "()J");
		stringClass = (*env) -> NewGlobalRef(env, (*env) -> FindClass(env, "java/lang/String"));
		booleanClass = (*env) -> NewGlobalRef(env, (*env) -> FindClass(env, "java/lang/Boolean"));
		booleanValueMethod = (*env) -> GetMethodID(env, booleanClass, "booleanValue", "()Z");
		doubleClass = (*env) -> NewGlobalRef(env, (*env) -> FindClass(env, "java/lang/Double"));
		doubleValueMethod = (*env) -> GetMethodID(env, doubleClass, "doubleValue", "()D");
	}

    if ((*env) -> IsInstanceOf(env, obj, integerClass)) {
      jint value = (*env) -> CallIntMethod(env, obj, integerValueMethod);
      return CFNumberCreate(NULL, kCFNumberSInt32Type, &value);
    }

    if ((*env) -> IsInstanceOf(env, obj, longClass)) {
      jlong value = (*env) -> CallLongMethod(env, obj, longValueMethod);
      return CFNumberCreate(NULL, kCFNumberSInt64Type, &value);
    }

    if ((*env) -> IsInstanceOf(env, obj, stringClass)) {
	    jsize slen = (*env) -> GetStringLength(env, obj);
	    const jchar *schars = (*env) -> GetStringChars(env, obj, NULL);
	    CFStringRef result = CFStringCreateWithCharacters(NULL, schars, slen);
	    (*env) -> ReleaseStringChars(env, obj, schars);
	    return result;
    }

    if ((*env) -> IsInstanceOf(env, obj, booleanClass)) {
      jboolean value = (*env) -> CallBooleanMethod(env, obj, booleanValueMethod);
      return value != 0 ? kCFBooleanTrue : kCFBooleanFalse;
    }

    if ((*env) -> IsInstanceOf(env, obj, doubleClass)) {
      jdouble value = (*env) -> CallDoubleMethod(env, obj, doubleValueMethod);
      return CFNumberCreate(NULL, kCFNumberDoubleType, &value);
    }

	throwException(env);
	return NULL;
}
