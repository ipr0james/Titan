package net.thenova.titan.module.module;

import de.arraying.kotys.JSONArray;
import de.arraying.lumberjack.LLogger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.thenova.titan.Titan;
import net.thenova.titan.module.ModuleClassLoader;
import net.thenova.titan.module.ModuleManager;
import net.thenova.titan.module.module.data.ModuleDescriptionFile;
import net.thenova.titan.module.module.expansion.Expansion;
import net.thenova.titan.module.module.expansion.ExpansionLoader;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
@Getter
@RequiredArgsConstructor
public class ModuleInstance {
    public enum Status {
        NON,
        MISSING_DEPENDENCY,
        ENABLED,
        DISABLED
    }

    // Used for the module loading section
    private final File jarFile;
    private final ModuleDescriptionFile descriptionFile;

    private final ModuleClassLoader loader;

    // Used for the module itself
    protected Module module;

    // Used during loading/init phases
    @Setter private Status status = Status.NON;

    public boolean load() {
        final LLogger logger = Titan.INSTANCE.getLogger();
        try {
            this.module = (Module) Class.forName(this.descriptionFile.getMain(), true, this.loader)
                    .getConstructor()
                    .newInstance();
        } catch (final InstantiationException
                | IllegalAccessException
                | NoSuchMethodException
                | InvocationTargetException ex) {
            logger.info("[ModuleInstance] [load] - Module '%s' failed to initialise Module instance\n%s",
                    this.jarFile.getName(),
                    ExceptionUtils.getStackTrace(ex));
            return false;
        } catch (final ClassNotFoundException ex) {
            logger.info("[ModuleInstance] [load] - Main class not found when loading. This should have failed before dumb...");
            return false;
        } catch (final Exception ex) {
            logger.info("[ModuleInstance] [load] - Module '%s' has failed to load.\n%s",
                    this.jarFile.getName(),
                    ExceptionUtils.getStackTrace(ex));
            return false;
        }

        try {
            if(this.descriptionFile.getLoader() != null) {
                final ExpansionLoader loader = (ExpansionLoader) Class.forName(this.descriptionFile.getLoader(), true, this.loader)
                        .getConstructor()
                        .newInstance();

                logger.info("[ModuleInstance] [load] - Module '%s' has loaded an expansion loader '%s'",
                        this.descriptionFile.getName(),
                        loader.name());

                ModuleManager.INSTANCE.addLoader(this.module, loader);
            }
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            logger.info("[ModuleInstance] [load] - Module '%s' failed to initialise ModuleLoader instance\n%s",
                    this.jarFile.getName(),
                    ExceptionUtils.getStackTrace(ex));
            return false;
        } catch (final ClassNotFoundException ex) {
            logger.info("[ModuleInstance] [load] - Loader class not found when loading. This should have failed before dumb...");
            return false;
        } catch (final Throwable ex) {
            logger.info("[ModuleInstance] [load] - Module '%s' has failed to load ModuleLoader.\n%s",
                    this.jarFile.getName(),
                    ExceptionUtils.getStackTrace(ex));
            return false;
        }

        try {
            this.module.load();
        } catch (final Throwable ex) {
            logger.info("[ModuleInstance] [load] - Module '%s' failed to load(), check debug for info.\n%s",
                    this.descriptionFile.getName(),
                    ExceptionUtils.getStackTrace(ex));
            return false;
        }

        return true;
    }

    /**
     * Enable the module
     */
    public boolean enable() {
        final LLogger logger = Titan.INSTANCE.getLogger();
        logger.info("[ModuleInstance] - Attempting enable() for '%s'",
                this.descriptionFile.getName());

        try {
            this.module.enable();

            if(this.module.expansions() != null && !this.module.expansions().isEmpty()) {
                for (final Expansion expansion : this.module.expansions()) {
                    final ExpansionLoader failed = ModuleManager.INSTANCE.getLoaders().values().stream()
                            .filter(loader -> loader.expansion().isAssignableFrom(expansion.getClass()))
                            .filter(loader -> !loader.enable(ModuleInstance.this, expansion))
                            .findFirst()
                            .orElse(null);

                    if(failed != null ) {
                        logger.info("[ModuleInstance] - enable() failed for module '%s' with loader '%s'",
                                this.descriptionFile.getName(),
                                failed.name());
                        this.status = Status.NON;
                        return false;
                    }
                }
            }

            this.status = Status.ENABLED;
            logger.info("[ModuleInstance] - enable() success for '%s'",
                    this.descriptionFile.getName());
            return true;
        } catch (final Throwable ex) {
            this.status = Status.NON;

            logger.info("[ModuleInstance] - Failed to enable module %s\n%s",
                    this.descriptionFile.getName(),
                    ExceptionUtils.getStackTrace(ex));
            return false;
        }
    }

    public void shutdown() {
        final LLogger logger = Titan.INSTANCE.getLogger();
        logger.info("[ModuleInstance] - Attempting shutdown() for '%s'",
                this.descriptionFile.getName());
        try {
            this.module.shutdown();
            if(this.module.expansions() != null && !this.module.expansions().isEmpty()) {
                for (final Expansion expansion : this.module.expansions()) {
                    ModuleManager.INSTANCE.getLoaders()
                            .values()
                            .stream()
                            .filter(loader -> loader.expansion().isAssignableFrom(expansion.getClass()))
                            .forEach(loader -> {
                                logger.info("[ModuleInstance] - Attempting shutdown() for module '%s' with expansion loader '%s'",
                                        this.descriptionFile.getName(),
                                        loader.name());
                                loader.unload(ModuleInstance.this, expansion);
                            });
                }
            }

            this.status = Status.DISABLED;
        } catch (final Throwable ex) {
            this.status = Status.NON;

            logger.info("[ModuleInstance] - Failed to disable module %s\n%s",
                    this.descriptionFile.getName(),
                    ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * Check for any missing dependencies, returning a set of names and disabling the module is any are found
     *
     * @return Set of ModuleInstance
     */
    public final Set<String> missingDependencies(final Collection<ModuleInstance> instances) {
        final JSONArray dependencies = this.descriptionFile.getDependency();

        if(dependencies == null) {
            // No dependencies
            return Collections.emptySet();
        }

        final Set<String> missing = new HashSet<>();
        for(int i = 0; i < dependencies.length(); i++) {
            final String name = dependencies.string(i);
            if(instances.stream().noneMatch(instance -> instance.getDescriptionFile().getName().equalsIgnoreCase(name))) {
                missing.add(name);
                this.status = Status.MISSING_DEPENDENCY;
            }
        }

        return missing;
    }
}
