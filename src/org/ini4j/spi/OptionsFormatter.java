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
import java.io.Writer;

public class OptionsFormatter extends AbstractFormatter implements OptionsHandler
{
    public static OptionsFormatter newInstance(Writer out, Config config)
    {
        OptionsFormatter instance = newInstance();

        instance.setOutput((out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out));
        instance.setConfig(config);

        return instance;
    }

    public void endOptions()
    {
        getOutput().flush();
    }

    public void startOptions()
    {
        assert true;
    }

    private static OptionsFormatter newInstance()
    {
        return ServiceFinder.findService(OptionsFormatter.class);
    }
}
