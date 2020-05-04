#!/bin/bash

NUM_COMPILE=`echo $(($(nproc) * 2))`

source build/envsetup.sh &> build_gsi_a.log
lunch pea_gsi_a-userdebug &>> build_gsi_a.log

function build() {
    make -j${NUM_COMPILE} ANDROID_COMPILE_WITH_JACK=false
}

time build &>> build_gsi_a.log
echo "DONE WITH BUILD" &>> build_gsi_a.log
