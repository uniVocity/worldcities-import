# World Cities Import

This simple project was created to demonstrate how **[uniVocity](http://www.univocity.com/pages/about-univocity)** can be used to load/transform massive amounts of data, and how it performs.

We will be using a couple of files made available for free by [Maxmind](http://www.maxmind.com). One is a 3 million plus rows CSV file, which has information about all the world's cities. 

The size of this file can present some problems, especially if you need to load the data into a different database schema. Additionally, there are inconsistencies, such as incomplete data and duplicate cities, which we will try to address.

We created two examples for you:

 * [LoadWorldCities.java] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java) - loads the [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) file into the database table [worldcities](./src/main/resources/database/mysql/worldcities.tbl). 
 
 * [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java) - migrates the [region_codes.csv](http://geolite.maxmind.com/download/geoip/misc/region_codes.csv) file to the database table [region](./src/main/resources/database/mysql/region.tbl). We will use uniVocity to manage the keys generated upon insertion of each record, and then migrate data from [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) to the [city](./src/main/resources/database/mysql/city.tbl) table. The  [city](./src/main/resources/database/mysql/city.tbl) table has a foreign key referencing [region](./src/main/resources/database/mysql/region.tbl). In this process we will discard duplicate cities, and cities that are not associated to any region.

## What to expect

These ETL processes will perform differently depending on your setup and hardware. For reference, here's my (very) modest hardware, an ultrabook: 

 * CPU: Intel i5-3337U @ 1.8 GHz
 * RAM: 4 GB
 * Storage: 128GB SSD drive
 * OS: Arch Linux 64 bit 
 * DB: MySQL

These are the statistics I got after processing 3,173,958 rows:

| Process           |   Time to complete | Rows inserted | Memory | Throughput | License |
|-------------------|-------------------:|--------------:|-------:|-----------:|---------|
|[LoadWorldCities.java] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java)|	64.094 seconds | 3,173,958 | 300 MB | 49,520.4 rows/s  | Evaluation |
|[LoadWorldCities.java] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java)|	8.069 minutes |	3,173,958 | 300 MB | 6,555.5 rows/s.  |	Free |
|[MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java)|61.835 seconds | 3,032,002 | 3 GB |51,269.3 rows/s  | Evaluation |
|[MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java)|11.717 minutes	| 3,032,002 | 3 GB |4,509.3 rows/s  | Free |
 
Notice that uniVocity is free for non-commercial use, but in this case batch operations are disabled. Nevertheless, the performance is not too bad considering the amount of data we are inserting without batching. We are constantly working to improve uniVocity's performance even further.

If you want to execute these processes with an evaluation license, run the [RequestLicense.java](./src/main/java/com/univocity/articles/importcities/RequestLicense.java) to obtain a free 30-day trial license. 

## Setting up

Before executing the example processes, you need to download the input files and setup your database.  

### Download instructions.

