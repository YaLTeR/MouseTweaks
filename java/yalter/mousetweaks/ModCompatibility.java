package yalter.mousetweaks;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Slot;
import yalter.mousetweaks.api.IMTModGuiContainer;

public class ModCompatibility extends DeobfuscationLayer {
	private static boolean initialized = false;

	private static boolean forestryInstalled = false;
	private static boolean codechickencoreInstalled = false;
	private static boolean NEIInstalled = false;

	public static void initialize() {
		if (initialized)
			return;

		forestryInstalled = Reflection.reflectForestry();
		if (forestryInstalled) {
			Logger.Log("Successfully reflected Forestry!");
		}

		codechickencoreInstalled = Reflection.reflectCodeChickenCore();
		if (codechickencoreInstalled) {
			Logger.Log("Successfully reflected CodeChickenCore!");
		}

		NEIInstalled = Reflection.reflectNEI();
		if (NEIInstalled) {
			Logger.Log("Successfully reflected NEI!");
		}

		initialized = true;
	}

	public static int getModGuiContainerID(GuiScreen guiScreen) {
		if (guiScreen instanceof IMTModGuiContainer)
			return Constants.MTMODGUICONTAINER;

		if (forestryInstalled && Reflection.forestry.isInstance("GuiForestry", guiScreen))
			return Constants.FORESTRY;

		if (codechickencoreInstalled && Reflection.codechickencore.isInstance("GuiContainerWidget", guiScreen))
			return Constants.CODECHICKENCORE;

		if ((NEIInstalled
				&& (Reflection.NEI.isInstance("GuiRecipe", guiScreen)))
				|| Reflection.NEI.isInstance("GuiEnchantmentModifier", guiScreen))
			return Constants.NEI;

		return Constants.NOTGUICONTAINER;
	}

	public static String getModNameFromModGuiContainerID(int id, GuiScreen guiScreen) {
		switch (id) {
			case Constants.MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).getModName();

			case Constants.FORESTRY:
				return "Forestry";
			case Constants.CODECHICKENCORE:
				return "CodeChickenCore";
			case Constants.NEI:
				return "NotEnoughItems";

			default:
				return "Unknown";
		}
	}

	public static boolean isWheelDisabledForThisModContainer(int modGuiContainerID, GuiScreen guiScreen) {
		switch (modGuiContainerID) {
			case Constants.MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).isWheelTweakDisabled();

			default:
				return false;
		}
	}

	public static Slot modGetSlot(int modGuiContainerID, GuiScreen guiScreen, Object modContainer, int slotNumber) {
		switch (modGuiContainerID) {
			case Constants.MTMODGUICONTAINER:
				return asSlot(((IMTModGuiContainer) guiScreen).getModSlot(modContainer, slotNumber));

			case Constants.FORESTRY:
				return getSlot(asContainer(modContainer), slotNumber);
			case Constants.NEI:
				return getSlot(asContainer(modContainer), slotNumber);

			default:
				return null;
		}
	}

	public static boolean modIsCraftingOutputSlot(int modGuiContainerID, GuiScreen guiScreen, Object modContainer, Slot selectedSlot) {
		switch (modGuiContainerID) {
			case Constants.MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).isCraftingOutputSlot(modContainer, selectedSlot);

			case Constants.FORESTRY:
				return (Reflection.is(modContainer, "ContainerSqueezer") && ((getSlotNumber(selectedSlot) == 9) || (getSlotNumber(selectedSlot) == 11)))
						|| (Reflection.is(modContainer, "ContainerMoistener") && (getSlotNumber(selectedSlot) == 9))
						|| (Reflection.is(modContainer, "ContainerCentrifuge") && ((getSlotNumber(selectedSlot) >= 1) && (getSlotNumber(selectedSlot) <= 9)))
						|| (Reflection.is(modContainer, "ContainerCarpenter") && (getSlotNumber(selectedSlot) == 21))
						|| (Reflection.is(modContainer, "ContainerFabricator") && (getSlotNumber(selectedSlot) == 20))
						|| (Reflection.is(modContainer, "ContainerWorktable") && (getSlotNumber(selectedSlot) == 27));

			default:
				return false;
		}
	}

	public static void modClickSlot(int modGuiContainerID, GuiScreen guiScreen, Object modContainer, Slot targetSlot, int mouseButton, boolean shiftPressed) {
		switch (modGuiContainerID) {
			case Constants.MTMODGUICONTAINER:
				((IMTModGuiContainer) guiScreen).clickModSlot(modContainer, targetSlot, mouseButton, shiftPressed);
				return;

			case Constants.FORESTRY:
				Reflection.forestry.invokeMethod(guiScreen, "handleMouseClick", targetSlot, 0, mouseButton, shiftPressed ? 1 : 0);
				return;

			case Constants.NEI:
				windowClick(getWindowId(asContainer(modContainer)), getSlotNumber(targetSlot), mouseButton, shiftPressed ? 1 : 0);
				return;

			default:
				return;
		}
	}

	public static Slot getModSelectedSlot(int modGuiContainerID, GuiScreen guiScreen, Object modContainer, int slotCount) {
		switch (modGuiContainerID) {
			case Constants.MTMODGUICONTAINER:
				return asSlot(((IMTModGuiContainer) guiScreen).getModSelectedSlot(modContainer, slotCount));

			case Constants.FORESTRY:
				return asSlot(Reflection.forestry.invokeMethod(guiScreen, "getSlotAtPosition", getRequiredMouseX(), getRequiredMouseY()));
			case Constants.NEI:
				return getSelectedSlot(asGuiContainer(guiScreen), asContainer(modContainer), slotCount);

			default:
				return null;
		}
	}

	public static int getModSlotCount(int modGuiContainerID, GuiScreen guiScreen, Object modContainer) {
		switch (modGuiContainerID) {
			case Constants.MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).getModSlotCount(modContainer);

			case Constants.FORESTRY:
				return getSlots(asContainer(modContainer)).size();
			case Constants.NEI:
				return getSlots(asContainer(modContainer)).size();

			default:
				return 0;
		}
	}

	public static Object getModContainer(int modGuiContainerID, GuiScreen guiScreen) {
		switch (modGuiContainerID) {
			case Constants.MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).getModContainer();

			case Constants.FORESTRY:
				return Reflection.forestry.getFieldValue("inventorySlots", guiScreen);
			case Constants.NEI:
				return getContainer(asGuiContainer(guiScreen));

			default:
				return null;
		}
	}

	public static void disableRMBDragIfRequired(int guiContainerID, GuiScreen guiScreen, Object modContainer, Slot firstSlot, boolean shouldClick) {
		switch (guiContainerID) {
			case Constants.MTMODGUICONTAINER:
				((IMTModGuiContainer) guiScreen).disableRMBDragIfRequired(modContainer, firstSlot, shouldClick);

				return;

			case Constants.NEI:
				disableVanillaRMBDrag(asGuiContainer(guiScreen));

				if (shouldClick) {
					modClickSlot(guiContainerID, guiScreen, modContainer, firstSlot, 1, false);
				}

				return;

			default:
				return;
		}
	}

	public static boolean isDisabledForThisModContainer(int guiContainerID, GuiScreen guiScreen, Object container) {
		switch (guiContainerID) {
			case Constants.MTMODGUICONTAINER:
				return ((IMTModGuiContainer) guiScreen).isMouseTweaksDisabled();

			case Constants.CODECHICKENCORE:
				return true;
			case Constants.NEI:
				return !Reflection.NEI.isInstance("GuiEnchantmentModifier", guiScreen);

			default:
				return false;
		}
	}
}
