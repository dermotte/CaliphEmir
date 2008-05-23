/*
 * This file is part of Caliph & Emir.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2005 by Werner Klieber (werner@klieber.info)
 * http://caliph-emir.sourceforge.net
 */
package at.wklieber.gui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

public abstract class GenericListener {
    /**
     * A convenient version of <CODE>create(listenerMethod, targetObject,
     * targetMethod)</CODE>. * This version looks up the listener and target Methods,
     * so you don't have to.
     */
    public static Object create(Class listenerInterface,
                                String listenerMethodName, Object target, String targetMethodName) {
        Method listenerMethod = getListenerMethod(listenerInterface, listenerMethodName);
        Method targetMethod = getTargetMethod(target, targetMethodName,
                        listenerMethod.getParameterTypes());
        return create(listenerMethod, target, targetMethod);
    }

    /**
     * Return an instance of a class that implements the
     * interface that contains * the declaration for <CODE>listenerMethod</CODE>. In
     * this new class, * <CODE>listenerMethod</CODE> will apply
     * <CODE>target.targetMethod</CODE> * to the incoming Event.
     */
    public static Object create(final Method listenerMethod, final Object target, final Method targetMethod) {
        /** * The implementation of the create method uses the Dynamic
         Proxy API * introduced in JDK 1.3. * * Create an instance of the DefaultInvoker
         and override the invoke * method to handle the invoking the targetMethod on the
         target. */
        InvocationHandler handler = new DefaultInvoker() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // Send all methods execept for the targetMethod to
                // the superclass for handling.
                if (listenerMethod.equals(method)) {
                    return targetMethod.invoke(target, args);
                } else {
                    return super.invoke(proxy, method, args);
                }
            }
        };
        Class cls = listenerMethod.getDeclaringClass();
        ClassLoader cl = cls.getClassLoader();
        return Proxy.newProxyInstance(cl, new Class[]{cls}, handler);
    }

    /**
     * Implementation of the InvocationHandler which handles the basic * object
     * methods.
     */
    private static class DefaultInvoker implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws
                Throwable {
            if (method.getDeclaringClass() == Object.class) {
                String methodName
                        = method.getName();
                if (methodName.equals("hashCode")) {
                    return
                    proxyHashCode(proxy);
                } else if (methodName.equals("equals")) {
                    return
                    proxyEquals(proxy, args[0]);
                } else if (methodName.equals("toString")) {
                    return
                    proxyToString(proxy);
                }
            } // Although listener methods are supposed to be void, we
            // allow for any return type here and produce null/0/false // as appropriate.
            return nullValueOf(method.getReturnType());
        }

        protected Integer
        proxyHashCode(Object proxy) {
            return new
            Integer(System.identityHashCode(proxy));
        }

        protected Boolean proxyEquals(Object
                                      proxy, Object other) {
            return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
        }

        protected String proxyToString(Object proxy) {
            return proxy.getClass().getName()
                           + '@' + Integer.toHexString(proxy.hashCode());
        }

        private final static Character
        char_0 = new Character((char) 0);
        private final static Byte byte_0 = new
                Byte((byte) 0);

        private final static Object nullValueOf(Class rt) {
            if
            (!rt.isPrimitive()) {
                return null;
            } else if (rt == void.class) {
                return null;
            } else if (rt == boolean.class) {
                return Boolean.FALSE;
            } else if (rt ==
                               char.class) {
                return char_0;
            } else { // this will convert to any other kind of  number
                return byte_0;
            }
        }
    } /* Helper methods for "EZ" version of create(): */

    private static Method getListenerMethod(Class listenerInterface, String
                                            listenerMethodName) {
        // given the arguments to create(), find out which  listener is desired:
        Method[] m = listenerInterface.getMethods();
        Method result = null;
        for (int i = 0; i < m.length; i++) {
            if
            (!listenerMethodName.equals(m[i].getName()))
                continue;
            if (result != null) {
                throw new RuntimeException("ambiguous method: " + m[i] + " vs. " + result);
            }
            result =
                    m[i];
        }
        if (result == null) {
            throw new RuntimeException("no such method" + listenerMethodName +
                    "in" + listenerInterface);
        }
        return result;
    }

    private static Method
    getTargetMethod(Object
                    target, String
                    targetMethodName, Class[]
                    parameterTypes) {
        Method[] m = target.getClass().getMethods();
        Method result =
                null;
        eachMethod:
        for (int i = 0; i < m.length; i++) {
            if
            (!targetMethodName.equals(m[i].getName()))
                continue eachMethod;
            Class[] p =
                    m[i].getParameterTypes();
            if (p.length != parameterTypes.length)
                continue
                eachMethod;
            for (int j = 0; j < p.length; j++) {
                if
                (!p[j].isAssignableFrom(parameterTypes[j]))
                    continue eachMethod;
            }
            if (result !=
                        null) {
                throw new RuntimeException("ambiguous method: " + m[i] + " vs. " + result);
            }
            result = m[i];
        }
        if (result == null) {
            throw new RuntimeException("no such method: " + targetMethodName +
                    " in " + target.getClass());
        }
        Method publicResult =
                raiseToPublicClass(result);
        if (publicResult != null) result = publicResult;
        return result;
    }

    private static Method
    raiseToPublicClass(Method
                       m) {
        Class c = m.getDeclaringClass();

        //TODO:if ( Modifier.isPublic(m.getModifiers()) & amp;&amp;
        if (Modifier.isPublic(m.getModifiers()) && Modifier.isPublic(c.getModifiers()))
            return m; // yes! // search for a public version which  m overrides
        Class sc = c.getSuperclass();
        if (sc != null) {
            Method
                    sm = raiseToPublicClass(m, sc);
            if (sm != null) return sm;
        }
        Class[] ints =
                c.getInterfaces();
        for (int i = 0; i < ints.length; i++) {
            Method im = raiseToPublicClass(m, ints[i]);
            if (im != null) return im;
        } // no public  version of  m here
        return null;
    }

    private static Method raiseToPublicClass(Method m, Class c) {
        try {
            Method sm = c.getMethod(m.getName(), m.getParameterTypes());
            return raiseToPublicClass(sm);
        } catch (NoSuchMethodException ee) {
            return null;
        }
    }

    private GenericListener() {
    }
}
