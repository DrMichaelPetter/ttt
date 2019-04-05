#!/bin/bash
input=`xinput | grep Finger | cut -f2 | cut -f2 -d=`
xinput disable $input
input=`xinput | grep Digitizer | cut -f2 | cut -f2 -d=`
xinput disable $input
command -v kdialog || {
    command -v zenity || { echo 'Touch disabled'; exit 1 ; }
    zenity --info --text="Touch disabled"
    exit 1
}
kdialog -msgbox 'Touch disabled'