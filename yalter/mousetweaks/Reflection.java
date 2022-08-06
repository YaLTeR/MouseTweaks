package yalter.mousetweaks;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.Slot;
import net.minecraft.src.ModLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Reflection {
	private static Obfuscation obfuscation;
	private static boolean checkObfuscation = true;

	public static ReflectionCache guiContainerClass;

	static void reflectGuiContainer() {
		Logger.Log("Reflecting GuiContainer...");

		guiContainerClass = new ReflectionCache();

		try {
			Method m = getMethod(GuiContainer.class, getObfuscatedName(Constants.GETSLOTATPOSITION_NAME), int.class, int.class);
			guiContainerClass.storeMethod(Constants.GETSLOTATPOSITION_NAME.mcpName, m);
		} catch (NoSuchMethodException e) {
			Logger.Log("Could not retrieve GuiContainer.getSlotAtPosition().");
			guiContainerClass = null;
			return;
		}

		Logger.Log("Success.");
	}
	
	public static String getSlotInventoryFieldName() {
		return getObfuscatedName(Constants.INVENTORY_FIELD_NAME);
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

	private static Method searchMethod(Class<?> clazz, String name, Class... args) throws NoSuchMethodException {
		Method method;

		do {
			try {
				method = clazz.getDeclaredMethod(name, args);

				method.setAccessible(true);
				return method;
			} catch (NoSuchMethodException e) {
				clazz = clazz.getSuperclass();
			}
		} while (clazz != null);

		throw new NoSuchMethodException();
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
			getMethod(GuiContainer.class, Constants.GETSLOTATPOSITION_NAME.mcpName, int.class, int.class);
			obfuscation = Obfuscation.MCP;
		} catch (NoSuchMethodException e) {
			obfuscation = Obfuscation.VANILLA;
		}

		Logger.Log("Detected obfuscation: " + obfuscation + ".");
	}
}
