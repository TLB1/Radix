![radix](https://github.com/user-attachments/assets/04d962b2-ce2f-4141-8496-ddd6cc4c180e)
## A Lightweight JDBC Utility Library
Radix aims to improve workflows with database connections and data conversions.
The library is simple, lightweight and is made to reduce the amount of sql queries in your projects.

Currently compatible with SQLite (using `org.xerial:sqlite-jdbc:3.46.0.0`)
## Features
- Create local databases
- Generate tables based on classes
- Insert records or collections of records
- Read tables (threadable)
- Cache tables using ids

*Note: Due to the data structure of the library, all features could be made threadable*
## Usage
*Currently unavailable on mavenCentral*
### Local Build
```bash
git clone https://github.com/TLB1/Radix.git
```
Run `./gradlew jar` to build the library, or use `./gradlew build` to also generate the javadoc locally.

The jar(s) will appear in `build\libs`
