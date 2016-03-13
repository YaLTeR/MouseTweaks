package yalter.mousetweaks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ReflectionCache {
	public boolean available = false;
	public boolean compatible = false;
	protected HashMap<String, Class> classes = new HashMap<String, Class>();
	protected HashMap<String, Method> methods = new HashMap<String, Method>();
	protected HashMap<String, Field> fields = new HashMap<String, Field>();

	public ReflectionCache() {
	}

	public Object getFieldValue(String name, Object obj) {
		try {
			if (fields.containsKey(name)) {
				Field field = fields.get(name);
				return field.get(obj);
			}

			Logger.Log("No such field: " + name + ".");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.Log("Failed to get a value of field: " + name + ".");
		}

		return null;
	}

	public boolean setFieldValue(Object obj, String name, Object value) {
		try {
			if (fields.containsKey(name)) {
				Field field = fields.get(name);
				field.set(obj, value);

				return true;
			}

			Logger.Log("No such field: " + name + ".");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.Log("Failed to set a value of field: " + name + ".");
		}

		return false;
	}

	public Object invokeMethod(Object obj, String name, Object... args) {
		try {
			if (methods.containsKey(name)) {
				Method method = methods.get(name);

				if (args != null)
					return method.invoke(obj, args);

				return method.invoke(obj, new Object[0]);
			}

			Logger.Log("No such method: " + name + ".");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.Log("Failed to invoke method: " + name + ".");
		}

		return null;
	}

	public Object invokeStaticMethod(String className, String name, Object... args) {
		if (classes.containsKey(className)) {
			Class clazz = classes.get(className);

			return invokeMethod(clazz, name, args);
		}

		return null;
	}

	public boolean isInstance(String className, Object obj) {
		if (classes.containsKey(className)) {
			Class clazz = classes.get(className);
			return clazz.isInstance(obj);
		}

		return false;
	}

	public void storeClass(String name, Class clazz) {
		classes.put(name, clazz);
	}

	public void storeMethod(String name, Method method) {
		methods.put(name, method);
	}

	public void storeField(String name, Field field) {
		fields.put(name, field);
	}
}
