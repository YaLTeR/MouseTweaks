package yalter.mousetweaks;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Slot;
import yalter.mousetweaks.api.IMTModGuiContainer;

public class ModCompatibility extends DeobfuscationLayer {
	private static boolean initialized = false;

	public static void initialize() {
		if (initialized)
			return;

		initialized = true;
	}

	public static GuiContainerID getGuiContainerID(GuiScreen guiScreen) {
		if (guiScreen instanceof IMTModGuiContainer)
			return GuiContainerID.MTMODGUICONTAINER;

		return GuiContainerID.NOTGUICONTAINER;
	}

	public static String getModNameFromGuiContainerID(GuiContainerID id, GuiScreen guiScreen) {
		switch (id) {
			case MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).getModName();

			default:
				return "Unknown";
		}
	}

	public static boolean isDisabledForThisContainer(GuiContainerID id, GuiScreen guiScreen) {
		switch (id) {
			case MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).isMouseTweaksDisabled();

			default:
				return false;
		}
	}

	public static boolean isWheelDisabledForThisContainer(GuiContainerID id, GuiScreen guiScreen) {
		switch (id) {
			case MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).isWheelTweakDisabled();

			default:
				return false;
		}
	}

	public static Slot getSlot(GuiContainerID id, GuiScreen guiScreen, Object container, int slotNumber) {
		switch (id) {
			case MTMODGUICONTAINER:
				return asSlot(((IMTModGuiContainer) guiScreen).getModSlot(container, slotNumber));

			default:
				return null;
		}
	}

	public static boolean isCraftingOutputSlot(GuiContainerID id, GuiScreen guiScreen, Object container, Slot selectedSlot) {
		switch (id) {
			case MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).isCraftingOutputSlot(container, selectedSlot);

			default:
				return false;
		}
	}

	public static void clickSlot(GuiContainerID id, GuiScreen guiScreen, Object container, Slot targetSlot, int mouseButton, boolean shiftPressed) {
		switch (id) {
			case MTMODGUICONTAINER:
				((IMTModGuiContainer) guiScreen).clickModSlot(container, targetSlot, mouseButton, shiftPressed);
				return;
		}
	}

	public static Slot getSelectedSlot(GuiContainerID id, GuiScreen guiScreen, Object container, int slotCount) {
		switch (id) {
			case MTMODGUICONTAINER:
				return asSlot(((IMTModGuiContainer) guiScreen).getModSelectedSlot(container, slotCount));

			default:
				return null;
		}
	}

	public static int getSlotCount(GuiContainerID id, GuiScreen guiScreen, Object container) {
		switch (id) {
			case MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).getModSlotCount(container);

			default:
				return 0;
		}
	}

	public static Object getContainer(GuiContainerID id, GuiScreen guiScreen) {
		switch (id) {
			case MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).getModContainer();

			default:
				return null;
		}
	}

	public static void disableRMBDragIfRequired(GuiContainerID id, GuiScreen guiScreen, Object container, Slot firstSlot, boolean shouldClick) {
		switch (id) {
			case MTMODGUICONTAINER:
				((IMTModGuiContainer) guiScreen).disableRMBDragIfRequired(container, firstSlot, shouldClick);
				return;
		}
	}
}
