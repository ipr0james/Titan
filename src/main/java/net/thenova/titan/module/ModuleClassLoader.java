package net.thenova.titan.module;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2020 ipr0james
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public final class ModuleClassLoader extends URLClassLoader {

    private final Map<String, Class<?>> classes = new HashMap<>();

    public ModuleClassLoader(final URL url, final ClassLoader parent) {
        super(new URL[]{ url }, parent);
    }

    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException {
        return this.findClass(name, true);
    }

    /**
     * Check the cache for the fetched class or check the loader.
     * If global, check all other modules for the class
     *
     * @param name - String
     * @param checkGlobal - Boolean
     * @return - Class
     * @throws ClassNotFoundException - Exception
     */
    Class<?> findClass(final String name, final boolean checkGlobal) throws ClassNotFoundException {
        Class<?> result = this.classes.get(name);
        if(result == null) {
            if(checkGlobal) {
                result = ModuleManager.INSTANCE.getClassByName(name);
            }
        }

        if(result == null) {
            result = super.findClass(name);
        }

        if(result != null) {
            this.classes.put(name, result);
        }

        return result;
    }
}
