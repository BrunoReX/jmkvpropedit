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
package org.ini4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class BasicProfileSection extends BasicOptionMap implements Profile.Section
{
    private static final long serialVersionUID = 985800697957194374L;
    private static final String[] EMPTY_STRING_ARRAY = {};
    private static final char REGEXP_ESCAPE_CHAR = '\\';
    private final Pattern _childPattern;
    private final String _name;
    private final BasicProfile _profile;

    protected BasicProfileSection(BasicProfile profile, String name)
    {
        _profile = profile;
        _name = name;
        _childPattern = newChildPattern(name);
    }

    @Override public Profile.Section getChild(String key)
    {
        return _profile.get(childName(key));
    }

    @Override public String getName()
    {
        return _name;
    }

    @Override public Profile.Section getParent()
    {
        Profile.Section ret = null;
        int idx = _name.lastIndexOf(_profile.getPathSeparator());

        if (idx >= 0)
        {
            String name = _name.substring(0, idx);

            ret = _profile.get(name);
        }

        return ret;
    }

    @Override public String getSimpleName()
    {
        int idx = _name.lastIndexOf(_profile.getPathSeparator());

        return (idx < 0) ? _name : _name.substring(idx + 1);
    }

    @Override public Profile.Section addChild(String key)
    {
        String name = childName(key);

        return _profile.add(name);
    }

    @Override public String[] childrenNames()
    {
        List<String> names = new ArrayList<String>();

        for (String key : _profile.keySet())
        {
            if (_childPattern.matcher(key).matches())
            {
                names.add(key.substring(_name.length() + 1));
            }
        }

        return names.toArray(EMPTY_STRING_ARRAY);
    }

    @Override public Profile.Section lookup(String... parts)
    {
        StringBuilder buff = new StringBuilder();

        for (String part : parts)
        {
            if (buff.length() != 0)
            {
                buff.append(_profile.getPathSeparator());
            }

            buff.append(part);
        }

        return _profile.get(childName(buff.toString()));
    }

    @Override public void removeChild(String key)
    {
        String name = childName(key);

        _profile.remove(name);
    }

    @Override boolean isPropertyFirstUpper()
    {
        return _profile.isPropertyFirstUpper();
    }

    @Override void resolve(StringBuilder buffer)
    {
        _profile.resolve(buffer, this);
    }

    private String childName(String key)
    {
        StringBuilder buff = new StringBuilder(_name);

        buff.append(_profile.getPathSeparator());
        buff.append(key);

        return buff.toString();
    }

    private Pattern newChildPattern(String name)
    {
        StringBuilder buff = new StringBuilder();

        buff.append('^');
        buff.append(Pattern.quote(name));
        buff.append(REGEXP_ESCAPE_CHAR);
        buff.append(_profile.getPathSeparator());
        buff.append("[^");
        buff.append(REGEXP_ESCAPE_CHAR);
        buff.append(_profile.getPathSeparator());
        buff.append("]+$");

        return Pattern.compile(buff.toString());
    }
}
