#!/bin/bash
xrandr --output eDP-1 --mode 1920x1080
input=`xinput | grep "Pen stylus" | cut -f2 | cut -f2 -d=`
# reset to whatever xsetwacom --get $input Area    said
xsetwacom --set $input Area 0 0 30000 16524