Download the following files from [Maxmind](http://www.maxmind.com):

 * Region codes of all countries in the world: http://geolite.maxmind.com/download/geoip/misc/region_codes.csv
 * All cities in the world:	http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz - this one is a big CSV file with more than 3 million records.
 
Unzip and place them inside the (src/main/resources/files)[.src/main/resources/files] directory. You should end up with the following structure:

```

	worldcities-import/src/main/resources/files/region_codes.csv
	worldcities-import/src/main/resources/files/worldcitiespop.txt
	
```
 
### Database setup.

We created scripts to generate the required tables for you in the database of your preference. This project includes scripts for [HSQLDB](./src/main/resources/database/hsqldb), [MySQL](./src/main/resources/database/mysql), [Oracle XE](./src/main/resources/database/oraclexe) and [Postgres](./src/main/resources/database/postgres). You can use any other database if you want, just follow [these instructions](./ADD_DB.md) (feel free to submit a pull request with additional scripts your database).

Simply edit the [connection.properties](./src/main/resources/connection.properties) file with the connection details for your database of choice.

#### If you are using Oracle XE:

Please download the JDBC drivers (`odjbc6.jar`) manually from [Oracle's website](http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html) and add it to your classpath manually.

To ensure almost everyone is able to execute this project, we made it compatible with the JDK 6. If you are still using the JDK 6, ensure you are downloading the compatible version of the JDBC driver.

You may also need to configure the database itself to allow bigger batch sizes, or increase the number of open cursors. 

## Executing the processes 

Just execute [LoadWorldCities.java] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java) or [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java) as a java program. The process will try to connect to your database and create the required tables if they are not present, and then the process will start.

If this is the first time you execute uniVocity, a pop-up will will be displayed asking if you agree with the uniVocity free license terms and conditions. Once you agree it will disappear and the process will start normally. Keep in mind that with the free license, batching is disabled.

## Explaining the configurations in [EtlProcess] (./src/main/java/com/univocity/articles/importcities/EtlProcess.java)

The [EtlProcess class] (./src/main/java/com/univocity/articles/importcities/EtlProcess.java) is an abstract class that initializes a [DataIntegrationEngine](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/engine/DataIntegrationEngine.java) object and delegates the definition of data mappings between the CSV files and the database tables to its subclasses.  

### A brief introduction

uniVocity works with abstractions called *data stores* and *data entities*. A *data entity* is an abstraction over anything that is able to store and/or retrieve data, and a *data store* is used to access and manage of data entities. This allows us to use virtually anything as sources and destinations of data. In this example we will use uniVociy to simply extracts data from one or more source data entities (the world cities files) and map the information to destination data entities (a few database tables).

In general, the first thing you need to do to use uniVocity is to configure the a few data stores.

### Configuring the CSV data store.

uniVocity comes with a few pre-defined data stores and you just need to provide some essential configurations. In the [EtlProcess class] (./src/main/java/com/univocity/articles/importcities/EtlProcess.java), we create a [CsvDataStoreConfiguration](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/entity/text/csv/CsvDataStoreConfiguration.java):

```java
CsvDataStoreConfiguration csv = new CsvDataStoreConfiguration("csv");
csv.setLimitOfRowsLoadedInMemory(batchSize);
csv.addEntities("files", "ISO-8859-1");

CsvEntityConfiguration regionCodesConfig = csv.getEntityConfiguration("region_codes");
regionCodesConfig.setHeaders("country", "region_code", "region_name");
regionCodesConfig.setHeaderExtractionEnabled(false);
```

In this snippet, `new CsvDataStoreConfiguration("csv");` creates a CSV data store with name "csv". `setLimitOfRowsLoadedInMemory(batchSize)` limits the number of rows loaded in memory by uniVocity at any given time. uniVocity will wait if the rows loaded during a data mapping cycle are taking too long to be consumed.

The `csv.addEntities("files", "ISO-8859-1");` adds all files under the [src/main/resources/files](./src/main/resources/files) to this data store, and will read them with using the `ISO-8859-1` encoding.

As the [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) has a header row to identify what data is in each column, no configuration is required. uniVocity will detect the available fields automatically.

However, the [region_codes.csv](http://geolite.maxmind.com/download/geoip/misc/region_codes.csv) does not have a header row so we need to provide this information manually. `csv.getEntityConfiguration("region_codes");` will return a configuration object for the `region_code` entity, which is used to provide the headers of the file. We also
disable header extraction by invoking `setHeaderExtractionEnabled(false)`. This way uniVocity won't consider the first row in the input file as the header row.

### Configuring the JDBC data store.

When interacting with a database, you are likely to use uniVocity's built-in [JdbcDataStoreConfiguration](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/entity/jdbc/JdbcDataStoreConfiguration.java):

```java
DataSource dataSource = database.getDataSource();
JdbcDataStoreConfiguration config = new JdbcDataStoreConfiguration("database", dataSource);
config.setLimitOfRowsLoadedInMemory(batchSize);

JdbcEntityConfiguration defaultConfig = config.getDefaultEntityConfiguration();
defaultConfig.setBatchSize(batchSize);
defaultConfig.retrieveGeneratedKeysUsingStatement(true);

database.applyDatabaseSpecificConfiguration(config);
```

To create a JDBC data store, a `javax.sql.DataSource` is required. In this example we use the one initialized by the [Database](./src/main/java/com/univocity/articles/importcities/databases/Database.java) class using the connection details provided in the [connection.properties](./src/main/resources/connection.properties) file.

With a datasource ready, a JDBC data store configuration can be created with `new JdbcDataStoreConfiguration("database", dataSource);`. The name of this data store will be "database".

Using the `getDefaultEntityConfiguration()` method, we can define default configurations for all entities (in this case tables) in the "database" data store. `setBatchSize(batchSize)` defines the size of bulk insert/update/delete operations over all tables in this data store.

`retrieveGeneratedKeysUsingStatement(true)` configures how generated keys should be extract upon insertion of new rows into any table of this database. In this case generated keys will be retrieved from the `java.sql.Statement` used to insert. The `true` flag indicates that the insert operations can be batched and the JDBC driver supports returning all generated keys after a batch operation. Some JDBC drivers do not support this and in this case batch insertion must be disabled. To circumvent this limitation, we implemented some strategies to allow insertions in batch with retrieval of generated keys in the [JdbcEntityConfiguration](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/entity/jdbc/JdbcEntityConfiguration.java) class.

Finally, any other database-specific configuration is applied by the underlying implementation of  [Database](./src/main/java/com/univocity/articles/importcities/databases/Database.java).
Refer to the implementation of each specific database [here](./src/main/java/com/univocity/articles/importcities/databases) to learn more.

Our JDBC data store implementation will identify all available tables in your database and initialize them as JDBC data entities. So that's all you need to do in terms of configuration.

### Configure a data integration engine
With the data stores properly configured, you can create an [EngineConfiguration](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/config/EngineConfiguration.java) object:

```java
EngineConfiguration config = new EngineConfiguration(engineName, databaseConfig, fileConfig);
```

With the engine configuration ready, you can just register this engine to make it available anywhere in your application:


```java
Univocity.registerEngine(config);
```

Once the engine has been registered, you can get an instance of [DataIntegrationEngine](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/engine/DataIntegrationEngine.java) using:

```java
engine = Univocity.getEngine(engineName);
``` 

And you are ready to define/modify data mappings among entities of your data stores and execute data mapping cycles!


#### Configuring how metadata is stored (optional)

To perform operations such as data change autodetection (not demonstrated here) and reference mappings (used in [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java)), uniVocity generates metadata for each record persisted. You can define where this metadata must be stored or simply use uniVocity with its in-memory metadata database, which is created automatically. If you want to use a database to store metadata, ensure it is tuned to allow fast insert operations.

To configure the metadata storage, create a [MetadataSettings](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/config/MetadataSettings.java) object with the `javax.sql.DataSource` for the database.

```java
MetadataSettings metadata = new MetadataSettings(metadataDatabase.getDataSource());
metadata.setMetadataTableName("metadata");
metadata.setTemporaryTableName("metadata_tmp");
metadata.setBatchSize(batchSize);
...
```

Finally, simply invoke the `engineConfiguration.setMetadataSettings(metadataConfig)` method to configure your data integration engine to use this database.

## Loading data with [LoadWorldCities] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java)

The [LoadWorldCities] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java) class is quite straightforward as a few simple field mappings are required:

```java
DataStoreMapping mapping = engine.map("csv", "database");

//Creates a mapping between the entities available in the data stores: worldcitiespop (from "csv") and worldcities (from "database")
EntityMapping cityMapping = mapping.map("worldcitiespop", "worldcities");

//All mappings require an identity. The worldcitiespop file does not have a single field that works as an identity, so we define
//a composite identity. In the destination, the worldcities table has an "ID" column, but in this process we are not interested
//in doing anything with it. We just give the column names to where values should be copied.
cityMapping.identity().associate("country", "city", "region").to("country", "city_ascii", "region");

//copies the value from "accentCitity" (in the csv file) to "city" (in the database table)
cityMapping.value().copy("accentCity").to("city");

//population, latitude and longitude have the same names on both source and destination
cityMapping.autodetectMappings();

//As we are just loading the worldcities table, no metadata is required. We can just delete all rows (if any) and insert all rows from the file.
cityMapping.persistence().notUsingMetadata().deleteAll().insertNewRows();
```

Using the [DataIntegrationEngine](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/engine/DataIntegrationEngine.java) initialized by the parent class
[EtlProcess] (./src/main/java/com/univocity/articles/importcities/EtlProcess.java), we start by creating a mapping between the 2 datastores configured previously: `engine.map("csv", "database")` will create a  [DataStoreMapping](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/config/builders/DataStoreMapping.java), which will be used to create mappings between their data entities.

`map("worldcitiespop", "worldcities")` will create an [EntityMapping](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/config/builders/EntityMapping.java) object which will be used to associate fields of the [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) file to the [WorldCities] (./src/main/resources/database/mysql/worldcities.tbl) database table.

All we have to do now is to associate fields in the source to the fields in the destination. uniVocity requires an identity mapping (...to be continued)


## Migrating data with [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java)  
