package yalter.mousetweaks.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yalter.mousetweaks.Main;
import yalter.mousetweaks.MouseButton;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {
    @WrapOperation(method = "handleAccumulatedMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z"))
    private boolean onMouseDragged(Screen screen, MouseButtonEvent event, double dx, double dy, Operation<Boolean> operation) {
        MouseButton button = MouseButton.fromEventButton(event.button());
        if (button != null) {
            if (Main.onMouseDrag(screen, event.x(), event.y(), button)) {
                return true;
            }
        }

        return operation.call(screen, event, dx, dy);
    }
}
