package yalter.mousetweaks.fabric.mixin;

import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yalter.mousetweaks.Main;
import yalter.mousetweaks.MouseButton;

@Mixin(Minecraft.class)
abstract class MixinMinecraft {
    @Inject(method = "setScreen", at = @At("RETURN"))
    private void onSetScreen(CallbackInfo ci) {
        // The screen passed to setScreen() as it might have been changed by a recursive invocation.
        // So instead of using that, just use the current screen.
        Screen screen = Minecraft.getInstance().screen;

        Main.onGuiOpen(screen);

        if (screen == null)
            return;

        ScreenMouseEvents.allowMouseClick(screen).register((_screen, x, y, eventButton) -> {
            MouseButton button = MouseButton.fromEventButton(eventButton);
            if (button != null)
                return !Main.onMouseClicked(x, y, button);
            return true;
        });

        ScreenMouseEvents.allowMouseRelease(screen).register((_screen, x, y, eventButton) -> {
            MouseButton button = MouseButton.fromEventButton(eventButton);
            if (button != null)
                return !Main.onMouseReleased(x, y, button);
            return true;
        });

        ScreenMouseEvents.allowMouseScroll(screen).register((_screen, x, y, horiz, vert) -> !Main.onMouseScrolled(x, y, vert));
    }
}
