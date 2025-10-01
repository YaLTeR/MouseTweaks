package yalter.mousetweaks.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yalter.mousetweaks.Main;
import yalter.mousetweaks.MouseButton;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {
    @WrapOperation(method = "handleAccumulatedMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(DDIDD)Z"))
    private boolean onMouseDragged(Screen screen, double x, double y, int activeButton, double dx, double dy, Operation<Boolean> operation) {
        MouseButton button = MouseButton.fromEventButton(activeButton);
        if (button != null) {
            if (Main.onMouseDrag(screen, x, y, button)) {
                return true;
            }
        }

        return operation.call(screen, x, y, activeButton, dx, dy);
    }
}
