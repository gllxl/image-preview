# Image Preview Playground

Open these files in WebStorm with the Image Preview plugin enabled:

- `ImagePreviewPlayground.ts` is the main playground for TypeScript variables, arrays, object fields, nested objects, and negative cases.
- `image-preview-playground.js` mirrors common JavaScript cases.
- `image-preview-playground.json` checks JSON arrays and object field values.
- `image-preview-playground.css` checks CSS `url(...)` references.

Expected behavior:

- Image references with `jpg`, `jpeg`, `png`, `gif`, `bmp`, `webp`, or `svg` extensions should get a gutter preview marker.
- Local examples point at SVG files in `./assets`, so they work without network access.
- Remote examples depend on your network and plugin remote-image settings.
- The negative cases should not get preview markers.

