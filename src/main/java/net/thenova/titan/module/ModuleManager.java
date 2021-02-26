package net.thenova.titan.module;

import de.arraying.kotys.JSON;
import de.arraying.lumberjack.LLogger;
import lombok.Getter;
import net.thenova.titan.Titan;
import net.thenova.titan.module.module.Module;
import net.thenova.titan.module.module.ModuleInstance;
import net.thenova.titan.module.module.PackagedModuleInstance;
import net.thenova.titan.module.module.data.ModuleDescriptionFile;
import net.thenova.titan.module.module.expansion.ExpansionLoader;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

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
public enum ModuleManager {
    INSTANCE;

    private enum ModuleStatus {
        DEPEND,
        LOAD,
        ENABLE
    }

    private final Set<ModuleInstance> modules = new HashSet<>();
    private final Set<ModuleClassLoader> classLoaders = new HashSet<>();

    private final Map<Module, ExpansionLoader> loaders = new HashMap<>();

    private LLogger logger;

    private File directoryModules;
    private File directoryData;

    private boolean initialised = false;

    /**
     * Used for initialisation of Titan using a non-packaged format.
     *
     * @param type Initialisation type
     */
    public final void init(final Titan.Type type) {
        this.logger = Titan.INSTANCE.getLogger();

        if(type == Titan.Type.PACKAGED) {
            this.logger.info("[ModuleManager] - Use init(Map<Module, ExpansionLoader>) for a packaged Module");
            return;
        }

        if(this.initialised) {
            this.logger.info("[ModuleManager] - ModuleManager has already been initialised...");
            return;
        }

        this.initialised = true;

        if(!(this.directoryModules = new File(Titan.INSTANCE.getDataRoot(), "module" + File.separator + "modules")).exists()) {
            this.logger.info("[ModuleManager] - Modules directory was %s created",
                    (this.directoryModules.mkdirs() ? "successfully" : "not"));
        }

        if(!(this.directoryData = new File(Titan.INSTANCE.getDataRoot(), "module" + File.separator + "data")).exists()) {
            this.logger.info("[ModuleManager] - Modules data folder was %s created",
                    (this.directoryData.mkdirs() ? "successfully" : "not"));
        }

        final File[] files;
        if((files = this.directoryModules.listFiles()) == null || files.length == 0) {
            this.logger.info("[ModuleManager] - Titan modules folder was empty. No modules have been loaded.");
            return;
        }

        this.logger.info("[ModuleManager] - Starting module jar loading...");

        Arrays.stream(files).forEach(file -> {
            if(!file.getName().endsWith(".jar")) {
                this.logger.info("[ModuleManager] - Failed to load '%s' as this is not a jar file.",
                        file.getName());
                return;
            }

            try {
                this.logger.info("[ModuleManager] - Module '%s' jar load status: %s",
                        file.getName(),
                        this.loadJar(file) ? "success" : "failed");
            } catch (final Throwable ex) {
                this.logger.info("[ModuleManager] - Failed to load '%s'. Please check debug log for errors.",
                        file.getName());
                Titan.INSTANCE.getLogger().info("[ModuleManager] - Error loading '%s'\n%s",
                        file.getName(),
                        ExceptionUtils.getStackTrace(ex));
            }
        });

        this.logger.info("[ModuleManager] - Module loading completed");
        this.checkDependencies(ModuleStatus.DEPEND);
        this.load();
    }

    /**
     * Used for initialisation of Titan using a packaged format.
     *
     * @param type Initialisation type
     * @param modules Which modules are packaged and being loaded
     */
    public final void init(final Titan.Type type, final Map<Module, ExpansionLoader> modules) {
        this.logger = Titan.INSTANCE.getLogger();

        if(type != Titan.Type.PACKAGED) {
            this.logger.info("[ModuleManager] - Use init() for loading as a non packaged jar.");
            return;
        }

        if(this.initialised) {
            this.logger.info("[ModuleManager] - ModuleManager has already been initialised...");
            return;
        }

        this.initialised = true;
        if(!(this.directoryData = new File(Titan.INSTANCE.getDataRoot(), "configs")).exists()) {
            this.logger.info("[ModuleManager] - Creation of 'configs' folder: %s",
                    this.directoryData.mkdirs());
        }

        if(modules.isEmpty()) {
            this.logger.info("[ModuleManager] - No modules were found.");
            return;
        }

        this.logger.info("[ModuleManager] - Beginning loading of Packaged modules...");
        this.logger.info("[ModuleManager] - Modules found: [%s]",
                modules.keySet().stream()
                        .map(module -> module.getClass().getName())
                        .collect(Collectors.joining(", ")));

        modules.forEach((module, loader) -> {
            this.modules.add(new PackagedModuleInstance(module));
            if(loader != null) {
                this.loaders.put(module, loader);
            }
        });

        this.load();
    }

