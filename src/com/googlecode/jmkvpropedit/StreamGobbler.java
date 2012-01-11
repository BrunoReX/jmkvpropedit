package com.googlecode.jmkvpropedit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JTextArea;

public class StreamGobbler extends Thread
{
    private final InputStream is;
    private final JTextArea text;

    public StreamGobbler(InputStream is, JTextArea text)
    {
        this.is = is;
        this.text = text;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ((line = br.readLine()) != null)
            {
                text.append(line + "\n"); // JTextArea.append is thread safe
                text.setCaretPosition(text.getText().length()); // Autoscroll
            }
        }
        catch (IOException ioe)
        {
            text.append(ioe.toString());
            ioe.printStackTrace();  
        }
    }
}