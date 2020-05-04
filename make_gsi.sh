#!/bin/bash

NUM_COMPILE=`echo $(($(nproc) * 2))`

source build/envsetup.sh &> build_gsi.log
lunch pea_gsi-userdebug &>> build_gsi.log

function build() {
    make -j${NUM_COMPILE} ANDROID_COMPILE_WITH_JACK=false
}

time build &>> build_gsi.log
echo "DONE WITH BUILD" &>> build_gsi.log
