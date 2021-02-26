package net.thenova.titan.module.module.data;

import de.arraying.kotys.JSONArray;
import de.arraying.kotys.JSONField;
import lombok.Getter;

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
public final class ModuleDescriptionFile {

    @JSONField(key = "name") private String name;
    @JSONField(key = "main") private String main;
    @JSONField(key = "loader") private String loader;

    @JSONField(key = "version") private String version;
    @JSONField(key = "url") private String url;

    @JSONField(key = "dependency") private JSONArray dependency;
}

