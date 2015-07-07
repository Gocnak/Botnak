package com.gocnak.util;

import java.util.Arrays;

/**
 * This wrapper class was made to make custom commands work. Because a reference of an array compared to
 * another reference is different, we need to override the equals() and
 * hashcode() methods to produce a fair comparison based on values of the array rather than just
 * the references to it.
 */

public class StringArray {

    public String[] data;

    public StringArray(String[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StringArray && Arrays.equals(data, ((StringArray) other).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
