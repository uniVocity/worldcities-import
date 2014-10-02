# World Cities Import

This simple project was created to demonstrate how **[uniVocity](http://www.univocity.com/pages/about-univocity)** can be used to load/transform massive amounts of data, and how it performs.

We will be using a couple of files made available for free by [Maxmind](http://www.maxmind.com). One is a 3 million plus rows CSV file, which has information about all the world's cities. 

The size of this file can present some problems, especially if you need to load the data into a different database schema. Additionally, there are inconsistencies, such as incomplete data and duplicate cities, which we will try to address.

We created two examples for you:

 * [LoadWorldCities.java] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java) - loads the [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) file into the database table [worldcities](./src/main/resources/database/mysql/worldcities.tbl). 
 
 * [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java) - migrates the [region_codes.csv](http://geolite.maxmind.com/download/geoip/misc/region_codes.csv) file to the database table [region](.src/main/resources/database/mysql/region.tbl). We will use uniVocity to manage the keys generated upon insertion of each record, and then migrate data from [worldcitiespop.txt](http://www.maxmind.com/download/worldcities/worldcitiespop.txt.gz) to the [city](.src/main/resources/database/mysql/city.tbl) table. The  [city](.src/main/resources/database/mysql/city.tbl) table has a foreign key referencing [region](.src/main/resources/database/mysql/region.tbl). In this process we will discard duplicate cities, and cities that are not associated to any region.

## What to expect

These ETL processes will perform differently depending on your setup and hardware. For reference, here's my (very) modest hardware, an ultrabook: 

 * CPU: Intel i5-3337U @ 1.8 GHz
 * RAM: 4 GB of 
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

We created scripts to generate the required tables for you in the database of your preference. This project includes scripts for (HSQLDB)[./src/main/resources/database/hsqldb], (MySQL)[./src/main/resources/database/mysql], (Oracle XE)[./src/main/resources/database/oraclexe] and (Postgres)[./src/main/resources/database/postgres]. You can use any other database if you want, just follow (these instructions)[./ADD_DB.md] (feel free to submit a pull request with additional scripts your database).

Simply edit the (connection.properties)[./src/main/resources/connection.properties] file with the connection details for your database of choice.

#### If you are using Oracle XE:

Please download the JDBC drivers (`odjbc6.jar`) manually from (Oracle's website)[http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html] and add it to your classpath manually.

To ensure almost everyone is able to execute this project, we made it compatible with the JDK 6. If you are still using the JDK 6, ensure you are downloading the compatible version of the JDBC driver.


## Executing the processes

Just execute [LoadWorldCities.java] (./src/main/java/com/univocity/articles/importcities/LoadWorldCities.java) or [MigrateWorldCities.java] (./src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java) as a java program. The process will try to connect to your database and create the required tables if they are not present, and then the process will start.

If this is the first time you execute uniVocity, a pop-up will will be displayed asking if you agree with the uniVocity free license terms and conditions. Once you agree it will disappear and the process will start normally. Keep in mind that with the free license, batching is disabled.

## Explaining the code

...coming soon.