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
import org.ini4j.Options;

public class OptionsBuilder implements OptionsHandler
{
    private boolean _header;
    private String _lastComment;
    private Options _options;

    public static OptionsBuilder newInstance(Options opts)
    {
        OptionsBuilder instance = newInstance();

        instance.setOptions(opts);

        return instance;
    }

    public void setOptions(Options value)
    {
        _options = value;
    }

    @Override public void endOptions()
    {

        // comment only .opt file ...
        if ((_lastComment != null) && _header)
        {
            setHeaderComment();
        }
    }

    @Override public void handleComment(String comment)
    {
        if ((_lastComment != null) && _header)
        {
            setHeaderComment();
            _header = false;
        }

        _lastComment = comment;
    }

    @Override public void handleOption(String name, String value)
    {
        if (getConfig().isMultiOption())
        {
            _options.add(name, value);
        }
        else
        {
            _options.put(name, value);
        }

        if (_lastComment != null)
        {
            if (_header)
            {
                setHeaderComment();
            }
            else
            {
                putComment(name);
            }

            _lastComment = null;
        }

        _header = false;
    }

    @Override public void startOptions()
    {
        if (getConfig().isHeaderComment())
        {
            _header = true;
        }
    }

    protected static OptionsBuilder newInstance()
    {
        return ServiceFinder.findService(OptionsBuilder.class);
    }

    private Config getConfig()
    {
        return _options.getConfig();
    }

    private void setHeaderComment()
    {
        if (getConfig().isComment())
        {
            _options.setComment(_lastComment);
        }
    }

    private void putComment(String key)
    {
        if (getConfig().isComment())
        {
            _options.putComment(key, _lastComment);
        }
    }
}
