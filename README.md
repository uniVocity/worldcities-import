# Import world cities using uniVocity

This simple project was created to demonstrate how **[uniVocity](http://www.univocity.com/pages/about-univocity)** can be used to load/transform massive amounts of data, and how it performs.

We will be using a couple of files made available for free by [Maxmind](http://www.maxmind.com). One is a 3 million plus rows CSV file, which has information about all the world's cities. 

The size of this file can present some problems, especially if you need to load the data into a different database schema. Additionally, there are inconsistencies, such as incomplete data and duplicate cities, which we will try to address.

We created two examples for you:
 * [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java) - migrates both the [region_codes.csv](http://geolite.maxmind.com/download/geoip/misc/region_codes.csv) and  [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) files into the database tables [region](./src/main/resources/database/mysql/region.tbl) and [city](./src/main/resources/database/mysql/city.tbl). We will use uniVocity to manage the keys generated upon insertion of each `region` record, and create `city` records with consistent foreign key values in the `region_id` column. In this process we will also discard duplicate cities, and cities that are not associated with any region.

 * [LoadWorldCities.java] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java) - loads the [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) file into the database table [worldcities](./src/main/resources/database/mysql/worldcities.tbl). This is just a simple copy with no special transformations involved.
 

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
|[MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java)|61.835 seconds | 3,032,002 | 3 GB |51,269.3 rows/s  | Evaluation |
|[MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java)|11.717 minutes	| 3,032,002 | 3 GB |4,509.3 rows/s  | Free |
|[LoadWorldCities.java] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java)|	64.094 seconds | 3,173,958 | 300 MB | 49,520.4 rows/s  | Evaluation |
|[LoadWorldCities.java] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java)|	8.069 minutes |	3,173,958 | 300 MB | 6,555.5 rows/s.  |	Free |
 
Notice that uniVocity is free for non-commercial use, but in this case batch operations are disabled. Nevertheless, the performance is not too bad considering the amount of data we are inserting without batching. We are constantly working to improve uniVocity's performance even further.

If you want to execute these processes with an evaluation license, run the [RequestLicense.java](./src/main/java/com/univocity/articles/importcities/RequestLicense.java) to obtain a free 30-day trial license. 

## Setting up

Before executing the example processes, you need to download the input files and setup your database.  

### Download instructions.

