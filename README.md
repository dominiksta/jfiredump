# jfiredump

A (very) small utility to dump a Firebird database to a file of SQL INSERT statements.

# Development

## Building

`mvn clean compile assembly:single` will put a jar file into the `/target` directory.

## Developing in eclipse

Maven apparently comes with an `eclipse` plugin. All you need to do is run `mvn
eclipse:eclipse` and then import the folder as an existing maven project.