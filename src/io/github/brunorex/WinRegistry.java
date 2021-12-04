/*
 * Copyright (c) 2012-2013 Bruno Barbieri
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package io.github.brunorex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/*
 * Original code by Réal Gagnon
 * Source: http://www.rgagnon.com/javadetails/java-0630.html
 *
 */

public class WinRegistry {
    public static final int HKEY_CURRENT_USER = 0x80000001;
    public static final int HKEY_LOCAL_MACHINE = 0x80000002;
    public static final int REG_SUCCESS = 0;
    public static final int REG_NOTFOUND = 2;
    public static final int REG_ACCESSDENIED = 5;

    private static final int KEY_ALL_ACCESS = 0xf003f;
    private static final int KEY_READ = 0x20019;
    private static Preferences userRoot = Preferences.userRoot();
    private static Preferences systemRoot = Preferences.systemRoot();
    private static Class<? extends Preferences> userClass = userRoot.getClass();
    private static Method regOpenKey = null;
    private static Method regCloseKey = null;
    private static Method regQueryValueEx = null;
    private static Method regEnumValue = null;
    private static Method regQueryInfoKey = null;
    private static Method regEnumKeyEx = null;
    private static Method regCreateKeyEx = null;
    private static Method regSetValueEx = null;
    private static Method regDeleteKey = null;
    private static Method regDeleteValue = null;

