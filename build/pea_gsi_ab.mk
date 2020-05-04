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

PRODUCT_BOOT_JARS += \
    com.twosixlabs.peandroid.pal

$(call inherit-product, build/make/target/product/aosp_arm64_ab.mk)

PRODUCT_NAME := pea_gsi_ab
PRODUCT_MODEL := PE for Android for arm64_ab
PRODUCT_BRAND := 26Labs


# NOTE: Uncomment below to increase the system partition size by 1 GB.
#       In order to undo the size increase, re-comment and do full rebuild of the image.

$(info $(shell ($(LOCAL_PATH)/spacefiller/generate.sh 1G)))
PRODUCT_COPY_FILES += \
    device/twosixlabs/build/spacefiller/fillerfile.tmp:system/fillerfile.tmp:twosixlabs \
