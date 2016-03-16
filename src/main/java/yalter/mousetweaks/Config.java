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
	public Set<OnTickMethod> onTickMethodOrder = new LinkedHashSet<OnTickMethod>(); // The order has to be preserved.
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

		rmbTweak = Integer.parseInt(properties.getProperty(Constants.CONFIG_RMB_TWEAK)) != 0;
		lmbTweakWithItem = Integer.parseInt(properties.getProperty(Constants.CONFIG_LMB_TWEAK_WITH_ITEM)) != 0;
		lmbTweakWithoutItem = Integer.parseInt(properties.getProperty(Constants.CONFIG_LMB_TWEAK_WITHOUT_ITEM)) != 0;
		wheelTweak = Integer.parseInt(properties.getProperty(Constants.CONFIG_WHEEL_TWEAK)) != 0;
		wheelSearchOrder = WheelSearchOrder.fromId(Integer.parseInt(properties.getProperty(Constants.CONFIG_WHEEL_SEARCH_ORDER)));
		debug = Integer.parseInt(properties.getProperty(Constants.CONFIG_DEBUG)) != 0;
		onTickMethodOrderFromString(properties.getProperty(Constants.CONFIG_ONTICK_METHOD_ORDER));

		save();
	}

	public void save() {
		Properties properties = new Properties();
		properties.setProperty(Constants.CONFIG_RMB_TWEAK, rmbTweak ? "1" : "0");
		properties.setProperty(Constants.CONFIG_LMB_TWEAK_WITH_ITEM, lmbTweakWithItem ? "1" : "0");
		properties.setProperty(Constants.CONFIG_LMB_TWEAK_WITHOUT_ITEM, lmbTweakWithoutItem ? "1" : "0");
		properties.setProperty(Constants.CONFIG_WHEEL_TWEAK, wheelTweak ? "1" : "0");
		properties.setProperty(Constants.CONFIG_WHEEL_SEARCH_ORDER, String.valueOf(wheelSearchOrder.ordinal()));
		properties.setProperty(Constants.CONFIG_DEBUG, debug ? "1" : "0");
		properties.setProperty(Constants.CONFIG_ONTICK_METHOD_ORDER, onTickMethodOrderString());

		try {
			File config = new File(fileName);
			boolean existed = config.exists();
			File parentDir = config.getParentFile();
			if (!parentDir.exists())
				parentDir.mkdirs();

			FileWriter configWriter = new FileWriter(config);
			properties.store(configWriter, null);
			configWriter.close();

			if (!existed)
				Logger.Log("Created the config file.");
		} catch (IOException e) {
			Logger.Log("Failed to write the config file: " + fileName);
			e.printStackTrace();
		}
	}

	public String onTickMethodOrderString() {
		String result = "";
		for (OnTickMethod method : onTickMethodOrder) {
			if (!result.isEmpty())
				result += ", ";

			switch (method) {
				case FORGE:
					result += Constants.ONTICKMETHOD_FORGE_NAME;
					break;

				case LITELOADER:
					result += Constants.ONTICKMETHOD_LITELOADER_NAME;
					break;
			}
		}
		return result;
	}

	public void onTickMethodOrderFromString(String string) {
		onTickMethodOrder.clear();
		String onTickMethods[] = string.trim().split("[\\s]*,[\\s]*");
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
		defaultValues.setProperty(Constants.CONFIG_ONTICK_METHOD_ORDER, "Forge, LiteLoader");
		defaultValues.setProperty(Constants.CONFIG_DEBUG, "0");
	}
}
