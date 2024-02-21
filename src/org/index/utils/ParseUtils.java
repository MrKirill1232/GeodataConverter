package org.index.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * @author Index
 */
public class ParseUtils
{
    public static String parseString(Object lookingObject)
    {
        return parseString(lookingObject, null);
    }

    public static String parseString(Object lookingObject, String defaultValue)
    {
        if (lookingObject == null)
        {
            return defaultValue;
        }
        try
        {
            if (lookingObject instanceof String)
            {
                return (String) lookingObject;
            }
            else if (lookingObject instanceof CharSequence)
            {
                return ((CharSequence) lookingObject).toString();
            }
            else if (lookingObject instanceof StringJoiner)
            {
                return ((StringJoiner) lookingObject).toString();
            }
            return String.valueOf(lookingObject);
        }
        catch (Exception e)
        {
            return defaultOrThrow("String value required, but found: ", lookingObject, defaultValue);
        }
    }

    public static boolean parseBoolean(Object lookingObject)
    {
        return parseBoolean(lookingObject, null);
    }

    public static boolean parseBoolean(Object lookingObject, Boolean defaultValue)
    {
        try
        {
            if (lookingObject instanceof Boolean)
            {
                return ((Boolean) lookingObject).booleanValue();
            }
            final String lookingObjectAsString = normilizeString(String.valueOf(lookingObject));
            if (lookingObjectAsString.equals("0"))
            {
                return false;
            }
            else if (lookingObjectAsString.equals("1"))
            {
                return true;
            }
            else if (lookingObjectAsString.equals("false"))
            {
                return false;
            }
            else if (lookingObjectAsString.equals("true"))
            {
                return true;
            }
            else
            {
                return defaultOrThrow("Boolean value required, but found: ", lookingObject, defaultValue);
            }
        }
        catch (Exception e)
        {
            return defaultOrThrow("Boolean value required, but found: ", lookingObject, defaultValue);
        }
    }

    public static int parseInteger(Object lookingObject)
    {
        return parseInteger(lookingObject, null);
    }

    public static int parseInteger(Object lookingObject, Integer defaultValue)
    {
        try
        {
            if (lookingObject instanceof Byte)
            {
                return ((Byte) lookingObject).intValue();
            }
            if (lookingObject instanceof Short)
            {
                return ((Short) lookingObject).intValue();
            }
            if (lookingObject instanceof Integer)
            {
                return ((Integer) lookingObject).intValue();
            }
            final String lookingObjectAsString = normilizeString(String.valueOf(lookingObject));
            return Integer.parseInt(lookingObjectAsString);
        }
        catch (Exception e)
        {
            return defaultOrThrow("Integer value required, but found: ", lookingObject, defaultValue);
        }
    }

    public static long parseLong(Object lookingObject)
    {
        return parseLong(lookingObject, null);
    }

    public static long parseLong(Object lookingObject, Long defaultValue)
    {
        try
        {
            if (lookingObject instanceof Byte)
            {
                return ((Byte) lookingObject).longValue();
            }
            else if (lookingObject instanceof Short)
            {
                return ((Short) lookingObject).longValue();
            }
            else if (lookingObject instanceof Integer)
            {
                return ((Integer) lookingObject).longValue();
            }
            else if (lookingObject instanceof Long)
            {
                return ((Long) lookingObject).longValue();
            }
            final String lookingObjectAsString = normilizeString(String.valueOf(lookingObject));
            return Long.parseLong(lookingObjectAsString);
        }
        catch (Exception e)
        {
            return defaultOrThrow("Long value required, but found: ", lookingObject, defaultValue);
        }
    }

    public static float parseFloat(Object lookingObject)
    {
        return parseFloat(lookingObject, null);
    }

    public static float parseFloat(Object lookingObject, Float defaultValue)
    {
        try
        {
            if (lookingObject instanceof Float)
            {
                return ((Float) lookingObject).floatValue();
            }
            else if (lookingObject instanceof Double)
            {
                Double doubleValue = ((Double) lookingObject);
                if (defaultValue > Float.MAX_VALUE)
                {
                    throw new IllegalArgumentException("Float value required, but found double: " + String.valueOf(lookingObject));
                }
                return doubleValue.floatValue();
            }
            final BigDecimal bigDecimalValue = parseValue(lookingObject, (defaultValue == null ? null : new BigDecimal(defaultValue)));
            if (bigDecimalValue.doubleValue() > Float.MAX_VALUE)
            {
                throw new IllegalArgumentException("Float value required, but found double: " + String.valueOf(lookingObject));
            }
            return bigDecimalValue.floatValue();
        }
        catch (Exception e)
        {
            return defaultOrThrow("Float value required, but found: ", lookingObject, defaultValue);
        }
    }

    public static double parseDouble(Object lookingObject)
    {
        return parseDouble(lookingObject, null);
    }

