PRODUCT_PACKAGES := \
	com.twosixlabs.peandroid \
	com.twosixlabs.peandroid.xml \
	com.twosixlabs.peandroid.privacymanager \
	com.twosixlabs.peandroid.privacymanager.xml \
	com.twosixlabs.peandroid.pal \
	com.twosixlabs.peandroid.pal.xml \
	PrivateDataService \
	PrivacyNotificationService \
	YesPolicyManager \
	PrivacyCheckup \
	PrivacyVerifier \
	RequiredVerifier

PRODUCT_PROPERTY_OVERRIDES := \
    ro.peandroid.version=5.0.0

PRODUCT_SDK_ADDON_NAME := pea_addon

PRODUCT_SDK_ADDON_COPY_FILES := $(LOCAL_PATH)/manifest.ini:manifest.ini

PRODUCT_SDK_ADDON_COPY_MODULES := \
	com.twosixlabs.peandroid:libs/peandroid.jar \
	com.twosixlabs.peandroid.pal:libs/pal.jar \
	com.twosixlabs.peandroid.privacymanager:libs/privacymanager.jar

PRODUCT_SDK_ADDON_STUB_MODULES := \
    com.twosixlabs.peandroid_stubs:src/peandroid.jar \
    com.twosixlabs.peandroid.pal_stubs:src/pal.jar \
    com.twosixlabs.peandroid.privacymanager_stubs:src/privacymanager.jar

PRODUCT_SDK_ADDON_DOC_MODULES := \
	com.twosixlabs.peandroid_doc \
	com.twosixlabs.peandroid.pal_addon_doc \
	com.twosixlabs.peandroid.privacymanager_doc

PRODUCT_SDK_ADDON_STUB_DEFS := $(LOCAL_PATH)/peandroid_sdk_addon_stub_defs.txt

PRODUCT_SDK_ADDON_SYS_IMG_SOURCE_PROP := $(LOCAL_PATH)/source.properties

# Since the add-on is an emulator, we also need to explicitly copy the kernel to images
PRODUCT_SDK_ADDON_COPY_FILES += prebuilts/qemu-kernel/x86_64/ranchu/kernel-qemu:images/x86_64/kernel-ranchu

PRODUCT_BOOT_JARS += \
    com.twosixlabs.peandroid.pal

# This add-on extends the default sdk product.
$(call inherit-product, $(SRC_TARGET_DIR)/product/sdk_x86_64.mk)

PRODUCT_NAME := pea_addon
PRODUCT_MODEL := PE for Android SDK Addon Image
PRODUCT_BRAND := 26Labs
