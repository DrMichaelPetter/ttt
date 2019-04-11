#!/bin/bash
xrandr --output eDP-1 --mode 1024x768
xrandr --output eDP-1 --mode 1400x1050 --pos 0x0
xrandr --output DP-1 --mode 1024x768 --pos 0x0 --scale-from 1400x1050
input=`xinput | grep "Pen stylus" | cut -f2 | cut -f2 -d=`
xsetwacom --set $input Area 3800 0 25800 16524