# jfiredump

![License](https://img.shields.io/github/license/dominiksta/jfiredump)
![jdk-version](https://img.shields.io/badge/jdk--version-1.8-green)

A small utility to dump a Firebird database to a file of SQL INSERT statements.

DISCLAIMER: /Please do not rely on this tool for backups!/ It is not well tested
enough to be used in that role. The reason this tool was written was because I
wanted to make the transition away from firebird easier (fb is a fine database,
but it lacks support from many modern programming languages that some of us have
to use). As such, dumps taken with this tool should be verified through some
other means when used in production.

## Alternatives

### FBExport

[fbexport](http://www.firebirdfaq.org/fbexport.php) does not handle `BLOB
SUB_TYPE TEXT` as strings because it treats them as generic blobs, setting their
value to `NULL` in the export.

### DBeaver

[DBeaver](https://dbeaver.io/) provides a competent exporter
with *way* more options then this tool. It is however part of a rather large IDE
and not really *scriptable* (outside of GUI automation - have fun with that).

### Flamerobin

[Flamerobin](http://www.flamerobin.org/) also allows exporting a database to
INSERT statements (or other formats like csv) by running a `SELECT` query and
then first selecting "Fetch all records" in the right-click menu, then selecting
all records with Control-A and finally selecting "Copy as INSERT
statements". This has the same problem as the exporter from DBeaver: it is not
scriptable and therefore requires quite a bit of manual labor to use.

### Other Alternatives?

I would not be aware of any other tools that can dump a firebird table/database
to INSERT statements, but I will gladly add them here if they are brought to my
attention.

## Compatibility

Jfiredump was built for **firebird version 2.x**. Major version 3 and up is not
supported and neither is major version 1. It *could* work with these versions,
but you should probably expect some incompatibilities.

It should correctly handle all common types *except blob types* (although `BLOB
SUB_TYPE TEXT` will be treated as a normal string). Warnings will be displayed
when an unsupported type is encountered.

# Usage

You can run the java file with `java -jar jfiredump-VERSION.jar` (obviously
replace VERSION with whatever version you use). The available command line
arguments should be mostly self-explanatory and can be shown by running

```
$ java -jar jfiredump-VERSION.jar --help
usage: jfiredump [<OPTIONS>] <FILE> {<TABLE>|!!all!!}
Available options:
 -e,--encoding <arg>       specify database encoding (firebird encoding names, see
                           https://github.com/FirebirdSQL/jaybird/wiki/Character-encodings
                           )
 -h,--host <arg>           specify database host (default: localhost)
    --help                 print this message
 -l,--line-endings <arg>   either LF or CRLF
 -o,--out-location <arg>   specify output location (default for single tables:
                           '<datetime><table>.sql', default for all tables: '<datetime>
                           jfiredump')
 -p,--password <arg>       specify database password (default: masterkey)
    --port <arg>           specify database port (default: 3050)
 -r,--run-file <arg>       run an existing .sql-File (only allows INSERT statements). When
                           using this option, the positional <TABLE> argument is ignored.
                           Will only commit when all statements were processed without
                           errors.
 -u,--user <arg>           specify database user (default: SYSDBA)
 -v,--verbose              verbose logging output for debugging
 -vv,--very-verbose        very verbose logging output for debugging
 ```

## Examples

### Exporting all tables in a database

```
$ java -jar jfiredump-VERSION.jar --line-endings CRLF --encoding WIN1252 \
  -p myPassword --port 3055 MY_DB.GDB !!all!!
```

### Exporting one table in a database

```
$ java -jar jfiredump-VERSION.jar --out-location table.sql MY_DB.GDB MY_TABLE 
```

### Running an existing dump file

```
$ java -jar jfiredump-VERSION.jar --run-file table.sql MY_DB.GDB
```

# Development

## Building

`mvn clean compile assembly:single` will put a jar file into the `/target` directory.

## Developing in eclipse

Maven apparently comes with an `eclipse` plugin. All you need to do is run `mvn
eclipse:eclipse` and then import the folder as an existing maven project.
