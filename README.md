# Mouse Tweaks

A mod that enhances inventory management by adding various functions to the mouse buttons.

## How to build

- `./gradlew -p fabric build`
- `./gradlew -p neoforge build`
- `./gradlew -p forge build`

## Running client gametests

These tests start Minecraft, then fully autonomously emulate input to test Mouse Tweaks functions.

```
./gradlew -p fabric runClientGametest
```

You can run them in a headless mode (no window) with `xvfb-run`:

```
xvfb-run ./gradlew -p fabric runClientGametest
```

## Compatibility

Mouse Tweaks should work with everything based on `AbstractContainerScreen`. If your GUI isn't based
on `AbstractContainerScreen`, or if you want to provide additional compatibility, take a look
at `src/main/java/yalter/mousetweaks/api/`.
