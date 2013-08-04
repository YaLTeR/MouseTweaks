package MouseTweaks;

import java.io.File;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Minecraft;
import net.minecraft.src.Slot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Main extends DeobfuscationLayer {
    
    private static GuiContainer guiContainer;
    private static Container container;
    private static Slot currentSlot = null;
    private static boolean mouseOverSomeSlot = false;
    private static boolean firstSlotClicked = true;
    private static Slot firstSlot = null;
    private static ItemStack oldStackOnMouse = null;
    
    private static boolean readConfig = false;
    private static boolean initialised = false;
    private static boolean disabled = false;
    
    public static boolean liteLoader = false;
    public static boolean minecraftForge = false;
    public static boolean optifine = false;
    public static boolean modLoader = false;
    public static boolean useModLoader = false;
    
    public static boolean DisableRMBTweak = false;
    
    public static int RMBTweak = 0;
    public static int LMBTweakWithItem = 0;
    public static int LMBTweakWithoutItem = 0;
    public static int WheelTweak = 0;
    public static int ForceModLoader = 0;
    
    public static Config mainConfig;
    
    public static boolean initialise() {
        if (disabled)
            return false;
        
        if (initialised)
            return true;
        
        initialised = true;
        
        mc = Minecraft.getMinecraft();
        
        mainConfig = new Config(mc.mcDataDir + File.separator + "config"
                + File.separator + "MouseTweaks.cfg");
        readConfigFile();
        
        modLoader = Reflection.doesClassExist("net.minecraft.src.ModLoader");
        minecraftForge = Reflection
                .doesClassExist("net.minecraftforge.client.MinecraftForgeClient");
        
        if (ForceModLoader == 1) {
            if (modLoader) {
                useModLoader = true;
                Logger.Log("ForceModLoader is set to 1, using ModLoader's onTick for mod operation...");
            } else {
                Logger.Log("ForceModLoader is set to 1, but ModLoader's main class is not present, quitting...");
                disabled = true;
                return false;
            }
        } else {
            liteLoader = Reflection.reflectLiteLoader();
            optifine = Reflection.reflectOptifine();
        }
        
        if (minecraftForge) {
            Logger.Log("Minecraft Forge is installed");
        } else {
            Logger.Log("Minecraft Forge is not installed");
        }
        
        if (!liteLoader && (ForceModLoader == 0)) {
            useModLoader = !Reflection.reflectMinecraft();
            if (!useModLoader) {
                if (!Reflection.replaceProfiler()) {
                    if (modLoader) {
                        useModLoader = true;
                        Logger.Log("Using ModLoader for mod operation");
                    } else {
                        Logger.Log("Failed to replace Minecraft profiler, quitting");
                        
                        disabled = true;
                        return false;
                    }
                }
            } else {
                Logger.Log("Using ModLoader for mod operation");
            }
        }
        
        boolean returnValue = Reflection.reflectGuiContainer();
        
        if (returnValue) {
            Logger.Log(Constants.NAME + " version " + Constants.VERSION
                    + " has been initialised");
        } else {
            disabled = true;
        }
        
        return returnValue;
    }
    
    public static void readConfigFile() {
        if (!mainConfig.readConfig()) {
            mainConfig.setPropertyValue("RMBTweak", 1);
            mainConfig.setPropertyValue("LMBTweakWithItem", 1);
            mainConfig.setPropertyValue("LMBTweakWithoutItem", 1);
            mainConfig.setPropertyValue("WheelTweak", 1);
            mainConfig.setPropertyValue("ForceModLoader", 0);
            
            mainConfig.saveConfig();
            
            Logger.Log("Mouse Tweaks config file was created.");
        }
        
        RMBTweak = mainConfig.getOrCreatePropertyValue("RMBTweak", 1);
        LMBTweakWithItem = mainConfig.getOrCreatePropertyValue("LMBTweakWithItem", 1);
        LMBTweakWithoutItem = mainConfig.getOrCreatePropertyValue("LMBTweakWithoutItem", 1);
        WheelTweak = mainConfig.getOrCreatePropertyValue("WheelTweak", 1);
        ForceModLoader = mainConfig.getOrCreatePropertyValue("ForceModLoader", 0);
        
        mainConfig.saveConfig();
    }
    
    public static void onUpdateInGame() {
        GuiScreen currentScreen = getCurrentScreen();
        if (currentScreen == null) {
            // Reset stuff
            firstSlot = null;
            currentSlot = null;
            firstSlotClicked = true;
            oldStackOnMouse = null;
            container = null;
            guiContainer = null;
            mouseOverSomeSlot = false;
            readConfig = true;
        } else {
            onUpdateInGUI(currentScreen);
        }
    }
    
    public static void onUpdateInGUI(GuiScreen currentScreen) {
        if (readConfig) {
            readConfig = false;
            readConfigFile();
        }
        
        if ((DisableRMBTweak || (RMBTweak == 0)) && (LMBTweakWithoutItem == 0)
                && (LMBTweakWithItem == 0))
            return;
        
        if (isGuiContainer(currentScreen) && isValidGuiContainer(currentScreen)) {
            guiContainer = asGuiContainer(currentScreen);
        }
        
        if (guiContainer != null) {
            /*
             * if (guiContainer != oldGuiContainer) { oldGuiContainer =
             * guiContainer; getThePlayer().sendChatToPlayer(new
             * StringBuilder().
             * append("Slots: ").append(guiContainer.inventorySlots
             * .inventorySlots.size()).toString()); }
             */
            
            int wheel = (WheelTweak == 1) ? Mouse.getDWheel() / 120 : 0;
            
            container = getContainer(guiContainer);
            int containerSize = getSlots(container).size();
            
            if (!mouseOverSomeSlot) {
                currentSlot = null;
            }
            
            mouseOverSomeSlot = false;
            
            ItemStack stackOnMouse = getStackOnMouse();
            
            if (!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)) {
                firstSlot = null;
                firstSlotClicked = false;
            }
            
            if ((oldStackOnMouse == null) && (stackOnMouse != null)
                    && Mouse.isButtonDown(1)) {
                firstSlotClicked = true;
            }
            
            oldStackOnMouse = stackOnMouse;
            
            for (int counter = 0; counter < containerSize; counter++) {
                Slot slot = getSlot(container, counter);
                if (isMouseOverSlot(guiContainer, slot)) {
                    mouseOverSomeSlot = true;
                    
                    if (currentSlot != slot) {
                        if (firstSlot == null) {
                            firstSlot = currentSlot;
                        }
                        
                        // Logger.Log(new
                        // StringBuilder().append("Current slot number: ").append(slot.slotNumber).toString());
                        
                        currentSlot = slot;
                        ItemStack originalStack = getSlotStack(slot);
                        
                        boolean shiftIsDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                                || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                        
                        if (Mouse.isButtonDown(1) && !DisableRMBTweak
                                && (RMBTweak == 1)) {
                            disableDefaultRMBDrag(guiContainer);
                            if ((firstSlot != null)
                                    && !firstSlotClicked
                                    && (getSlotNumber(firstSlot) <= getSlots(
                                            container).size())) {
                                windowClick(getWindowId(container),
                                        getSlotNumber(firstSlot), 1, 0);
                                firstSlotClicked = true;
                            }
                            if (stackOnMouse != null) {
                                if ((originalStack == null)
                                        || areStacksCompatible(originalStack, stackOnMouse)) {
                                    windowClick(getWindowId(container),
                                            getSlotNumber(slot), 1, 0);
                                }
                            }
                        } else if (Mouse.isButtonDown(0)) {
                            if (LMBTweakWithItem == 1) {
                                if (stackOnMouse != null) {
                                    if ((originalStack != null)
                                            && areStacksCompatible(originalStack, stackOnMouse)) {
                                        if (shiftIsDown) {
                                            windowClick(getWindowId(container),
                                                    getSlotNumber(slot), 0, 1);
                                        } else {
                                            if ((getItemStackSize(stackOnMouse) + getItemStackSize(originalStack)) <= getMaxItemStackSize(stackOnMouse)) {
                                                windowClick(
                                                        getWindowId(container),
                                                        getSlotNumber(slot), 0, 0);
                                                windowClick(
                                                        getWindowId(container),
                                                        getSlotNumber(slot), 0, 0);
                                            }
                                        }
                                    }
                                } else if (LMBTweakWithoutItem == 1) {
                                    if (originalStack != null) {
                                        if (shiftIsDown) {
                                            windowClick(getWindowId(container),
                                                    getSlotNumber(slot), 0, 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (wheel != 0)
                    {
                        int numItemsToMove = Math.abs(wheel);
                        
                        if (containerSize > Constants.INVENTORY_SIZE)
                        {
                            ItemStack originalStack = getSlotStack(slot);
                            if ((originalStack != null)
                                    && ((stackOnMouse == null) || ((isCraftingOutputSlot(container, slot)) ? areStacksCompatible(originalStack, stackOnMouse) : !areStacksCompatible(originalStack, stackOnMouse)))) {
                                do {
                                    Slot applicableSlot = null;
                                    
                                    int slotCounter = 0;
                                    int countUntil = containerSize
                                            - Constants.INVENTORY_SIZE;
                                    if (counter < countUntil) {
                                        slotCounter = countUntil;
                                        countUntil = containerSize;
                                    }
                                    for (int i = slotCounter; i < countUntil; i++)
                                    {
                                        Slot sl = getSlot(container, i);
                                        ItemStack stackSl = getSlotStack(sl);
                                        
                                        if (stackSl == null) {
                                            if ((applicableSlot == null)
                                                    && (wheel < 0)
                                                    && sl.isItemValid(originalStack))
                                            {
                                                applicableSlot = sl;
                                            }
                                        } else if ((getItemStackID(originalStack) == getItemStackID(stackSl))
                                                && (!hasSubtypesItemStack(originalStack) || (getItemStackItemDamage(originalStack) == getItemStackItemDamage(stackSl)))) {
                                            if ((wheel < 0)
                                                    && (stackSl.stackSize < stackSl.getMaxStackSize()))
                                            {
                                                applicableSlot = sl;
                                                break;
                                            } else if (wheel > 0)
                                            {
                                                applicableSlot = sl;
                                                break;
                                            }
                                        }
                                    }
                                    
                                    if (isCraftingOutputSlot(container, slot)) {
                                        if (wheel < 0) {
                                            boolean mouseWasEmpty = stackOnMouse == null;
                                            
                                            for (int i = 0; i < numItemsToMove; i++) {
                                                windowClick(getWindowId(container), getSlotNumber(slot), 0, 0);
                                            }
                                            
                                            if ((applicableSlot != null)
                                                    && mouseWasEmpty) {
                                                windowClick(getWindowId(container), getSlotNumber(applicableSlot), 0, 0);
                                            }
                                        }
                                        
                                        break;
                                    }
                                    
                                    if (applicableSlot != null)
                                    {
                                        Slot slotTo = (wheel < 0) ? applicableSlot : slot;
                                        Slot slotFrom = (wheel < 0) ? slot : applicableSlot;
                                        ItemStack stackTo = (getSlotStack(slotTo) != null) ? copyItemStack(getSlotStack(slotTo)) : null;
                                        ItemStack stackFrom = copyItemStack(getSlotStack(slotFrom));
                                        
                                        if (wheel < 0) {
                                            if ((stackTo != null)
                                                    && ((getMaxItemStackSize(stackTo) - getItemStackSize(stackTo)) <= numItemsToMove)) {
                                                windowClick(getWindowId(container), getSlotNumber(slotFrom), 0, 0);
                                                windowClick(getWindowId(container), getSlotNumber(slotTo), 0, 0);
                                                windowClick(getWindowId(container), getSlotNumber(slotFrom), 0, 0);
                                                
                                                numItemsToMove -= getMaxItemStackSize(stackTo)
                                                        - getItemStackSize(stackTo);
                                            } else {
                                                windowClick(getWindowId(container), getSlotNumber(slotFrom), 0, 0);
                                                
                                                if (getItemStackSize(stackFrom) <= numItemsToMove) {
                                                    windowClick(getWindowId(container), getSlotNumber(slotTo), 0, 0);
                                                } else {
                                                    for (int i = 0; i < numItemsToMove; i++) {
                                                        windowClick(getWindowId(container), getSlotNumber(slotTo), 1, 0);
                                                    }
                                                }
                                                
                                                windowClick(getWindowId(container), getSlotNumber(slotFrom), 0, 0);
                                                
                                                numItemsToMove = 0;
                                            }
                                        } else {
                                            if ((getMaxItemStackSize(stackTo) - getItemStackSize(stackTo)) <= numItemsToMove) {
                                                windowClick(getWindowId(container), getSlotNumber(slotFrom), 0, 0);
                                                windowClick(getWindowId(container), getSlotNumber(slotTo), 0, 0);
                                                windowClick(getWindowId(container), getSlotNumber(slotFrom), 0, 0);
                                            } else {
                                                windowClick(getWindowId(container), getSlotNumber(slotFrom), 0, 0);
                                                
                                                if (stackFrom.stackSize <= numItemsToMove) {
                                                    windowClick(getWindowId(container), getSlotNumber(slotTo), 0, 0);
                                                    numItemsToMove -= getMaxItemStackSize(stackFrom);
                                                } else {
                                                    for (int i = 0; i < numItemsToMove; i++) {
                                                        windowClick(getWindowId(container), getSlotNumber(slotTo), 1, 0);
                                                    }
                                                    
                                                    numItemsToMove = 0;
                                                }
                                                
                                                windowClick(getWindowId(container), getSlotNumber(slotFrom), 0, 0);
                                            }
                                            
                                            if (getMaxItemStackSize(stackTo) == getMaxItemStackSize(stackTo)) {
                                                numItemsToMove = 0;
                                            }
                                        }
                                    } else {
                                        break;
                                    }
                                } while (numItemsToMove != 0);
                            }
                        }
                    }
                    
                    break;
                }
            }
        }
        
        container = null;
        guiContainer = null;
        firstSlot = null;
        
        /*
         * if (MTMC.isValidModInventory(currentScreen)) {
         * MTMC.handleOnTickInModInventory(currentScreen); }
         */
    }
    
}
