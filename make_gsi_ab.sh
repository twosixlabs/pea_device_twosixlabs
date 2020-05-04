#!/bin/bash

NUM_COMPILE=`echo $(($(nproc) * 2))`

source build/envsetup.sh &> build_gsi_ab.log
lunch pea_gsi_ab-userdebug &>> build_gsi_ab.log

function build() {
    make -j${NUM_COMPILE} ANDROID_COMPILE_WITH_JACK=false
}

time build &>> build_gsi_ab.log
echo "DONE WITH BUILD" &>> build_gsi_ab.log
