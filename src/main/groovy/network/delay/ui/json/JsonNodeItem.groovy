package network.delay.ui.json;

import groovy.transform.CompileStatic

@CompileStatic
class JsonNodeItem {
    String key
    String value
    boolean isObject
    boolean isArray

    JsonNodeItem(String key, String value, boolean isObject, boolean isArray = false) {
        this.key = key
        this.value = value
        this.isObject = isObject
        this.isArray = isArray
    }

    @Override
    String toString() {
        if (isObject) return key + " { }"
        if (isArray) return key + " [ ]"
        return "${key}: ${value}"
    }
}