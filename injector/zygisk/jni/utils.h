//
// Created by o0kam1 on 2025/6/28.
//

#ifndef YABR_UTILS_H
#define YABR_UTILS_H

#include <jni.h>
#include <android/log.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "YABR_zygisk_loader", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "YABR_zygisk_loader", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "YABR_zygisk_loader", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "YABR_zygisk_loader", __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "YABR_zygisk_loader", __VA_ARGS__)

bool cstrEquals(const char* str1, const char* str2);

char* jstr2cstr(JNIEnv* env, jstring jstr);

void loadDex(JNIEnv* env, jstring jdexPath, jstring jodexPath, jstring jclassName,
             const char* methodName, jstring jarg1, jstring jarg2);

#endif //YABR_UTILS_H
