package net.thenova.titan.module.module.expansion;

import net.thenova.titan.module.module.ModuleInstance;

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
public interface ExpansionLoader {

    String name();

    Class<? extends Expansion> expansion();

    /**
     * Handle loading of a module
     *
     * @param instance ModuleInstance being enabled
     * @return Return enable success
     */
    boolean enable(final ModuleInstance instance, final Expansion expansion);

    /**
     * Handle reloading of a module
     *
     * @param instance ModuleInstance being reloaded
     */
    void reload(final ModuleInstance instance, final Expansion expansion);

    /**
     * Handle unloading of a module
     *
     * @param instance ModuleInstance being unloaded
     */
    void unload(final ModuleInstance instance, final Expansion expansion);
}
