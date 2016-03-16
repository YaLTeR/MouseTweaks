package yalter.mousetweaks;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Reflection {
	public static ReflectionCache guiContainerClass;
	public static ReflectionCache minecraft;

	public enum Obfuscation {MCP, FORGE, VANILLA}
	private static Obfuscation obfuscation;
	private static boolean checkObfuscation = true;

	public static boolean reflectGuiContainer() {
		guiContainerClass = new ReflectionCache();
		guiContainerClass.storeClass("GuiContainer", GuiContainer.class);

		Method isMouseOverSlot = getMethod(GuiContainer.class,
			getObfuscatedName(
				Constants.ISMOUSEOVERSLOT_MCP_NAME,
				Constants.ISMOUSEOVERSLOT_FORGE_NAME,
				Constants.ISMOUSEOVERSLOT_NAME),
			Slot.class,
			int.class,
			int.class);

		if (isMouseOverSlot == null) {
			Logger.Log("Failed to get isMouseOverSlot method, quitting.");
			return false;
		}

		guiContainerClass.storeMethod(Constants.ISMOUSEOVERSLOT_FORGE_NAME, isMouseOverSlot);

		Field field;
		field = getField(GuiContainer.class,
				getObfuscatedName(Constants.FIELDE_MCP_NAME, Constants.FIELDE_FORGE_NAME, Constants.FIELDE_NAME));

		if (field == null) {
			Logger.Log("Failed to retrieve the E field, disabling RMBTweak.");
			Main.disableRMBTweak = true;
		} else {
			guiContainerClass.storeField(Constants.FIELDE_FORGE_NAME, field);

			field = getField(GuiContainer.class,
					getObfuscatedName(Constants.FIELDq_MCP_NAME, Constants.FIELDq_FORGE_NAME, Constants.FIELDq_NAME));

			if (field == null) {
				Logger.Log("Failed to retreive the q field, disabling RMBTweak.");
				Main.disableRMBTweak = true;
			} else {
				guiContainerClass.storeField(Constants.FIELDq_FORGE_NAME, field);
			}
		}

		return true;
	}

	public static boolean is(Object object, String name) {
		return object.getClass().getSimpleName().equals(name);
	}

	public static boolean doesClassExist(String name) {
		Class clazz = getClass(name);
		return clazz != null;
	}

	public static Class getClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			;
		}

		return null;
	}

	public static Field getField(Class clazz, String name) {
		try {
			Field field;

			try {
				field = clazz.getField(name);
			} catch (Exception e) {
				field = null;
			}

			if (field == null) {
				field = clazz.getDeclaredField(name);
			}

			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			Logger.Log("Could not retrieve field \"" + name + "\" from class \"" + clazz.getName() + "\".");

			if (Config.debug) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static Field getFinalField(Class clazz, String name) {
		try {
			Field field;

			try {
				field = clazz.getField(name);
			} catch (Exception e) {
				field = null;
			}

			if (field == null) {
				field = clazz.getDeclaredField(name);
			}

			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);

			modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);

			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			Logger.Log("Could not retrieve field \"" + name + "\" from class \"" + clazz.getName() + "\"");

			if (Config.debug) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static Method getMethod(Class<?> clazz, String name, Class... args) {
		try {
			Method method;

			try {
				method = clazz.getMethod(name, args);
			} catch (Exception e) {
				method = null;
			}

			if (method == null) {
				if ((args != null) && (args.length != 0)) {
					method = clazz.getDeclaredMethod(name, args);
				} else {
					method = clazz.getDeclaredMethod(name);
				}
			}

			method.setAccessible(true);
			return method;
		} catch (Exception e) {
			Logger.Log("Could not retrieve method \"" + name + "\" from class \"" + clazz.getName()	+ "\"");

			if (Config.debug) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static String methodToString(Method method) {
		return Modifier.toString(method.getModifiers()) + " " + ((method.getReturnType() != null) ? method
				.getReturnType().getName() : "void") + " " + method.getName();
	}

	public static String getObfuscatedName(String mcpName, String forgeName, String originalName) {
		if (checkObfuscation) {
			checkObfuscation();
		}

		if (obfuscation == Obfuscation.MCP)
			return mcpName;
		else if (obfuscation == Obfuscation.FORGE)
			return forgeName;
		else
			return originalName;
	}

	public static void checkObfuscation() {
		checkObfuscation = false;

		Logger.Log("Obfuscation check, please ignore the following errors (if they come up).");

		if (getField(GuiContainer.class, Constants.FIELDE_MCP_NAME) != null)
			obfuscation = Obfuscation.MCP;
		else if (getField(GuiContainer.class, Constants.FIELDE_FORGE_NAME) != null)
			obfuscation = Obfuscation.FORGE;
		else
			obfuscation = Obfuscation.VANILLA;

		Logger.Log("Obfuscation check completed.");
	}
}
