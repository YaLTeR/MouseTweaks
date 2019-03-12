package yalter.mousetweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import yalter.mousetweaks.api.IMTModGuiContainer2;
import yalter.mousetweaks.api.IMTModGuiContainer2Ex;
import yalter.mousetweaks.forge.ForgeMouseState;
import yalter.mousetweaks.handlers.*;

import java.io.File;
import java.util.List;

public class Main {
	private static boolean liteLoader = false;
	private static boolean forge = false;

	public static Config config;
	public static OnTickMethod onTickMethod;

	private static Minecraft mc;

	private static IMouseState mouseState = new SimpleMouseState();
	private static GuiScreen oldGuiScreen = null;
	private static Slot oldSelectedSlot = null;
	private static Slot firstRightClickedSlot = null;
	private static boolean oldRMBDown = false;
	private static boolean disableForThisContainer = false;
	private static boolean disableWheelForThisContainer = false;

	private static IGuiScreenHandler handler = null;

	private static boolean readConfig = false;
	private static boolean initialized = false;
	private static boolean disabled = false;

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

		Reflection.reflectGuiContainer();

		forge = ((entryPoint == Constants.EntryPoint.FORGE || Reflection.doesClassExist(
			"net.minecraftforge.client.MinecraftForgeClient")));
		if (forge) {
			Logger.Log("Minecraft Forge is installed.");
		} else {
			Logger.Log("Minecraft Forge is not installed.");
		}

		liteLoader = ((entryPoint == Constants.EntryPoint.LITELOADER) || Reflection.doesClassExist(
			"com.mumfrey.liteloader.core.LiteLoader"));
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

						if (mouseState.getClass() != ForgeMouseState.class) {
							Logger.DebugLog("Switching to ForgeMouseState.");
							mouseState = new ForgeMouseState();
						}

						((ForgeMouseState) mouseState).simpleScrolling = (config.scrollHandling == ScrollHandling.SIMPLE);

