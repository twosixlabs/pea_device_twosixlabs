#!/bin/bash

SIZE_GIGS=$1

MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
FILLER_FILE=$MY_DIR/fillerfile.tmp

if [ ! -f $FILLER_FILE ]; then
    fallocate -l $SIZE_GIGS $FILLER_FILE
fi
