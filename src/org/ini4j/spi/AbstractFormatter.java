/*
 * Copyright 2005,2009 Ivan SZKIBA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ini4j.spi;

import org.ini4j.Config;

import java.io.PrintWriter;

abstract class AbstractFormatter implements HandlerBase
{
    private static final char OPERATOR = '=';
    private static final char COMMENT = '#';
    private static final char SPACE = ' ';
    private Config _config = Config.getGlobal();
    private boolean _header = true;
    private PrintWriter _output;

    @Override public void handleComment(String comment)
    {
        if (getConfig().isComment() && (!_header || getConfig().isHeaderComment()) && (comment != null) && (comment.length() != 0))
        {
            for (String line : comment.split(getConfig().getLineSeparator()))
            {
                getOutput().print(COMMENT);
                getOutput().print(line);
                getOutput().print(getConfig().getLineSeparator());
            }

            if (_header)
            {
                getOutput().print(getConfig().getLineSeparator());
            }
        }

        _header = false;
    }

    @Override public void handleOption(String optionName, String optionValue)
    {
        if (getConfig().isStrictOperator())
        {
            if (getConfig().isEmptyOption() || (optionValue != null))
            {
                getOutput().print(escapeFilter(optionName));
                getOutput().print(OPERATOR);
            }

            if (optionValue != null)
            {
                getOutput().print(escapeFilter(optionValue));
            }

            if (getConfig().isEmptyOption() || (optionValue != null))
            {
                getOutput().print(getConfig().getLineSeparator());
            }
        }
        else
        {
            String value = ((optionValue == null) && getConfig().isEmptyOption()) ? "" : optionValue;

            if (value != null)
            {
                getOutput().print(escapeFilter(optionName));
                getOutput().print(SPACE);
                getOutput().print(OPERATOR);
                getOutput().print(SPACE);
                getOutput().print(escapeFilter(value));
                getOutput().print(getConfig().getLineSeparator());
            }
        }

        setHeader(false);
    }

    protected Config getConfig()
    {
        return _config;
    }

    protected void setConfig(Config value)
    {
        _config = value;
    }

    protected PrintWriter getOutput()
    {
        return _output;
    }

    protected void setOutput(PrintWriter value)
    {
        _output = value;
    }

    void setHeader(boolean value)
    {
        _header = value;
    }

    String escapeFilter(String input)
    {
        return getConfig().isEscape() ? EscapeTool.getInstance().escape(input) : input;
    }
}
