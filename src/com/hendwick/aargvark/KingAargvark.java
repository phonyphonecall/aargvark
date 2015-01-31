package com.hendwick.aargvark;

import com.hendwick.aargvark.annotation.Aargument;
import com.hendwick.aargvark.annotation.Aargvark;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Martin Wickham on 1/27/2015.
 */
public class KingAargvark {

    private String[] args;
    private Object aargvark;
    private Class<?> aargvarkClass;
    private Map<String, Field> options;
    private Iterator<String> currentArg;
    private boolean strictOptions;
    private boolean extrasAreFatal;
    private boolean enableHelp;

    public KingAargvark(String[] args) {
        this.args = args;
    }

//    public <T> T parse(Class<T> aargvarkClass) throws AargvarkException {
//        try {
//            T aargvark = aargvarkClass.newInstance();
//            this.aargvarkClass = aargvarkClass;
//            this.aargvark = aargvark;
//            parse();
//            return aargvark;
//        } catch (InstantiationException | IllegalAccessException e) {
//            throw new AargvarkException("Aargvark class (" + aargvarkClass.getCanonicalName() + ") must have a public zero-arg constructor.", e);
//        } finally {
//            this.aargvark = null;
//            this.aargvarkClass = null;
//        }
//    }

    public void marshal(Object aargvark) throws AargvarkException {
        try {
            this.aargvark = aargvark;
            this.aargvarkClass = aargvark.getClass();
            parse();
        } finally {
            this.aargvark = null;
            this.aargvarkClass = null;
        }

    }

    private void parse() throws AargvarkException {
        parseAargvarkAnnotation();
        setupOptions();
        parseArguments();
        cleanup();
    }

    private void parseAargvarkAnnotation() {
        Aargvark aargvark = aargvarkClass.getAnnotation(Aargvark.class);
        if(aargvark != null) {
            this.strictOptions = aargvark.strictOptions();
            this.enableHelp = aargvark.enableHelp();
            this.extrasAreFatal = aargvark.extrasAreFatal();
        }
    }

    private void setupOptions() {
        options = new HashMap<>();
        List<Field> fields = Arrays.asList(aargvarkClass.getFields());
        fields.forEach((field) -> {
            Aargument aargument = field.getAnnotation(Aargument.class);
            if (aargument != null) {
                options.put(getShortName(aargument), field);
                options.put(getLongName(field), field);
            }
        });
    }

    private String getLongName(Field field) {
        return String.format("--%s", field.getName());
    }

    private String getShortName(Aargument aargument) {
        return String.format("-%c", aargument.shortName());
    }

    private void parseArguments() throws AargvarkException {
        currentArg = Arrays.asList(args).iterator();
        while(currentArg.hasNext()) {
            String arg = currentArg.next();
            if (arg.startsWith("-")) {
                parseArgument(arg);
            }
        }
    }

    private void parseArgument(String arg) throws AargvarkException {
        if (arg.equals("--help")) {
            if (enableHelp) {
                printHelp();
                System.out.println("WARNING: '--help' ignored due to configuration");
                return;
            }
        }
        Field f = options.get(arg);
        if (f != null) {
            parseField(arg, f);
            Aargument annotation = f.getAnnotation(Aargument.class);
            String shortName = getShortName(annotation);
            String longName = getLongName(f);
            options.remove(shortName);
            options.remove(longName);
        } else {
            unknownOption(arg);
        }
    }

    private void printHelp() {
        System.out.println("Usage:");
        List<Field> fields = Arrays.asList(aargvarkClass.getFields());
        fields.forEach((field) -> {
            Aargument aargument = field.getAnnotation(Aargument.class);
            if (aargument != null) {
                System.out.println(String.format("\t-%c  --%s  %s", aargument.shortName(), field.getName(), aargument.usage()));
            }
        });
        System.exit(-1);
    }

