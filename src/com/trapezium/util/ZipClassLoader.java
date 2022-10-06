/*
 * @(#)ZipClassLoader.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.util;

import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.zip.*;


/** This subclass of ClassLoader can load classes from a zip file.
 */
public class ZipClassLoader extends ClassLoader {
    private static char favoredSep = File.separatorChar;
    private static char altSep = (File.separatorChar == '/' ? '\\' : '/');

    ZipFile zipFile;
    static Hashtable cache = new Hashtable();

    public ZipClassLoader(ZipFile zf) {
        zipFile = zf;
    }
    
    public static Class classForName(String name) throws ClassNotFoundException {
        Class c = (Class) cache.get(name);
        if (c == null) {
            throw new ClassNotFoundException(name + " not found by ZipClassLoader");
        }
        return c;
    }

    public static String massageClassToFile(String name) {
        int namelen = name.length();
        char[] buf = new char[namelen + 6];    // 6 = length of ".class"
        name.getChars(0, namelen, buf, 0);
        ".class".getChars(0, 6, buf, namelen);      // append ".class" to buf

        int i;
        for (i = 0; i < namelen - 1; i++) { // last character can't be a separator
            if (buf[i] == '.') {
                buf[i] = favoredSep;
            }
        }
        return new String(buf);
    }
    
    private ZipEntry findClassEntry(String name) {
        String filename = massageClassToFile(name);
        ZipEntry ze = zipFile.getEntry(filename);
        if (ze == null) {
            ze = zipFile.getEntry(filename.replace(favoredSep, altSep));
        }
        return ze;
    }

    private byte loadClassData(ZipEntry entry)[] {
        long size = entry.getSize();
        if (size <= 0 || size > Integer.MAX_VALUE) {
            return new byte[0];
        }
        try {
            return loadClassData(zipFile.getInputStream(entry), (int) size);
        } catch (Exception e) {
            System.out.println("Error loading class data from zip entry: " + e);
            return new byte[0];
        }            
    }

        
    private static byte loadClassData(InputStream is, int size)[] {
        byte[] data = new byte[size];
        try {
            DataInputStream dis = new DataInputStream( is );
            dis.readFully(data);
        } catch (Exception e) {
            System.out.println("Error reading class data: " + e);
            return new byte[0];
        }
        return data;
    }

    public synchronized Class loadClass(ZipEntry ze) throws ClassNotFoundException {
        String name = ze.getName();
//trapezium.chisel.ChiselSet.dbgStr.println( "original name is '" + name + "'" );

        // pare off the trailing ".class" and put in an array
        char[] namebuf = name.substring(0, name.length() - 6).toCharArray();
        for (int i = 0; i < namebuf.length - 1; i++) {  // last char can't be a separator
            if (namebuf[i] == favoredSep || namebuf[i] == altSep) {
                namebuf[i] = '.';
            }
        }
        String classname = new String(namebuf);
//trapezium.chisel.ChiselSet.dbgStr.println( "class name is '" + classname + "'" );
        Class c = (Class) cache.get(classname);
        if (c == null) {
            byte data[] = loadClassData(ze);
            if (data.length > 0) {
                c = defineClass(classname, data, 0, data.length);
                cache.put(classname, c);
                //trapezium.chisel.ChiselSet.dbgStr.println( "put in cache" );
            } else {
                throw new ClassNotFoundException("Class " + classname + " has no data.");
            }
        }
        resolveClass(c);
        //trapezium.chisel.ChiselSet.dbgStr.println( "got the class in!" );
        return c;
    }

    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        //trapezium.chisel.ChiselSet.dbgStr.println( "loadClass " + name );
        Class c = (Class) cache.get(name);
        if (c == null) {
            ZipEntry entry = findClassEntry(name);
            if (entry == null) {
                try {
                    c = Class.forName(name);
                } catch (Exception e) {
                    //trapezium.chisel.ChiselSet.dbgStr.println( "failure #1" );
                    throw new ClassNotFoundException("Class " + name + " not found.");
                }                
                return c;
            }
            byte[] data = loadClassData(entry);
            if (data.length > 0) {
                c = defineClass(name, data, 0, data.length);
                cache.put(name, c);
            } else {
                //trapezium.chisel.ChiselSet.dbgStr.println( "failure #2" );
                throw new ClassNotFoundException("Class " + name + " has no data.");
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
 }

