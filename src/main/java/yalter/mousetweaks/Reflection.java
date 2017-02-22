package yalter.mousetweaks;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.Slot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {
	private static Obfuscation obfuscation;
	private static boolean checkObfuscation = true;

	public static ReflectionCache guiContainerClass;
	public static ReflectionCache guiContainerCreative;

	static void reflectGuiContainer() {
		Logger.Log("Reflecting GuiContainer...");

		guiContainerClass = new ReflectionCache();

		try {
			Field f = getField(GuiContainer.class, getObfuscatedName(Constants.IGNOREMOUSEUP_NAME));
			guiContainerClass.storeField(Constants.IGNOREMOUSEUP_NAME.forgeName, f);
		} catch (NoSuchFieldException e) {
			Logger.Log("Could not retrieve GuiContainer.ignoreMouseUp.");
			guiContainerClass = null;
			return;
		}

		try {
			Field f = getField(GuiContainer.class, getObfuscatedName(Constants.DRAGSPLITTING_NAME));
			guiContainerClass.storeField(Constants.DRAGSPLITTING_NAME.forgeName, f);
		} catch (NoSuchFieldException e) {
			Logger.Log("Could not retrieve GuiContainer.dragSplitting.");
			guiContainerClass = null;
			return;
		}

		try {
			Field f = getField(GuiContainer.class, getObfuscatedName(Constants.DRAGSPLITTINGBUTTON_NAME));
			guiContainerClass.storeField(Constants.DRAGSPLITTINGBUTTON_NAME.forgeName, f);
		} catch (NoSuchFieldException e) {
			Logger.Log("Could not retrieve GuiContainer.dragSplittingButton.");
			guiContainerClass = null;
			return;
		}

		try {
			Method m = getMethod(GuiContainer.class, getObfuscatedName(Constants.GETSLOTATPOSITION_NAME), int.class, int.class);
			guiContainerClass.storeMethod(Constants.GETSLOTATPOSITION_NAME.forgeName, m);
		} catch (NoSuchMethodException e) {
			Logger.Log("Could not retrieve GuiContainer.getSlotAtPosition().");
			guiContainerClass = null;
			return;
		}

		Logger.Log("Success.");
	}

	static void reflectGuiContainerCreative() {
		Logger.Log("Reflecting GuiContainerCreative...");

		guiContainerCreative = new ReflectionCache();

		try {
			Method m = getMethod(GuiContainerCreative.class, getObfuscatedName(Constants.HANDLEMOUSECLICK_NAME), Slot.class, int.class, int.class, int.class);
			guiContainerCreative.storeMethod(Constants.HANDLEMOUSECLICK_NAME.forgeName, m);
		} catch (NoSuchMethodException e) {
			Logger.Log("Could not retrieve GuiContainerCreative.handleMouseClick().");
			guiContainerCreative = null;
			return;
		}

		Logger.Log("Success.");
	}

	static boolean doesClassExist(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static Field getField(Class clazz, String name) throws NoSuchFieldException {
		Field field;

		try {
			field = clazz.getField(name);
		} catch (NoSuchFieldException e) {
			field = clazz.getDeclaredField(name);
		}

		field.setAccessible(true);
		return field;
	}

	private static Method getMethod(Class<?> clazz, String name, Class... args) throws NoSuchMethodException {
		Method method;

		try {
			method = clazz.getMethod(name, args);
		} catch (NoSuchMethodException e) {
			method = clazz.getDeclaredMethod(name, args);
		}

		method.setAccessible(true);
		return method;
	}

	private static String getObfuscatedName(ObfuscatedName obfuscatedName) {
		if (checkObfuscation) {
			checkObfuscation();
		}

		return obfuscatedName.get(obfuscation);
	}

	private static void checkObfuscation() {
		checkObfuscation = false;

		try {
			getField(GuiContainer.class, Constants.IGNOREMOUSEUP_NAME.mcpName);
			obfuscation = Obfuscation.MCP;
		} catch (NoSuchFieldException e) {
			try {
				getField(GuiContainer.class, Constants.IGNOREMOUSEUP_NAME.forgeName);
				obfuscation = Obfuscation.FORGE;
			} catch (NoSuchFieldException ex) {
				obfuscation = Obfuscation.VANILLA;
			}
		}

		Logger.Log("Detected obfuscation: " + obfuscation + ".");
	}
}
