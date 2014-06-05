package yalter.mousetweaks;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Config {
	private String fileName;
	private Properties properties = new Properties();

	public Config(String fileName) {
		this.fileName = fileName;
	}

	public boolean readConfig() {
		properties.clear();

		try {
			FileReader configReader = new FileReader(fileName);
			properties.load(configReader);
			configReader.close();

			if (properties.size() == 0)
				return false;

			return true;
		} catch (FileNotFoundException ignored) {
		} catch (IOException e) {
			Logger.Log("Failed to read the config file: " + fileName);
			e.printStackTrace();
		}

		return false;
	}

	public boolean saveConfig() {
		try {
			FileWriter configWriter = new FileWriter(fileName);
			properties.store(configWriter, null);
			configWriter.close();

			return true;
		} catch (IOException e) {
			Logger.Log("Failed to write the config file: " + fileName);
			e.printStackTrace();
		}

		return false;
	}

	public String getOrCreateProperty(String name, String defaultValue) {
		if (properties.containsKey(name))
			return properties.getProperty(name);
		else
			properties.setProperty(name, defaultValue);

		return defaultValue;
	}

	public int getOrCreateIntProperty(String name, int defaultValue) {
		return Integer.parseInt(getOrCreateProperty(name, String.valueOf(defaultValue)));
	}

	public void setProperty(String name, String value) {
		properties.setProperty(name, value);
	}

	public void setIntProperty(String name, int value) {
		setProperty(name, String.valueOf(value));
	}
}
