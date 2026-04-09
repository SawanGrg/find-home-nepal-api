package com.beta.FindHome.utils;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Component
public class FieldUpdaterUtils {

    /**
     * Updates non-null fields from the source object into the target object and returns the updated target.
     *
     * @param source the object containing the new values (DTO)
     * @param target the entity to be updated
     * @param <T>    the type of the target object
     * @return the updated target object
     */
    public static <T> T updateFields(Object source, T target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target objects must not be null.");
        }

        BeanWrapper srcWrapper = new BeanWrapperImpl(source);
        BeanWrapper trgWrapper = new BeanWrapperImpl(target);

        for (String propertyName : getNonNullPropertyNames(source)) {
            System.out.println("Processing Property: " + propertyName);
            if (trgWrapper.isWritableProperty(propertyName)) {
                Object value = srcWrapper.getPropertyValue(propertyName);
                System.out.println("Updating Property: " + propertyName + " with value: " + value);
                trgWrapper.setPropertyValue(propertyName, value);
            }
        }
        return target;
    }

    /**
     * Retrieves property names of non-null fields in the given object.
     *
     * @param source the object to analyze
     * @return an array of non-null property names
     */
    private static String[] getNonNullPropertyNames(Object source) {
        BeanWrapper srcWrapper = new BeanWrapperImpl(source);
        Set<String> nonNullPropertyNames = new HashSet<>();

        for (PropertyDescriptor descriptor : srcWrapper.getPropertyDescriptors()) {
            // Get the value of the property
            Object value = srcWrapper.getPropertyValue(descriptor.getName());

            // Check if the value is not null (handles Integer, String, BigDecimal, Float, etc.)
            if (value != null && !isEmptyValue(value)) {
                nonNullPropertyNames.add(descriptor.getName());
            }
        }

        return nonNullPropertyNames.toArray(new String[0]);
    }

    /**
     * Checks if the value is considered empty. This method ensures that types like BigDecimal or Integer are also considered.
     *
     * @param value the value to check
     * @return true if the value is empty or null (for cases like BigDecimal.ZERO, Integer.ZERO)
     */
    private static boolean isEmptyValue(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).compareTo(BigDecimal.ZERO) == 0;
        } else if (value instanceof Integer) {
            return ((Integer) value).intValue() == 0;
        } else if (value instanceof Float) {
            return ((Float) value).floatValue() == 0;
        } else if (value instanceof Double) {
            return ((Double) value).doubleValue() == 0;
        }
        return false; // For other types, non-null is considered valid
    }
}
