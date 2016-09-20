#!/usr/bin/env bash
source ~/.bash_profile
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_ROOT="$DIR"
APP_STUDIO_ROOT="$DIR/SolitaireTheme"

cd $APP_ROOT

packageTheme

cd $APP_STUDIO_ROOT

./gradlew assembleRelease

cd $APP_ROOT
