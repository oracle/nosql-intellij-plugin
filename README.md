# How to Build

Download NoSQL Java SDK :
- To build plugin you will need to download the latest NoSQL Java SDK
  from [GitHub](https://github.com/oracle/nosql-java-sdk).

Intellij:
- For Intellij Update the 'DOWNLOAD_PATH/oracle-nosql-java-sdk/lib/nosqldriver.jar' with the path to downloaded library in 'oracle.nosql/oracle.nosql.intellij.plugin/gradle.properties' file

- Run the build.sh script with "-platform intellij"

- The above build will generate the plugin zip
  in 'oracle.nosql/oracle.nosql.intellij.plugin/build/distributions/'

