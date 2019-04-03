#!/bin/bash
xrandr --output eDP-1 --mode 1024x768
xrandr --output eDP-1 --mode 1920x1080 --pos 0x0
xrandr --output HDMI-2 --mode 1280x720 --pos 0x0
xrandr --output HDMI-2 --scale-from 1920x1080 --pos 0x0
