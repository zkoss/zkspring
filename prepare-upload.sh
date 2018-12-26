#!/bin/bash
VERSION=$1

set -e

UPLOAD_FOLDER=./target/to_upload
MVN_FOLDER=$VERSION/maven

ZS_CORE_PATH=$UPLOAD_FOLDER/zkspring-core/$MVN_FOLDER
ZS_SECURITY_PATH=$UPLOAD_FOLDER/zkspring-security/$MVN_FOLDER

rm -rf $UPLOAD_FOLDER

mkdir -p $ZS_CORE_PATH
mkdir -p $ZS_SECURITY_PATH

cp zkspring-core/target/zkspring-core-$VERSION-bundle.jar $ZS_CORE_PATH

cp zkspring-security/target/zkspring-security-$VERSION-bundle.jar $ZS_SECURITY_PATH
