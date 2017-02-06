#!/bin/bash
source ~/.profile

if [ "$1" == "" ] || [ "$2" == "" ]; then
	return
fi

/usr/local/bin/TexturePacker --sheet "$1".png --data "$1".plist "$2"/UI_CardResources/*png
