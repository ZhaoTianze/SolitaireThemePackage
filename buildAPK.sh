#!/usr/bin/env bash
source ~/.bash_profile
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_ROOT="$DIR"
APP_STUDIO_ROOT="$DIR/SolitaireTheme"
PARAMS="$1"

echo "param[res, build]...
res: not build apk！
build: not copy res！"

cd $APP_ROOT

# if [[ $PARAMS != "build" ]]; then
# 	packageTheme
# fi

java -jar package.jar

cd $APP_STUDIO_ROOT

if [[ $PARAMS != "res" ]]; then
	./gradlew myRelease
fi



cd $APP_ROOT