    public static double parseDouble(Object lookingObject, Double defaultValue)
    {
        try
        {
            if (lookingObject instanceof Float)
            {
                return ((Float) lookingObject).doubleValue();
            }
            else if (lookingObject instanceof Double)
            {
                return ((Double) lookingObject).doubleValue();
            }
            final BigDecimal bigDecimalValue = parseValue(lookingObject, (defaultValue == null ? null : new BigDecimal(defaultValue)));
            return bigDecimalValue.doubleValue();
        }
        catch (Exception e)
        {
            return defaultOrThrow("Double value required, but found: ", lookingObject, defaultValue);
        }
    }

    public static BigDecimal parseValue(Object lookingObject, BigDecimal defaultValue)
    {
        try
        {

            if (lookingObject instanceof BigDecimal)
            {
                return ((BigDecimal) lookingObject);
            }
            else if (lookingObject instanceof BigInteger)
            {
                return new BigDecimal((BigInteger) lookingObject);
            }
            final String lookingObjectAsString = normilizeString(String.valueOf(lookingObject)).replaceAll(",", ".");
            return new BigDecimal(lookingObjectAsString);
        }
        catch (Exception e)
        {
            return defaultOrThrow("Big Decimal value required, but found: ", lookingObject, defaultValue);
        }
    }

    public static String[] parseStringArray(Object lookingObject, String[] defaultValue)
    {
        try
        {
            if (lookingObject.getClass() == String.class)
            {
                String lookingObjectAsString = normilizeString(String.valueOf(lookingObject), false, false);
                if (lookingObjectAsString.startsWith("[") && lookingObjectAsString.endsWith("]"))
                {
                    lookingObjectAsString = lookingObjectAsString.substring(1, lookingObjectAsString.length() - 1);
                }
                return lookingObjectAsString.replaceAll(";", ",").split(",");
            }
            if (lookingObject instanceof String[])
            {
                return ((String[]) lookingObject);
            }
            if (lookingObject instanceof Object[])
            {
                Object[] inputArray = ((Object[]) lookingObject);
                final String[] outputArray = new String[inputArray.length];
                for (int index = 0; index < outputArray.length; index++)
                {
                    outputArray[index] = String.valueOf(inputArray[index]);
                }
                return outputArray;
            }
            if (lookingObject instanceof Collection<?>)
            {
                Collection<?> inputList = ((Collection<?>) lookingObject);
                final String[] outputArray = new String[inputList.size()];
                int counter = 0;
                for (Object listElement : inputList)
                {
                    outputArray[counter++] = String.valueOf(listElement);
                }
                return outputArray;
            }
            return defaultOrThrow("String array required, but found: ", lookingObject, defaultValue);
        }
        catch (Exception e)
        {
            return defaultOrThrow("String array required, but found: ", lookingObject, defaultValue);
        }
    }

    public static <T extends Enum<T>> T parseEnum(Object lookingObject, T defaultValue, Class<T> enumClass)
    {
        try
        {
            final String lookingObjectAsString = String.valueOf(lookingObject);
            return Enum.valueOf(enumClass, lookingObjectAsString);
        }
        catch (Exception ignored)
        {
            return defaultValue;
        }
    }

    public static int[] parseIntArray(Object lookingObject, int[] defaultValue)
    {
        final String[] splited = parseStringArray(lookingObject, null);
        if (splited == null)
        {
            return defaultOrThrow("Integer array required, but found: ", lookingObject, defaultValue);
        }
        final int[] returnArray = new int[splited.length];
        for (int index = 0; index < splited.length; index++)
        {
            int value;
            try
            {
                value = parseInteger(splited[index], null);
            }
            catch (IllegalArgumentException npe)
            {
                continue;
            }
            returnArray[index] = value;
        }
        return returnArray;
    }

    private static <T> T defaultOrThrow(String throwString, Object lookingObject, T defaultValue)
    {
        if (defaultValue == null)
        {
            throw new IllegalArgumentException(throwString + String.valueOf(lookingObject));
        }
        return defaultValue;
    }

    public static String normilizeString(String inputString)
    {
        return normilizeString(inputString, true, true);
    }

    public static String normilizeString(String inputString, boolean replaceSpace, boolean toLowerCase)
    {
        if (inputString == null || inputString.isEmpty())
        {
            return "";
        }
        final String returnString = inputString
                .replaceAll("\n", "")
                .replaceAll("\t", "")
                .replaceAll("\r", "")
                .replaceAll("_", "")
                .replaceAll("-", "")
                .replaceAll(replaceSpace ? " " : "", "")
                .replaceAll("\0", replaceSpace ? "" : " ")   // replace all null values
                .replaceAll("&nbsp", replaceSpace ? "" : " ")
                .trim();
        return toLowerCase
                ? returnString.toLowerCase(Locale.ROOT)
                : returnString;
    }
}
