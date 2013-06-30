package MouseTweaks;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Main extends DeobfuscationLayer {

    private static GuiContainer guiContainer;
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

    public static Config mainConfig;

    public static boolean initialise() {
        if (disabled)
            return false;

        if (initialised)
            return true;

        initialised = true;

        mc = Minecraft.getMinecraft();

        mainConfig = new Config(Minecraft.getMinecraftDir() + File.separator + "config"
                + File.separator + "MouseTweaks.cfg");
        readConfigFile();

        liteLoader = Reflection.reflectLiteLoader();
        minecraftForge = Reflection
                .doesClassExist("net.minecraftforge.client.MinecraftForgeClient");
        optifine = Reflection.reflectOptifine();
        modLoader = Reflection.doesClassExist("net.minecraft.src.ModLoader");

        if (minecraftForge) {
            Logger.Log("Minecraft Forge is installed");
        } else {
            Logger.Log("Minecraft Forge is not installed");
        }
        
        if (!liteLoader) {
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
            Logger.Log(Constants.NAME + " version " + Constants.VERSION + " has been initialised");
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

            mainConfig.saveConfig();

            Logger.Log("Mouse Tweaks config file was created.");
        }

        RMBTweak = mainConfig.getPropertyValue("RMBTweak");
        LMBTweakWithItem = mainConfig.getPropertyValue("LMBTweakWithItem");
        LMBTweakWithoutItem = mainConfig.getPropertyValue("LMBTweakWithoutItem");
    }

    public static void onUpdateInGame() {
        GuiScreen currentScreen = getCurrentScreen();
        if (currentScreen == null) {
            // Reset stuff
            firstSlot = null;
            currentSlot = null;
            firstSlotClicked = true;
            oldStackOnMouse = null;
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

            if (!mouseOverSomeSlot) {
                currentSlot = null;
            }

            mouseOverSomeSlot = false;

            ItemStack stackOnMouse = getStackOnMouse();

            if (!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)) {
                firstSlot = null;
                firstSlotClicked = false;
            }

            if ((oldStackOnMouse == null) && (stackOnMouse != null) && Mouse.isButtonDown(1)) {
                firstSlotClicked = true;
            }

            oldStackOnMouse = stackOnMouse;

            for (int counter = 0; counter < getSlots(getContainer(guiContainer)).size(); counter++) {
                Slot slot = getSlot(getContainer(guiContainer), counter);
                if (isMouseOverSlot(guiContainer, slot)) {
                    mouseOverSomeSlot = true;

                    if (currentSlot != slot) {
                        if (firstSlot == null) {
                            firstSlot = currentSlot;
                        }

                        currentSlot = slot;
                        ItemStack originalStack = getSlotStack(slot);

                        boolean shiftIsDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                                || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

                        if (Mouse.isButtonDown(1) && !DisableRMBTweak && (RMBTweak == 1)) {
                            disableDefaultRMBDrag(guiContainer);
                            if ((firstSlot != null)
                                    && !firstSlotClicked
                                    && (getSlotNumber(firstSlot) <= getSlots(
                                            getContainer(guiContainer)).size())) {
                                windowClick(getWindowId(getContainer(guiContainer)),
                                        getSlotNumber(firstSlot), 1, 0);
                                firstSlotClicked = true;
                            }
                            if (stackOnMouse != null) {
                                if ((originalStack == null)
                                        || ((getItemStackID(originalStack) == getItemStackID(stackOnMouse)) && (!hasSubtypesItemStack(originalStack) || (getItemStackItemDamage(originalStack) == getItemStackItemDamage(stackOnMouse))))) {
                                    windowClick(getWindowId(getContainer(guiContainer)),
                                            getSlotNumber(slot), 1, 0);
                                }
                            }
                        } else if (Mouse.isButtonDown(0)) {
                            if (LMBTweakWithItem == 1) {
                                if (stackOnMouse != null) {
                                    if ((originalStack != null)
                                            && ((getItemStackID(originalStack) == getItemStackID(stackOnMouse)) && (!hasSubtypesItemStack(originalStack) || (getItemStackItemDamage(originalStack) == getItemStackItemDamage(stackOnMouse))))) {
                                        if (shiftIsDown) {
                                            windowClick(getWindowId(getContainer(guiContainer)),
                                                    getSlotNumber(slot), 0, 1);
                                        } else {
                                            if ((stackOnMouse.stackSize + originalStack.stackSize) <= stackOnMouse
                                                    .getMaxStackSize()) {
                                                windowClick(
                                                        getWindowId(getContainer(guiContainer)),
                                                        getSlotNumber(slot), 0, 0);
                                                windowClick(
                                                        getWindowId(getContainer(guiContainer)),
                                                        getSlotNumber(slot), 0, 0);
                                            }
                                        }
                                    }
                                } else if (LMBTweakWithoutItem == 1) {
                                    if (originalStack != null) {
                                        if (shiftIsDown) {
                                            windowClick(getWindowId(getContainer(guiContainer)),
                                                    getSlotNumber(slot), 0, 1);
                                        }
                                    }
                                }
                            }
                        } 
                    }

                    break;
                }
            }
        }

        guiContainer = null;
        firstSlot = null;

        /*
         * if (MTMC.isValidModInventory(currentScreen)) {
         * MTMC.handleOnTickInModInventory(currentScreen); }
         */
    }

}
