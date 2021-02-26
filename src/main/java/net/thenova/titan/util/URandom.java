package net.thenova.titan.util;

import java.util.Random;

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
public final class URandom {

    private static final Random random = new Random();

    /**
     * Generates a random number between min and max
     *
     * @param min Int minimum output
     * @param max Int maximum output
     * @return int value
     */
    public static int integer(final int min, final int max){
        return URandom.random.nextInt((max - min) + 1) + min;
    }

    public static int integer(final int bound) {
        return URandom.random.nextInt(bound);
    }

    public static boolean bool() {
        return URandom.random.nextBoolean();
    }

    public static double doub(final double min, final double max) {
        return Math.random() < 0.5 ? ((1 - Math.random()) * (max - min) + min) : (Math.random() * (max - min) + min);
    }
}
