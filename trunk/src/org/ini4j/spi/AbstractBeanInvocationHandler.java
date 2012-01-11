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

import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class AbstractBeanInvocationHandler implements InvocationHandler
{
    private static final String PROPERTY_CHANGE_LISTENER = "PropertyChangeListener";
    private static final String VETOABLE_CHANGE_LISTENER = "VetoableChangeListener";
    private static final String ADD_PREFIX = "add";
    private static final String READ_PREFIX = "get";
    private static final String REMOVE_PREFIX = "remove";
    private static final String READ_BOOLEAN_PREFIX = "is";
    private static final String WRITE_PREFIX = "set";
    private static final String HAS_PREFIX = "has";

    private static enum Prefix
    {
        READ(READ_PREFIX),
        READ_BOOLEAN(READ_BOOLEAN_PREFIX),
        WRITE(WRITE_PREFIX),
        ADD_CHANGE(ADD_PREFIX + PROPERTY_CHANGE_LISTENER),
        ADD_VETO(ADD_PREFIX + VETOABLE_CHANGE_LISTENER),
        REMOVE_CHANGE(REMOVE_PREFIX + PROPERTY_CHANGE_LISTENER),
        REMOVE_VETO(REMOVE_PREFIX + VETOABLE_CHANGE_LISTENER),
        HAS(HAS_PREFIX);
        private int _len;
        private String _value;

        private Prefix(String value)
        {
            _value = value;
            _len = value.length();
        }

        public static Prefix parse(String str)
        {
            Prefix ret = null;

            for (Prefix p : values())
            {
                if (str.startsWith(p.getValue()))
                {
                    ret = p;

                    break;
                }
            }

            return ret;
        }

        public String getTail(String input)
        {
            return Introspector.decapitalize(input.substring(_len));
        }

        public String getValue()
        {
            return _value;
        }
    }

    private PropertyChangeSupport _pcSupport;
    private Object _proxy;
    private VetoableChangeSupport _vcSupport;

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws PropertyVetoException
    {
        Object ret = null;
        Prefix prefix = Prefix.parse(method.getName());

        if (prefix != null)
        {
            String tail = prefix.getTail(method.getName());

            updateProxy(proxy);
            switch (prefix)
            {

                case READ:
                    ret = getProperty(prefix.getTail(method.getName()), method.getReturnType());
                    break;

                case READ_BOOLEAN:
                    ret = getProperty(prefix.getTail(method.getName()), method.getReturnType());
                    break;

                case WRITE:
                    setProperty(tail, args[0], method.getParameterTypes()[0]);
                    break;

                case HAS:
                    ret = Boolean.valueOf(hasProperty(prefix.getTail(method.getName())));
                    break;

                case ADD_CHANGE:
                    addPropertyChangeListener((String) args[0], (PropertyChangeListener) args[1]);
                    break;

                case ADD_VETO:
                    addVetoableChangeListener((String) args[0], (VetoableChangeListener) args[1]);
                    break;

                case REMOVE_CHANGE:
                    removePropertyChangeListener((String) args[0], (PropertyChangeListener) args[1]);
                    break;

                case REMOVE_VETO:
                    removeVetoableChangeListener((String) args[0], (VetoableChangeListener) args[1]);
                    break;

                default:
                    break;
            }
        }

        return ret;
    }

    protected abstract Object getPropertySpi(String property, Class<?> clazz);

    protected abstract void setPropertySpi(String property, Object value, Class<?> clazz);

    protected abstract boolean hasPropertySpi(String property);

    protected synchronized Object getProperty(String property, Class<?> clazz)
    {
        Object o;

        try
        {
            o = getPropertySpi(property, clazz);
            if (o == null)
            {
                o = zero(clazz);
            }
            else if (clazz.isArray() && (o instanceof String[]) && !clazz.equals(String[].class))
            {
                String[] str = (String[]) o;

                o = Array.newInstance(clazz.getComponentType(), str.length);
                for (int i = 0; i < str.length; i++)
                {
                    Array.set(o, i, parse(str[i], clazz.getComponentType()));
                }
            }
            else if ((o instanceof String) && !clazz.equals(String.class))
            {
                o = parse((String) o, clazz);
            }
        }
        catch (Exception x)
        {
            o = zero(clazz);
        }

        return o;
    }

    protected synchronized void setProperty(String property, Object value, Class<?> clazz) throws PropertyVetoException
    {
        boolean pc = (_pcSupport != null) && _pcSupport.hasListeners(property);
        boolean vc = (_vcSupport != null) && _vcSupport.hasListeners(property);
        Object oldVal = null;
        Object newVal = ((value != null) && clazz.equals(String.class) && !(value instanceof String)) ? value.toString() : value;

        if (pc || vc)
        {
            oldVal = getProperty(property, clazz);
        }

        if (vc)
        {
            fireVetoableChange(property, oldVal, value);
        }

        setPropertySpi(property, newVal, clazz);
        if (pc)
        {
            firePropertyChange(property, oldVal, value);
        }
    }

    protected synchronized Object getProxy()
    {
        return _proxy;
    }

    protected synchronized void addPropertyChangeListener(String property, PropertyChangeListener listener)
    {
        if (_pcSupport == null)
        {
            _pcSupport = new PropertyChangeSupport(_proxy);
        }

        _pcSupport.addPropertyChangeListener(property, listener);
    }

    protected synchronized void addVetoableChangeListener(String property, VetoableChangeListener listener)
    {
        if (_vcSupport == null)
        {
            _vcSupport = new VetoableChangeSupport(_proxy);
        }

        _vcSupport.addVetoableChangeListener(property, listener);
    }

    protected synchronized void firePropertyChange(String property, Object oldValue, Object newValue)
    {
        if (_pcSupport != null)
        {
            _pcSupport.firePropertyChange(property, oldValue, newValue);
        }
    }

    protected synchronized void fireVetoableChange(String property, Object oldValue, Object newValue) throws PropertyVetoException
    {
        if (_vcSupport != null)
        {
            _vcSupport.fireVetoableChange(property, oldValue, newValue);
        }
    }

    protected synchronized boolean hasProperty(String property)
    {
        boolean ret;

        try
        {
            ret = hasPropertySpi(property);
        }
        catch (Exception x)
        {
            ret = false;
        }

        return ret;
    }

    protected Object parse(String value, Class<?> clazz) throws IllegalArgumentException
    {
        return BeanTool.getInstance().parse(value, clazz);
    }

    protected synchronized void removePropertyChangeListener(String property, PropertyChangeListener listener)
    {
        if (_pcSupport != null)
        {
            _pcSupport.removePropertyChangeListener(property, listener);
        }
    }

    protected synchronized void removeVetoableChangeListener(String property, VetoableChangeListener listener)
    {
        if (_vcSupport != null)
        {
            _vcSupport.removeVetoableChangeListener(property, listener);
        }
    }

    protected Object zero(Class<?> clazz)
    {
        return BeanTool.getInstance().zero(clazz);
    }

    private synchronized void updateProxy(Object value)
    {
        if (_proxy == null)
        {
            _proxy = value;
        }
    }
}
