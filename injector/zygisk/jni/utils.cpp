//
// Created by o0kam1 on 2025/6/28.
//

#include "utils.h"
#include <string.h>
#include <stdlib.h>

bool cstrEquals(const char* str1, const char* str2) {
    if (str1 == nullptr && str2 == nullptr) return true;
    if (str1 == str2) return true;
    if (strcmp(str1, str2) == 0) return true;
    return false;
}

char* jstr2cstr(JNIEnv* env, jstring jstr) {
    char* ret = nullptr;
    if (jstr != nullptr) {
        auto str = env->GetStringUTFChars(jstr, nullptr);
        if (str != nullptr) {
            auto len = strlen(str);
            ret = new char[len + 1];
            memcpy(ret, str, len + 1);
            env->ReleaseStringUTFChars(jstr, str);
        }
    }
    return ret;
}

/**  参数说明：
 *  jdexPath        dex存储路径
 *  jodexPath       优化后的dex包存放位置
 *  jclassName      需要调用jar包中的类名
 *  jmethodName     需要调用的类中的静态方法
 */
void loadDex(JNIEnv *env, jstring jdexPath, jstring jodexPath, jstring jclassName,
    const char* methodName, jstring jarg1, jstring jarg2) {

    if (!jdexPath) {
        LOGD("MEM ERR");
        return;
    }

    if (!jodexPath) {
        LOGD("MEM ERR");
        return;
    }

    if (!jclassName) {
        LOGD("MEM ERR");
        return;
    }

    jclass classloaderClass = env->FindClass("java/lang/ClassLoader");
    jmethodID getsysClassloaderMethod = env->GetStaticMethodID(classloaderClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
    jobject loader = env->CallStaticObjectMethod(classloaderClass, getsysClassloaderMethod);
    jclass dexLoaderClass = env->FindClass("dalvik/system/DexClassLoader");
    jmethodID initDexLoaderMethod = env->GetMethodID(dexLoaderClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
    jobject dexLoader = env->NewObject(dexLoaderClass,initDexLoaderMethod, jdexPath, jodexPath, NULL, loader);
    jmethodID findclassMethod = env->GetMethodID(dexLoaderClass, "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");

    if (findclassMethod == NULL) {
        findclassMethod = env->GetMethodID(dexLoaderClass, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    }

    jclass javaClientClass = (jclass)env->CallObjectMethod(dexLoader, findclassMethod, jclassName);
    jmethodID targetMethod = env->GetStaticMethodID(javaClientClass, methodName, "(Ljava/lang/String;Ljava/lang/String;)V");

    if (targetMethod == NULL) {
        LOGD("target method(%s) not found", methodName);
        return;
    }

    env->CallStaticVoidMethod(javaClientClass, targetMethod, jarg1, jarg2);
}
