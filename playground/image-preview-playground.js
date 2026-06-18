// Image Preview Playground: JavaScript

const jsVariable = "./assets/logo.svg";

const jsArray = [
  "./assets/hero.svg",
  "./assets/avatar.svg",
  "https://resources.jetbrains.com/storage/products/company/brand/logos/WebStorm_icon.png",
];

const jsObject = {
  image: "./assets/card.svg",
  nested: {
    empty: "./assets/nested/empty-state.svg",
  },
  remote: "https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA_icon.svg",
};

const jsArrayOfObjects = [
  { image: "./assets/logo.svg" },
  { image: "https://resources.jetbrains.com/storage/products/company/brand/logos/PyCharm_icon.svg" },
];

const jsNegativeCases = {
  textFile: "./assets/not-image.txt",
  dynamicTemplate: `./assets/${"logo"}.svg`,
  protocolRelative: "//cdn.example.com/logo.png",
};

export { jsVariable, jsArray, jsObject, jsArrayOfObjects, jsNegativeCases };

