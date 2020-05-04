# This work was authored by Two Six Labs, LLC and is sponsored by a
# subcontract agreement with Raytheon BBN Technologies Corp. under Prime
# Contract No. FA8750-16-C-0006 with the Air Force Research Laboratory (AFRL).

# The Government has unlimited rights to use, modify, reproduce, release,
# perform, display, or disclose computer software or computer software
# documentation marked with this legend. Any reproduction of technical data,
# computer software, or portions thereof marked with this legend must also
# reproduce this marking.

# (C) 2020 Two Six Labs, LLC.  All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-java-files-under,src) $(call all-java-files-under,../common)
LOCAL_SRC_FILES += \
	src/android/privatedata/IMicroPALProvider.aidl
LOCAL_AIDL_INCLUDES += $(LOCAL_PATH)/../common/src/frameworks/base/core/java
LOCAL_MODULE := com.twosixlabs.peandroid.pal
include $(BUILD_JAVA_LIBRARY)

# Build the documentation
include $(CLEAR_VARS)
LOCAL_MODULE:= com.twosixlabs.peandroid.pal_addon_doc
LOCAL_SRC_FILES := $(call all-java-files-under,src) $(call all-java-files-under, ../common) $(call all-subdir-html-files)
LOCAL_DROIDDOC_SOURCE_PATH := $(LOCAL_PATH)/../common/src/frameworks/base/core/java
LOCAL_DROIDDOC_SOURCE_PATH += $(TARGET_OUT_COMMON_INTERMEDIATES)/JAVA_LIBRARIES/com.twosixlabs.peandroid.pal_intermediates/aidl/src
LOCAL_DROIDDOC_SOURCE_PATH += $(TARGET_OUT_COMMON_INTERMEDIATES)/JAVA_LIBRARIES/com.twosixlabs.peandroid.pal_intermediates/aidl/dotdot/common/src/frameworks/base/core/java
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_DROIDDOC_OPTIONS := -link https://developer.android.com/reference/
LOCAL_DROIDDOC_USE_STANDARD_DOCLET := true
include $(BUILD_DROIDDOC)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := com.twosixlabs.peandroid.pal.xml
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

# Build stub jar ==============================================================

# Generate the stub source files
include $(CLEAR_VARS)
LOCAL_MODULE := pal-stubs
LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-java-files-under, ../common)
LOCAL_SRC_FILES += \
    ../../../$(TARGET_OUT_COMMON_INTERMEDIATES)/JAVA_LIBRARIES/com.twosixlabs.peandroid.pal_intermediates/aidl/src/android/privatedata/IMicroPALProvider.java

LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_JAVA_LIBRARIES := framework

pal_packages := $(strip \
  android.pal.item \
  android.pal.item.audio \
  android.pal.item.calendar \
  android.pal.item.communication \
  android.pal.item.device \
  android.pal.item.empty \
  android.pal.item.image \
  android.pal.item.location \
  android.pal.item.sensor \
  android.pal.item.utils \
  android.privatedata \
)

LOCAL_DROIDDOC_OPTIONS:= \
    -stubpackages $(subst $(space),:,$(pal_packages)) \
    -stubs $(TARGET_OUT_COMMON_INTERMEDIATES)/JAVA_LIBRARIES/pal-stubs_intermediates/src \
    -nodocs
LOCAL_UNINSTALLABLE_MODULE := true

include $(BUILD_DROIDDOC)

# Package stubbed sources in jar
addon_stub_name := pal-stubs
stub_timestamp := $(OUT_DOCS)/pal-stubs-timestamp
include $(LOCAL_PATH)/../build/build_addon_stubs.mk

.PHONY: pal_stubs
pal_stubs: $(full_src_target)

# Remember the target for the stubbed jar
pal_stubs_jar := $(full_src_target)

# Build the stub source files into a jar.
include $(CLEAR_VARS)
LOCAL_MODULE := com.twosixlabs.peandroid.pal_stubs
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_SRC_FILES := ../../../$(pal_stubs_jar)
LOCAL_DEX_PREOPT := false
include $(BUILD_PREBUILT)