Download the following files from [Maxmind](http://www.maxmind.com):

 * Region codes of all countries in the world: http://geolite.maxmind.com/download/geoip/misc/region_codes.csv
 * All cities in the world:	http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz - this one is a big CSV file with more than 3 million records.
 
Unzip and place them inside the [src/main/resources/files](./src/main/resources/files) directory. You should end up with the following structure:

```

	worldcities-import/src/main/resources/files/region_codes.csv
	worldcities-import/src/main/resources/files/worldcitiespop.txt
	
```
 
### Database setup.

We created scripts to generate the required tables for you in the database of your preference. This project includes scripts for [HSQLDB](./src/main/resources/database/hsqldb), [MySQL](./src/main/resources/database/mysql), [Oracle XE](./src/main/resources/database/oraclexe), [Microsoft SQL Server](./src/main/resources/database/sqlserver) and [Postgres](./src/main/resources/database/postgres). You can use any other database if you want, just follow [these instructions](./ADD_DB.md) (feel free to submit a pull request with additional scripts your database).

Simply edit the [connection.properties](./src/main/resources/connection.properties) file with the connection details for your database of choice.

#### If you are using Oracle XE:

Please download the JDBC drivers (`odjbc6.jar`) from [Oracle's website](http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html) and add it to your classpath manually.

To ensure almost everyone is able to execute this project, we made it compatible with the JDK 6. If you are still using the JDK 6, ensure you are downloading the compatible version of the JDBC driver.

You may also want to configure the database itself to allow bigger batch sizes, and increase the number of open cursors. 

#### If you are using Microsoft SQL Server:

Please download the JDBC drivers (`sqljdbc4.jar`) from [Microsft's website](http://www.microsoft.com/en-us/download/details.aspx?displaylang=en&id=11774) and add it to your classpath manually.

Additionally, add the `sqljdbc_auth.dll` file that comes with the JDBC driver package to the project root. 

## Executing the processes 

Just execute [LoadWorldCities.java] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java) or [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java) as a java program. The process will try to connect to your database and create the required tables if they are not present, and then ETL process will start.

If this is the first time you execute uniVocity, a pop-up will will be displayed asking if you agree with the uniVocity free license terms and conditions. Once you agree it will disappear and the process will start normally. Keep in mind that with the free license, batching is disabled.

## Explaining the configurations in [EtlProcess] (./src/main/java/com/univocity/articles/importcities/EtlProcess.java)

The [EtlProcess class] (./src/main/java/com/univocity/articles/importcities/EtlProcess.java) is an abstract class that initializes a [DataIntegrationEngine](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/engine/DataIntegrationEngine.java) object and delegates the definition of data mappings between the CSV files and the database tables to its subclasses.  

### A brief introduction

uniVocity works with abstractions called *data stores* and *data entities*. A *data entity* is an abstraction over anything that is able to store and/or retrieve data, and a *data store* is used to access and manage of data entities. This allows us to use virtually anything as sources and destinations of data. In this example we will use uniVociy to simply extract data from one or more source data entities (the world cities files) and map the information to destination data entities (a few database tables).

In general, the first thing you need to do to use uniVocity is to configure a few data stores.

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

In this snippet, `new CsvDataStoreConfiguration("csv")` creates a CSV data store with name "csv". `setLimitOfRowsLoadedInMemory(batchSize)` limits the number of rows loaded in memory by uniVocity at any given time. uniVocity will wait if the rows loaded during a data mapping cycle are taking too long to be consumed.

`csv.addEntities("files", "ISO-8859-1")` adds all files under the [src/main/resources/files](./src/main/resources/files) to this data store, and will read them with using the `ISO-8859-1` encoding.

As the [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) has a header row to identify each column, no configuration is required. uniVocity will detect the available fields automatically.

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

`retrieveGeneratedKeysUsingStatement(true)` configures how generated keys should be extracted upon insertion of new rows into any table of this database. In this case generated keys will be retrieved from the `java.sql.Statement` used to insert data. The `true` flag indicates that the insert operations can be batched and the JDBC driver supports returning all generated keys after a batch operation. Some JDBC drivers do not support this and in this case batch insertion must be disabled. To circumvent this limitation, we implemented some strategies to allow insertions in batch with retrieval of generated keys in the [JdbcEntityConfiguration](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/entity/jdbc/JdbcEntityConfiguration.java) class.

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

You are now ready to define/modify data mappings among entities of your data stores and execute data mapping cycles!


#### Configuring how metadata is stored (optional)

To perform operations such as data change autodetection (not demonstrated here) and reference mappings (used in [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java)), uniVocity generates metadata for each record persisted. You can define where this metadata should be stored or simply use uniVocity with its in-memory metadata database, which is created automatically. If you want to use your database to store metadata, ensure it is tuned to allow fast insert operations.

To configure the metadata storage, create a [MetadataSettings](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/config/MetadataSettings.java) object with the `javax.sql.DataSource` that provides connections to your database.

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
EntityMapping cityMapping = mapping.map("worldcitiespop", "worldcities");
cityMapping.identity().associate("country", "city", "region").to("country", "city_ascii", "region");
cityMapping.value().copy("accentCity").to("city");
cityMapping.autodetectMappings();
cityMapping.persistence().notUsingMetadata().deleteAll().insertNewRows();
```

Using the [DataIntegrationEngine](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/engine/DataIntegrationEngine.java) initialized by the parent class
[EtlProcess] (./src/main/java/com/univocity/articles/importcities/EtlProcess.java), we create mapping between the 2 datastores configured previously: `engine.map("csv", "database")` will create a  [DataStoreMapping](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/config/builders/DataStoreMapping.java), which will be used to define mappings between their data entities.

`map("worldcitiespop", "worldcities")` will create an [EntityMapping](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/config/builders/EntityMapping.java) object which will be used to associate fields of the [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) file with columns of the [WorldCities] (./src/main/resources/database/mysql/worldcities.tbl) database table.

All we have to do now is to associate fields in the source with the fields in the destination. uniVocity requires you to elect a combination of one or more fields as the identifiers of each record of both entities. This is done with the command `identity().associate("country", "city", "region").to("country", "city_ascii", "region")`. Unless other entity mappings have references to the values mapped here, the uniqueness of the identifier values is not essential. The list of fields inside the `associate` method selects the columns in the [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) file, while the list of fields inside the `to` method selects the fields of the [WorldCities] (./src/main/resources/database/mysql/worldcities.tbl) table. Altough unusal, the identity of a mapping does not need to involve the primary keys of a database table.

`value().copy("accentCity").to("city")` creates a value mapping, which will simply copy the values in the `accentCity` column of our CSV file to the column `city` of our database table.

As the fields `population`, `latitude` and `longitude` have the same names on both source and destination entities, we can simply call `autodetectMappings()`.

The last line in the snippet defines how the data should be persisted in the destination: `persistence().notUsingMetadata().deleteAll().insertNewRows()` configures the entity mapping to not generate any metadata (as we won't use it for anything useful in this example), to delete all rows in the [WorldCities] (./src/main/resources/database/mysql/worldcities.tbl) table and to insert the new rows mapped from the source entity.

Finally, we can execute a mapping cycle. This is done in the main method of the [LoadWorldCities] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java) class 

```java
LoadWorldCities loadCities = new LoadWorldCities();
try {
	loadCities.execute();
} finally {
	loadCities.shutdown();
}
```  

## Migrating data with [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java)

The [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java) is a bit more involved than the [LoadWorldCities] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java) class as we are mapping the information to a new schema, eliminating duplicate/invalid records and processing foreign keys. 

We start by adding a function to the engine to convert some String values to upppercase, as the character case of region and country codes is not consistent in our input files:

```java
engine.addFunction(EngineScope.STATELESS, "toUpperCase", new FunctionCall<String, String>() {
	@Override
	public String execute(String input) {
		return input == null ? null : input.toUpperCase();
	}
});
``` 
You can use functions to perform all sorts of powerful operations, refer to the [FunctionCall javadoc](http://docs.univocity.com/api/1.0.3/com/univocity/api/engine/FunctionCall.html) for more information.

Getting back to our mappings, we start by creating a mapping from the CSV entity [region_codes.csv](http://geolite.maxmind.com/download/geoip/misc/region_codes.csv) to the database table 
[region](./src/main/resources/database/mysql/region.tbl).

```java
EntityMapping regionMapping = mapping.map("region_codes", "region");
regionMapping.identity().associate("country", "region_code").toGeneratedId("id").readingWith("toUpperCase");
regionMapping.value().copy("country", "region_name").to("country", "name");
regionMapping.persistence().usingMetadata().deleteAll().insertNewRows();
```  
The identity mapping will associate the combination of values (converted to uppercase) from `country` and `region_code` in the source CSV with the generated primary key `id` of the 
[region](./src/main/resources/database/mysql/region.tbl) table.

With `value().copy("country", "region_name").to("country", "name")` we will copy the `country` and `region_name` from the CSV to the table columns `country` and `name` respectively. 

Finally, the `persistence().usingMetadata().deleteAll().insertNewRows();` instructs uniVocity to generate metadata for this entity mapping. This means that it will create some information for each mapped record that enables features such as identification of modified data in the source entity, and reference mappings. In this example we are interested in the reference mappings only, as can be seen in the next entity mapping:

```java
EntityMapping cityMapping = mapping.map("worldcitiespop", "city");
cityMapping.identity().associate("country", "city", "region").toGeneratedId("id");

cityMapping.reference().using("country", "region").referTo("region_codes", "region").on("region_id").readingWith("toUpperCase").onMismatch().discard();

cityMapping.value().copy("accentCity").to("name");
cityMapping.autodetectMappings();
cityMapping.persistence().notUsingMetadata().deleteAll().insertNewRows();
``` 

Here we map all cities in the [worldcitiespop](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) file into the database table [city](./src/main/resources/database/mysql/city.tbl).

The reference mapping `reference().using("country", "region").referTo("region_codes", "region").on("region_id").readingWith("toUpperCase")` states that: with the values from fields `country` and `region` in the worldcitiespop CSV (converted to uppercase), query the metadata of the entity mapping from `region_codes` to `region`, to find the identifiers generated for the combination of country and region codes, and copy that identifier to the `region_id` column of the database table [city](./src/main/resources/database/mysql/city.tbl). This mapping will generate correct foreign keys, and uniVocity ensures the data is consistent. 

Still on the reference mapping, `onMismatch().discard()` instructs uniVocity to discard any rows from the worldcitiespop CSV that do not have a correct association to a region (the file contains regions with null/inexistent region codes);

Again, we use `autodetectMappings()` to automatically generate mappings to copy values from fields whose names are the same in both entity and destination.

With `persistence().notUsingMetadata().deleteAll().insertNewRows()` we indicate that no metadata is to be generated for this mapping. We just want to delete all rows in the  [city](./src/main/resources/database/mysql/city.tbl) table and insert the data mapped from worldcitiespop.

The last step in this mapping definition is to attach a [RowReader](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/engine/RowReader.java) to the input of this mapping. It will collect the values from [worldcitiespop](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) fields `country`, `city` and `region`, and discard any input row with duplicate or missing data:

```java
...
@Override
public void initialize(RowMappingContext context) {
	//before processing the first row, we have a chance to get the indexes of each
	//input field we are interesed in
	COUNTRY = context.getInputIndex("country");
	CITY = context.getInputIndex("city");
	REGION = context.getInputIndex("region");
}

@Override
public void processRow(Object[] inputRow, Object[] outputRow, RowMappingContext context) {
	//let's create an entry with the input data
	String entry = createEntry(inputRow);

	//if the entry is not null, we check whether it has been processed already
	if (entry != null) {
		if (entries.contains(entry)) {
			//if it has, then we discard the row
			context.discardRow();
		} else {
			//if it hasn't then we store the entry so any other duplicate entry will be discarded
			entries.add(entry);
		}
	} else {
		//if the information in the row is incomplete, we discard the row.
		context.discardRow();
	}
}

private String createEntry(Object[] inputRow) {
	//Let's get the values of the elements we are interested in
	String country = (String) inputRow[COUNTRY];
	String city = (String) inputRow[CITY];
	String region = (String) inputRow[REGION];

	//If any of this information is null, then the row will be discarded
	if (country == null || city == null || region == null) {
		return null;
	}

	entryBuilder.setLength(0);
	entryBuilder.append(country).append('|');
	entryBuilder.append(city).append('|');
	entryBuilder.append(region);

	return entryBuilder.toString().toLowerCase();
}
...
```

Finally, we can execute a mapping cycle from the main method of [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java)
Remember to set the JVM args to execute with 3 gigabytes of memory (i.e. `-Xms3G -Xmx3G`), as the  [RowReader](http://github.com/uniVocity/univocity-api/blob/master/src/main/java/com/univocity/api/engine/RowReader.java) stores more than 3 million entries into a hashset to identify and eliminate duplicates.

```java 
MigrateWorldCities migrateCities = new MigrateWorldCities();
try {
	migrateCities.execute();
} finally {
	migrateCities.shutdown();
}
```

## And that's it

We hope you enjoyed this tutorial. It probably took more time to explain it than to actually write the code. We work very hard to make **[uniVocity](http://www.univocity.com/pages/about-univocity)** your framework of choice to implement all sorts of ETL tools, data mapping, data integration and data synchronizaion solutions. 

#### If you have any questions and suggestions don't hesitate to [e-mail us](mailto:dev@univocity.com) 