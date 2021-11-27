Changelog
======================================================================

`0.0.2` - _unreleased_
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