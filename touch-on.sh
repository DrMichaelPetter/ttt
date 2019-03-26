#!/bin/bash
input=`xinput | grep Digitizer | cut -f2 | cut -f2 -d=`
xinput enable $input
