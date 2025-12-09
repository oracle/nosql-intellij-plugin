# Change Log
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [1.5.4] - 2025-12-01

### Added
- Added support for OCI session tokens when adding cloud connections
 using an OCI config file.

### Changed
- Removed the need to manually configure the Oracle NoSQL SDK path for 
Cloud, On-prem, and CloudSim connections. The SDK is now included solely 
as a Gradle dependency.
- Updated the connection display name in the NoSQL Tool Window for cloud 
connections created using OCI config files.
- Updated the Update Row (Advanced DDL) dialog with a new scrollable, 
JSON-aware text area, replacing the old non-JSON editor.
- Creating a cloud connection using an OCI config file no longer requires 
specifying the endpoint. Only the config file location and profile are required.
- Upgraded Gradle to version `8.14.2+.`
- Upgraded the Oracle NoSQL Java SDK to version `5.4.18.`
- Updated the minimum supported IntelliJ Platform version to 
`2025.1.7+ (build 251.29188.11+)`.
- Updated IntelliJ plugin tooling from `org.jetbrains.intellij` v1.17.3  
to `org.jetbrains.intellij.platform` v2.10.5.

### Fixed
- Resolved the issue described in GitHub Issue #11, affecting both 
On-prem and Cloud connections when using kv-25.3 and above.
- Fixed JSON explorer issues in the table row view, including problems 
navigating JSON collection tables.
- Fixed IntelliJ runtime errors occurring inside the plugin:
   * `java.lang.Throwable: TreeUI should be accessed only from EDT`
   * `com.intellij.diagnostic.PluginException: Cannot init component state`

## [1.5.3] - 2025-03-04
This was the initial release of the Oracle NoSQL Database IntelliJ Plugin delivered on GitHub.

### Added

- **Download Enhancements:**
    * Download row into a JSON file.
    * Download query results into JSON files.
- **Indexing Improvements:**
    * Create JSON Index.
    * Create Index using DDL.
- **Schema and Data Type Support:**
    * View Table DDL.
    * Support for complex data types such as Array, Maps, Records, and JSON.
    * Full Schema renderer for Map, Array, Record, Binary, Fixed Binary, and Index 
	in the NoSQL tool window tree.
    * Support for Binary and Fixed Binary.
- **Command and Query Execution:**
    * Command History.
    * Execute system requests for On-prem connections.
    * Result navigation, including JSON path exploration.
    * Pretty printing SQL.
    * Syntax highlighting.
    * Column and SQL auto-completion.
- **Table and Connection Management:**
    * Creation of child tables.
    * Adding cloud connection using a config file.
    * Multiple connection support for Cloud, On-prem, and CloudSim.
    * Global Active Tables support for Cloud.
    * Multi-region table support for On-prem.
    * Edit reserved capacity of cloud tables.
    * Added namespace support for On-prem connections.
- **Additional Features:**
    * Support for JSON Collection Tables.
    * Support for MR Counters.
    * Added "View Index DDL" feature.
    * Support for composite primary keys.