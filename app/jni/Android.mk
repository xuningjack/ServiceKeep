LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#######编译breakpad#########
#include $(MY_ROOT_PATH)/googlebreakpad/Android.mk

#########链接##########
LOCAL_LDLIBS := -llog
LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_STATIC_LIBRARIES := libc android
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
#LOCAL_MODULE_FILENAME := "libhelper.so"

LOCAL_MODULE := helper
LOCAL_SRC_FILES := helper.c
LOCAL_JNI_SHARED_LIBRARIES := libhelper.so

include $(BUILD_SHARED_LIBRARY)
