package yalter.mousetweaks.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.*;
import yalter.mousetweaks.IGuiScreenHandler;
import yalter.mousetweaks.MouseButton;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;
import yalter.mousetweaks.api.MouseTweaksIgnore;
import yalter.mousetweaks.mixin.AbstractContainerScreenAccessor;

import java.util.List;

public class GuiContainerHandler implements IGuiScreenHandler {
    Minecraft mc;
    private final AbstractContainerScreen screen;
    private final AbstractContainerScreenAccessor screenAccessor;

    public GuiContainerHandler(AbstractContainerScreen screen) {
        this.mc = Minecraft.getInstance();
        this.screen = screen;
        this.screenAccessor = (AbstractContainerScreenAccessor) screen;
    }

    @Override
    public boolean isMouseTweaksDisabled() {
        return screen.getClass().isAnnotationPresent(MouseTweaksIgnore.class);
    }

    @Override
    public boolean isWheelTweakDisabled() {
        return screen.getClass().isAnnotationPresent(MouseTweaksDisableWheelTweak.class);
    }

    @Override
    public List<Slot> getSlots() {
        return screen.getMenu().slots;
    }

    @Override
    public Slot getSlotUnderMouse(double mouseX, double mouseY) {
        return screenAccessor.mousetweaks$invokeFindSlot(mouseX, mouseY);
    }

    @Override
    public boolean disableRMBDraggingFunctionality() {
        screenAccessor.mousetweaks$setSkipNextRelease(true);

        if (screenAccessor.mousetweaks$getIsQuickCrafting() && screenAccessor.mousetweaks$getQuickCraftingButton() == 1) {
            screenAccessor.mousetweaks$setIsQuickCrafting(false);
            return true;
        }

        return false;
    }

    @Override
    public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
        screenAccessor.mousetweaks$invokeSlotClicked(
                slot,
                slot.index,
                mouseButton.getValue(),
                shiftPressed ? ClickType.QUICK_MOVE : ClickType.PICKUP
        );
    }

    @Override
    public boolean isCraftingOutput(Slot slot) {
        return (slot instanceof ResultSlot
                || slot instanceof FurnaceResultSlot
                || slot instanceof MerchantResultSlot);
    }

    @Override
    public boolean isIgnored(Slot slot) {
        return false;
    }
}
