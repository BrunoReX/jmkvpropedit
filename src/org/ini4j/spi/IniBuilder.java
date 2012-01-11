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
import org.ini4j.Ini;
import org.ini4j.Profile;

public class IniBuilder extends AbstractProfileBuilder implements IniHandler
{
    private Ini _ini;

    public static IniBuilder newInstance(Ini ini)
    {
        IniBuilder instance = newInstance();

        instance.setIni(ini);

        return instance;
    }

    public void setIni(Ini value)
    {
        _ini = value;
    }

    @Override Config getConfig()
    {
        return _ini.getConfig();
    }

    @Override Profile getProfile()
    {
        return _ini;
    }

    private static IniBuilder newInstance()
    {
        return ServiceFinder.findService(IniBuilder.class);
    }
}