    static {
        try {
            regOpenKey = userClass.getDeclaredMethod(
                    "WindowsRegOpenKey",
                    new Class[] {
                        int.class,
                        byte[].class,
                        int.class
                    });

            regCloseKey = userClass.getDeclaredMethod(
                    "WindowsRegCloseKey",
                    new Class[] {
                        int.class
                    });

            regQueryValueEx = userClass.getDeclaredMethod(
                    "WindowsRegQueryValueEx",
                    new Class[] {
                        int.class,
                        byte[].class
                    });

            regEnumValue = userClass.getDeclaredMethod(
                    "WindowsRegEnumValue",
                    new Class[] {
                        int.class,
                        int.class,
                        int.class
                    });

            regQueryInfoKey = userClass.getDeclaredMethod(
                    "WindowsRegQueryInfoKey1",
                    new Class[] {
                        int.class
                    });

            regEnumKeyEx = userClass.getDeclaredMethod(
                    "WindowsRegEnumKeyEx",
                    new Class[] {
                        int.class,
                        int.class,
                        int.class
                    });

            regCreateKeyEx = userClass.getDeclaredMethod(
                    "WindowsRegCreateKeyEx",
                    new Class[] {
                        int.class,
                        byte[].class
                    });

            regSetValueEx = userClass.getDeclaredMethod(
                    "WindowsRegSetValueEx",
                    new Class[] {
                        int.class,
                        byte[].class,
                        byte[].class
                    });

            regDeleteValue = userClass.getDeclaredMethod(
                    "WindowsRegDeleteValue",
                    new Class[] {
                        int.class,
                        byte[].class
                    });

            regDeleteKey = userClass.getDeclaredMethod(
                    "WindowsRegDeleteKey",
                    new Class[] {
                        int.class,
                        byte[].class
                    });

            regOpenKey.setAccessible(true);
            regCloseKey.setAccessible(true);
            regQueryValueEx.setAccessible(true);
            regEnumValue.setAccessible(true);
            regQueryInfoKey.setAccessible(true);
            regEnumKeyEx.setAccessible(true);
            regCreateKeyEx.setAccessible(true);
            regSetValueEx.setAccessible(true);
            regDeleteValue.setAccessible(true);
            regDeleteKey.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

  private WinRegistry() { }

  /**
    * Read a value from key and value name
    * @param hkey   HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
    * @param key
    * @param valueName
    * @return the value
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
    public static String readString(int hkey,
                                    String key,
                                    String valueName) throws
                                    IllegalArgumentException,
                                    IllegalAccessException,
                                    InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE) {
            return readString(systemRoot, hkey, key, valueName);
        } else if (hkey == HKEY_CURRENT_USER) {
            return readString(userRoot, hkey, key, valueName);
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

  /**
    * Read value(s) and value name(s) form given key
    * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
    * @param key
    * @return the value name(s) plus the value(s)
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
    public static Map<String, String> readStringValues(int hkey, String key) throws
                                                       IllegalArgumentException,
                                                       IllegalAccessException,
                                                       InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE) {
            return readStringValues(systemRoot, hkey, key);
        } else if (hkey == HKEY_CURRENT_USER) {
            return readStringValues(userRoot, hkey, key);
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

  /**
    * Read the value name(s) from a given key
    * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
    * @param key
    * @return the value name(s)
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
    public static List<String> readStringSubKeys(int hkey, String key) throws
                                                IllegalArgumentException,
                                                IllegalAccessException,
                                                InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE) {
            return readStringSubKeys(systemRoot, hkey, key);
        } else if (hkey == HKEY_CURRENT_USER) {
            return readStringSubKeys(userRoot, hkey, key);
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

  /**
    * Create a key
    * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
    * @param key
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
    public static void createKey(int hkey, String key) throws
                                 IllegalArgumentException,
                                 IllegalAccessException,
                                 InvocationTargetException {
        int [] ret;

        if (hkey == HKEY_LOCAL_MACHINE) {
            ret = createKey(systemRoot, hkey, key);
            regCloseKey.invoke(
                    systemRoot,
                    new Object[] {
                        Integer.valueOf(ret[0])
                    });
        } else if (hkey == HKEY_CURRENT_USER) {
            ret = createKey(userRoot, hkey, key);
            regCloseKey.invoke(
                    userRoot,
                    new Object[] {
                        Integer.valueOf(ret[0])
                    });
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }

        if (ret[1] != REG_SUCCESS) {
            throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
        }
    }

  /**
    * Write a value in a given key/value name
    * @param hkey
    * @param key
    * @param valueName
    * @param value
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
    public static void writeStringValue(int hkey,
                                        String key,
                                        String valueName,
                                        String value) throws
                                        IllegalArgumentException,
                                        IllegalAccessException,
                                        InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE) {
            writeStringValue(systemRoot, hkey, key, valueName, value);
        } else if (hkey == HKEY_CURRENT_USER) {
            writeStringValue(userRoot, hkey, key, valueName, value);
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

  /**
    * Delete a given key
    * @param hkey
    * @param key
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
    public static void deleteKey(int hkey, String key) throws
                                 IllegalArgumentException,
                                 IllegalAccessException,
                                 InvocationTargetException {
        int rc = -1;

        if (hkey == HKEY_LOCAL_MACHINE) {
            rc = deleteKey(systemRoot, hkey, key);
        } else if (hkey == HKEY_CURRENT_USER) {
            rc = deleteKey(userRoot, hkey, key);
        } if (rc != REG_SUCCESS) {
            throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
        }
    }

   /**
    * delete a value from a given key/value name
    * @param hkey
    * @param key
    * @param value
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
    public static void deleteValue(int hkey,
                                   String key,
                                   String value) throws
                                   IllegalArgumentException,
                                   IllegalAccessException,
                                   InvocationTargetException {
        int rc = -1;

        if (hkey == HKEY_LOCAL_MACHINE) {
            rc = deleteValue(systemRoot, hkey, key, value);
        } else if (hkey == HKEY_CURRENT_USER) {
            rc = deleteValue(userRoot, hkey, key, value);
        }

        if (rc != REG_SUCCESS) {
            throw new IllegalArgumentException("rc=" + rc
                    + "  key=" + key + "  value=" + value);
        }
    }

    // =====================

    private static int deleteValue(Preferences root,
                                   int hkey,
                                   String key,
                                   String value) throws
                                   IllegalArgumentException,
                                   IllegalAccessException,
                                   InvocationTargetException {
        int[] handles = (int[]) regOpenKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(hkey),
                    toCstr(key),
                    Integer.valueOf(KEY_ALL_ACCESS)
                });

        if (handles[1] != REG_SUCCESS) {
            return handles[1];  // can be REG_NOTFOUND, REG_ACCESSDENIED
        }

        int rc = ((Integer) regDeleteValue.invoke(
                root,
                new Object[] {
                    Integer.valueOf(handles[0]),
                    toCstr(value)
                })).intValue();

        regCloseKey.invoke(root,
                new Object[] {
                    Integer.valueOf(handles[0])
                });

        return rc;
    }

    private static int deleteKey(Preferences root,
                                 int hkey,
                                 String key) throws
                                 IllegalArgumentException,
                                 IllegalAccessException,
                                 InvocationTargetException {
        int rc = ((Integer) regDeleteKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(hkey),
                    toCstr(key)
                })).intValue();

        return rc;  // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
    }

    private static String readString(Preferences root,
                                     int hkey,
                                     String key,
                                     String value) throws
                                     IllegalArgumentException,
                                     IllegalAccessException,
                                     InvocationTargetException {
        int[] handles = (int[]) regOpenKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(hkey),
                    toCstr(key),
                    Integer.valueOf(KEY_READ)
                });

        if (handles[1] != REG_SUCCESS) {
            return null;
        }

        byte[] valb = (byte[]) regQueryValueEx.invoke(
                root,
                new Object[] {
                    Integer.valueOf(handles[0]),
                    toCstr(value)
                });

        regCloseKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(handles[0])
                });

        return (valb != null ? new String(valb).trim() : null);
    }

    private static Map<String,String> readStringValues(Preferences root,
                                                       int hkey,
                                                       String key) throws
                                                       IllegalArgumentException,
                                                       IllegalAccessException,
                                                       InvocationTargetException {
        HashMap<String, String> results = new HashMap<String,String>();

        int[] handles = (int[]) regOpenKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(hkey),
                    toCstr(key),
                    Integer.valueOf(KEY_READ)
                });

        if (handles[1] != REG_SUCCESS) {
            return null;
        }

        int[] info = (int[]) regQueryInfoKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(handles[0])
                });

        int count = info[2]; // count
        int maxlen = info[3]; // value length max

        for (int index = 0; index < count; index++)  {
            byte[] name = (byte[]) regEnumValue.invoke(
                    root,
                    new Object[] {
                        Integer.valueOf
                        (handles[0]),
                        Integer.valueOf(index),
                        Integer.valueOf(maxlen+1)
                    });

           String value = readString(hkey, key, new String(name));
           results.put(new String(name).trim(), value);
        }

        regCloseKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(handles[0])
                });

        return results;
    }

    private static List<String> readStringSubKeys(Preferences root,
                                                  int hkey,
                                                  String key) throws
                                                  IllegalArgumentException,
                                                  IllegalAccessException,
                                                  InvocationTargetException {
        List<String> results = new ArrayList<String>();

        int[] handles = (int[]) regOpenKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(hkey),
                    toCstr(key),
                    Integer.valueOf(KEY_READ)
                });

        if (handles[1] != REG_SUCCESS) {
            return null;
        }

        int[] info = (int[]) regQueryInfoKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(handles[0])
                });

        int count = info[0]; // count
        int maxlen = info[3]; // value length max

        for (int index = 0; index < count; index++) {
            byte[] name = (byte[]) regEnumKeyEx.invoke(
                    root,
                    new Object[] {
                        Integer.valueOf
                        (handles[0]),
                        Integer.valueOf(index),
                        Integer.valueOf(maxlen + 1)
                    });

            results.add(new String(name).trim());
        }

        regCloseKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(handles[0])
                });

        return results;
    }

    private static int[] createKey(Preferences root,
                                   int hkey,
                                   String key) throws
                                   IllegalArgumentException,
                                   IllegalAccessException,
                                   InvocationTargetException {
        return (int[]) regCreateKeyEx.invoke(
                root,
                new Object[] {
                    Integer.valueOf(hkey),
                    toCstr(key)
                });
    }

    private static void writeStringValue(Preferences root,
                                         int hkey,
                                         String key,
                                         String valueName,
                                         String value) throws
                                         IllegalArgumentException,
                                         IllegalAccessException,
                                         InvocationTargetException {
        int[] handles = (int[]) regOpenKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(hkey),
                    toCstr(key),
                    Integer.valueOf(KEY_ALL_ACCESS)
                });

        regSetValueEx.invoke(
                root,
                new Object[] {
                    Integer.valueOf(handles[0]),
                    toCstr(valueName),
                    toCstr(value)
                });

        regCloseKey.invoke(
                root,
                new Object[] {
                    Integer.valueOf(handles[0])
                });
    }

    // utility
    private static byte[] toCstr(String str) {
        byte[] result = new byte[str.length()+1];

        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }

        result[str.length()] = 0;
        return result;
    }
}
