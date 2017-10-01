package util.settings;

import gui.forms.GUIMain;
import util.Utils;

import java.awt.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * This class was made for the standardization of Settings found in the Settings class.
 * <p>
 * We can generify most of the settings by setting them to their types,
 * then converting them from the Properties Strings back into their
 * original class types, using the Converter class.
 * <p>
 * Settings are now just one line, unless there's a need to add a ChangeListener.
 * <p>
 * Adding a ChangeListener to the setting allows for callbacks
 * to be used elsewhere when the setting is changed.
 * <p>
 * A primary example can be when the settings are loaded,
 * the callbacks are used to update the main GUI to reflect the updated settings.
 */
public class Setting<E> {

    private E actualValue, defaultValue;
    private String nameInFile;
    private Class<E> type;
    private List<ChangeListener<E>> listeners;

    /**
     * Constructor for the setting.
     *
     * @param nameInFile   The name as stored in the Properties file.
     * @param defaultValue The default value of the setting.
     * @param type         The class that the value of the setting derives from.
     */
    public Setting(String nameInFile, E defaultValue, Class<E> type) {
        this.nameInFile = nameInFile;
        this.defaultValue = defaultValue;
        this.type = type;
        this.listeners = new ArrayList<>();
    }

    public void addChangeListener(ChangeListener<E> listener) {
        this.listeners.add(listener);
    }

    public E getValue() {
        return actualValue == null ? defaultValue : actualValue;
    }

    public synchronized void setValue(E value) {
        boolean changed = !getValue().equals(value);
        actualValue = value;
        if (!listeners.isEmpty() && changed) //only fire if there's a change
            listeners.forEach(cl -> cl.onChange(value));
    }

    public synchronized void load(Properties p) {
        String prop = p.getProperty(nameInFile);
        setValue(prop != null ? Converter.convert(prop, type) : defaultValue);
    }

    public synchronized void save(Properties p) {
        //allows for temporary settings (i.e. stMuted) to not be saved
        if (!"".equals(nameInFile))
            p.setProperty(nameInFile, getValue().toString());
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    /**
     * An event to be fired if the setting actually changed.
     *
     * @param <E> Rarely used, but the type that concords with the Setting.
     */
    public interface ChangeListener<E> {
        void onChange(E value);
    }

    /**
     * Credit to http://balusc.blogspot.com/2007/08/generic-object-converter.html
     */
    @SuppressWarnings("unused")
    private static class Converter {

        private static final Map<String, Method> CONVERTERS = new HashMap<>();

        static {
            // Preload converters.
            Method[] methods = Converter.class.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getParameterTypes().length == 1) {
                    // Converter should accept 1 argument. This skips the convert() method.
                    CONVERTERS.put(method.getParameterTypes()[0].getName() + "_"
                            + method.getReturnType().getName(), method);
                }
            }
        }

        /**
         * Convert the given object value to the given class.
         *
         * @param from The object value to be converted.
         * @param to   The type class which the given object should be converted to.
         * @return The converted object value.
         */
        public static <T> T convert(Object from, Class<T> to) {
            // Null is just null.
            if (from == null) {
                return null;
            }

            // Can we cast? Then just do so.
            if (to.isAssignableFrom(from.getClass())) {
                return to.cast(from);
            }

            // Lookup the suitable converter.
            String converterId = from.getClass().getName() + "_" + to.getName();
            Method converter = CONVERTERS.get(converterId);
            if (converter != null) {
                // Convert the value.
                try {
                    return to.cast(converter.invoke(to, from));
                } catch (Exception e) {
                    GUIMain.log("Cannot convert from " + from.getClass().getName() + " to " + to.getName()
                            + ". Conversion failed with " + e.getMessage());
                    return null;
                }
            } else {
                GUIMain.log("Cannot convert setting! Converter not found for " + converterId);
                return null;
            }
        }

        //Converter Methods (used in Reflection)

        public static Boolean stringToBoolean(String value) {
            return Boolean.valueOf(value);
        }

        public static Integer stringToInteger(String value) {
            return Integer.valueOf(value);
        }

        public static Float stringToFloat(String value) {
            return Float.valueOf(value);
        }

        public static Font stringToFont(String value) {
            return Utils.stringToFont(value);
        }

        public static URL stringToURL(String value) throws MalformedURLException {
            return new URL(value);
        }
    }
}