Changelog
======================================================================

`0.0.4` - _unreleased_
----------------------------------------------------------------------

### Added

- New `--run-file` command line argument to execute an exsiting sql file. Only
  INSERT statements are allowed since this is intended to be used to restore
  dumps.
- Source database (host, port, path) added to the header information of exported
  files.

### Changed

- The **order of the positional command line arguments** has changed! The "file"
  argument now comes before the "table" argument. This is done because the new
  option `--run-file` (used to restore from a dump) will simply use the existing
  "file" argument and ignore the "table" argument.
- The output folder when exporting all tables was changed to include to the
  current date and time. This way, when exporting all tables multimple times,
  the individual files end up in different folders. This made
  development/testing a little bit easier for me.
- Binary data set to `NULL` is now exported as `NULL`. Binary data will only be
  set to `'[BINARY_DATA_LOST_IN_EXPORT]` when it is not `NULL`.

### Fixed

- Milliseconds in time types were incorrectly zero-padded, resulting in a slight
  decrease in the value
- Typo 'jfirebird' -> 'jfiredump' in file headers
- Typo 'SQL_DIALECT' -> 'SQL DIALECT' in file headers

`0.0.3` - _2021-11-29_
----------------------------------------------------------------------

### Added

- Allow setting database encoding explicitly with the `-e` or `--encoding`
  option.
- Allow setting export line endings with the `-l` or `--line-endings` option to
  LF or CRLF. When specifying an encoding with `--encoding` that starts with
  either "WIN" or "DOS", CRLF line endings are used by default. This way,
  newlines in exported strings should be handled properly.

### Changed

- The user will no longer be spammed with warnings for every single item of an
  unsupported type. Instead, warnings for an unsupported type will only be
  displayed once per table.
- The `SET SQL DIALECT 3;` statement at the top of every file is not commented
  out. This is because this specific command seems to only work in `isql` and
  not in any other tool I tried (Flamerobin and DBeaver). The disclaimer in the
  comment should be enough to alert users to the correct usage imo.

### Fixed

- Escape single quotes in strings as double quotes (`'` -> `''`)
- Typo 'singletables' -> 'single tables' in CLI help

`0.0.2` - _2021-11-27_
----------------------------------------------------------------------

### Added

- More detailed meta information in exported file header
- All tables of the database can now be exported with one command by setting the
  second positional argument to `!!all!!` instead of the table name

### Changed

- `--outfile` was renamed to `--out-location` and covers both the output
  directory when exporting an entire database as well as the file name when
  exporting a single table
- Unsupported binary types will now be exported to the string
  `'[BINARY_DATA_LOST_IN_EXPORT]'`, mimicking the behaviour of Flamerobin (but
  with clearer wording imo).

`0.0.1` - _2021-11-26_
----------------------------------------------------------------------

- Initial Release