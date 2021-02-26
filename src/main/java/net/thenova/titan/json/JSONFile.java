package net.thenova.titan.json;

import de.arraying.kotys.JSON;
import de.arraying.kotys.JSONDefaultMarshalFormat;
import lombok.Getter;
import net.thenova.titan.Titan;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
@SuppressWarnings("WeakerAccess")
public final class JSONFile {

    private final String name;

    private File file;
    private JSON json;

    public JSONFile(final ClassLoader loader, final String name) {
        this(loader, "", name);
    }

    /**
     * Creation of JSON Files, handles copying + creation from resources dir
     *
     * @param loader Uses the ClassLoader to retrieve the resources dir of the plugin for copying
     * @param path Folder path for location of JSON File
     * @param name File name
     */
    public JSONFile(final ClassLoader loader, final String path, final String name) {
        this.name = name;
        File file = new File(path, name + ".json");

        // Create the File directory
        if(!file.getParentFile().exists()) {
            Titan.INSTANCE.getLogger().debug("[JSONFile] - Directory '%s' for File '%s' has been created: %b",
                    path,
                    name,
                    file.getParentFile().mkdirs());
        }

        // Create the File, copy if specified
        if(!file.exists()) {
            if(loader == null) {
                try {
                    Titan.INSTANCE.getLogger().debug("[JSONFile] - File '%s' has been created in directory '%s': %b",
                            name,
                            path,
                            file.createNewFile());
                } catch (final IOException ex) {
                    Titan.INSTANCE.getLogger().info("[JSONFile] - Failed to create %s.json\n%s",
                            this.name,
                            ExceptionUtils.getStackTrace(ex));
                }
            } else {
                try {
                    final InputStream stream = this.getResource(loader, file.getName());
                    if(stream == null) {
                        Titan.INSTANCE.getLogger().info("[JSONFile] - Failed to copy '%s' as the resource could not be found.",
                                name);
                        return;
                    }

                    Files.copy(stream, file.toPath());
                } catch (final IOException ex) {
                    Titan.INSTANCE.getLogger().info("[JSONFile] - File '%s' could not be copied.\n%s",
                            file.getName(),
                            ExceptionUtils.getStackTrace(ex));
                }

                file = new File(path, name + ".json");
            }
        }

        // Obtain the JSON from the file
        JSON json;
        try {
            json = new JSON(file);
        } catch (final IOException ex) {
            Titan.INSTANCE.getLogger().info("[JSONFile] - Could not create JSON for file '%s'\n%s", file.getName(), ExceptionUtils.getStackTrace(ex));
            return;
        } catch (final IllegalArgumentException e) {
            json = new JSON();
        }

        this.file = file;
        this.json = json;

        // Handle version control
        if(file.exists() && loader != null) {
            final InputStream stream = this.getResource(loader, this.file.getName());
            if(stream == null) {
                Titan.INSTANCE.getLogger().info("[JSONFile] - Failed to handle Version Control updates for '%s', failed to retrieve resource.", name);
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                final JSON resource = new JSON(br.lines().collect(Collectors.joining()));

                if(resource.has("version-control")) {
                    final JSON vc = resource.json("version-control");

                    if(!this.json.has("version-control")) {
                        this.json.put("version-control", vc);
                        this.check(this.json, resource);
                    }

                    if(vc.decimal("version") > this.json.json("version-control").decimal("version")) {
                        this.json.json("version-control").put("version", vc.decimal("version"));

                        this.check(this.json, resource);
                    }

                    this.save();
                }
            } catch (final IOException ex) {
                Titan.INSTANCE.getLogger().info("[JSONFile] - Failed to load resource as JSON for file '%s'\n%s", this.file.getName(), ExceptionUtils.getStackTrace(ex));
            }
        }
    }

    public static JSONFile create(final JSONFileData file) {
        return new JSONFile(file.loader(), file.path(), file.name());
    }