    private void parseField(String arg, Field f) throws AargvarkException {
        Class<?> fieldClass = f.getType();
        if (fieldClass == boolean.class) {
            parseBooleanField(arg, f);
        } else if (fieldClass.isEnum()) {
            parseEnumField(arg, f);
        } else if (fieldClass == String.class) {
            parseStringField(arg, f);
        } else if (fieldClass == int.class) {
            parseIntegerField(arg, f);
        } else if (fieldClass == short.class) {
            parseShortField(arg, f);
        } else if (fieldClass == float.class) {
            parseFloatField(arg, f);
        } else if (fieldClass == double.class) {
            parseDoubleField(arg, f);
        } else {
            throw new AargvarkException(String.format("Unsupported Aargument type: %s", fieldClass.getCanonicalName()));
        }
    }

    private void parseBooleanField(String arg, Field f) throws AargvarkException {
        try {
            f.setBoolean(aargvark, true);
        } catch (IllegalAccessException e) {
            illegalAccess(arg, f, e);
        }
    }

    private void parseEnumField(String arg, Field f) throws AargvarkException {
        try {
            String valueName = getArgument(arg);
            Class<?> type = f.getType();
            Method valueOf = type.getMethod("valueOf", Class.class, String.class);
            Object value = valueOf.invoke(null, type, valueName);
            if (value != null) {
                f.set(aargvark, value);
            } else {
                invalidValue(arg, f, valueName);
            }
        } catch (IllegalAccessException e) {
            illegalAccess(arg, f, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("... so %s isEnum(), but has no valueOf() method? WTF?", f.getType().getCanonicalName()), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Couldn't invoke valueOf on type "+f.getType().getCanonicalName(), e);
        }
    }

    private void parseStringField(String arg, Field f) throws AargvarkException {
        try {
            f.set(aargvark, getArgument(arg));
        } catch (IllegalAccessException e) {
            illegalAccess(arg, f, e);
        }
    }

    private void parseIntegerField(String arg, Field f) throws AargvarkException {
        String val = getArgument(arg);
        try {
            f.setInt(aargvark, Integer.parseInt(val));
        } catch (IllegalAccessException e) {
            illegalAccess(arg, f, e);
        } catch (NumberFormatException e) {
            invalidValue(arg, f, val);
        }
    }

    private void parseShortField(String arg, Field f) throws AargvarkException {
        String val = getArgument(arg);
        try {
            f.setShort(aargvark, Short.parseShort(val));
        } catch (IllegalAccessException e) {
            illegalAccess(arg, f, e);
        } catch (NumberFormatException e) {
            invalidValue(arg, f, val);
        }
    }

    private void parseDoubleField(String arg, Field f) throws AargvarkException {
        String val = getArgument(arg);
        try {
            f.setDouble(aargvark, Double.parseDouble(val));
        } catch (IllegalAccessException e) {
            illegalAccess(arg, f, e);
        } catch (NumberFormatException e) {
            invalidValue(arg, f, val);
        }
    }

    private void parseFloatField(String arg, Field f) throws AargvarkException {
        String val = getArgument(arg);
        try {
            f.setFloat(aargvark, Float.parseFloat(val));
        } catch (IllegalAccessException e) {
            illegalAccess(arg, f, e);
        } catch (NumberFormatException e) {
            invalidValue(arg, f, val);
        }
    }

    private String getArgument(String arg) throws AargvarkException {
        if (currentArg.hasNext()) {
            return currentArg.next();
        }
        throw new AargvarkException(String.format("Missing argument for parameter %s", arg));
    }

    private void cleanup() throws AargvarkException {
        for (Map.Entry<String, Field> entry : options.entrySet())
            if (entry.getValue().getAnnotation(Aargument.class).require()) {
                throw new AargvarkException(String.format("Missing required parameter: '%s'", entry.getKey()));
            }
    }



    private void illegalAccess(String arg, Field f, Throwable e) throws AargvarkException {
        throw new AargvarkException(String.format("Field %s must be public", f.getName()), e);
    }

    private void invalidValue(String arg, Field f, String value) throws AargvarkException {
        throw new AargvarkException(String.format("Invalid value for parameter %s: '%s'", arg, value));
    }
    private void unknownOption(String arg) throws AargvarkException {
        if (strictOptions)
            throw new AargvarkException("Unknown option: " + arg);
    }
}