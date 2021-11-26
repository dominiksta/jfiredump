# jfiredump

![License](https://img.shields.io/github/license/dominiksta/jfiredump)
![jdk-version](https://img.shields.io/badge/jdk--version-1.8-green)

A (very) small utility to dump a Firebird database to a file of SQL INSERT
statements.

## Alternatives

### `fbexport`

[fbexport](http://www.firebirdfaq.org/fbexport.php) does not handle `BLOB
SUB_TYPE TEXT` as strings because it treats them as generic blobs, setting their
value to `NULL` in the export.

### `dbeaver`

[dbeaver](http://www.firebirdfaq.org/fbexport.php) provides a competent exporter
with *way* more options then this tool. It is however part of a rather large IDE
and not really *scriptable* (outside of GUI automation - have fun with that).

### Other Alternatives?

I would not be aware of any other tools that can dump a firebird table/database
to INSERT statements, but I will gladly add them here if they are brought to my
attention.

## Compatibility

Jfiredump was built for **firebird version 2.x**. Major version 3 and up is not
supported and neither is major version 1. It *could* work with these versions,
but you should probably expect some incompatibilities.

It should correctly handle all common types except blob types (although `BLOB
SUB_TYPE TEXT` will be treated as a normal string). Warnings will be displayed
when an unsupported type is encountered.

# Usage

You can run the java file with `java -jar jfiredump-VERSION.jar` (obviously
replace VERSION with whatever version you use). The available command line
arguments should be mostly self-explanatory and can be shown by running

```
$ java -jar jfiredump-VERSION.jar --help
usage: jfiredump [options] [table] [file]
Available options:
 -h,--host <arg>       specify database host (default: localhost)
    --help             print this message
 -o,--outfile <arg>    specify output file (default:
                       <datetime><table>.sql)
 -p,--password <arg>   specify database password (default: masterkey)
    --port <arg>       specify database port (default: 3050)
 -u,--user <arg>       specify database user (default: SYSDBA)
 -v,--verbose          verbose logging output for debugging
 -vv,--very-verbose    very verbose logging output for debugging
```

# Development

## Building

`mvn clean compile assembly:single` will put a jar file into the `/target` directory.

## Developing in eclipse

Maven apparently comes with an `eclipse` plugin. All you need to do is run `mvn
eclipse:eclipse` and then import the folder as an existing maven project.
