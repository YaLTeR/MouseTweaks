package yalter.mousetweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.inventory.Slot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Reflection {
	public static ReflectionCache guiContainerClass;
	public static ReflectionCache liteLoaderClass;
	public static ReflectionCache liteModMouseTweaks;
	public static ReflectionCache gameSettings;
	public static ReflectionCache minecraft;

	public static ReflectionCache forestry;
	public static ReflectionCache codechickencore;
	public static ReflectionCache NEI;

	public enum Obfuscation {MCP, FORGE, VANILLA}
	private static Obfuscation obfuscation;
	private static boolean checkObfuscation = true;

	public static boolean reflectGuiContainer() {
		guiContainerClass = new ReflectionCache();
		guiContainerClass.storeClass("GuiContainer", GuiContainer.class);

		Method isMouseOverSlot = getMethod(GuiContainer.class,
				getObfuscatedName(Constants.ISMOUSEOVERSLOT_MCP_NAME, Constants.ISMOUSEOVERSLOT_FORGE_NAME, Constants.ISMOUSEOVERSLOT_NAME), Slot.class, int.class,
				int.class);

		if (isMouseOverSlot == null) {
			Logger.Log("Failed to get isMouseOverSlot method, quitting");
			return false;
		}

		guiContainerClass.storeMethod(Constants.ISMOUSEOVERSLOT_FORGE_NAME, isMouseOverSlot);

		Field field;
		field = getField(GuiContainer.class,
				getObfuscatedName(Constants.FIELDE_MCP_NAME, Constants.FIELDE_FORGE_NAME, Constants.FIELDE_NAME));

		if (field == null) {
			Logger.Log("Failed to retrieve the E field, disabling RMBTweak");
			Main.DisableRMBTweak = true;
		} else {
			guiContainerClass.storeField(Constants.FIELDE_FORGE_NAME, field);

			field = getField(GuiContainer.class,
					getObfuscatedName(Constants.FIELDq_MCP_NAME, Constants.FIELDq_FORGE_NAME, Constants.FIELDq_NAME));

			if (field == null) {
				Logger.Log("Failed to retreive the q field, disabling RMBTweak");
				Main.DisableRMBTweak = true;
			} else {
				guiContainerClass.storeField(Constants.FIELDq_FORGE_NAME, field);
			}
		}

		return true;
	}

	public static boolean registerLiteModListenerInLiteLoader() {
		liteLoaderClass = new ReflectionCache();

		Class lL = getClass("com.mumfrey.liteloader.core.LiteLoader");
		if (lL != null) {
			liteLoaderClass.storeClass("LiteLoader", lL);

			liteLoaderClass.available = true;

			Class llEvents = getClass("com.mumfrey.liteloader.core.Events");
			if (llEvents != null) {
				liteLoaderClass.storeClass("Events", llEvents);

				Field llEventsField = getField(lL, "events");
				if (llEventsField != null) {
					liteLoaderClass.storeField("events", llEventsField);

					Class lM = getClass("com.mumfrey.liteloader.LiteMod");
					if (lM != null) {
						liteLoaderClass.storeClass("LiteMod", lM);

						Method addListener = getMethod(llEvents, "addListener", lM);
						if (addListener != null) {
							liteLoaderClass.storeMethod("addListener", addListener);

							Method getInstance = getMethod(lL, "getInstance");
							if (getInstance != null) {
								liteLoaderClass.storeMethod("getInstance", getInstance);

								Object liteLoaderInstance = liteLoaderClass.invokeStaticMethod("LiteLoader", "getInstance");
								if (liteLoaderInstance != null) {
									Object llEventsInstance = liteLoaderClass.getFieldValue("events", liteLoaderInstance);
									if (llEventsInstance != null) {
										Class mouseTweaks = getClass("yalter.mousetweaks.loaders.LiteModMouseTweaks");
										if (mouseTweaks != null) {
											liteModMouseTweaks = new ReflectionCache();
											liteModMouseTweaks.storeClass("LiteModMouseTweaks", mouseTweaks);

											Method getLMMTInstance = getMethod(mouseTweaks, "getInstance");
											if (getLMMTInstance != null) {
												liteModMouseTweaks.storeMethod("getInstance", getLMMTInstance);

												Object LMMTInstance = liteModMouseTweaks.invokeStaticMethod("LiteModMouseTweaks", "getInstance");
												if (LMMTInstance != null) {
													liteLoaderClass.invokeMethod(llEventsInstance, "addListener", LMMTInstance);

													liteLoaderClass.compatible = true;
													Logger.Log("Registered the LiteModMouseTweaks' listener in LiteLoader.");

													return true;
												} else {
													Logger.Log("Could not get the instance from the getInstance method of LiteModMouseTweaks! Have you modified the class?");
												}
											} else {
												Logger.Log("Could not get the getInstance method from LiteModMouseTweaks! Have you modified the class?");
											}
										} else {
											Logger.Log("Could not get the LiteModMouseTweaks class! Have you deleted it accidentally?");
										}
									}
								} else {
									Logger.Log("Failed to retrieve the LiteLoader instance!");
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	public static boolean reflectOptifine() {
		gameSettings = new ReflectionCache();
		gameSettings.storeClass("GameSettings", GameSettings.class);

		Field optifineProfilerEnabled = getField(GameSettings.class, "ofProfiler");
		if (optifineProfilerEnabled != null) {
			gameSettings.storeField("ofProfiler", optifineProfilerEnabled);

			Logger.Log("Optifine is installed.");
			return true;
		}

		Logger.Log("Optifine is not installed.");
		return false;
	}

	public static boolean replaceProfiler() {
		minecraft = new ReflectionCache();
		minecraft.storeClass("Minecraft", Minecraft.class);

		Field profilerField = getFinalField(Minecraft.class, getObfuscatedName(Constants.MCPROFILER_MCP_NAME, Constants.MCPROFILER_FORGE_NAME, Constants.MCPROFILER_NAME));
		if (profilerField != null) {
			minecraft.storeField("mcProfiler", profilerField);
			return minecraft.setFieldValue(Minecraft.getMinecraft(), "mcProfiler", new ProfilerCustom());
		}

		Logger.Log("Failed to get the Minecraft profiler field!");
		return false;
	}

	public static boolean reflectForestry() {
		forestry = new ReflectionCache();

		Class guiForestryClass = getClass("forestry.core.gui.GuiForestry");
		if (guiForestryClass != null) {
			forestry.storeClass("GuiForestry", guiForestryClass);
			forestry.available = true;

			Field inventorySlotsField = getField(guiForestryClass, "inventorySlots");
			if (inventorySlotsField != null) {
				forestry.storeField("inventorySlots", inventorySlotsField);

				Method getSlotAtPositionMethod = getMethod(guiForestryClass, "getSlotAtPosition", int.class, int.class);
				if (getSlotAtPositionMethod != null) {
					forestry.storeMethod("getSlotAtPosition", getSlotAtPositionMethod);

					Method handleMouseClickMethod = getMethod(guiForestryClass, "handleMouseClick", Slot.class, int.class, int.class, int.class);
					if (handleMouseClickMethod != null) {
						forestry.storeMethod("handleMouseClick", handleMouseClickMethod);

						forestry.compatible = true;

						// Class containerForestryClass = getClass( "forestry.core.gui.ContainerForestry" );
						// if ( containerForestryClass != null ) {
						// forestry.storeClass( "ContainerForestry", containerForestryClass );
						//
						// Field slotCountField = getField( containerForestryClass, "slotCount" );
						// if ( slotCountField != null ) {
						// forestry.storeField( "slotCount", slotCountField );
						//
						// forestry.compatible = true;
						// }
						// }
					}
				}
			}
		}

		return forestry.compatible;
	}

	public static boolean reflectCodeChickenCore() {
		codechickencore = new ReflectionCache();

		Class guiContainerWidgetClass = getClass("codechicken.core.inventory.GuiContainerWidget");
		if (guiContainerWidgetClass != null) {
			codechickencore.storeClass("GuiContainerWidget", guiContainerWidgetClass);

			codechickencore.available = true;
			codechickencore.compatible = true;
		}

		return codechickencore.compatible;
	}

	public static boolean reflectNEI() {
		NEI = new ReflectionCache();

		Class guiRecipeClass = getClass("codechicken.nei.recipe.GuiRecipe");
		if (guiRecipeClass != null) {
			NEI.storeClass("GuiRecipe", guiRecipeClass);
			NEI.available = true;

			Class guiEnchantmentModifierClass = getClass("codechicken.nei.GuiEnchantmentModifier");
			if (guiEnchantmentModifierClass != null) {
				NEI.storeClass("GuiEnchantmentModifier", guiEnchantmentModifierClass);

				NEI.compatible = true;
			}
		}

		return NEI.compatible;
	}

	public static boolean is(Object object, String name) {
		return object.getClass().getSimpleName() == name;
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
			Field field = null;

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
			if (name != "ofProfiler") {
				Logger.Log("Could not retrieve field \"" + name + "\" from class \"" + clazz.getName()
						+ "\"");

				if (Main.Debug == 1) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	public static Field getFinalField(Class clazz, String name) {
		try {
			Field field = null;

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
			Logger.Log("Could not retrieve field \"" + name + "\" from class \"" + clazz.getName()
					+ "\"");

			if (Main.Debug == 1) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static Method getMethod(Class<?> clazz, String name, Class... args) {
		try {
			Method method = null;

			try {
				method = clazz.getMethod(name, args);
			} catch (Exception e) {
				method = null;
			}

			if (method == null) {
				if ((args != null) & (args.length != 0)) {
					method = clazz.getDeclaredMethod(name, args);
				} else {
					method = clazz.getDeclaredMethod(name, new Class[0]);
				}
			}

			method.setAccessible(true);
			return method;
		} catch (Exception e) {
			Logger.Log("Could not retrieve method \"" + name + "\" from class \"" + clazz.getName()
					+ "\"");

			if (Main.Debug == 1) {
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

		if (getField(Minecraft.class, Constants.MCPROFILER_MCP_NAME) != null)
			obfuscation = Obfuscation.MCP;
		else if (getField(Minecraft.class, Constants.MCPROFILER_FORGE_NAME) != null)
			obfuscation = Obfuscation.FORGE;
		else
			obfuscation = Obfuscation.VANILLA;

		Logger.Log("Obfuscation check completed.");
	}
}
