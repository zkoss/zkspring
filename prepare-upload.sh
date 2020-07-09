#!/bin/bash
VERSION=$1

set -e

UPLOAD_FOLDER=./target/to_upload

rm -rf $UPLOAD_FOLDER

MVN_FOLDER=maven
BIN_FOLDER=zk

ZS_CORE_PATH=$UPLOAD_FOLDER/zkspring-core/$VERSION
ZS_SECURITY_PATH=$UPLOAD_FOLDER/zkspring-security/$VERSION

ZS_CORE_MVN_PATH=$ZS_CORE_PATH/$MVN_FOLDER
ZS_CORE_BIN_PATH=$ZS_CORE_PATH/$BIN_FOLDER
ZS_SECURITY_MVN_PATH=$ZS_SECURITY_PATH/$MVN_FOLDER
ZS_SECURITY_BIN_PATH=$ZS_SECURITY_PATH/$BIN_FOLDER

mkdir -p $ZS_CORE_MVN_PATH
mkdir -p $ZS_CORE_BIN_PATH/tmp_zip
mkdir -p $ZS_SECURITY_MVN_PATH
mkdir -p $ZS_SECURITY_BIN_PATH/tmp_zip


ZS_CORE_JARS_PREFIX=zkspring-core/target/zkspring-core-$VERSION
cp $ZS_CORE_JARS_PREFIX-javadoc.jar $ZS_CORE_PATH
cp $ZS_CORE_JARS_PREFIX-bundle.jar  $ZS_CORE_MVN_PATH

cp $ZS_CORE_JARS_PREFIX.jar         $ZS_CORE_BIN_PATH/tmp_zip
cp $ZS_CORE_JARS_PREFIX-javadoc.jar $ZS_CORE_BIN_PATH/tmp_zip
cp $ZS_CORE_JARS_PREFIX-sources.jar $ZS_CORE_BIN_PATH/tmp_zip
cp -r zkdoc                         $ZS_CORE_BIN_PATH/tmp_zip/doc
(cd $ZS_CORE_BIN_PATH/tmp_zip && zip -r ../zkspring-core-bin-$VERSION.zip *)


ZS_SECURITY_JARS_PREFIX=zkspring-security/target/zkspring-security-$VERSION
cp $ZS_SECURITY_JARS_PREFIX-javadoc.jar $ZS_SECURITY_PATH
cp $ZS_SECURITY_JARS_PREFIX-bundle.jar  $ZS_SECURITY_MVN_PATH

cp $ZS_SECURITY_JARS_PREFIX.jar         $ZS_SECURITY_BIN_PATH/tmp_zip
cp $ZS_SECURITY_JARS_PREFIX-javadoc.jar $ZS_SECURITY_BIN_PATH/tmp_zip
cp $ZS_SECURITY_JARS_PREFIX-sources.jar $ZS_SECURITY_BIN_PATH/tmp_zip
cp -r zkdoc                             $ZS_SECURITY_BIN_PATH/tmp_zip/doc
(cd $ZS_SECURITY_BIN_PATH/tmp_zip && zip -r ../zkspring-security-bin-$VERSION.zip *)

rm -r $ZS_CORE_BIN_PATH/tmp_zip
rm -r $ZS_SECURITY_BIN_PATH/tmp_zip
