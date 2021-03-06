#!/bin/bash
CURDIR=`readlink -f \`dirname $0\``
source ${CURDIR}/test_func.sh

ENMASSE_DIR=$1
KUBEADM=$2
SYSTEMTESTS=$3
TESTCASE=$4
failure=0

SANITIZED_PROJECT=$OPENSHIFT_PROJECT
SANITIZED_PROJECT=${SANITIZED_PROJECT//_/-}
SANITIZED_PROJECT=${SANITIZED_PROJECT//\//-}
export OPENSHIFT_PROJECT=$SANITIZED_PROJECT

setup_test ${ENMASSE_DIR} ${KUBEADM}

#environment info
LOG_DIR="${ARTIFACTS_DIR}/openshift-info/"
mkdir -p ${LOG_DIR}
get_openshift_info ${LOG_DIR} services default "-before"
get_openshift_info ${LOG_DIR} pods default "-before"

run_test ${TESTCASE} || failure=$(($failure + 1))

#environment info
get_openshift_info ${LOG_DIR} pv ${OPENSHIFT_PROJECT}
get_openshift_info ${LOG_DIR} pods ${OPENSHIFT_PROJECT} 
get_openshift_info ${LOG_DIR} services default "-after"
get_openshift_info ${LOG_DIR} pods default "-after"

#store artifacts
$CURDIR/collect_logs.sh ${ARTIFACTS_DIR}

if [ $failure -gt 0 ]
then
    echo "Systemtests failed"
    oc get events
    exit 1
else
    teardown_test $OPENSHIFT_PROJECT
fi
