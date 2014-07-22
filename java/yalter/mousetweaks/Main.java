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
	public static boolean optifine = false;

	public static boolean useForge = false;
	public static boolean useLiteLoader = false;
	public static boolean useProfiler = false;

	public static boolean DisableRMBTweak = false;

	public static int RMBTweak = 0;
	public static int LMBTweakWithItem = 0;
	public static int LMBTweakWithoutItem = 0;
	public static int WheelTweak = 0;
	public static int WheelSearchOrder = 1;

	public enum OnTickMethod {PROFILER, FORGE, LITELOADER}
	public static List<OnTickMethod> onTickMethodOrder = new LinkedList<OnTickMethod>();

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
	private static int guiContainerID = 0;

	private static boolean readConfig = false;
	private static boolean initialised = false;
	private static boolean disabled = false;

	public static boolean initialise() {
		return initialise(Constants.EntryPoint.UNDEFINED);
	}

	public static boolean initialise(Constants.EntryPoint entryPoint) {
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

		minecraftForge = Reflection.doesClassExist("net.minecraftforge.client.MinecraftForgeClient");
		if (minecraftForge) {
			Logger.Log("Minecraft Forge is installed.");
		} else {
			Logger.Log("Minecraft Forge is not installed.");
		}

		liteLoader = ((entryPoint == Constants.EntryPoint.LITELOADER) // If we're initialising from LiteModMouseTweaks, the check is not necessary.
				|| Reflection.doesClassExist("com.mumfrey.liteloader.core.LiteLoader"));
		if (liteLoader) {
			Logger.Log("LiteLoader is installed.");
		} else {
			Logger.Log("LiteLoader is not installed.");
		}

		// Find the first suitable OnTick method.
		methodLoop:
		for (OnTickMethod method : onTickMethodOrder) {
			switch (method) {
				case FORGE:
					if (minecraftForge) {
						useForge = true;
						Logger.Log("Using Forge for the mod operation.");
						break methodLoop;
					}
					break;

				case LITELOADER:
					if (liteLoader) {
						useLiteLoader = ((entryPoint == Constants.EntryPoint.LITELOADER)
								|| Reflection.registerLiteModListenerInLiteLoader());
						if (useLiteLoader) {
							Logger.Log("Using LiteLoader for the mod operation.");
							break methodLoop;
						}
					}
					break;

				case PROFILER:
					if (Reflection.replaceProfiler()) {
						useProfiler = true;
						Logger.Log("Using the Minecraft profiler for the mod operation.");

						optifine = Reflection.reflectOptifine();

						break methodLoop;
					}
					break;
			}
		}

		if (!useForge && !useLiteLoader && !useProfiler) {
			// Nothing worked at all.
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

		// Load the OnTick method order.
		String onTickMethodString = mainConfig.getOrCreateProperty("OnTickMethodOrder",
				Constants.ONTICKMETHOD_FORGE_NAME + ", "
						+ Constants.ONTICKMETHOD_LITELOADER_NAME + ", "
						+ Constants.ONTICKMETHOD_PROFILER_NAME);

		onTickMethodString = onTickMethodString.trim();
		String onTickMethods[] = onTickMethodString.split("[\\s]*,[\\s]*");
		for (String method : onTickMethods) {
			if (Constants.ONTICKMETHOD_FORGE_NAME.equalsIgnoreCase(method) && !onTickMethodOrder.contains(OnTickMethod.FORGE))
				onTickMethodOrder.add(OnTickMethod.FORGE);
			else if (Constants.ONTICKMETHOD_LITELOADER_NAME.equalsIgnoreCase(method) && !onTickMethodOrder.contains(OnTickMethod.LITELOADER))
				onTickMethodOrder.add(OnTickMethod.LITELOADER);
			else if (Constants.ONTICKMETHOD_PROFILER_NAME.equalsIgnoreCase(method) && !onTickMethodOrder.contains(OnTickMethod.PROFILER))
				onTickMethodOrder.add(OnTickMethod.PROFILER);
		}

		// Make sure we have one of each.
		if (!onTickMethodOrder.contains(OnTickMethod.FORGE))
			onTickMethodOrder.add(OnTickMethod.FORGE);
		if (!onTickMethodOrder.contains(OnTickMethod.LITELOADER))
			onTickMethodOrder.add(OnTickMethod.LITELOADER);
		if (!onTickMethodOrder.contains(OnTickMethod.PROFILER))
			onTickMethodOrder.add(OnTickMethod.PROFILER);

		boolean savedConfig = saveConfigFile();

		if (savedConfig && !loadedConfig)
			Logger.Log("Mouse Tweaks config file was created.");
	}

	public static boolean saveConfigFile() {
		mainConfig.setIntProperty("RMBTweak", RMBTweak);
		mainConfig.setIntProperty("LMBTweakWithItem", LMBTweakWithItem);
		mainConfig.setIntProperty("LMBTweakWithoutItem", LMBTweakWithoutItem);
		mainConfig.setIntProperty("WheelTweak", WheelTweak);
		mainConfig.setIntProperty("WheelSearchOrder", WheelSearchOrder);
		mainConfig.setIntProperty("Debug", Debug);

		String onTickMethodString = "";
		for (OnTickMethod method : onTickMethodOrder) {
			if (!onTickMethodString.isEmpty())
				onTickMethodString += ", ";

			switch (method) {
				case FORGE:
					onTickMethodString += Constants.ONTICKMETHOD_FORGE_NAME;
					break;

				case LITELOADER:
					onTickMethodString += Constants.ONTICKMETHOD_LITELOADER_NAME;
					break;

				case PROFILER:
					onTickMethodString += Constants.ONTICKMETHOD_PROFILER_NAME;
					break;
			}
		}
		mainConfig.setProperty("OnTickMethodOrder", onTickMethodString);

		return mainConfig.saveConfig();
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

			guiContainerID = Constants.NOTASSIGNED;
		} else {
			if (readConfig) {
				readConfig = false;
				readConfigFile();
			}

			if (guiContainerID == Constants.NOTASSIGNED) {
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
			if (guiContainerID == Constants.NOTGUICONTAINER)
				return;

			container = getContainerWithID(currentScreen);
			disableForThisContainer = isDisabledForThisContainer(currentScreen);

			if (Debug == 1) {
				Logger.Log(new StringBuilder().append("You have just opened a ").append(getGuiContainerNameFromID(currentScreen)).append(" container (").append(currentScreen.getClass().getSimpleName()).append((container == null) ? "" : "; ").append((container == null) ? "" : container.getClass().getSimpleName()).append("), which has ").append(getSlotCountWithID(currentScreen)).append(" slots!").toString());
			}

			disableWheelForThisContainer = isWheelDisabledForThisContainer(currentScreen);
		}

		if (guiContainerID == Constants.NOTGUICONTAINER)
			return;

		if ((Main.DisableRMBTweak || (Main.RMBTweak == 0)) && (Main.LMBTweakWithoutItem == 0) && (Main.LMBTweakWithItem == 0)
				&& (Main.WheelTweak == 0))
			return;

		if (disableForThisContainer)
			return;

		int slotCount = getSlotCountWithID(currentScreen); // It's better to have this here, because there are some inventories that change slot count during
		// runtime (for example NEI's crafting recipe GUI).
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

		// To correctly determine, when and how the default RMB drag needs to be disabled, we need a bunch of conditions...
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

			if (Debug == 1) {
				Logger.Log(new StringBuilder().append("You have selected a new slot, it's slot number is ").append(getSlotNumber(selectedSlot)).toString());
			}

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
			if (Debug == 1) {
				Logger.Log("numItemsToMove: " + numItemsToMove);
			}

			if (slotCount > Constants.INVENTORY_SIZE) {
				ItemStack originalStack = getSlotStack(selectedSlot);
				boolean isCraftingOutput = isCraftingOutputSlot(currentScreen, selectedSlot);

				if ((originalStack != null)
						&& ((stackOnMouse == null) || (isCraftingOutput ? areStacksCompatible(originalStack, stackOnMouse) : !areStacksCompatible(originalStack, stackOnMouse)))) {
					do {
						Slot applicableSlot = null;

						int slotCounter = 0;
						int countUntil = slotCount
								- Constants.INVENTORY_SIZE;
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

	public static int getGuiContainerID(GuiScreen currentScreen) {
		int containerID = ModCompatibility.getModGuiContainerID(currentScreen); // This first because a lot of mod containers extend the vanilla Minecraft one.
		if (containerID == Constants.NOTGUICONTAINER)
			return (isGuiContainer(currentScreen) && isValidGuiContainer(currentScreen)) ? Constants.MINECRAFT : Constants.NOTGUICONTAINER;
		else
			return containerID;
	}

	public static Object getContainerWithID(GuiScreen currentScreen) {
		if (guiContainerID == Constants.MINECRAFT)
			return getContainer(asGuiContainer(currentScreen));
		else
			return ModCompatibility.getModContainer(guiContainerID, currentScreen);
	}

	public static int getSlotCountWithID(GuiScreen currentScreen) {
		if (guiContainerID == Constants.MINECRAFT)
			return getSlots(asContainer(container)).size();
		else
			return ModCompatibility.getModSlotCount(guiContainerID, currentScreen, container);
	}

	public static String getGuiContainerNameFromID(GuiScreen currentScreen) {
		switch (guiContainerID) {
			case Constants.NOTASSIGNED:
				return "Unknown";
			case Constants.NOTGUICONTAINER:
				return "Wrong";
			case Constants.MINECRAFT:
				return "Vanilla Minecraft";

			default:
				return ModCompatibility.getModNameFromModGuiContainerID(guiContainerID, currentScreen);
		}
	}

	public static boolean isDisabledForThisContainer(GuiScreen currentScreen) {
		if (guiContainerID == Constants.MINECRAFT)
			return false;
		else
			return ModCompatibility.isDisabledForThisModContainer(guiContainerID, currentScreen, container);
	}

	public static boolean isWheelDisabledForThisContainer(GuiScreen currentScreen) {
		if (guiContainerID == Constants.MINECRAFT)
			return false;
		else
			return ModCompatibility.isWheelDisabledForThisModContainer(guiContainerID, currentScreen);
	}

	public static Slot getSelectedSlotWithID(GuiScreen currentScreen, int slotCount) {
		if (guiContainerID == Constants.MINECRAFT)
			return getSelectedSlot(asGuiContainer(currentScreen), asContainer(container), slotCount);
		else
			return ModCompatibility.getModSelectedSlot(guiContainerID, currentScreen, container, slotCount);
	}

	public static void clickSlot(GuiScreen currentScreen, Slot targetSlot, int mouseButton, boolean shiftPressed) {
		if (guiContainerID == Constants.MINECRAFT) {
			windowClick(getWindowId(asContainer(container)), getSlotNumber(targetSlot), mouseButton, shiftPressed ? 1 : 0);
		} else {
			ModCompatibility.modClickSlot(guiContainerID, currentScreen, container, targetSlot, mouseButton, shiftPressed);
		}
	}

	public static boolean isCraftingOutputSlot(GuiScreen currentScreen, Slot targetSlot) {
		if (guiContainerID == Constants.MINECRAFT)
			return isVanillaCraftingOutputSlot(asContainer(container), targetSlot);
		else
			return ModCompatibility.modIsCraftingOutputSlot(guiContainerID, currentScreen, container, targetSlot);
	}

	public static Slot getSlotWithID(GuiScreen currentScreen, int slotNumber) {
		if (guiContainerID == Constants.MINECRAFT)
			return getSlot(asContainer(container), slotNumber);
		else
			return ModCompatibility.modGetSlot(guiContainerID, currentScreen, container, slotNumber);
	}

	public static void disableRMBDragWithID(GuiScreen currentScreen) {
		if (guiContainerID == Constants.MINECRAFT) {
			disableVanillaRMBDrag(asGuiContainer(currentScreen));

			if (shouldClick) {
				clickSlot(currentScreen, firstSlot, 1, false);
			}
		} else {
			ModCompatibility.disableRMBDragIfRequired(guiContainerID, currentScreen, container, firstSlot, shouldClick);
		}
	}
}
