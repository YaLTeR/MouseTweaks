package yalter.mousetweaks.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import yalter.mousetweaks.Main;
import yalter.mousetweaks.MouseButton;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {
    @WrapOperation(method = "handleAccumulatedMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/Screen;mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z"))
    private boolean onMouseDragged(Screen screen, MouseButtonEvent mouseButtonEvent, double dx, double dy, Operation<Boolean> operation) {
        MouseButton button = MouseButton.fromEventButton(mouseButtonEvent.button());
        if (button != null) {
            if (Main.onMouseDrag(screen, mouseButtonEvent.x(), mouseButtonEvent.y(), button)) {
                return true;
            }
        }

        return operation.call(screen, mouseButtonEvent, dx, dy);
    }
}
