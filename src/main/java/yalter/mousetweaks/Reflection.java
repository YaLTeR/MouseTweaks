package yalter.mousetweaks;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Reflection {
	private static Obfuscation obfuscation;
	private static boolean checkObfuscation = true;

	private static HashMap<Class<?>, Method> HMCCache = new HashMap<Class<?>, Method>();

	public static ReflectionCache guiContainerClass;

	static void reflectGuiContainer() {
		Logger.Log("Reflecting GuiContainer...");

		guiContainerClass = new ReflectionCache();

		try {
			Field f = getField(ContainerScreen.class, getObfuscatedName(Constants.IGNOREMOUSEUP_NAME));
			guiContainerClass.storeField(Constants.IGNOREMOUSEUP_NAME.forgeName, f);
		} catch (NoSuchFieldException e) {
			Logger.Log("Could not retrieve GuiContainer.ignoreMouseUp.");
			guiContainerClass = null;
			return;
		}

		try {
			Field f = getField(ContainerScreen.class, getObfuscatedName(Constants.DRAGSPLITTING_NAME));
			guiContainerClass.storeField(Constants.DRAGSPLITTING_NAME.forgeName, f);
		} catch (NoSuchFieldException e) {
			Logger.Log("Could not retrieve GuiContainer.dragSplitting.");
			guiContainerClass = null;
			return;
		}

		try {
			Field f = getField(ContainerScreen.class, getObfuscatedName(Constants.DRAGSPLITTINGBUTTON_NAME));
			guiContainerClass.storeField(Constants.DRAGSPLITTINGBUTTON_NAME.forgeName, f);
		} catch (NoSuchFieldException e) {
			Logger.Log("Could not retrieve GuiContainer.dragSplittingButton.");
			guiContainerClass = null;
			return;
		}

		try {
			Method m = getMethod(ContainerScreen.class,
			                     getObfuscatedName(Constants.GETSELECTEDSLOT_NAME),
			                     double.class,
			                     double.class);
			guiContainerClass.storeMethod(Constants.GETSELECTEDSLOT_NAME.forgeName, m);
		} catch (NoSuchMethodException e) {
			Logger.Log("Could not retrieve GuiContainer.getSelectedSlot().");
			guiContainerClass = null;
			return;
		}

		Logger.Log("Success.");
	}

	public static Method getHMCMethod(ContainerScreen object) {
		if (HMCCache.containsKey(object.getClass())) {
			return HMCCache.get(object.getClass());
		}

		try {
			Method method = searchMethod(object.getClass(),
			                             getObfuscatedName(Constants.HANDLEMOUSECLICK_NAME),
			                             Slot.class,
			                             int.class,
			                             int.class,
			                             ClickType.class);

			Logger.DebugLog("Found handleMouseClick() for " + object.getClass().getSimpleName() + ", caching.");

			HMCCache.put(object.getClass(), method);
			return method;
		} catch (NoSuchMethodException e) {
			CrashReport crashreport = CrashReport.makeCrashReport(e,
			                                                      "MouseTweaks could not find handleMouseClick() in a "
			                                                      + "ContainerScreen.");
			throw new ReportedException(crashreport);
		}
	}

	public static Method getHMCMethod(Object object) {
		if (HMCCache.containsKey(object.getClass())) {
			return HMCCache.get(object.getClass());
		}

		try {
			Method method = searchMethod(object.getClass(),
			                             getObfuscatedName(Constants.HANDLEMOUSECLICK_NAME),
			                             Slot.class,
			                             int.class,
			                             int.class,
			                             ClickType.class);

			Logger.DebugLog("Found handleMouseClick() for " + object.getClass().getSimpleName() + ", caching.");

			HMCCache.put(object.getClass(), method);
			return method;
		} catch (NoSuchMethodException e) {
			Logger.DebugLog("Could not find handleMouseClick() for "
			                + object.getClass().getSimpleName()
			                + ", using windowClick().");
			return null;
		}
	}

	static boolean doesClassExist(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
		Field field;

		try {
			field = clazz.getField(name);
		} catch (NoSuchFieldException e) {
			field = clazz.getDeclaredField(name);
		}

		field.setAccessible(true);
		return field;
	}

	private static Method getMethod(Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException {
		Method method;

		try {
			method = clazz.getMethod(name, args);
		} catch (NoSuchMethodException e) {
			method = clazz.getDeclaredMethod(name, args);
		}

		method.setAccessible(true);
		return method;
	}

	private static Method searchMethod(Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException {
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
			getField(ContainerScreen.class, Constants.IGNOREMOUSEUP_NAME.mcpName);
			obfuscation = Obfuscation.MCP;
		} catch (NoSuchFieldException e) {
			try {
				getField(ContainerScreen.class, Constants.IGNOREMOUSEUP_NAME.forgeName);
				obfuscation = Obfuscation.FORGE;
			} catch (NoSuchFieldException ex) {
				obfuscation = Obfuscation.VANILLA;
			}
		}

		Logger.Log("Detected obfuscation: " + obfuscation + ".");
	}
}