    /**
     * Add any missing values from the provided JSON
     *
     * @param json JSON to be added
     */
    public final void addFromFile(final JSON json) {
        this.check(this.json, json);
    }

    /**
     * Return a file from a loaded jars resources
     *
     * @param loader Resource location
     * @param filename - Resource name
     * @return InputStream for internal usage
     */
    private InputStream getResource(final ClassLoader loader, final String filename) {
        try {
            final URL url = loader.getResource(filename);
            if (url == null) {
                Titan.INSTANCE.getLogger().info("[JSONFile] - Failed to retrieve resource '%s', this could not be created.",
                        filename);
                return null;
            }

            final URLConnection connection = url.openConnection();

            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (final IOException ex) {
            Titan.INSTANCE.getLogger().info("[JSONFile] - Error occurred receiving resource for '%s'.\n%s",
                    filename,
                    ExceptionUtils.getStackTrace(ex));
            return null;
        }
    }

    /**
     * Compare 2 JSON objects for differences in keys
     *
     * @param json JSON to use as reference
     * @param resource JSON to check for difference
     */
    private void check(final JSON json, final JSON resource) {
        resource.raw().forEach((key, value) -> {
            if (!json.has(key)) {
                json.put(key, value);
            }

            if(value instanceof JSON) {
                this.check(json.json(key), resource.json(key));
            }
        });
    }

    /**
     * Add a value to the file if it is not currently present
     *
     * @param path Path to be used
     * @param value Value to be set if not present
     */
    public final void add(final String path, final Object value) {
        if(this.get(path) == null) {
            this.set(path, value);
        }
    }

    /**
     * Set a value into the file regardless of if it exists already.
     *
     * @param path Path to be used
     * @param value Value to be set
     */
    public final void set(final String path, final Object value) {
        final String[] paths = path.split("\\.");

        this.getPath(path).put(paths[paths.length - 1], value);
        this.save();
    }

    /**
     * Read a value from loaded JSON file and cast to clazz
     *
     * @param path Path to be used
     * @param clazz Class T to be cast by
     * @return Value cast to class T
     */
    public final <T> T get(final String path, final Class<T> clazz) {
        final Object result = this.get(path);

        return result == null ? null : clazz.cast(this.get(path));
    }

    /**
     * Read the JSON file to a specified location
     *
     * @param path Path to be used
     * @return Value of path to be returned
     */
    public final Object get(final String path) {
        final String[] paths = path.split("\\.");
        final JSON json = this.getPath(path);

        final Object result = json.object(paths[paths.length - 1]);
        if(result == null) {
            Titan.INSTANCE.getLogger().debug("[JSONFile] - Failed to find value at path '%s' for file '%s'",
                    path,
                    this.file.getName());
            return null;
        } else {
            return json.object(paths[paths.length - 1]);
        }
    }

    /**
     * Check the JSON object for a specified JSON value at path separated by "."
     *
     * @param path Path to be used
     * @return JSON object or empty object of Path
     */
    private JSON getPath(final String path) {
        final String[] paths = path.split("\\.");
        JSON json = this.json;

        if(paths.length > 1) {
            for(int i = 0; i < paths.length - 1; i++) {
                final String part = paths[i];
                JSON pathPart = json.json(paths[i]);
                if(pathPart == null) {
                    pathPart = new JSON();
                    json.put(part, pathPart);
                }

                json = pathPart;
            }
        }

        return json;
    }

    /**
     * Save a JSON object back to file format
     */
    public final void save() {
        this.save(this.json);
    }

    /**
     * Save a JSON object back to file format
     *
     * @param json Object to be saved
     */
    public final void save(final JSON json) {
        try {
            Files.write(file.toPath(), json.setFormat(new JSONDefaultMarshalFormat()).marshal().getBytes());
        } catch (final IOException ex) {
            Titan.INSTANCE.getLogger().info("[JSONFile] [save(json)] - Failed to write '%s' back to file\n%s",
                    this.file.getName(),
                    ExceptionUtils.getStackTrace(ex));
        }
    }
}
