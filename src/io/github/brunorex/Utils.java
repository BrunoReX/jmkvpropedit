/*
 * Copyright (c) 2012-2018 Bruno Barbieri
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

import java.awt.Component;
import java.awt.event.*;
import java.io.File;
import java.text.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class Utils {

    /* Start of OS detection functions */

    public static boolean isWindows() {
        String OS = System.getProperty("os.name");

        if (OS.toLowerCase().startsWith("windows"))
            return true;
        else
            return false;
    }

    public static boolean isMac() {
        String OS = System.getProperty("os.name");

        if (OS.toLowerCase().startsWith("mac"))
            return true;
        else
            return false;
    }

    public static boolean isLinux() {
        String OS = System.getProperty("os.name");

        if (OS.toLowerCase().startsWith("linux"))
            return true;
        else
            return false;
    }

    /* End of OS detection functions */


    /* Start of escaping functions */

    public static String escapeName(String name) {
        if (!name.isEmpty()) {
            name = name.replace("\"", "####escaped__quotes#####"); // lol 
            name = name.replace("\\","\\\\");
        }

        return name;
    }

    public static String escapeQuotes(String text) {
        text = text.replace("\"", "\\\"");

        return text;
    }

    public static String escapeBackslashes(String text) {
        text = text.replace("\\", "\\\\");

        return text;
    }

    public static String fixEscapedQuotes(String text) {
        if (!text.isEmpty()) {
            text = text.replace("####escaped__quotes#####", "\\\"");
        }

        return text;
    }

    /* End of escaping functions */


    /* Start of right-click menu code */

    private static void showRCMenu(JTextComponent text, MouseEvent e) {
        int selStart = text.getSelectionStart();
        int selEnd = text.getSelectionEnd();

        JPopupMenu rightClickMenu = new JPopupMenu();

        JMenuItem copyMenuItem = new JMenuItem(text.getActionMap()
                .get(DefaultEditorKit.copyAction));

        JMenuItem cutMenuItem = new JMenuItem(text.getActionMap()
                .get(DefaultEditorKit.cutAction));

        JMenuItem pasteMenuItem = new JMenuItem(text.getActionMap()
                .get(DefaultEditorKit.pasteAction));

        JMenuItem selectAllMenuItem = new JMenuItem(text.getActionMap()
                .get(DefaultEditorKit.selectAllAction));

        copyMenuItem.setText("Copy");
        cutMenuItem.setText("Cut");
        pasteMenuItem.setText("Paste");
        selectAllMenuItem.setText("Select All");

        rightClickMenu.add(copyMenuItem);
        rightClickMenu.add(cutMenuItem);
        rightClickMenu.add(pasteMenuItem);
        rightClickMenu.addSeparator();
        rightClickMenu.add(selectAllMenuItem);

        if (text.getText().isEmpty()) {
            copyMenuItem.setEnabled(false);
            selectAllMenuItem.setEnabled(false);
            cutMenuItem.setEnabled(false);
        }

        if (selStart == selEnd) {
            copyMenuItem.setEnabled(false);
            cutMenuItem.setEnabled(false);
        }

        if ((selStart+selEnd) == text.getText().length()) {
            selectAllMenuItem.setEnabled(false);
        }

        if (!text.isEditable()) {
            cutMenuItem.setEnabled(false);
            pasteMenuItem.setEnabled(false);
        }

        rightClickMenu.show(text, e.getX(), e.getY());
    }

    public static void addRCMenuMouseListener(final JTextComponent text) {
        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isMetaDown() && text.isEnabled()) {
                    text.requestFocus();
                    showRCMenu(text, e);
                }
            }
        });
    }

    /* End of right-click menu code */


    public static String padNumber(int pad, int number) {
        NumberFormat formatter = new DecimalFormat("0");

        if (pad > 0) {
            String n = "";
            for (int i = 0; i < pad; i++) {
                n += 0;
            }
            formatter = new DecimalFormat(n);
        }

        return formatter.format(number);
    }

    public static int getDotIndex(String file) {
        int dotIndex = file.lastIndexOf(".");

        if (dotIndex != -1)
            return dotIndex;
        else
            return file.length();
    }

    public static int getSeparatorIndex(String file) {
        int sepIndex = file.lastIndexOf(File.separator);

        if (sepIndex != -1)
            return sepIndex+1;
        else
            return 0;
    }

    public static String getFileNameWithoutExt(String file) {
        return file.substring(getSeparatorIndex(file), getDotIndex(file));
    }

    public static String getPathWithoutExt(String file) {
        return file.substring(0, getDotIndex(file));
    }

    /**
     * http://niravjavadeveloper.blogspot.com/2011/05/resize-jtable-columns.html
     */
    public static void adjustColumnPreferredWidths(JTable table) {
        // strategy - get max width for cells in column and
        // make that the preferred width
        TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < table.getColumnCount(); col++) {
            int maxwidth = 0;

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer rend = table.getCellRenderer(row, col);

                Object value = table.getValueAt(row, col) + "   ";

                Component comp = rend.getTableCellRendererComponent(
                        table, value, false, false, row, col);

                maxwidth = Math.max(comp.getPreferredSize().width, maxwidth);
            }

            TableColumn column = columnModel.getColumn(col);
            column.setPreferredWidth(maxwidth);
        }
    }
}
