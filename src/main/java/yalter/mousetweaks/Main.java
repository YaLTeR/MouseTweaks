package yalter.mousetweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;

public class Main extends DeobfuscationLayer {

	public static boolean liteLoader = false;
	public static boolean forge = false;

	public static boolean disableRMBTweak = false;

	public static Config config;
	public static OnTickMethod onTickMethod;

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
	private static boolean initialized = false;
	private static boolean disabled = false;

	public static boolean initialize() {
		return initialize(Constants.EntryPoint.UNDEFINED);
	}

	public static boolean initialize(Constants.EntryPoint entryPoint) {
		Logger.Log("A call to initialize, entry point: " + entryPoint.toString() + ".");

		if (disabled)
			return false;

		if (initialized)
			return true;
		initialized = true;

		mc = Minecraft.getMinecraft();
		config = new Config(mc.mcDataDir + File.separator + "config" + File.separator + "MouseTweaks.cfg");
		config.read();

		if (!Reflection.reflectGuiContainer()) {
			disabled = true;
			return false;
		}

		forge = ((entryPoint == Constants.EntryPoint.FORGE
				|| Reflection.doesClassExist("net.minecraftforge.client.MinecraftForgeClient")));
		if (forge) {
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
		Logger.Log("Mouse Tweaks has been initialized.");

		return true;
	}

	public static boolean findOnTickMethod(boolean print_always) {
		OnTickMethod previous_method = onTickMethod;
		for (OnTickMethod method : config.onTickMethodOrder) {
			switch (method) {
				case FORGE:
					if (forge) {
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
				config.read();
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

			Logger.DebugLog("You have just opened " + currentScreen.getClass().getSimpleName() + ".");

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

		if ((Main.disableRMBTweak || !config.rmbTweak)
				&& !config.lmbTweakWithItem
				&& !config.lmbTweakWithoutItem
				&& !config.wheelTweak)
			return;

		if (disableForThisContainer)
			return;

		// It's better to have this here, because there are some inventories
		// that change slot count at runtime (for example, NEI's crafting recipe GUI).
		int slotCount = getSlotCountWithID(currentScreen);
		if (slotCount == 0) // If there are no slots, then there is nothing to do.
			return;

		int wheel = (config.wheelTweak && !disableWheelForThisContainer) ? Mouse.getDWheel() / 120 : 0;
		if (config.wheelScrollDirection == WheelScrollDirection.INVERTED)
			wheel = -wheel;

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

				if (config.rmbTweak && !Main.disableRMBTweak && (firstSlot != null) && !firstSlotClicked) {
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
				if (config.rmbTweak && !Main.disableRMBTweak) {

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
					if (config.lmbTweakWithItem) {
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
				} else if (config.lmbTweakWithoutItem) {
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

						if ((wheel < 0) || config.wheelSearchOrder == WheelSearchOrder.FIRST_TO_LAST) {
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
											&& (stackSl.getCount() < stackSl.getMaxStackSize())) {
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
											&& (stackSl.getCount() < stackSl.getMaxStackSize())) {
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
			windowClick(getWindowId(asContainer(container)), getSlotNumber(targetSlot), mouseButton, shiftPressed);
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
