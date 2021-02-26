package net.thenova.titan.libraries;

import de.arraying.kotys.JSON;
import de.arraying.kotys.JSONArray;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public final class Property {

    private final String value;

    /**
     * Creates a new property.
     */
    public Property() {
        this(null);
    }

    /**
     * Defines that the property should default to something.
     *
     * @param fallback The default value.
     * @return The property that will definitely contain a value.
     */
    public Property defaulting(final Object fallback) {
        return this.value != null ? this : new Property(fallback.toString());
    }

    /**
     * Gets the value as a boolean.
     *
     * @return A boolean.
     */
    public boolean asBoolean() {
        return Boolean.parseBoolean(this.value);
    }

    /**
     * Gets the value as a byte.
     *
     * @return A byte.
     */
    public byte asByte() {
        return Byte.parseByte(this.value);
    }

    /**
     * Gets the value as a character.
     *
     * @return A character.
     */
    public char asChar() {
        return this.value.length() == 0 ? 0 : this.value.charAt(0);
    }

    /**
     * Gets the value as a short.
     *
     * @return A short.
     */
    public short asShort() {
        return Short.parseShort(this.value);
    }

    /**
     * Gets the value as an integer.
     *
     * @return An integer.
     */
    public int asInt() {
        return Integer.parseInt(this.value);
    }

    /**
     * Gets the value as a long.
     *
     * @return A long.
     */
    public long asLong() {
        return Long.parseLong(this.value);
    }

    /**
     * Gets the value as a float decimal.
     *
     * @return A float.
     */
    public float asFloat() {
        return Float.parseFloat(this.value);
    }

    /**
     * Gets the value as a double decimal.
     *
     * @return A double.
     */
    public double asDouble() {
        return Double.parseDouble(this.value);
    }

    /**
     * Gets the value as a string.
     *
     * @return A string.
     */
    public String asString() {
        return this.value;
    }

    /**
     * Gets the value as a JSON object
     *
     * @return A JSON object.
     */
    public final JSON asJSON() {
        return new JSON(this.value);
    }

    /**
     * Gets the value as a JSON Array object
     *
     * @return JSON Array object
     */
    public final JSONArray asArray() {
        return new JSONArray(this.value);
    }

    /**
     * Gets the object as a string.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return this.asString();
    }
}
