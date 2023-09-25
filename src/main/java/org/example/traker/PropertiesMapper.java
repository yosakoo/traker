package org.example.traker;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

public class PropertiesMapper {

    private final BufferedReader reader;
    private static final Set<Class<?>> AVAILABLE_TYPES = Set.of(String.class, int.class, double.class, String[].class, int[].class, double[].class);
    Map<Class<?>, Function<String, Object>> converters = new HashMap<>();

    {
        converters.put(String.class, s -> s);
        converters.put(int.class, Integer::valueOf);
        converters.put(double.class, Double::valueOf);
        converters.put(String[].class, s -> s.split(","));
        converters.put(int[].class, s -> Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray());
        converters.put(double[].class, s -> Arrays.stream(s.split(",")).mapToDouble(Double::parseDouble).toArray());
    }

    public PropertiesMapper(BufferedReader reader) {
        this.reader = reader;
    }

    public <T> T readProperties(Class<T> clazz) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Field[] fields = clazz.getDeclaredFields();
        assertAvailableFieldTypes(fields);
        Map<String, String> properties = retrieveProperties();

        T obj = clazz.getConstructor(new Class[0]).newInstance();

        fillWithProperties(fields, properties, obj);

        return obj;
    }

    private void assertAvailableFieldTypes(Field[] fields) {
        Arrays.stream(fields).forEach(this::assertAvailableFieldType);
    }

    private void assertAvailableFieldType(Field field) {
        if(!AVAILABLE_TYPES.contains(field.getType()))
            throw new RuntimeException("Not an available type");
    }

    private Map<String, String> retrieveProperties() throws IOException {
        Map<String, String> props = new HashMap<>();
        String line;

        while ((line = reader.readLine()) != null) {
            String[] pair = line.split("=");
            props.put(pair[0], pair[1]);
        }

        return props;
    }

    private void fillWithProperties(Field[] fields, Map<String, String> props, Object obj) throws IllegalAccessException {
        for(Field field : fields) {
            if(!props.containsKey(field.getName()))
                throw new RuntimeException("Property " + field.getName() + " not found");

            field.setAccessible(true);
            String strValue = props.get(field.getName()).replaceAll("\\s", "");
            Object value = converters.get(field.getType()).apply(strValue);
            field.set(obj, value);
        }
    }
}