    /**
     * Load a module from its Jar File
     *
     * @param file File of the Jar
     * @return Return whether loading stage was successful
     */
    private boolean loadJar(final File file) {
        final ModuleDescriptionFile descriptionFile = this.getDescription(file);
        if(descriptionFile == null) {
            return false;
        }

        final ModuleClassLoader classLoader;
        try {
            classLoader = new ModuleClassLoader(file.toURI().toURL(), this.getClass().getClassLoader());
        } catch (final MalformedURLException ex) {
            this.logger.info("[ModuleInstance] [loadJar] - Error loading URLClassLoader for '%s'\n%s",
                    file.getName(),
                    ExceptionUtils.getStackTrace(ex));
            return false;
        }

        try {
            Class.forName(descriptionFile.getMain(), false, classLoader);
        } catch (final ClassNotFoundException ex) {
            this.logger.info("[ModuleManager] [loadJar] - Module '%s' has an invalid main class, path: %s",
                    file.getName(),
                    descriptionFile.getMain());
            return false;
        } catch (final NoClassDefFoundError ignored) { }

        try {
            if(descriptionFile.getLoader() != null) {
                Class.forName(descriptionFile.getLoader(), false, classLoader);
            }
        } catch (final ClassNotFoundException ex) {
            this.logger.info("[ModuleManager] [loadJar] - Module '%s' has an invalid Expansion Loader class, path: %s",
                    file.getName(),
                    descriptionFile.getLoader());
            return false;
        }

        this.modules.add(new ModuleInstance(file, descriptionFile, classLoader));
        this.classLoaders.add(classLoader);
        return true;
    }

    /**
     * Attempt to load all Modules in order
     */
    private void load() {
        for(final ModuleInstance instance : this.getOrdered()) {
            if(!instance.load()) {
                this.modules.remove(instance);
                if(instance.getModule() != null) {
                    this.loaders.remove(instance.getModule());
                }

                this.checkDependencies(ModuleStatus.LOAD);
                this.load();
                break;
            }
        }
    }

    /**
     * Enable all modules
     */
    public final void enable() {
        for(final ModuleInstance instance : this.getOrdered()) {
            if(!instance.enable()) {
                this.modules.remove(instance);
                if(instance.getModule() != null) {
                    this.loaders.remove(instance.getModule());
                }
                try {
                    instance.shutdown();
                } catch (final Throwable ignored) {}

                this.checkDependencies(ModuleStatus.ENABLE);
                this.enable();
                break;
            }
        }
        this.logger.info("[ModuleManager] - Modules {%s} have now been enabled.",
                this.modules.stream()
                        .map(instance -> instance.getDescriptionFile().getName())
                        .collect(Collectors.joining(", ")));
    }

    public final void shutdown() {
        this.modules.forEach(ModuleInstance::shutdown);
    }

    /**
     * Check for Modules having their dependencies.
     *
     * @param status Current status of Module to check against
     */
    private void checkDependencies(final ModuleStatus status) {
        boolean missing = false;
        for(final ModuleInstance instance : new HashSet<>(this.modules)) {
            final Set<String> set = instance.missingDependencies(this.modules);
            if(set.isEmpty()) {
                continue;
            }

            this.modules.remove(instance);

            if(instance.getModule() != null) {
                this.loaders.remove(instance.getModule());
            }

            final String message;
            switch(status) {
                case DEPEND:
                    message = "[ModuleManager] - Module '%s' is missing dependencies {%s} and has now been unloaded.";
                    break;
                case LOAD:
                    message = "[ModuleManager] - Module '%s' has failed to load as dependencies {%s} failed to load.";
                    break;
                case ENABLE:
                    message = "[ModuleManager] - Module '%s' has failed to enable as dependencies {%s} failed to enable.";
                    break;
                default:
                    message = "ERROR";
            }
            this.logger.info(message, instance.getDescriptionFile().getName(), String.join(", ", set));
            missing = true;
        }

        if(missing) {
            this.checkDependencies(status);
        }
    }

    /**
     * Retrieve the module.json file from the specified File, handle exceptions for info
     *
     * @param file File to check/fetch from
     * @return Return the object of the file or throw exception and return null
     */
    private ModuleDescriptionFile getDescription(final File file) {
        try {
            final JarFile jar = new JarFile(file);
            final JarEntry entry = jar.getJarEntry("module.json");

            if (entry == null) {
                throw new FileNotFoundException("module.json");
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8))) {
                return new JSON(br.lines().collect(Collectors.joining())).marshal(ModuleDescriptionFile.class);
            }
        } catch (final IOException ex) {
            if(ex instanceof FileNotFoundException) {
                this.logger.info("[ModuleManager] [getDescription] - Module '%s' did not contain a module.json file",
                        file.getName());
            } else {
                this.logger.info("[ModuleManager] [getDescription] - Error loading ModuleDescriptionFile for '%s'\n%s",
                        file.getName(),
                        ExceptionUtils.getStackTrace(ex));
            }
            return null;
        }
    }

    /**
     * Add an expansion loader
     *
     * @param module Which module the loader belongs to
     * @param loader ExpansionLoader itself
     */
    public final void addLoader(final Module module, final ExpansionLoader loader) {
        this.loaders.put(module, loader);
    }

    /**
     * Return an ordered list of Modules for which should be handled first
     *
     * @return List of all modules in order
     */
    private List<ModuleInstance> getOrdered() {
        final List<ModuleInstance> ordered = new ArrayList<>();
        final Set<ModuleInstance> modules = new HashSet<>(this.modules);

        while(!modules.isEmpty()) {
            new HashSet<>(modules).stream()
                    .filter(instance -> instance.missingDependencies(ordered).isEmpty())
                    .forEach(instance -> {
                        ordered.add(instance);
                        modules.remove(instance);
                    });
        }

        return ordered;
    }

    /**
     * Check all current cached loaders for a specific class
     *
     * @param name Path to class
     * @return Class if found or null
     */
    public final Class<?> getClassByName(final String name) {
        Class<?> clazz = null;
        for (final ModuleClassLoader loader : this.classLoaders) {
            try {
                clazz = loader.findClass(name, false);
            } catch (ClassNotFoundException ignored) {}

            if (clazz != null) {
                return clazz;
            }
        }

        return null;
    }
}
