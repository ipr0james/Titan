package net.thenova.titan.libraries.pagination;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright 2020 Arraying
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
public final class Pagination<T> {

    private final List<T> entries;
    @Getter private final int perPage;
    private final int total;

    /**
     * Creates a new pagination.
     * @param entries The total entries.
     * @param perPage The number of entries per page.
     */
    public Pagination(final List<T> entries, final int perPage) {
        this.entries = entries;
        this.perPage = perPage;

        if(entries.size() % perPage == 0) {
            this.total = entries.size() / perPage;
        } else {
            this.total = (entries.size() / perPage) + 1;
        }
    }

    /**
     * Gets a specific page.
     *
     * @param number Integer
     * @return List of Page<T
     */
    public final List<Page<T>> page(final int number) {
        if(number < 1) {
            return new ArrayList<>();
        }
        if(number > this.total) {
            return page(this.total);
        }

        final int start = this.entries.size() < this.perPage ? 0 : (number * this.perPage) - this.perPage;
        final int end = Math.min(start + this.perPage, this.entries.size());
        final List<Page<T>> entries = new ArrayList<>();

        for(int i = start; i < end; i++) {
            entries.add(new Page<>(i + 1, this.entries.get(i)));
        }

        return entries;
    }

    /**
     * Gets the total number of pages.
     *
     * @return - Integer
     */
    public final int total() {
        return this.total;
    }

    public final List<T> entries() {
        return this.entries;
    }

}