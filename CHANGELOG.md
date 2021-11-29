Changelog
======================================================================

`0.0.3` - _unreleased_
----------------------------------------------------------------------

### Added

- Allow setting database encoding explicitly with the `-e` or `--encoding`
  option.

### Changed

- The user will no longer be spammed with warnings for every single item of an
  unsupported type. Instead, warnings for an unsupported type will only be
  displayed once per table.
- The `SET SQL DIALECT 3;` statement at the top of every file is not commented
  out. This is because this specific command seems to only work in `isql` and
  not in any other tool I tried (Flamerobin and DBeaver). The disclaimer in the
  comment should be enough to alert users to the correct usage imo.

### Fixed

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