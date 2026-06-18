// Image Preview Playground: TypeScript
// Open this file in WebStorm and look for gutter preview markers next to image references.

// 1. Variable initializers
export const remotePng = "https://resources.jetbrains.com/storage/products/company/brand/logos/WebStorm_icon.png";
export const remoteSvg = "https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA_icon.svg";
export const localLogo = "./assets/logo.svg";
export const parentRelativeHero = "./assets/hero.svg";

// 2. Arrays
export const gallery = [
  "./assets/logo.svg",
  "./assets/hero.svg",
  "https://resources.jetbrains.com/storage/products/company/brand/logos/PhpStorm_icon.png",
  "https://resources.jetbrains.com/storage/products/company/brand/logos/PyCharm_icon.svg",
];

export const typedGallery: string[] = [
  "./assets/avatar.svg",
  "./assets/card.svg",
  "https://resources.jetbrains.com/storage/products/company/brand/logos/Rider_icon.png",
];

export const readonlyGallery = [
  "./assets/nested/empty-state.svg",
  "https://resources.jetbrains.com/storage/products/company/brand/logos/CLion_icon.svg",
] as const;

// 3. Object field values
export const userCard = {
  name: "Ada",
  avatar: "./assets/avatar.svg",
  cover: "https://resources.jetbrains.com/storage/products/company/brand/logos/DataGrip_icon.png",
  badge: "./assets/card.svg",
};

export const nestedConfig = {
  brand: {
    logo: "./assets/logo.svg",
    hero: "./assets/hero.svg",
  },
  emptyState: {
    illustration: "./assets/nested/empty-state.svg",
  },
  remote: {
    productIcon: "https://resources.jetbrains.com/storage/products/company/brand/logos/GoLand_icon.svg",
  },
};

// 4. Objects that contain arrays
export const mixedContent = {
  thumbnails: [
    "./assets/avatar.svg",
    "https://resources.jetbrains.com/storage/products/company/brand/logos/RubyMine_icon.png",
  ],
  cards: [
    {
      image: "./assets/card.svg",
      icon: "https://resources.jetbrains.com/storage/products/company/brand/logos/Aqua_icon.svg",
    },
  ],
};

// 5. Static template string: supported when the path is fully static.
export const staticTemplateImage = `./assets/logo.svg`;

// 6. Negative cases: these should not show preview markers.
export const notAnImage = "https://example.com/assets/readme.txt";
export const protocolRelativeUrl = "//cdn.example.com/image.png";
export const dynamicTemplateImage = `./assets/${"logo"}.svg`;
export const dataUri = "data:image/svg+xml,<svg></svg>";

export const objectKeysAreNotImages = {
  "./assets/logo.svg": "this key should not be previewed",
  value: "plain text",
};

