#!/bin/bash
PRG=`type $0`
PRG=${PRG##* }
# If PRG is a symlink, trace it to the real home directory
while [ -L "$PRG" ]
do
    newprg=$(ls -l ${PRG})
    newprg=${newprg##*-> }
    [ ${newprg} = ${newprg#/} ] && newprg=${PRG%/*}/${newprg}
    PRG="$newprg"
done
PRG=${PRG%/*}
echo Changing to application folder ${PRG}
cd ${PRG}
# Uncomment template to create desired setting
# cd linuxscripts
#
# # 4:3 format presentation on 720p (16:9) beamer
# # bash vncserver43.sh
# # bash beamer720p.sh # make sure to use the correct screen identifiers
#
# # 4:3 format presentation on XGA (4:3) beamer
# # bash vncserver43.sh
# # bash beamerXGA.sh # make sure to use the correct screen identifiers
#
# bash disable.sh # disable touch input
# cd ..

# adjust font and fontsize to your liking
LD_LIBRARY_PATH=linux64 java  -Dswing.plaf.metal.controlFont="DejaVu Sans Mono Book-20" -Xmx4096M -jar ttt.jar

# cd linuxscripts
# bash nativeResolution.sh
# cd ..

cd -
