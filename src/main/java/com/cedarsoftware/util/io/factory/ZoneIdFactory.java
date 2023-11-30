package com.cedarsoftware.util.io.factory;

import java.time.ZoneId;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.ReaderContext;

/**
 * @author Kenny Partlow (kpartlow@gmail.com)
 * <br>
 * Copyright (c) Cedar Software LLC
 * <br><br>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <br><br>
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">License</a>
 * <br><br>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.*
 */
public class ZoneIdFactory implements JsonReader.ClassFactory {
    @Override
    public ZoneId newInstance(Class<?> c, JsonObject jObj, ReaderContext context) {
        Object value = jObj.getValue();

        if (value instanceof String) {
            return fromString((String) value);
        }

        return fromJsonObject(jObj);
    }

    protected ZoneId fromString(String id) {
        return ZoneId.of(id);
    }

    protected ZoneId fromJsonObject(JsonObject job) {
        String value = (String) job.get("id");
        return value == null ? null : fromString(value);
    }

    @Override
    public boolean isObjectFinal() {
        return true;
    }
}