						if (print_always || onTickMethod != previous_method)
							Logger.Log("Using Forge for the mod operation.");
						return true;
					}
					break;

				case LITELOADER:
					if (liteLoader) {
						onTickMethod = OnTickMethod.LITELOADER;

						if (mouseState.getClass() != SimpleMouseState.class) {
							Logger.DebugLog("Switching to SimpleMouseState.");
							mouseState = new SimpleMouseState();
						}

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
		GuiScreen currentScreen = mc.currentScreen;
		if (currentScreen == null) {
			// Reset stuff
			oldGuiScreen = null;
			oldSelectedSlot = null;
			firstRightClickedSlot = null;
			disableForThisContainer = false;
			disableWheelForThisContainer = false;
			readConfig = true;

			handler = null;
		} else {
			if (readConfig) {
				readConfig = false;
				config.read();
				findOnTickMethod(false);
			}

			onUpdateInGui(currentScreen);
		}

		oldRMBDown = mouseState.isButtonPressed(MouseButton.RIGHT);
	}

	public static void onMouseInput() {
		mouseState.update();
	}

	private static void onUpdateInGui(GuiScreen currentScreen) {

		if (oldGuiScreen != currentScreen) {
			oldGuiScreen = currentScreen;

			Logger.DebugLog("You have just opened " + currentScreen.getClass().getSimpleName() + ".");

			handler = findHandler(currentScreen);

			// don't handle any mouse inputs that were started from the old gui
			mouseState.clear();

			if (handler == null) {
				disableForThisContainer = true;

				Logger.DebugLog("No valid handler found; MT is disabled.");

				return;
			} else {
				disableForThisContainer = handler.isMouseTweaksDisabled();
				disableWheelForThisContainer = handler.isWheelTweakDisabled();

				Logger.DebugLog("Handler: "
				                + handler.getClass().getSimpleName()
				                + "; MT is "
				                + (disableForThisContainer
				                   ? "disabled"
				                   : "enabled")
				                + "; wheel tweak is "
				                + (disableWheelForThisContainer ? "disabled" : "enabled")
				                + ".");
			}
		}

		// If everything is disabled there's nothing to do.
		if (!config.rmbTweak && !config.lmbTweakWithItem && !config.lmbTweakWithoutItem && !config.wheelTweak)
			return;

		if (disableForThisContainer)
			return;

		Slot selectedSlot = handler.getSlotUnderMouse();

		if (mouseState.isButtonPressed(MouseButton.RIGHT)) {
			if (!oldRMBDown)
				firstRightClickedSlot = selectedSlot;

			if (config.rmbTweak && handler.disableRMBDraggingFunctionality()) {
				// Check some conditions to see if we really need to click the first slot.
				if (firstRightClickedSlot != null
				    // This condition is here to prevent double-clicking.
				    && (firstRightClickedSlot != selectedSlot || oldSelectedSlot == selectedSlot)
				    && !handler.isIgnored(firstRightClickedSlot)
				    && !handler.isCraftingOutput(firstRightClickedSlot)) {
					ItemStack targetStack = firstRightClickedSlot.getStack();
					ItemStack stackOnMouse = mc.player.inventory.getItemStack();

					if (!stackOnMouse.isEmpty()
					    && areStacksCompatible(stackOnMouse, targetStack)
					    && firstRightClickedSlot.isItemValid(stackOnMouse)) {
						handler.clickSlot(firstRightClickedSlot, MouseButton.RIGHT, false);
					}
				}
			}
		} else {
			firstRightClickedSlot = null;
		}

		if (oldSelectedSlot != selectedSlot) {
			oldSelectedSlot = selectedSlot;

			// Nothing to do if no slot is selected.
			if (selectedSlot == null)
				return;

			// Prevent double-clicking.
			if (firstRightClickedSlot == selectedSlot)
				firstRightClickedSlot = null;

			Logger.DebugLog("You have selected a new slot, it's slot number is " + selectedSlot.slotNumber);

			// Copy stacks, otherwise when we click stuff they get updated and mess up the logic.
			ItemStack targetStack = selectedSlot.getStack().copy();
			ItemStack stackOnMouse = mc.player.inventory.getItemStack().copy();

			boolean shiftIsDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

			if (mouseState.isButtonPressed(MouseButton.RIGHT)) {
				// Right mouse button
				if (config.rmbTweak) {
					if (!handler.isIgnored(selectedSlot)
					    && !handler.isCraftingOutput(selectedSlot)
					    && !stackOnMouse.isEmpty()
					    && areStacksCompatible(stackOnMouse, targetStack)
					    && selectedSlot.isItemValid(stackOnMouse)) {
						handler.clickSlot(selectedSlot, MouseButton.RIGHT, false);
					}
				}
			} else if (mouseState.isButtonPressed(MouseButton.LEFT)) {
				// Left mouse button
				if (!stackOnMouse.isEmpty()) {
					if (config.lmbTweakWithItem) {
						if (!handler.isIgnored(selectedSlot)
						    && !targetStack.isEmpty()
						    && areStacksCompatible(stackOnMouse, targetStack)) {
							if (shiftIsDown) {
								// If shift is down, we just shift-click the slot and the item gets moved into another
								// inventory.
								handler.clickSlot(selectedSlot, MouseButton.LEFT, true);
							} else {
								// If shift is not down, we need to merge the item stack on the mouse with the one in
								// the slot.
								if ((stackOnMouse.getCount() + targetStack.getCount())
								    <= stackOnMouse.getMaxStackSize()) {
									// We need to click on the slot so that our item stack gets merged with it, and
									// then click again to return the stack to the mouse. However, if the slot is
									// crafting output, then the item is added to the mouse stack on the first click
									// and we don't need to click the second time.
									handler.clickSlot(selectedSlot, MouseButton.LEFT, false);

									if (!handler.isCraftingOutput(selectedSlot))
										handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
								}
							}
						}
					}
				} else if (config.lmbTweakWithoutItem) {
					if (!targetStack.isEmpty() && shiftIsDown && !handler.isIgnored(selectedSlot)) {
						handler.clickSlot(selectedSlot, MouseButton.LEFT, true);
					}
				}
			}
		}

		handleWheel(selectedSlot);
	}

	private static void handleWheel(Slot selectedSlot) {
		if (!config.wheelTweak || disableWheelForThisContainer)
			return;
		int wheel = mouseState.consumeScrollAmount();

		int numItemsToMove = Math.abs(wheel);
		
		if (numItemsToMove == 0 || selectedSlot == null || handler.isIgnored(selectedSlot))
			return;

		ItemStack stackOnMouse = mc.player.inventory.getItemStack().copy();
		ItemStack originalStack = selectedSlot.getStack().copy();
		boolean isCraftingOutput = handler.isCraftingOutput(selectedSlot);

		// Rather complex condition to determine when the wheel tweak can't be used.
		if (originalStack.isEmpty()
		    || (!stackOnMouse.isEmpty()
		        && (isCraftingOutput
		            ? !areStacksCompatible(originalStack, stackOnMouse)
		            : areStacksCompatible(originalStack, stackOnMouse))))
			return;

		List<Slot> slots = handler.getSlots();

		if (config.wheelScrollDirection.isPositionAware() && otherInventoryIsAbove(selectedSlot, slots)) {
			wheel = -wheel;
		}
		if (config.wheelScrollDirection.isInverted()) {
			wheel = -wheel;
		}
		boolean pushItems = wheel < 0;

		if (isCraftingOutput) {
			if (pushItems) {
				if (originalStack.isEmpty())
					return;

				Slot applicableSlot = findWheelApplicableSlot(slots, selectedSlot, pushItems);

				for (int i = 0; i < numItemsToMove; i++)
					handler.clickSlot(selectedSlot, MouseButton.LEFT, false);

				if (applicableSlot != null && stackOnMouse.isEmpty())
					handler.clickSlot(applicableSlot, MouseButton.LEFT, false);
			}

			return;
		}

		do {
			Slot applicableSlot = findWheelApplicableSlot(slots, selectedSlot, pushItems);
			if (applicableSlot == null)
				break;

			if (pushItems) {
				Slot slotTo = applicableSlot;
				Slot slotFrom = selectedSlot;
				ItemStack stackTo = slotTo.getStack().copy();
				ItemStack stackFrom = slotFrom.getStack().copy();

				numItemsToMove = Math.min(numItemsToMove, stackFrom.getCount());

				if (!stackTo.isEmpty() && (stackTo.getMaxStackSize() - stackTo.getCount()) <= numItemsToMove) {
					// The applicable slot fits in less items than we can move.
					handler.clickSlot(slotFrom, MouseButton.LEFT, false);
					handler.clickSlot(slotTo, MouseButton.LEFT, false);
					handler.clickSlot(slotFrom, MouseButton.LEFT, false);

					numItemsToMove -= stackTo.getMaxStackSize() - stackTo.getCount();
				} else {
					handler.clickSlot(slotFrom, MouseButton.LEFT, false);

					if (stackFrom.getCount() <= numItemsToMove) {
						handler.clickSlot(slotTo, MouseButton.LEFT, false);
					} else {
						for (int i = 0; i < numItemsToMove; i++)
							handler.clickSlot(slotTo, MouseButton.RIGHT, false);
					}

					handler.clickSlot(slotFrom, MouseButton.LEFT, false);

					break;
				}
			} else {
				Slot slotTo = selectedSlot;
				Slot slotFrom = applicableSlot;
				ItemStack stackTo = slotTo.getStack().copy();
				ItemStack stackFrom = slotFrom.getStack().copy();

				if (stackTo.getCount() == stackTo.getMaxStackSize())
					break;

				if ((stackTo.getMaxStackSize() - stackTo.getCount()) <= numItemsToMove) {
					handler.clickSlot(slotFrom, MouseButton.LEFT, false);
					handler.clickSlot(slotTo, MouseButton.LEFT, false);

					if (!handler.isCraftingOutput(slotFrom))
						handler.clickSlot(slotFrom, MouseButton.LEFT, false);
				} else {
					handler.clickSlot(slotFrom, MouseButton.LEFT, false);

					if (handler.isCraftingOutput(slotFrom)) {
						handler.clickSlot(slotTo, MouseButton.LEFT, false);
						--numItemsToMove;
					} else if (stackFrom.getCount() <= numItemsToMove) {
						handler.clickSlot(slotTo, MouseButton.LEFT, false);
						numItemsToMove -= stackFrom.getCount();
					} else {
						for (int i = 0; i < numItemsToMove; i++)
							handler.clickSlot(slotTo, MouseButton.RIGHT, false);

						numItemsToMove = 0;
					}

					if (!handler.isCraftingOutput(slotFrom))
						handler.clickSlot(slotFrom, MouseButton.LEFT, false);
				}
			}
		} while (numItemsToMove > 0);
	}

	// Returns true if the other inventory is above the selected slot inventory.
	//
	// This is used for the inventory position aware scroll direction. To prevent any surprises, this should have the
	// same logic for what constitutes the "other" inventory as findWheelApplicableSlot().
	private static boolean otherInventoryIsAbove(Slot selectedSlot, List<Slot> slots) {
		boolean selectedIsInPlayerInventory = selectedSlot.inventory == mc.player.inventory;
		for (Slot slot : slots) {
			if ((slot.inventory == mc.player.inventory) != selectedIsInPlayerInventory
			    && slot.yPos < selectedSlot.yPos) {
				return true;
			}
		}
		return false;
	}

	// Finds the appropriate handler to use with this GuiScreen. Returns null if no handler was found.
	@SuppressWarnings("deprecation")
	private static IGuiScreenHandler findHandler(GuiScreen currentScreen) {
		if (currentScreen instanceof IMTModGuiContainer2Ex) {
			return new IMTModGuiContainer2ExHandler((IMTModGuiContainer2Ex) currentScreen);
		} else if (currentScreen instanceof IMTModGuiContainer2) {
			return new IMTModGuiContainer2Handler((IMTModGuiContainer2) currentScreen);
		} else if (currentScreen instanceof yalter.mousetweaks.api.IMTModGuiContainer) {
			return new IMTModGuiContainerHandler((yalter.mousetweaks.api.IMTModGuiContainer) currentScreen);
		} else if (currentScreen instanceof GuiContainerCreative) {
			return new GuiContainerCreativeHandler((GuiContainerCreative) currentScreen);
		} else if (currentScreen instanceof GuiContainer) {
			return new GuiContainerHandler((GuiContainer) currentScreen);
		}

		return null;
	}

	// Returns true if we can put items from one stack into another.
	// This is different from ItemStack.areItemsEqual() because here empty stacks are compatible with anything.
	private static boolean areStacksCompatible(ItemStack a, ItemStack b) {
		return a.isEmpty() || b.isEmpty() || (a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b));
	}

	private static Slot findWheelApplicableSlot(List<Slot> slots, Slot selectedSlot, boolean pushItems) {
		int startIndex, endIndex, direction;
		if (pushItems || config.wheelSearchOrder == WheelSearchOrder.FIRST_TO_LAST) {
			startIndex = 0;
			endIndex = slots.size();
			direction = 1;
		} else {
			startIndex = slots.size() - 1;
			endIndex = -1;
			direction = -1;
		}

		ItemStack originalStack = selectedSlot.getStack();
		boolean findInPlayerInventory = (selectedSlot.inventory != mc.player.inventory);
		Slot rv = null;

		for (int i = startIndex; i != endIndex; i += direction) {
			Slot slot = slots.get(i);

			if (handler.isIgnored(slot))
				continue;

			if (findInPlayerInventory) {
				if (slot.inventory != mc.player.inventory)
					continue;
			} else {
				if (slot.inventory == mc.player.inventory)
					continue;
			}

			ItemStack stack = slot.getStack();

			if (stack.isEmpty()) {
				if (rv == null && pushItems && slot.isItemValid(originalStack) && !handler.isCraftingOutput(slot)) {
					rv = slot;
				}
			} else if (areStacksCompatible(originalStack, stack)) {
				if (pushItems) {
					if (!handler.isCraftingOutput(slot) && stack.getCount() < stack.getMaxStackSize())
						return slot;
				} else {
					return slot;
				}
			}
		}

		return rv;
	}
}
