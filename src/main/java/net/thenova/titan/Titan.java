package net.thenova.titan;

import de.arraying.lumberjack.LFsRules;
import de.arraying.lumberjack.LLogLevel;
import de.arraying.lumberjack.LLogger;
import de.arraying.lumberjack.LLoggerBuilder;
import lombok.Getter;
import net.thenova.titan.module.ModuleManager;
import net.thenova.titan.module.module.Module;
import net.thenova.titan.module.module.expansion.ExpansionLoader;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
public enum Titan {
    INSTANCE;

    public enum Type {
        STANDALONE,
        IMPLEMENTATION,
        PACKAGED
    }

    private final LLogger logger = LLoggerBuilder.create("default")
            .withRouteStdOut(LLogLevel.INFO)
            .withRouteFs(LLogLevel.INFO, new LFsRules() {
                @Override
                public final File getDirectory() {
                    return new File(Titan.this.dataRoot, "logs");
                }

                @Override
                public final int getLineLimit() {
                    return -1;
                }

                @Override
                public final long getTimeLimit() {
                    return TimeUnit.DAYS.toMillis(1);
                }

                @Override
                public final String formatFileName(final long time, final long uid) {
                    return String.format("default-%d.txt", uid);
                }
            })
            .withRouteFs(LLogLevel.DEBUG, new LFsRules() {
                @Override
                public final File getDirectory() {
                    return new File(Titan.this.dataRoot, "logs");
                }

                @Override
                public final int getLineLimit() {
                    return -1;
                }

                @Override
                public final long getTimeLimit() {
                    return TimeUnit.DAYS.toMillis(1);
                }

                @Override
                public final String formatFileName(final long time, final long uid) {
                    return String.format("debug-%d.txt", uid);
                }
            })
            .withThreadPoolSize(1)
            .build();

    private Type type;

    /* Root of all flat-file data*/
    private File dataRoot;

    /**
     * The main method.
     * @param args Startup params
     */
    public static void main(final String[] args) {
        Titan.INSTANCE.init(Type.STANDALONE, new File("."));

        ModuleManager.INSTANCE.init(Type.STANDALONE);
    }

    /**
     * Handle loading of Titan when it is compiled within
     *
     * @param dataRoot Where data should be stored.
     */
    public void initImplementation(final File dataRoot) {
        this.init(Type.IMPLEMENTATION, dataRoot);

        ModuleManager.INSTANCE.init(Type.IMPLEMENTATION);
    }

    /**
     * Handle loading of titan in a packaged format.
     *
     * @param dataRoot Where data should be stored.
     * @param modules Modules being used/loaded
     */
    public void initPackaged(final File dataRoot, final Map<Module, ExpansionLoader> modules) {
        this.init(Type.PACKAGED, dataRoot);

        ModuleManager.INSTANCE.init(Type.PACKAGED, modules);
    }

    /**
     * Replacement for multiple handling of setting defaults
     *
     * @param type What usage Type has Titan been set to
     * @param dataRoot Where data should be stored
     */
    private void init(final Type type, final File dataRoot) {
        this.type = type;
        this.dataRoot = dataRoot;

        this.logger.info("[Titan] - Titan is initializing in mode %s", this.type.toString());
        this.logger.info("[Titan] - Data root has been set to '%s'", this.dataRoot.getAbsolutePath());
    }

    /**
     * Handle post loading, enabling full functionality
     */
    public void enable() {
        ModuleManager.INSTANCE.enable();
    }

    /**
     * Handle shutdown for Titan
     */
    public void shutdown() {
        ModuleManager.INSTANCE.shutdown();
    }
}
