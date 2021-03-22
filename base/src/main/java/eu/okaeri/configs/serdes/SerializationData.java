package eu.okaeri.configs.serdes;

import eu.okaeri.configs.Configurer;
import eu.okaeri.configs.schema.GenericsDeclaration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SerializationData {

    private Map<String, Object> data = new LinkedHashMap<>();
    private final Configurer configurer;

    public SerializationData(Configurer configurer) {
        this.configurer = configurer;
    }

    public Map<String, Object> asMap() {
        return this.data;
    }

    public void add(String key, Object value) {
        value = this.configurer.simplify(value, null);
        this.data.put(key, value);
    }

    public <T> void add(String key, Object value, Class<T> clazz) {
        GenericsDeclaration genericType = new GenericsDeclaration(clazz);
        Object object = this.configurer.simplify(value, genericType);
        this.data.put(key, object);
    }

    public <T> void addCollection(String key, Collection<?> collection, Class<T> collectionClazz) {
        GenericsDeclaration genericType = new GenericsDeclaration(collection.getClass(), Collections.singletonList(new GenericsDeclaration(collectionClazz)));
        Object object = this.configurer.simplifyCollection(collection, genericType);
        this.data.put(key, object);
    }

    public void addFormatted(String key, String format, Object value) {
        if (value == null) {
            this.data.put(key, null);
            return;
        }
        this.add(key, String.format(format, value));
    }
}
