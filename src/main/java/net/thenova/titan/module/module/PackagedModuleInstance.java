package net.thenova.titan.module.module;

import net.thenova.titan.Titan;
import net.thenova.titan.module.module.data.ModuleDescriptionFile;
import org.apache.commons.lang3.exception.ExceptionUtils;

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
public class PackagedModuleInstance extends ModuleInstance{

    public PackagedModuleInstance(final Module module) {
        super(null, new ModuleDescriptionFile(), null);

        super.module = module;
    }

    @Override
    public boolean load() {
        try {
            this.module.load();
        } catch (final Throwable ex) {
            Titan.INSTANCE.getLogger().info("[PackagedModuleInstance] [load] - Failed to enable for %s\n%s",
                    this.module.getClass().getName(),
                    ExceptionUtils.getStackTrace(ex));
            return false;
        }

        return true;
    }
}
