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
import org.ini4j.Profile;
import org.ini4j.Reg;

import org.ini4j.Registry.Key;
import org.ini4j.Registry.Type;

public class RegBuilder extends AbstractProfileBuilder
{
    private Reg _reg;

    public static RegBuilder newInstance(Reg reg)
    {
        RegBuilder instance = newInstance();

        instance.setReg(reg);

        return instance;
    }

    public void setReg(Reg value)
    {
        _reg = value;
    }

    @Override public void handleOption(String rawName, String rawValue)
    {
        String name = (rawName.charAt(0) == EscapeTool.DOUBLE_QUOTE) ? RegEscapeTool.getInstance().unquote(rawName) : rawName;
        TypeValuesPair tv = RegEscapeTool.getInstance().decode(rawValue);

        if (tv.getType() != Type.REG_SZ)
        {
            ((Key) getCurrentSection()).putType(name, tv.getType());
        }

        for (String value : tv.getValues())
        {
            super.handleOption(name, value);
        }
    }

    @Override Config getConfig()
    {
        return _reg.getConfig();
    }

    @Override Profile getProfile()
    {
        return _reg;
    }

    private static RegBuilder newInstance()
    {
        return ServiceFinder.findService(RegBuilder.class);
    }
}
