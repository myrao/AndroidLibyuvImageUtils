LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_LDFLAGS += -ljnigraphics
LOCAL_SHARED_LIBRARIES := libyuv
LOCAL_MODULE := yuv_utils
LOCAL_SRC_FILES := tech_shutu_jni_YuvUtils.c
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := yuv
LOCAL_SRC_FILES := $(LOCAL_PATH)/libyuv.so
include $(PREBUILT_SHARED_LIBRARY)

