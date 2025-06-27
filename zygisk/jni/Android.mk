LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := yabr_zygisk
LOCAL_SRC_FILES := yabr.cpp utils.cpp
LOCAL_LDLIBS := -llog -lstdc++
include $(BUILD_SHARED_LIBRARY)
