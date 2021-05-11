#!/bin/bash
#
# bundle ubercube into a "installable" zip
#
set -ex

FNAME=ubercube.zip
OS=linux

rm ${FNAME} || true
zip ${FNAME} -r res
zip ${FNAME} -j target/ubercube.jar bin/${OS}/*
