<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Image Preview Changelog

## [0.4.2] - 2026-06-16

### Added
- Recognize image references in JavaScript and TypeScript arrays.
- Recognize image references in JavaScript, TypeScript, and JSON object field values.
- Recognize image references in JSON arrays.
- Add detailed JS, TS, JSON, CSS, remote URL, and local path examples.

### Fixed
- Ignore JSON property names and interpolated JavaScript/TypeScript template strings to avoid false image previews.

## [0.4.1] - 2026-05-19

### Changed
- Rename the Marketplace display name to Image Preview.

### Fixed
- Fix SVG decoding on newer JetBrains runtimes when the default JAXP document builder provider is not accessible.
- Fix stale inline image size annotations appearing on the wrong lines after inserting content before an image URL.

## [0.4.0] - 2026-05-15

### Added
- Restore and expand the Image Preview settings page under Tools.
- Add preview controls for gutter icons, actual thumbnails, and inline image size text.
- Add loading controls for remote images, local images, and click-only preview loading.
- Add image size limits, popup fit-to-screen scaling, preview cache clearing, and remote allowed-domain filtering.
- Add tests for settings persistence, domain filtering, loading policy, plugin registration, thumbnail icon behavior, popup sizing, and local SVG loading.

## [0.2.1] - 2024-08-21

### Fixed
- Temporarily removed svg support

## [0.2.0] - 2024-08-17

### Added
- Added a settings panel with a thumbnail display of the actual icons
- LineMaker Support scss files
- Support 2024.2 and later

### Chore
-  Support 2024.1

## [0.1.7] - 2024-03-02

### Chore
-  Support 2024.1

## [0.1.6] - 2023-07-11

### Chore
-  Remove plugin version limit

## [0.1.5] - 2023-05-21

### Added
-  Added image size info

## [0.1.4] - 2023-04-20

### Fixed
-  Fixed LinePainter wrong display

## [0.1.3] - 2023-04-19

### Added
-  Added Line Painter ( show image size )
-  Support 2023.1

### Fixed
- Fix the error caused by wrong URL

## [0.1.2] - 2023-03-23

### Added
-  Added ImagePool to caching images

## [0.1.1] - 2023-03-20

### Fixed
- fix popup position

## [0.1.0] - 2023-03-16

### Added
- LineMaker Support CSS Files

## [0.0.2] - 2023-03-16

### Added
- LineMaker Support JS/TS Files
