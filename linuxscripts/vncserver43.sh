#!/bin/bash
vncserver -kill :1
vncserver -geometry 1400x1050
command -v kdialog || {
    command -v zenity || { echo 'VNC started'; exit 1 ; }
    zenity --info --text="VNC started"
    exit 1
}
kdialog -msgbox 'VNC started'
