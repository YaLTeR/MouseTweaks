package MouseTweaks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ReflectedClass {
    private Class reflectedClass;

    public boolean available = false;
    public boolean compatible = false;
    public HashMap methods = new HashMap();
    public HashMap fields = new HashMap();

    public ReflectedClass() {
    }

    public Object getFieldValue(String name, Object obj) {
        try {
            if (fields.containsKey(name)) {
                Field field = (Field) fields.get(name);
                return field.get(obj);
            }

            Logger.Log("No such field: " + name);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.Log("Failed to get a value of field: " + name);
        }

        return null;
    }

    public boolean setFieldValue(Object obj, String name, Object value) {
        try {
            if (fields.containsKey(name)) {
                Field field = (Field) fields.get(name);
                field.set(obj, value);

                return true;
            }

            Logger.Log("No such field: " + name);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.Log("Failed to set a value of field: " + name);
        }

        return false;
    }

    public Object invokeMethod(Object obj, String name, Object... args) {
        try {
            if (methods.containsKey(name)) {
                Method method = (Method) methods.get(name);

                if (args != null)
                    return method.invoke(obj, args);

                return method.invoke(obj, new Object[0]);
            }

            Logger.Log("No such method: " + name);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.Log("Failed to invoke method: " + name);
        }

        return null;
    }

    public Object invokeMethod(String name, Object... args) {
        return invokeMethod(reflectedClass, name, args);
    }

    public void storeClass(Class clazz) {
        reflectedClass = clazz;
    }

    public void storeMethod(String name, Method method) {
        methods.put(name, method);
    }

    public void storeField(String name, Field field) {
        fields.put(name, field);
    }
}
