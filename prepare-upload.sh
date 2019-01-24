#!/bin/bash
VERSION=$1

set -e

UPLOAD_FOLDER=./target/to_upload
MVN_FOLDER=maven

ZS_CORE_PATH=$UPLOAD_FOLDER/zkspring-core
ZS_SECURITY_PATH=$UPLOAD_FOLDER/zkspring-security

rm -rf $UPLOAD_FOLDER

mkdir -p $ZS_CORE_PATH/$VERSION/$MVN_FOLDER
mkdir -p $ZS_SECURITY_PATH/$VERSION/$MVN_FOLDER

cp zkspring-core/target/zkspring-core-$VERSION-javadoc.jar $ZS_CORE_PATH/$VERSION/
cp zkspring-core/target/zkspring-core-$VERSION-bundle.jar $ZS_CORE_PATH/$VERSION/$MVN_FOLDER

cp zkspring-security/target/zkspring-security-$VERSION-javadoc.jar $ZS_SECURITY_PATH/$VERSION/
cp zkspring-security/target/zkspring-security-$VERSION-bundle.jar $ZS_SECURITY_PATH/$VERSION/$MVN_FOLDER
