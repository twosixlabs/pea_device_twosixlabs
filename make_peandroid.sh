#!/bin/bash

NUM_COMPILE=`echo $(($(nproc) * 2))`

source build/envsetup.sh &> addons.log

function build_addon() {
    local addon=$1
    echo "Running 'make -j${NUM_COMPILE} ANDROID_COMPILE_WITH_JACK=false TARGET_PREBUILT_KERNEL=true PRODUCT-${addon}_addon-sdk_addon'"
    make -j${NUM_COMPILE} ANDROID_COMPILE_WITH_JACK=false TARGET_PREBUILT_KERNEL=true PRODUCT-${addon}_addon-sdk_addon
    if [ $? -eq 0 ]; then
        echo "Successfully built ${addon} addon"
    else
        echo "Error while building ${addon} addon"
        return 1
    fi
    
    mv out/host/linux-x86/sdk_addon/${addon}_addon-eng.${USER}-linux-x86.zip out/host/linux-x86/sdk_addon/${addon}_pie_sdk_addon_v${vnum}.zip

    if [ $? -eq 0 ]; then
        echo "Successfully renamed ${addon} addon"
    else
        echo "Error while renaming ${addon} addon"
        return 1
    fi
}

function gen_xml() {
    development/build/tools/mk_sdk_repo_xml.sh \
	out/host/linux-x86/sdk_addon/addon.xml prebuilts/devtools/repository/sdk-addon-07.xsd \
	add-on macosx out/host/linux-x86/sdk_addon/pea_pie_sdk_addon_v${vnum}.zip:pea_pie_sdk_addon_v${vnum}.zip \
	       linux out/host/linux-x86/sdk_addon/pea_pie_sdk_addon_v${vnum}.zip:pea_pie_sdk_addon_v${vnum}.zip \
	       windows out/host/linux-x86/sdk_addon/pea_pie_sdk_addon_v${vnum}.zip:pea_pie_sdk_addon_v${vnum}.zip \

    if [ $? -eq 0 ]; then
        echo "Successfully generated addon.xml"
    else
        echo "Error while generating addon.xml"
        return 1
    fi

    # Until we have a genral SDK addon, use the pal system-images for the emulator
    mv out/host/linux-x86/sdk_addon/pea_addon-eng.${USER}-linux-x86-img.zip out/host/linux-x86/sdk_addon/pea_pie_system-images_v${vnum}.zip
   
    development/build/tools/mk_sdk_repo_xml.sh \
	out/host/linux-x86/sdk_addon/addon-sys-img.xml prebuilts/devtools/repository/sdk-sys-img-03.xsd \
	system-image macosx out/host/linux-x86/sdk_addon/pea_pie_system-images_v${vnum}.zip:pea_pie_system-images_v${vnum}.zip \
	linux out/host/linux-x86/sdk_addon/pea_pie_system-images_v${vnum}.zip:pea_pie_system-images_v${vnum}.zip \
	windows out/host/linux-x86/sdk_addon/pea_pie_system-images_v${vnum}.zip:pea_pie_system-images_v${vnum}.zip

    if [ $? -eq 0 ]; then
        echo "Successfully generated addon-sys-img.xml"
    else
        echo "Error while generating addon-sys-img.xml"
        return 1
    fi    
}

if [ $# -ne 1 ]; then
    echo "You must give a version number as an argument."
    exit 1
fi

vnum=$1

time build_addon "pea" &>> addons.log
if [ $? -ne 0 ]; then
    echo "Error while building PE Android addon"
    exit 1
fi

gen_xml &>>addons.log
