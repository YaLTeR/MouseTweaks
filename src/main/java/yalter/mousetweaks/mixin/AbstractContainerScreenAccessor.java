package yalter.mousetweaks.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Invoker
    Slot invokeFindSlot(double x, double y);

    @Invoker
    void invokeSlotClicked(Slot slot, int index, int button, ClickType clickType);

    @Accessor
    boolean getIsQuickCrafting();
    @Accessor
    void setIsQuickCrafting(boolean value);

    @Accessor
    int getQuickCraftingButton();

    @Accessor
    void setSkipNextRelease(boolean value);
}
