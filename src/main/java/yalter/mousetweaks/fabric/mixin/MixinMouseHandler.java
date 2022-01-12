package yalter.mousetweaks.fabric.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yalter.mousetweaks.Main;
import yalter.mousetweaks.MouseButton;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {
    @Shadow
    private int activeButton;

    @SuppressWarnings("target")
    @Dynamic("Lambda that calls screen.mouseDragged()")
    @Inject(method = "lambda$onMove$11(Lnet/minecraft/client/gui/components/events/GuiEventListener;DDDD)V", at = @At("HEAD"), cancellable = true)
    private void onMouseDragged(GuiEventListener screen, double x, double y, double dx, double dy, CallbackInfo ci) {
        MouseButton button = MouseButton.fromEventButton(this.activeButton);
        if (button != null && screen instanceof Screen) {
            if (Main.onMouseDrag((Screen) screen, x, y, button)) {
                ci.cancel();
            }
        }
    }
}
