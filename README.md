# Image Preview

![Build](https://github.com/gllxl/image-preview/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Get familiar with the [template documentation][template].
- [ ] Verify the [pluginGroup](./gradle.properties), [plugin ID](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the Plugin ID in the above README badges.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

<!-- Plugin description -->
A preview image url plugin, when you define an image url, you can preview it content and size in IDE,

- JS/TS/JSON/CSS files are supported;
- Remote image URLs and local image paths are supported;

<!-- Plugin description end -->

## Settings

Open <kbd>Settings/Preferences</kbd> > <kbd>Tools</kbd> > <kbd>Image Preview</kbd> to configure preview icons, actual thumbnails, inline image size text, automatic loading for remote/local images, click-only preview loading, maximum image size, popup scaling, preview cache clearing, and allowed remote domains.

## Supported references

Image Preview recognizes image references in JavaScript, TypeScript, JSON, and CSS files. Supported image extensions are `jpg`, `jpeg`, `png`, `gif`, `bmp`, `webp`, and `svg`.

Remote images:

```ts
const avatar = "https://cdn.example.com/users/avatar.png";
const hero = "https://cdn.example.com/assets/hero.webp?v=2#banner";
```

Local images:

```ts
const logo = "./assets/logo.svg";
const banner = "../shared/banner.jpeg";
const absoluteIcon = "/Users/me/project/public/icon.png";
const fileUri = "file:///Users/me/project/public/photo.jpg";
```

JavaScript and TypeScript arrays:

```ts
const gallery = [
  "https://cdn.example.com/gallery/cover.jpg",
  "./assets/detail.png",
  "../shared/empty-state.svg",
];

export const thumbnails: string[] = [
  "https://cdn.example.com/thumbs/one.webp",
  "./thumbs/two.png",
];
```

JavaScript and TypeScript object fields:

```ts
const card = {
  title: "Launch",
  image: "https://cdn.example.com/cards/launch.png",
  fallbackImage: "./assets/card-fallback.svg",
};

export const theme = {
  brand: {
    logo: "../brand/logo.svg",
    appIcon: "file:///Users/me/project/icons/app.png",
  },
};
```

JSON arrays and object fields:

```json
{
  "logo": "./assets/logo.svg",
  "hero": "https://cdn.example.com/site/hero.jpg",
  "gallery": [
    "https://cdn.example.com/gallery/one.png",
    "../images/two.webp"
  ],
  "nested": {
    "emptyState": "./states/empty.svg"
  }
}
```

CSS `url(...)` references:

```css
.hero {
  background-image: url("./assets/hero.png");
}

.brand {
  mask-image: url(https://cdn.example.com/brand/icon.svg);
}
```

Dynamic JavaScript/TypeScript template strings with interpolations are ignored because the final path cannot be resolved statically:

```ts
const dynamicAvatar = `./avatars/${userId}.png`;
```

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Image Preview"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/gllxl/image-preview/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
