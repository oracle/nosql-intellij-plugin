# Oracle NoSQL Database IntelliJ Plugin

Oracle NoSQL Database IntelliJ Plugin Version 1.5.2

## Overview

The Oracle NoSQL Database IntelliJ Plugin accelerates application development by integrating with IntelliJ IDEA. The plugin enables you to:

- Connect to [Oracle NoSQL Database Cloud Service](https://www.oracle.com/database/nosql-cloud.html), 
[Oracle NoSQL Database on-premises](https://www.oracle.com/database/technologies/related/nosql.html) and to
[Oracle NoSQL Cloud Simulator](https://www.oracle.com/downloads/cloud/nosql-cloud-sdk-downloads.html).
- Explore and manipulate development/test data.
- Build and test Oracle NoSQL Database queries efficiently.

## Features

The IntelliJ Plugin provides the following capabilities:

### Table Management

- View tables in a well-defined tree structure using the Schema Explorer.
- View metadata about columns, indexes, primary key(s), and shard key(s) for a table.
- View table data in a well-formatted JSON structure.
- Create tables using form-based schema entry or supply DDL statements.
- Drop tables.
- Add new columns using form-based entry or supply DDL statements.
- Drop columns.
- Create indexes using form-based schema entry or supply DDL statements, including complex data types for example, JSON, Map, Array and Record.
- Drop indexes.
- View the create table DDL
- Create child tables
- Update provisioned capacity of the cloud tables
- View create index DDL

### Query and Data Manipulation

- Execute SQL queries on a table and view query results in tabular format.
- Execute DML statements to update, insert, and delete data from a table.
- Download query results into JSON files.
- Work with complex data types such as Array, Maps, Records and JSON.
- Work with the binary and fixed binary data types.
- View and use previously executed SQL queries with command history
- Execute queries faster with auto-completion that suggests table names, column names, and functions.
- Syntax highlighted SQL in query workbench to reduce errors
- Prettify SQL with a single-click
- Navigate through JSON results efficiently

### Advanced Features

- Support for Json Collection tables
- Support for MR Counters
- Support for composite primary keys

### Multi-Region and Cloud Features

- Add Cloud connections using configuration file
- Add multiple Cloud, On-prem and Cloudsim connections
- Use Global Active Tables for Cloud
- Use Multi-region tables for On-prem
- Execute System Requests for the On-prem connections
- Use namespace support for On-prem connections

## Installation

Refer to the [Oracle NoSQL IntelliJ Plugin installation guide](https://docs.oracle.com/en/database/other-databases/nosql-database/24.4/plugins/setting-intellij-plug.html#GUID-D4D4DF59-2B59-4404-8461-C9EB0A6BA68B)

### Prerequisites

- IntelliJ IDEA Build '242.23726.103'
- Oracle NoSQL Java SDK '5.4.16' or later ([Download here](https://github.com/oracle/nosql-java-sdk)).
- Gradle version 8.10.2
- Maven version 3.9.6
- Java 17

## Getting Started

### Connect to Your Database

- On-Prem  :
  1. Deploy a Oracle NoSQL on-premises store [Install and configure Oracle NoSQL Database](https://docs.oracle.com/en/database/other-databases/nosql-database/24.4/admin/install-and-upgrade.html)
  2. Configure the [Oracle NoSQL Database Proxy](https://docs.oracle.com/en/database/other-databases/nosql-database/24.4/admin/proxy.html)
  3. [Connect to Oracle NoSQL Database On-premises server](https://docs.oracle.com/en/database/other-databases/nosql-database/24.4/plugins/connecting-oracle-nosqldatabase-intellij.html#GUID-DACD40CB-A57C-4336-8879-7252EFA645C2)

- Cloud    : [Connect to Oracle NoSQL Cloud Service](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/#GUID-8DB8A86D-5DEB-4E02-941B-9636A81650B3:~:text=Service%20or%20simulator.-,Connecting%20to%20Oracle%20NoSQL%20Database%20Cloud%20Service%20from%20IntelliJ,-Learn%20how%20to)

- CloudSim : [Connect to Oracle NoSQL CloudSimulator](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-64B71CFD-2C24-4A1E-A169-87AD1F37CC7F:~:text=in%20your%20schema.-,Connecting%20to%20Oracle%20NoSQL%20Database%20Cloud%20Simulator%20from%20IntelliJ,-Learn%20how%20to)

### Explore and Manage Tables

Once you connect to your database -

- Use the Schema Explorer to explore your tables.

- Click the table name to view its columns, indexes, primary key(s), and shard key(s). Each column name is displayed alongside its data type.

- You can refresh the schema or table at any time to re-query your deployment and populate Oracle NoSQL Database with the most up-to-date data.

- In the Schema Explorer, locate the connection and click the Refresh icon to reload the schema. Alternatively, you can right-click the connection and select Refresh Schema.
- To refresh a table, in the Schema Explorer, locate the table name right-click on it and select Refresh Table.

## Example Workflows

### Table Management

- [Create new table](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-54C067AF-D099-42D8-817F-A62E31992E20__CRT_TBL)

- [Drop table](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-54C067AF-D099-42D8-817F-A62E31992E20__DRP_TBL)

- [Add new columns](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-54C067AF-D099-42D8-817F-A62E31992E20__ADD_COL)

- [Drop column](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-54C067AF-D099-42D8-817F-A62E31992E20__DRP_COL)

- [Create index](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-54C067AF-D099-42D8-817F-A62E31992E20__CRT_IND)

- [Drop index](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-54C067AF-D099-42D8-817F-A62E31992E20__DRP_IND)

### Insert/Update/Delete a Row into the table

[Perform DML operations](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-E6426278-6494-41B3-AA5D-F7C70DDD1F39:~:text=it%20is%20initiated.-,Perform%20DML%20operations%20using%20IntelliJ,-You%20can%20add)

### Query tables

1. Browsing database table
2. Executing query
3. Download row in JSON
4. Download Query Result in JSON
5. To view query execution plan
6. Recently used commands

[Query tables reference](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-E6426278-6494-41B3-AA5D-F7C70DDD1F39:~:text=cannot%20be%20updated.-,Query%20tables,-Locate%20the%20Schema)


### View table creation DDL for a particular table

- Right-click the target table and select **View Table DDL**.
- Table creation DDL is displayed in a separate window, which also includes an option to copy the DDL.

### Create child tables

1. Right-click the target table and select **Create Child Table**
2. You can create the child table in two ways:

   - **Simple DDL Input** : You can use this mode to create the child table declaratively, that is, without writing a DDL statement.
   - **Advanced DDL Input** : You can use this mode to create the child table using a DDL statement.

3. Click **Create**.

### Work with complex data types such as Array, Maps, Record and JSON

- Perform DDL and DML operations for these data types and also create indexes.
- The complex data types are only supported through DDL and DML statements.

### Work with the binary and fixed binary data types

1. Create a table with **Binary** and **Fixed Binary** data types columns in it.
2. Right-click the target table and select insert row.
3. You can insert data in binary or fixed binary column in three ways :

   - **Direct input in TextField** : You can use this mode to insert data in binary column in Base64 format.
   - **Through file** : You can use this mode to open a prompt and select a .bin file to be inserted in the column.
   - **Advance input:JSON Schema** : You can insert the complete row in the table through advance input.

4. Click on **Insert**.
5. The table view of the binary and fixed binary column does not display the inserted text or file.
6. Instead of text, there is an option called **Download Binary Object**, which allows the user to download the binary object in .bin format to local storage and view its contents using any .bin editor.
7. A prompt appears asking the user to choose a location to download the binary object. The user can select the desired location and click **OK**.
8. A IntelliJ notification appears with one-click link to open the downloaded .bin file.

### Full schema renderer for the Map, Array, Record, Binary, Fixed Binary and Index in the NoSQL tool window tree

- Full detailed schema of the data type will be displayed in the NoSQL tool window tree.

### Global Active Tables for Cloud

[Basic Global Active Tables concepts](https://docs.oracle.com/en-us/iaas/nosql-database/doc/basic-concepts.html)

1. Connect to a cloud connection.
2. Create a table (One column must be of JSON type).
3. [Freeze/Unfreeze the table](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-54C067AF-D099-42D8-817F-A62E31992E20__FR_UNFR_SCHEMA) 
4. The Global Active Table provides support for :
   - Add Replica
   - View Replicas
   - Drop Replicas
- Reference : [Manage regional replicas](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-54C067AF-D099-42D8-817F-A62E31992E20__MNG_REP)

### Multi-Region Tables for On-prem

1. Set up the multiple kvstore and proxy.
2. [Set up the Multi-region environment](https://docs.oracle.com/en/database/other-databases/nosql-database/24.4/admin/use-case-1-set-multi-region-environment.html)
3. Use the plugin to create and manage the table and regions.
4. The above task can be achieved by **Execute DDL** option present in the NoSQL tool window.

### Execute System Requests for the On-prem connections

- System Request is an on-premise-only request used to perform any table-independent administrative operation such as create/drop of namespaces and security-relevant operations (create/drop users and roles). These operations are asynchronous and completion needs to be checked.

- Examples of statements used in this object include:

  - CREATE NAMESPACE mynamespace
  - CREATE USER some_user IDENTIFIED BY password
  - CREATE ROLE some_role
  - GRANT ROLE some_role TO USER some_user

- This request can be executed by using the **Execute DDL** option present in the NoSQL tool window.

### Edit provisioned capacity of the cloud tables

- [Edit provisioned capacity](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/index.html#GUID-54C067AF-D099-42D8-817F-A62E31992E20__EDIT_RES_CAP)

### Use namespace support for On-prem connections

- The namespace support is now available in the plugin.
- The namespaces can be created from the **Execute DDL** option present in the NoSQL tool window.
  More information on namespace :  [Oracle NoSQL Database Namespaces](https://docs.oracle.com/en/database/other-databases/nosql-database/24.4/java-driver-table/introducing-namespaces.html)
- While adding an On-prem connection, enter a namespace in the Namespace field.
- Fill the created namespace to set up a connection to a particular namespace under particular kvstore connection.
- Once connected to this connection, it will list all the tables under that namespace.

### Json Collection Tables

- To Create a new Json Collection Table :

   1. Hover over the database where you want to add the new table.
   2. Right click on the connection name and select **Create Table**.
   3. In the table creation mode, select **For Advanced DDL input**
   4. Supply the valid DDL statement for creating Json Collection table in the query box to create the table.

- To Insert a row in Json Collection Table :

   1. Right-click the target table and select **Insert Row**.
   3. You can INSERT a new ROW in only one mode : **Advanced JSON Input**
   4. Supply a valid Json string corresponding to a row in the Json Collection table in the text box.
   5. Click **Insert Row**.

- To Update a single row :

   1. Right-click on the target row. A context-menu appears.
   2. Select **Update Row** where you can only update in **For Advanced DDL input** mode.
   3. In the Json input, supply the Json string corresponding to the updated row to update a row.

- To create index :

   1. Right-click the target table and select **Create Index**.
   2. You can add index in only one mode : **Advanced DDL Input**
   3. Supply a valid DDL statement in the text box.
   4. Click **Create Index**.

- Rest everything can be used in the same manner as a normal table.

### MR Counters

- To use MR Counters, first we have to create a multi region table by selecting **Advanced DDL input** in the table creation mode and supplying a valid DDL statement with MR Counters.
- For inserting a new row, insert as usual ensuring that it contains all the MR Counter fields.
- For Updating a particular row containing MR Counter :

   1. Right-click on the target row. A context-menu will appear.
   2. Select **Update Row**.
   3. In both the input modes namely Simple DDL input and Advanced DDL input, you have to increment or decrement all the MR Counters as :
      - For Simple DDL input with non JSON fields, increment or decrement MR Counter fields as "<mrCounterName> +/- <value>"
      - For Json fields in Simple DDL input option containing nested MR Counters or Advanced DDL input option, increment or decrement MR Counters in the Json string as "<mrCounterName>" = "<mrCounterName> +/- <value>"

### Composite Primary Key

- While creating a table, you can create a composite primary key :

   1. **Simple DDL input**
      - Click the **Add Primary Key Column** button to add more fields to the primary key.

   2. **Advanced DDL input**
      - Supply a valid DDL statement to create a table with composite primary keys.

- To view the fields that makes up a composite primary key :

   1. Click the target table to see the listed columns, Primary Keys, Indexes and Shard Keys.
   2. Click the Primary key and a list of columns making the primary key appears.

### View Index DDL

1. Click the target table to see the listed columns, Primary Keys, Indexes and Shard Keys.
2. Locate the target-index and right-click on it.
3. Click **View Index DDL**.
4. Index creation DDL will be displayed in another window which also has a option to copy the DDL.


### Contributing

This project welcomes contributions from the community. Before submitting a pull request, please [review our contribution guide](./CONTRIBUTING.md)

### Security

Please consult the [security guide](./SECURITY.md) for our responsible security vulnerability disclosure process

### Help and Support

There are a few ways to get help or report issues:

- Refer to [Oracle NoSQL Cloud Plugin Documentation](https://docs.oracle.com/en/cloud/paas/nosql-cloud/yooud/#articletitle)
- Refer to [Oracle NoSQL On-Premises Documentation](https://docs.oracle.com/en/database/other-databases/nosql-database/24.4/plugins/intellij-plugin.html)
- Post your question on the [Oracle NoSQL Database Community](https://forums.oracle.com/ords/apexds/domain/dev-community/category/nosql_database)
- [Email support](mailto:oraclenosql-info_ww@oracle.com)

### License

Copyright (C) 2024, 2025 Oracle and/or its affiliates. All rights reserved.

This plugin is licensed under the Universal Permissive License 1.0. See [LICENSE](https://www.apache.org/licenses/LICENSE-2.0)
for details.
