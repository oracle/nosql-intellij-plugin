# Change Log
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [1.5.6] - 2026-09-01

### Added
- Added a first-use approval prompt before the plugin creates a database
  connection from project-stored settings. Approval is scoped to the current
  IDE session and the selected project/connection target.
- Added URL validation for On-prem and CloudSim connection endpoints. URLs
  must use `http` or `https`, include a host, and must not include embedded
  credentials.
- Added IntelliJ PasswordSafe storage for sensitive connection values,
  including On-prem passwords, On-prem truststore passphrases, Cloud
  private-key passphrases, and CloudSim tenant identifiers.
- Added automatic migration for existing project connection settings so older
  projects do not require a manual migration step.
- Added a build option to skip IntelliJ searchable-options generation in
  restricted-network or VPN environments:
  `-Poracle.nosql.skipSearchableOptions=true`.
- Added JetBrains Marketplace agreement, approval guideline, and developer
  agreement documents to the repository.

### Changed
- Opening a project or Schema Explorer no longer automatically connects to a
  configured database. Users must explicitly use Refresh or Refresh Schema to
  connect and load schema information.
- CloudSim now requires an explicit tenant identifier. Blank values and the old
  `exampleId` value are rejected, and tenant identifiers must be alphanumeric.
- CloudSim tenant identifiers are treated as sensitive token material and are
  not shown in connection display text.
- On-prem SSL truststore settings now apply only to the Oracle NoSQL connection
  created by the plugin and no longer change IntelliJ IDEA JVM-wide trust
  settings.
- Updated the plugin release version to `1.5.6`.
- Updated the plugin display/artifact name to Oracle NoSQL Database Connector.
- Updated the IntelliJ Platform Gradle Plugin from `2.10.5` to `2.16.0`.
- Updated the IntelliJ Platform dependency to resolve IntelliJ IDEA Ultimate
  explicitly and updated plugin verification to use the current target IDE.
- Updated the Gradle wrapper to `9.3.0`.
- Updated the plugin distribution build so `./gradlew buildPlugin` creates the
  deployable plugin ZIP:
  `plugin/build/distributions/Oracle-NoSQL-Database-Connector-<version>.zip`.
- Updated the root `buildPlugin` task to depend on `:plugin:buildPlugin`.
- Updated plugin release notes in Gradle and `plugin.xml` to describe the
  changes from `1.5.4` to `1.5.6`.
- Updated the Oracle NoSQL Java SDK from `5.4.18` to `5.4.22`.
- Updated Gson from `2.11.0` to `2.13.2`.
- Updated README setup, connection, CloudSim, credential-storage, and build
  documentation for the new connection and build behavior.

### Fixed
- Removed an unused `PluginManagerCore.getPlugin(PluginId)` call that caused
  JetBrains Marketplace to report internal API usage.
- Replaced the legacy project XML parser used during startup with migration
  based on IntelliJ persistent project state.
- Fixed build instability after the Gradle IntelliJ Platform Plugin 2.x
  upgrade.
- Fixed record-view and update-row shared-state issues that could affect
  concurrent row update, row JSON download, and binary object download actions.
- Fixed deprecated IntelliJ API usage and plugin configuration structure for
  newer IntelliJ Platform compatibility.
- Improved binary object download handling so errors are reported without using
  shared static state.

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
