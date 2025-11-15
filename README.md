# Keycloak User Federation From Sybase Database

A simple example of creating custom User Federation module for keycloak. Tested on KeyCloak 11.0.2 with Java 8.

## Features
- Can connect with existing database in any database (as long JDBC can connect to it)
- Using Email as Login-name
- Enter query to get password in existing database, based on email
- Enter query to get several user attribute in existing database, based on email
