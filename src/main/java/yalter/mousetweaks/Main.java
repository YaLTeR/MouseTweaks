package yalter.mousetweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Main extends DeobfuscationLayer {

	public static boolean liteLoader = false;
	public static boolean minecraftForge = false;

	public static boolean DisableRMBTweak = false;

	public static int RMBTweak = 0;
	public static int LMBTweakWithItem = 0;
	public static int LMBTweakWithoutItem = 0;
	public static int WheelTweak = 0;
	public static int WheelSearchOrder = 1;

	public enum OnTickMethod { FORGE, LITELOADER }
	public static List<OnTickMethod> onTickMethodOrder = new LinkedList<OnTickMethod>();
	public static OnTickMethod onTickMethod;

	public static int Debug = 0;

	public static Config mainConfig;
	private static GuiScreen oldGuiScreen = null;
	private static Object container = null;
	private static Slot oldSelectedSlot = null;
	private static Slot firstSlot = null;
	private static ItemStack oldStackOnMouse = null;
	private static boolean firstSlotClicked = false;
	private static boolean shouldClick = true;
	private static boolean disableForThisContainer = false;
	private static boolean disableWheelForThisContainer = false;

	private static GuiContainerID guiContainerID;

	private static boolean readConfig = false;
	private static boolean initialised = false;
	private static boolean disabled = false;

	public static boolean initialize() {
		return initialize(Constants.EntryPoint.UNDEFINED);
	}

	public static boolean initialize(Constants.EntryPoint entryPoint) {
		Logger.Log("A call to initialise, entry point: " + entryPoint.toString() + ".");

		if (disabled)
			return false;

		if (initialised)
			return true;

		initialised = true;

		if (!Reflection.reflectGuiContainer()) {
			disabled = true;
			return false;
		}

		mc = Minecraft.getMinecraft();

		mainConfig = new Config(mc.mcDataDir + File.separator + "config" + File.separator + "MouseTweaks.cfg");
		readConfigFile();

		minecraftForge = ((entryPoint == Constants.EntryPoint.FORGE
				|| Reflection.doesClassExist("net.minecraftforge.client.MinecraftForgeClient")));
		if (minecraftForge) {
			Logger.Log("Minecraft Forge is installed.");
		} else {
			Logger.Log("Minecraft Forge is not installed.");
		}

		liteLoader = ((entryPoint == Constants.EntryPoint.LITELOADER)
				|| Reflection.doesClassExist("com.mumfrey.liteloader.core.LiteLoader"));
		if (liteLoader) {
			Logger.Log("LiteLoader is installed.");
		} else {
			Logger.Log("LiteLoader is not installed.");
		}

		if (!findOnTickMethod(true)) {
			// No OnTick methods work.
			disabled = true;
			return false;
		}

		ModCompatibility.initialize();
		Logger.Log("Mouse Tweaks has been initialised.");

		return true;
	}

	public static void readConfigFile() {
		boolean loadedConfig = mainConfig.readConfig();

		RMBTweak = mainConfig.getOrCreateIntProperty("RMBTweak", 1);
		LMBTweakWithItem = mainConfig.getOrCreateIntProperty("LMBTweakWithItem", 1);
		LMBTweakWithoutItem = mainConfig.getOrCreateIntProperty("LMBTweakWithoutItem", 1);
		WheelTweak = mainConfig.getOrCreateIntProperty("WheelTweak", 1);
		WheelSearchOrder = mainConfig.getOrCreateIntProperty("WheelSearchOrder", 1);
		Debug = mainConfig.getOrCreateIntProperty("Debug", 0);

		String onTickMethodString = mainConfig.getOrCreateProperty("OnTickMethodOrder",
				Constants.ONTICKMETHOD_FORGE_NAME + ", "
				+ Constants.ONTICKMETHOD_LITELOADER_NAME);
		onTickMethodOrderFromString(onTickMethodString);

		boolean savedConfig = saveConfigFile();
		if (savedConfig && !loadedConfig)
			Logger.Log("Mouse Tweaks config file was created.");
	}

	public static boolean saveConfigFile() {
		Logger.DebugLog("saveConfigFile()");

		mainConfig.setIntProperty("RMBTweak", RMBTweak);
		mainConfig.setIntProperty("LMBTweakWithItem", LMBTweakWithItem);
		mainConfig.setIntProperty("LMBTweakWithoutItem", LMBTweakWithoutItem);
		mainConfig.setIntProperty("WheelTweak", WheelTweak);
		mainConfig.setIntProperty("WheelSearchOrder", WheelSearchOrder);
		mainConfig.setIntProperty("Debug", Debug);
		mainConfig.setProperty("OnTickMethodOrder", onTickMethodOrderToString());

		return mainConfig.saveConfig();
	}

	public static void onTickMethodOrderFromString(String string) {
		onTickMethodOrder.clear();

		string = string.trim();
		String onTickMethods[] = string.split("[\\s]*,[\\s]*");
		for (String method : onTickMethods) {
			if (Constants.ONTICKMETHOD_FORGE_NAME.equalsIgnoreCase(method) && !onTickMethodOrder.contains(OnTickMethod.FORGE))
				onTickMethodOrder.add(OnTickMethod.FORGE);
			else if (Constants.ONTICKMETHOD_LITELOADER_NAME.equalsIgnoreCase(method) && !onTickMethodOrder.contains(OnTickMethod.LITELOADER))
				onTickMethodOrder.add(OnTickMethod.LITELOADER);
		}

		// Make sure we have one of each.
		if (!onTickMethodOrder.contains(OnTickMethod.FORGE))
			onTickMethodOrder.add(OnTickMethod.FORGE);
		if (!onTickMethodOrder.contains(OnTickMethod.LITELOADER))
			onTickMethodOrder.add(OnTickMethod.LITELOADER);
	}

	public static String onTickMethodOrderToString() {
		String result = "";

		for (OnTickMethod method : onTickMethodOrder) {
			if (!result.isEmpty())
				result += ", ";

			switch (method) {
				case FORGE:
					result += Constants.ONTICKMETHOD_FORGE_NAME;
					break;

				case LITELOADER:
					result += Constants.ONTICKMETHOD_LITELOADER_NAME;
					break;
			}
		}

		return result;
	}

	public static boolean findOnTickMethod(boolean print_always) {
		OnTickMethod previous_method = onTickMethod;
		for (OnTickMethod method : onTickMethodOrder) {
			switch (method) {
				case FORGE:
					if (minecraftForge) {
						onTickMethod = OnTickMethod.FORGE;
						if (print_always || onTickMethod != previous_method)
							Logger.Log("Using Forge for the mod operation.");
						return true;
					}
					break;

				case LITELOADER:
					if (liteLoader) {
						onTickMethod = OnTickMethod.LITELOADER;
						if (print_always || onTickMethod != previous_method)
							Logger.Log("Using LiteLoader for the mod operation.");
						return true;
					}
					break;
			}
		}

		return false;
	}

	public static void onUpdateInGame() {
		GuiScreen currentScreen = getCurrentScreen();
		if (currentScreen == null) {
			// Reset stuff
			oldGuiScreen = null;
			container = null;
			oldSelectedSlot = null;
			firstSlot = null;
			oldStackOnMouse = null;
			firstSlotClicked = false;
			shouldClick = true;
			disableForThisContainer = false;
			disableWheelForThisContainer = false;
			readConfig = true;

			guiContainerID = GuiContainerID.NOTASSIGNED;
		} else {
			if (readConfig) {
				readConfig = false;
				readConfigFile();
				findOnTickMethod(false);
			}

			if (guiContainerID == GuiContainerID.NOTASSIGNED) {
				guiContainerID = getGuiContainerID(currentScreen);
			}

			onUpdateInGui(currentScreen);
		}
	}

	public static void onUpdateInGui(GuiScreen currentScreen) {

		if (oldGuiScreen != currentScreen) {
			oldGuiScreen = currentScreen;

			// If we opened an inventory from another inventory (for example, NEI's options menu).
			guiContainerID = getGuiContainerID(currentScreen);
			if (guiContainerID == GuiContainerID.NOTGUICONTAINER)
				return;

			container = getContainerWithID(currentScreen);
			disableForThisContainer = isDisabledForThisContainer(currentScreen);

			Logger.DebugLog(
				new StringBuilder()
					.append("You have just opened a ")
					.append(getGuiContainerNameFromID(currentScreen))
					.append(" container (")
					.append(currentScreen.getClass().getSimpleName())
					.append((container == null) ? "" : "; ")
					.append((container == null) ? "" : container.getClass().getSimpleName())
					.append("), which has ")
					.append(getSlotCountWithID(currentScreen))
					.append(" slots!")
					.toString());

			disableWheelForThisContainer = isWheelDisabledForThisContainer(currentScreen);
		}

		if (guiContainerID == GuiContainerID.NOTGUICONTAINER)
			return;

		if ((Main.DisableRMBTweak || (Main.RMBTweak == 0))
				&& (Main.LMBTweakWithoutItem == 0)
				&& (Main.LMBTweakWithItem == 0)
				&& (Main.WheelTweak == 0))
			return;

		if (disableForThisContainer)
			return;

		// It's better to have this here, because there are some inventories
		// that change slot count at runtime (for example, NEI's crafting recipe GUI).
		int slotCount = getSlotCountWithID(currentScreen);
		if (slotCount == 0) // If there are no slots, then there is nothing to do.
			return;

		int wheel = ((Main.WheelTweak == 1) && !disableWheelForThisContainer) ? Mouse.getDWheel() / 120 : 0;

		if (!Mouse.isButtonDown(1)) {
			firstSlotClicked = false;
			firstSlot = null;
			shouldClick = true;
		}

		Slot selectedSlot = getSelectedSlotWithID(currentScreen, slotCount);

		// Copy the stacks, so that they don't change while we do our stuff.
		ItemStack stackOnMouse = copyItemStack(getStackOnMouse());
		ItemStack targetStack = copyItemStack(getSlotStack(selectedSlot));

		// To correctly determine when and how the default RMB drag needs to be disabled, we need a bunch of conditions...
		if (Mouse.isButtonDown(1) && (oldStackOnMouse != stackOnMouse) && (oldStackOnMouse == null)) {
			shouldClick = false;
		}

		if (oldSelectedSlot != selectedSlot) {
			// ...and some more conditions.
			if (Mouse.isButtonDown(1) && !firstSlotClicked && (firstSlot == null) && (oldSelectedSlot != null)) {
				if (!areStacksCompatible(stackOnMouse, getSlotStack(oldSelectedSlot))) {
					shouldClick = false;
				}

				firstSlot = oldSelectedSlot;
			}

			if (Mouse.isButtonDown(1) && (oldSelectedSlot == null) && !firstSlotClicked && (firstSlot == null)) {
				shouldClick = false;
			}

			if (selectedSlot == null) {
				oldSelectedSlot = selectedSlot;

				if ((firstSlot != null) && !firstSlotClicked) {
					firstSlotClicked = true;
					disableRMBDragWithID(currentScreen);
					firstSlot = null;
				}

				return;
			}

			Logger.DebugLog(
				new StringBuilder()
					.append("You have selected a new slot, it's slot number is ")
					.append(getSlotNumber(selectedSlot)).toString());

			boolean shiftIsDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

			if (Mouse.isButtonDown(1)) { // Right mouse button
				if ((Main.RMBTweak == 1) && !Main.DisableRMBTweak) {

					if ((stackOnMouse != null) && areStacksCompatible(stackOnMouse, targetStack)
							&& !isCraftingOutputSlot(currentScreen, selectedSlot)) {
						if ((firstSlot != null) && !firstSlotClicked) {
							firstSlotClicked = true;
							disableRMBDragWithID(currentScreen);
							firstSlot = null;
						} else {
							shouldClick = false;
							disableRMBDragWithID(currentScreen);
						}

						clickSlot(currentScreen, selectedSlot, 1, false);
					}

				}
			} else if (Mouse.isButtonDown(0)) { // Left mouse button
				if (stackOnMouse != null) {
					if (Main.LMBTweakWithItem == 1) {
						if ((targetStack != null) && areStacksCompatible(stackOnMouse, targetStack)) {

							if (shiftIsDown) { // If shift is down, we just shift-click the slot and the item gets moved into another inventory.
								clickSlot(currentScreen, selectedSlot, 0, true);
							} else { // If shift is not down, we need to merge the item stack on the mouse with the one in the slot.
								if ((getItemStackSize(stackOnMouse) + getItemStackSize(targetStack)) <= getMaxItemStackSize(stackOnMouse)) {
									// We need to click on the slot so that our item stack gets merged with it,
									// and then click again to return the stack to the mouse.
									// However, if the slot is crafting output, then the item is added to the mouse stack
									// on the first click and we don't need to click the second time.
									clickSlot(currentScreen, selectedSlot, 0, false);
									if (!isCraftingOutputSlot(currentScreen, selectedSlot))
										clickSlot(currentScreen, selectedSlot, 0, false);
								}
							}
						}

					}
				} else if (Main.LMBTweakWithoutItem == 1) {
					if (targetStack != null) {
						if (shiftIsDown) {
							clickSlot(currentScreen, selectedSlot, 0, true);
						}
					}
				}
			}

			oldSelectedSlot = selectedSlot;
		}

		if ((wheel != 0) && (selectedSlot != null)) {
			int numItemsToMove = Math.abs(wheel);
			Logger.DebugLog("numItemsToMove: " + numItemsToMove);

			if (slotCount > Constants.INVENTORY_SIZE) {
				ItemStack originalStack = getSlotStack(selectedSlot);
				boolean isCraftingOutput = isCraftingOutputSlot(currentScreen, selectedSlot);

				if ((originalStack != null)
						&& ((stackOnMouse == null) || (isCraftingOutput ? areStacksCompatible(originalStack, stackOnMouse) : !areStacksCompatible(originalStack, stackOnMouse)))) {
					do {
						Slot applicableSlot = null;

						int slotCounter = 0;
						int countUntil = slotCount - Constants.INVENTORY_SIZE;
						if (getSlotNumber(selectedSlot) < countUntil) {
							slotCounter = countUntil;
							countUntil = slotCount;
						}

						if ((wheel < 0) || (Main.WheelSearchOrder == 0)) {
							for (int i = slotCounter; i < countUntil; i++) {
								Slot sl = getSlotWithID(currentScreen, i);
								ItemStack stackSl = getSlotStack(sl);

								if (stackSl == null) {
									if ((applicableSlot == null)
											&& (wheel < 0)
											&& sl.isItemValid(originalStack)
											&& !isCraftingOutputSlot(currentScreen, sl)) {
										applicableSlot = sl;
									}
								} else if (areStacksCompatible(originalStack, stackSl)) {
									if ((wheel < 0)
											&& (stackSl.stackSize < stackSl.getMaxStackSize())) {
										applicableSlot = sl;
										break;
									} else if (wheel > 0) {
										applicableSlot = sl;
										break;
									}
								}
							}
						} else {
							for (int i = countUntil - 1; i >= slotCounter; i--) {
								Slot sl = getSlotWithID(currentScreen, i);
								ItemStack stackSl = getSlotStack(sl);

								if (stackSl == null) {
									if ((applicableSlot == null)
											&& (wheel < 0)
											&& sl.isItemValid(originalStack)) {
										applicableSlot = sl;
									}
								} else if (areStacksCompatible(originalStack, stackSl)) {
									if ((wheel < 0)
											&& (stackSl.stackSize < stackSl.getMaxStackSize())) {
										applicableSlot = sl;
										break;
									} else if (wheel > 0) {
										applicableSlot = sl;
										break;
									}
								}
							}
						}

						if (isCraftingOutput) {
							if (wheel < 0) {
								boolean mouseWasEmpty = stackOnMouse == null;

								for (int i = 0; i < numItemsToMove; i++) {
									clickSlot(currentScreen, selectedSlot, 0, false);
								}

								if ((applicableSlot != null)
										&& mouseWasEmpty) {
									clickSlot(currentScreen, applicableSlot, 0, false);
								}
							}

							break;
						}

						if (applicableSlot != null) {
							Slot slotTo = (wheel < 0) ? applicableSlot : selectedSlot;
							Slot slotFrom = (wheel < 0) ? selectedSlot : applicableSlot;
							ItemStack stackTo = (getSlotStack(slotTo) != null) ? copyItemStack(getSlotStack(slotTo)) : null;
							ItemStack stackFrom = copyItemStack(getSlotStack(slotFrom));

							if (wheel < 0) {
								numItemsToMove = Math.min(numItemsToMove, getItemStackSize(stackFrom));

								if ((stackTo != null)
										&& ((getMaxItemStackSize(stackTo) - getItemStackSize(stackTo)) <= numItemsToMove)) {
									clickSlot(currentScreen, slotFrom, 0, false);
									clickSlot(currentScreen, slotTo, 0, false);
									clickSlot(currentScreen, slotFrom, 0, false);

									numItemsToMove -= getMaxItemStackSize(stackTo) - getItemStackSize(stackTo);
								} else {
									clickSlot(currentScreen, slotFrom, 0, false);

									if (getItemStackSize(stackFrom) <= numItemsToMove) {
										clickSlot(currentScreen, slotTo, 0, false);
									} else {
										for (int i = 0; i < numItemsToMove; i++) {
											clickSlot(currentScreen, slotTo, 1, false);
										}
									}

									clickSlot(currentScreen, slotFrom, 0, false);

									numItemsToMove = 0;
								}
							} else {
								if ((getMaxItemStackSize(stackTo) - getItemStackSize(stackTo)) <= numItemsToMove) {
									clickSlot(currentScreen, slotFrom, 0, false);
									clickSlot(currentScreen, slotTo, 0, false);
									clickSlot(currentScreen, slotFrom, 0, false);
								} else {
									clickSlot(currentScreen, slotFrom, 0, false);

									if (getItemStackSize(stackFrom) <= numItemsToMove) {
										clickSlot(currentScreen, slotTo, 0, false);
										numItemsToMove -= getMaxItemStackSize(stackFrom);
									} else {
										for (int i = 0; i < numItemsToMove; i++) {
											clickSlot(currentScreen, slotTo, 1, false);
										}

										numItemsToMove = 0;
									}

									clickSlot(currentScreen, slotFrom, 0, false);
								}

								if (getMaxItemStackSize(stackTo) == getMaxItemStackSize(stackTo)) {
									numItemsToMove = 0;
								}
							}
						} else {
							break;
						}
					}
					while (numItemsToMove != 0);
				}
			}
		}

		oldStackOnMouse = stackOnMouse;
	}

	public static GuiContainerID getGuiContainerID(GuiScreen currentScreen) {
		// Mod containers extending GuiContainer will be identified as MINECRAFT below,
		// so check for overrides before that.
		GuiContainerID id = ModCompatibility.getGuiContainerID(currentScreen);
		if (id == GuiContainerID.NOTGUICONTAINER)
			return isGuiContainer(currentScreen)
				? GuiContainerID.MINECRAFT
				: GuiContainerID.NOTGUICONTAINER;
		else
			return id;
	}

	public static Object getContainerWithID(GuiScreen currentScreen) {
		if (guiContainerID == GuiContainerID.MINECRAFT)
			return getContainer(asGuiContainer(currentScreen));
		else
			return ModCompatibility.getContainer(guiContainerID, currentScreen);
	}

	public static int getSlotCountWithID(GuiScreen currentScreen) {
		if (guiContainerID == GuiContainerID.MINECRAFT)
			return getSlots(asContainer(container)).size();
		else
			return ModCompatibility.getSlotCount(guiContainerID, currentScreen, container);
	}

	public static String getGuiContainerNameFromID(GuiScreen currentScreen) {
		switch (guiContainerID) {
			case NOTASSIGNED:
				return "Unknown";
			case NOTGUICONTAINER:
				return "Wrong";
			case MINECRAFT:
				return "Vanilla Minecraft";

			default:
				return ModCompatibility.getModNameFromGuiContainerID(guiContainerID, currentScreen);
		}
	}

	public static boolean isDisabledForThisContainer(GuiScreen currentScreen) {
		if (guiContainerID == GuiContainerID.MINECRAFT)
			return isGuiContainerCreative(currentScreen);
		else
			return ModCompatibility.isDisabledForThisContainer(guiContainerID, currentScreen);
	}

	public static boolean isWheelDisabledForThisContainer(GuiScreen currentScreen) {
		if (guiContainerID == GuiContainerID.MINECRAFT)
			return false;
		else
			return ModCompatibility.isWheelDisabledForThisContainer(guiContainerID, currentScreen);
	}

	public static Slot getSelectedSlotWithID(GuiScreen currentScreen, int slotCount) {
		if (guiContainerID == GuiContainerID.MINECRAFT)
			return getSelectedSlot(asGuiContainer(currentScreen), asContainer(container), slotCount);
		else
			return ModCompatibility.getSelectedSlot(guiContainerID, currentScreen, container, slotCount);
	}

	public static void clickSlot(GuiScreen currentScreen, Slot targetSlot, int mouseButton, boolean shiftPressed) {
		if (guiContainerID == GuiContainerID.MINECRAFT) {
			windowClick(getWindowId(asContainer(container)), getSlotNumber(targetSlot), mouseButton, shiftPressed ? 1 : 0);
		} else {
			ModCompatibility.clickSlot(guiContainerID, currentScreen, container, targetSlot, mouseButton, shiftPressed);
		}
	}

	public static boolean isCraftingOutputSlot(GuiScreen currentScreen, Slot targetSlot) {
		if (guiContainerID == GuiContainerID.MINECRAFT)
			return isVanillaCraftingOutputSlot(asContainer(container), targetSlot);
		else
			return ModCompatibility.isCraftingOutputSlot(guiContainerID, currentScreen, container, targetSlot);
	}

	public static Slot getSlotWithID(GuiScreen currentScreen, int slotNumber) {
		if (guiContainerID == GuiContainerID.MINECRAFT)
			return getSlot(asContainer(container), slotNumber);
		else
			return ModCompatibility.getSlot(guiContainerID, currentScreen, container, slotNumber);
	}

	public static void disableRMBDragWithID(GuiScreen currentScreen) {
		if (guiContainerID == GuiContainerID.MINECRAFT) {
			disableVanillaRMBDrag(asGuiContainer(currentScreen));

			if (shouldClick) {
				clickSlot(currentScreen, firstSlot, 1, false);
			}
		} else {
			ModCompatibility.disableRMBDragIfRequired(guiContainerID, currentScreen, container, firstSlot, shouldClick);
		}
	}
}
