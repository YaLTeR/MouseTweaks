package yalter.mousetweaks;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class Config {
	protected static final Properties defaultValues = new Properties();
	protected String fileName;

	public boolean rmbTweak = true;
	public boolean lmbTweakWithItem = true;
	public boolean lmbTweakWithoutItem = true;
	public boolean wheelTweak = true;
	public WheelSearchOrder wheelSearchOrder = WheelSearchOrder.LAST_TO_FIRST;
	public WheelScrollDirection wheelScrollDirection = WheelScrollDirection.NORMAL;
	public Set<OnTickMethod> onTickMethodOrder = new LinkedHashSet<OnTickMethod>(); // The order has to be preserved.
	public MouseHandling mouseHandling = MouseHandling.SIMPLE;
	public static boolean debug = false;

	public Config(String fileName) {
		this.fileName = fileName;
	}

	public void read() {
		Properties properties = new Properties(defaultValues);

		try {
			FileReader configReader = new FileReader(fileName);
			properties.load(configReader);
			configReader.close();
		} catch (FileNotFoundException ignored) {
		} catch (IOException e) {
			Logger.Log("Failed to read the config file: " + fileName);
			e.printStackTrace();
		}

		rmbTweak = parseIntOrDefault(properties.getProperty(Constants.CONFIG_RMB_TWEAK), 1) != 0;
		lmbTweakWithItem = parseIntOrDefault(properties.getProperty(Constants.CONFIG_LMB_TWEAK_WITH_ITEM), 1) != 0;
		lmbTweakWithoutItem = parseIntOrDefault(properties.getProperty(Constants.CONFIG_LMB_TWEAK_WITHOUT_ITEM), 1)
		                      != 0;
		wheelTweak = parseIntOrDefault(properties.getProperty(Constants.CONFIG_WHEEL_TWEAK), 1) != 0;
		wheelSearchOrder
			= WheelSearchOrder.fromId(parseIntOrDefault(properties.getProperty(Constants.CONFIG_WHEEL_SEARCH_ORDER),
			                                            1));
		wheelScrollDirection
			=
			WheelScrollDirection.fromId(parseIntOrDefault(properties.getProperty(Constants.CONFIG_WHEEL_SCROLL_DIRECTION),
			                                                0));
		onTickMethodOrderFromString(properties.getProperty(Constants.CONFIG_ONTICK_METHOD_ORDER));
		mouseHandling = MouseHandling.fromId(parseIntOrDefault(properties.getProperty(Constants.CONFIG_MOUSE_HANDLING), 0));
		debug = parseIntOrDefault(properties.getProperty(Constants.CONFIG_DEBUG), 0) != 0;
	}

	private static int parseIntOrDefault(String s, int defaultValue) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public void save() {
		try {
			File config = new File(fileName);
			boolean existed = config.exists();
			File parentDir = config.getParentFile();
			if (!parentDir.exists())
				parentDir.mkdirs();

			FileWriter configWriter = new FileWriter(config);

			writeBoolean(configWriter, Constants.CONFIG_RMB_TWEAK, rmbTweak);
			writeBoolean(configWriter, Constants.CONFIG_LMB_TWEAK_WITH_ITEM, lmbTweakWithItem);
			writeBoolean(configWriter, Constants.CONFIG_LMB_TWEAK_WITHOUT_ITEM, lmbTweakWithoutItem);
			writeBoolean(configWriter, Constants.CONFIG_WHEEL_TWEAK, wheelTweak);
			writeString(configWriter, Constants.CONFIG_WHEEL_SEARCH_ORDER, String.valueOf(wheelSearchOrder.ordinal()));
			writeString(configWriter,
			            Constants.CONFIG_WHEEL_SCROLL_DIRECTION,
			            String.valueOf(wheelScrollDirection.ordinal()));
			writeString(configWriter, Constants.CONFIG_ONTICK_METHOD_ORDER, onTickMethodOrderString());
			writeString(configWriter, Constants.CONFIG_MOUSE_HANDLING, String.valueOf(mouseHandling.ordinal()));
			writeBoolean(configWriter, Constants.CONFIG_DEBUG, debug);

			configWriter.close();

			if (!existed)
				Logger.Log("Created the config file.");
		} catch (IOException e) {
			Logger.Log("Failed to write the config file: " + fileName);
			e.printStackTrace();
		}
	}

	private static void writeString(FileWriter configWriter, String name, String value) throws IOException {
		configWriter.write(name + '=' + value + '\n');
	}

	private static void writeBoolean(FileWriter configWriter, String name, boolean value) throws IOException {
		writeString(configWriter, name, value ? "1" : "0");
	}

	public String onTickMethodOrderString() {
		StringBuilder result = new StringBuilder();
		for (OnTickMethod method : onTickMethodOrder) {
			if (result.length() > 0)
				result.append(", ");

			switch (method) {
				case FORGE:
					result.append(Constants.ONTICKMETHOD_FORGE_NAME);
					break;

				case LITELOADER:
					result.append(Constants.ONTICKMETHOD_LITELOADER_NAME);
					break;
			}
		}
		return result.toString();
	}

	public void onTickMethodOrderFromString(String string) {
		onTickMethodOrder.clear();
		String[] onTickMethods = string.trim().split("[\\s]*,[\\s]*");
		for (String method : onTickMethods) {
			if (Constants.ONTICKMETHOD_FORGE_NAME.equalsIgnoreCase(method))
				onTickMethodOrder.add(OnTickMethod.FORGE);
			else if (Constants.ONTICKMETHOD_LITELOADER_NAME.equalsIgnoreCase(method))
				onTickMethodOrder.add(OnTickMethod.LITELOADER);
		}

		// Make sure we have one of each.
		onTickMethodOrder.add(OnTickMethod.FORGE);
		onTickMethodOrder.add(OnTickMethod.LITELOADER);
	}

	static {
		defaultValues.setProperty(Constants.CONFIG_RMB_TWEAK, "1");
		defaultValues.setProperty(Constants.CONFIG_LMB_TWEAK_WITH_ITEM, "1");
		defaultValues.setProperty(Constants.CONFIG_LMB_TWEAK_WITHOUT_ITEM, "1");
		defaultValues.setProperty(Constants.CONFIG_WHEEL_TWEAK, "1");
		defaultValues.setProperty(Constants.CONFIG_WHEEL_SEARCH_ORDER, "1");
		defaultValues.setProperty(Constants.CONFIG_WHEEL_SCROLL_DIRECTION, "0");
		defaultValues.setProperty(Constants.CONFIG_ONTICK_METHOD_ORDER, "Forge, LiteLoader");
		defaultValues.setProperty(Constants.CONFIG_MOUSE_HANDLING, "0");
		defaultValues.setProperty(Constants.CONFIG_DEBUG, "0");
	}
}
