package MouseTweaks;

import java.util.List;

import net.minecraft.src.Container;
import net.minecraft.src.ContainerPlayer;
import net.minecraft.src.ContainerWorkbench;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiContainerCreative;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Minecraft;
import net.minecraft.src.PlayerControllerMP;
import net.minecraft.src.ScaledResolution;
import net.minecraft.src.Slot;

import org.lwjgl.input.Mouse;

public class DeobfuscationLayer {
    
    protected static Minecraft mc;
    
    // protected MouseTweaksModCompatibility MTMC;
    
    protected static GuiScreen getCurrentScreen() {
        return mc.currentScreen;
    }
    
    protected static boolean isGuiContainer(GuiScreen guiScreen) {
        return (guiScreen != null) && (guiScreen instanceof GuiContainer);
    }
    
    protected static boolean isValidGuiContainer(GuiScreen guiScreen) {
        return (guiScreen != null)
                && !(guiScreen.getClass().getSimpleName().contains("CJB_GuiCrafting"))
                && !(guiScreen.getClass().equals(GuiContainerCreative.class));
    }
    
    protected static boolean isCraftingOutputSlot(Container container, Slot slot) {
        return ((container instanceof ContainerWorkbench) && (getSlotNumber(slot) == 0))
                || ((container instanceof ContainerPlayer) && (getSlotNumber(slot) == 0));
    }
    
    protected static GuiContainer asGuiContainer(GuiScreen guiScreen) {
        return (GuiContainer) guiScreen;
    }
    
    protected static Container getContainer(GuiContainer guiContainer) {
        return guiContainer.inventorySlots;
    }
    
    protected static List<?> getSlots(Container container) {
        return container.inventorySlots;
    }
    
    protected static Slot getSlot(Container container, int index) {
        return (Slot) (getSlots(container).get(index));
    }
    
    protected static ItemStack getSlotStack(Slot slot) {
        return slot.getStack();
    }
    
    protected static int getItemStackID(ItemStack itemStack) {
        return itemStack.itemID;
    }
    
    protected static boolean hasSubtypesItemStack(ItemStack itemStack) {
        return itemStack.getHasSubtypes();
    }
    
    protected static int getItemStackItemDamage(ItemStack itemStack) {
        return itemStack.getItemDamage();
    }
    
    protected static int getWindowId(Container container) {
        return container.windowId;
    }
    
    protected static void windowClick(int windowId, int slotNumber, int mouseButton,
            int shiftPressed) {
        // if (slotNumber != -1) {
        getPlayerController().windowClick(windowId, slotNumber, mouseButton, shiftPressed,
                getThePlayer());
        // }
    }
    
    protected static EntityClientPlayerMP getThePlayer() {
        return mc.thePlayer;
    }
    
    protected static InventoryPlayer getInventoryPlayer() {
        return getThePlayer().inventory;
    }
    
    protected static GameSettings getGameSettings() {
        return mc.gameSettings;
    }
    
    protected static int getDisplayWidth() {
        return mc.displayWidth;
    }
    
    protected static int getDisplayHeight() {
        return mc.displayHeight;
    }
    
    protected static ItemStack getStackOnMouse() {
        return getInventoryPlayer().getItemStack();
    }
    
    protected static PlayerControllerMP getPlayerController() {
        return mc.playerController;
    }
    
    protected static int getSlotNumber(Slot slot) {
        return slot.slotNumber;
    }
    
    protected static int getItemStackSize(ItemStack itemStack) {
        return itemStack.stackSize;
    }
    
    protected static int getMaxItemStackSize(ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }
    
    protected static ItemStack copyItemStack(ItemStack itemStack) {
        return itemStack.copy();
    }
    
    protected static boolean areStacksCompatible(ItemStack itemStack1, ItemStack itemStack2) {
        return (getItemStackID(itemStack1) == getItemStackID(itemStack2)) && (!hasSubtypesItemStack(itemStack1) || (getItemStackItemDamage(itemStack1) == getItemStackItemDamage(itemStack2)));
    }
    
    protected static boolean isMouseOverSlot(GuiContainer guiContainer, Slot slot) {
        boolean returnValue = false;
        returnValue = (Boolean) Reflection.guiContainerClass.invokeMethod(guiContainer,
                "isMouseOverSlot", slot, getRequiredMouseX(), getRequiredMouseY());
        
        return returnValue;
    }
    
    protected static void disableDefaultRMBDrag(GuiContainer guiContainer) {
        Reflection.guiContainerClass.setFieldValue(guiContainer, "field_94068_E", true);
        Reflection.guiContainerClass.setFieldValue(guiContainer, "field_94076_q", false);
    }
    
    protected static int getRequiredMouseX() {
        ScaledResolution var8 = new ScaledResolution(getGameSettings(), getDisplayWidth(),
                getDisplayHeight());
        int var9 = var8.getScaledWidth();
        int var11 = (Mouse.getX() * var9) / getDisplayWidth();
        
        return var11;
    }
    
    protected static int getRequiredMouseY() {
        ScaledResolution var8 = new ScaledResolution(getGameSettings(), getDisplayWidth(),
                getDisplayHeight());
        int var10 = var8.getScaledHeight();
        int var13 = var10 - ((Mouse.getY() * var10) / getDisplayHeight()) - 1;
        
        return var13;
    }
    
    protected static void sendClientMessage(String message) {
        getThePlayer().sendChatMessage(message);
    }
}
