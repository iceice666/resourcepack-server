
## v1.0.8 (2024/05/05)

### Changed
- rename subcommand `refreshSha1` to `reload`
- rename subcommand `set` to `reload`

### Fixed
- [#8](https://github.com/iceice666/resourcepack-server/issues/8)
Now server side will check the following rules when calculating the sha1 value of the resource pack:
    - The file is under the game directory
    - The file has `zip` extension (e.g. `file.zip`)
    - The file includes `pack.mcmeta`


