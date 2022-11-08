/*-
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates.  All rights reserved.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 *  https://oss.oracle.com/licenses/upl/
 */

package nosql.cloud.table.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import oracle.nosql.driver.values.ArrayValue;
import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.MapValue;

/**
 * Utility class related to configuration.
 */
public class ConfigUtils {
    private ConfigUtils() {

    }

    public static void requireValue(String param) {
        throw new InvalidConfigException("missing required parameter: " + param);
    }

    public static void invalidValue(String param, String msg) {
        throw new InvalidConfigException("invalid " + param + ": " + msg);
    }

    public static void invalidValue(String msg) {
        throw new InvalidConfigException(msg);
    }

    public static void checkFileExist(File file) {
        checkFileExist(file, false, true, false);
    }

    public static void checkDirExist(File file) {
        checkFileExist(file, true, true, false);
    }

    public static void checkFileExist(File file,
                                      boolean isDir,
                                      boolean canRead,
                                      boolean canWrite) {

        final String path = toFilePath(file);
        if (!file.exists() && !isDir) {
            throw new InvalidConfigException("File not exist: " + path);
        }
        if (!file.exists() && isDir) {
            throw new InvalidConfigException("Directory not exist: " + path);
        }
        if (isDir && !file.isDirectory()) {
            throw new InvalidConfigException("Not a directory: " + path);
        }
        if(!isDir && file.isDirectory()) {
            throw new InvalidConfigException("Not a file: " + path);
        }
        if (canRead && !file.canRead()) {
            throw new InvalidConfigException("No read access to " + path);
        }
        if (canWrite && !file.canWrite()) {
            throw new InvalidConfigException("No write access to " + path);
        }
    }

    /**
     * Returns the file path of the specified file.
     * @param file the specified file
     * @return the file path string.
     */
    public static String toFilePath(File file) {
        if (file == null) {
            return null;
        }
        String filepath;
        try {
            filepath = file.getCanonicalPath();
        } catch (IOException ioe) {
            filepath = file.getAbsolutePath();
        }
        return filepath;
    }

    /**
     * reads a string value for fieldName key from JSON config.
     * @param config JSON config
     * @param fieldName key in JSON config
     * @return String value associated with fieldName. Returns null if
     * fieldName does not exist or value is not a String
     */
    public static String readString(MapValue config, String fieldName) {
        return readString(config, fieldName, null);
    }

    /**
     * reads a string value for fieldName key from JSON config with default
     * value.
     * @param config JSON config
     * @param fieldName  key in JSON config
     * @param defaultValue default value to be returned if key does not exist
     *                    or value is not String.
     * @return String value associated with fieldName if any or defaultValue
     */
    public static String readString(MapValue config,
                                    String fieldName,
                                    String defaultValue) {
        try {
            return config.getString(fieldName);
        } catch (RuntimeException ignored) {

        }
        return defaultValue;
    }

    /**
     * reads a boolean value for field key from JSON config.
     * @param config JSON config
     * @param field key in JSON config
     * @return boolean value associated with field. false will be returned if
     * field doesn't exist or value is not boolean.
     */
    public static boolean readBoolean(MapValue config, String field) {
        return readBoolean(config, field, false);
    }

    /**
     * reads a boolean value for field key from JSON config with default value.
     * @param config JSON config
     * @param field key in JSON config
     * @param defaultValue default value to be returned if field does not
     *                     exist or value is not boolean
     * @return boolean value associated with field if any or defaultValue
     */
    public static boolean readBoolean(MapValue config,
                                      String field,
                                      boolean defaultValue) {
        try {
            return config.getBoolean(field);
        } catch (RuntimeException ignored) {

        }
        return defaultValue;
    }

    /**
     * reads an integer value for field key from JSON config.
     * @param config JSON config
     * @param field key in JSON config
     * @return value of the field. 0 will be returned if key doesn't exist or
     * value type is not integer.
     */
    public static int readInt(MapValue config, String field) {
        return readInt(config, field, 0);
    }

    /**
     * reads an integer value for field key from JSON config with default value
     * @param config JSON config
     * @param field key in JSON config
     * @param defaultValue default value to be returned if key does not exist
     *                    or value is not integer
     * @return value of the field if any or defaultValue.
     */
    public static int readInt(MapValue config, String field,
                              int defaultValue) {
        try {
            return config.getInt(field);
        } catch (RuntimeException ignored) {

        }
        return defaultValue;
    }

    /**
     * reads an array of String from JSON config for field key.
     * @param config JSON config
     * @param field key in JSON config
     * @return String array. null will be returned if field doesn't exist in
     * config or value type is not String array
     */
    public static String[] readStringArray(MapValue config, String field) {
        try {
            ArrayValue arrayValue = config.get(field).asArray();
            List<String> values = new ArrayList<>();
            for (FieldValue fieldValue : arrayValue) {
                values.add(fieldValue.getString());
            }
            return values.toArray(new String[0]);
        } catch (RuntimeException ignored) {

        }
        return null;
    }

    /**
     * reads an enum value from given JSON config and enum class.
     * @param config JSON config
     * @param field key in JSON config
     * @param enumClass enum class for which value belongs to
     * @param <E> enum type
     * @return enum value from enum class
     * @throws IllegalArgumentException If value is not of type enumClass
     */
    public static <E extends Enum<E>> E readEnum(MapValue config,
                                                 String field,
                                                 Class<E> enumClass) {
        return readEnum(config, field, enumClass, null);
    }

    /**
     * reads an enum value from given JSON config and enum class with default
     * value
     *
     * @param config       JSON config
     * @param field        key in JSON config
     * @param enumClass    enum class for which value belongs to
     * @param <E>          enum type
     * @param defaultValue default value to be returned if key does not exist
     * @return enum value from enum class
     * @throws IllegalArgumentException If value is not of type enumClass
     */
    public static <E extends Enum<E>> E readEnum(MapValue config,
                                                 String field,
                                                 Class<E> enumClass,
                                                 E defaultValue) {
        String value = readString(config, field);
        if (value != null) {
            try {
                return Enum.valueOf(enumClass, value.toUpperCase());
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException(
                        "Invalid '" + field + "': " + value);
            }
        }
        return defaultValue;
    }

    /**
     * Exception class for all invalid configurations.
     */
    public static class InvalidConfigException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public InvalidConfigException(String msg) {
            super(msg);
        }

        @Override
        public String getMessage() {
            return "Invalid configuration: " + super.getMessage();
        }
    }
}
