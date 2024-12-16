#!/bin/sh
export SCRIPTNAME=build.sh
PLATFORM=

usage() {
    echo "usage: $SCRIPTNAME <-platform intellij> [-help]"
    exit 1
}

build_common() {
    echo "##########Building common model##########"
    pushd oracle.nosql > /dev/null
    pushd oracle.nosql.model > /dev/null
    mvn clean install -DskipTests=true
    popd > /dev/null
    popd > /dev/null
}

build_intellij() {
    echo "##########Building intellij plugin##########"
    pushd oracle.nosql > /dev/null
    pushd oracle.nosql.intellij.plugin > /dev/null
    ./gradlew clean
    ./gradlew buildPlugin
    #sign jar
    #java -jar ../../Client.jar batchsign -task sign_file_batch -user oraclenosql_grp -file_to_sign ./build/distributions/*.zip -signed_location ./build/distributions/  -global_uid vsettipa -sign_method java2
}

while [ $# -gt 0 ]
do
    case "$1" in
        -platform)
            if [ "$2" != "intellij" -a "$2" != "all" ]; then
                echo "invalid value \"$2\" for platform" >&2
                usage >&2
            fi
            PLATFORM=$2;
            shift
            shift;;
        -help)
            usage
            ;;
        *)
            echo "Unknown option: $1" >&2
            usage >&2
            ;;
    esac
done

if [ "$PLATFORM" = "" ]; then
    echo "$SCRIPTNAME: The -platform option must be specified" >&2
    usage >&2
fi

if [ "$PLATFORM" = "intellij" ]; then
    build_common
    build_intellij
fi
