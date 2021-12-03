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
    public Slot invokeFindSlot(double x, double y);

    @Invoker("slotClicked")
    public void invokeSlotClicked(Slot slot, int index, int button, ClickType clickType);

    @Accessor
    public boolean getIsQuickCrafting();
    @Accessor("isQuickCrafting")
    public void setIsQuickCrafting(boolean value);

    @Accessor
    public int getQuickCraftingButton();

    @Accessor("skipNextRelease")
    public void setSkipNextRelease(boolean value);
}
