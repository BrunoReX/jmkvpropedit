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

import org.ini4j.Registry.Key;

class BasicRegistryKey extends BasicProfileSection implements Registry.Key
{
    private static final long serialVersionUID = -1390060044244350928L;
    private static final String META_TYPE = "type";

    public BasicRegistryKey(BasicRegistry registry, String name)
    {
        super(registry, name);
    }

    @Override public Key getChild(String key)
    {
        return (Key) super.getChild(key);
    }

    @Override public Key getParent()
    {
        return (Key) super.getParent();
    }

    @Override public Registry.Type getType(Object key)
    {
        return (Registry.Type) getMeta(META_TYPE, key);
    }

    @Override public Registry.Type getType(Object key, Registry.Type defaultType)
    {
        Registry.Type type = getType(key);

        return (type == null) ? defaultType : type;
    }

    @Override public Key addChild(String key)
    {
        return (Key) super.addChild(key);
    }

    @Override public Key lookup(String... path)
    {
        return (Key) super.lookup(path);
    }

    @Override public Registry.Type putType(String key, Registry.Type type)
    {
        return (Registry.Type) putMeta(META_TYPE, key, type);
    }

    @Override public Registry.Type removeType(Object key)
    {
        return (Registry.Type) removeMeta(META_TYPE, key);
    }
}
