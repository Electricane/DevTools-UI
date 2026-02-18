package network.delay.ui.json;

import groovy.transform.CompileStatic

@CompileStatic
class JsonNodeItem {
    String key
    String value
    boolean isObject = false

    JsonNodeItem(String key, String value, boolean isObject = false) {
        this.key = key
        this.value = value
        this.isObject = isObject
    }

    @Override
    String toString() {
        return isObject ? "$key { }" : "$key: $value"
    }
}