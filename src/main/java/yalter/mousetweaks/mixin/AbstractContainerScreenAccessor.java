package yalter.mousetweaks.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Invoker("findSlot")
    Slot mousetweaks$invokeFindSlot(double x, double y);

    @Invoker("slotClicked")
    void mousetweaks$invokeSlotClicked(Slot slot, int index, int button, ClickType clickType);

    @Accessor("isQuickCrafting")
    boolean mousetweaks$getIsQuickCrafting();

    @Accessor("isQuickCrafting")
    void mousetweaks$setIsQuickCrafting(boolean value);

    @Accessor("quickCraftingButton")
    int mousetweaks$getQuickCraftingButton();

    @Accessor("skipNextRelease")
    void mousetweaks$setSkipNextRelease(boolean value);
}
