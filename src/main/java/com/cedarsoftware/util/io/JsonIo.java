package com.cedarsoftware.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * This is the main API for json-io.  Use these methods to convert:<br/>
 * <ul>
 * <li><b>1. Input</b>: Java root <b>Output</b>: JSON<pre>String json = JsonIo.toJson(root, writeOptions)</pre></li>
 * <li><b>2. Input</b>: Java root <b>Output</b>: JSON -> outputStream <pre>JsonIo.toJson(OutputStream, root, writeOptions)</pre></li>
 * <li><b>3. Input</b>: JSON, <b>Output</b>: Java objects<pre>BillingInfo billInfo = JsonIo.toObjects(String | InputStream, readOptions, BillingInfo.class)</pre></li>
 * <li><b>4. Input</b>: JSON, <b>Output</b>: JsonObject<pre>JsonObject JsonIo.toJsonValues(String | InputStream, readOptions)</pre></li></ul>
 * Often, the goal is to get JSON to Java objects and from Java objects to JSON.  That is #1 and #3 above. <br/>
 * <br/>
 * For approaches #1 and #2 above, json-io will check the root object type (regular Java class or JsonObject instance) to
 * know which type of Object Graph it is serializing to JSON.<br/>
 * <br/>
 * There are occasions where you may just want the raw JSON data, without anchoring it to a set of "DTO" Java objects.
 * For example, you may have an extreme amount of data, and you want to process it as fast as possible, and in
 * streaming mode.  The JSON specification has great primitives which are universally useful in many languages. In
 * Java that is boolean, null, long [or BigInteger], and double [or BigDecimal], and String.<br/>
 * <br/>
 * When JsonValue is returned (option #4 above), your root value will be a JsonObject instance (Map).  In the future,
 * you may also recceive any of the following:
 * <ul>JSON object {...}<br/>
 * JSON array [...]<br/>
 * JSON primitive (boolean, null, long, double, String).</ul>
 * <br/>
 * <b>JsonObject</b> implements the JsonValue interface and
 * represents any JSON object {...}. It also implements the <code> Map</code> interface to make it easy to put/get values to/from it.<br/>
 * <br/>
 * <b>JsonArray</b> implements JsonValue and represents an array [...] and also implements<code> List</code> interface, making it
 * easy to set/get values to/from it.<br/>
 * <br/>
 * <b>JsonPrimitive</b> implements JsonValue and represents one of the 5 JSON primitive value types.<br/>
 * <br/>
 * @author John DeRegnaucourt (jdereg@gmail.com)
 * @author Kenny Partlow (kpartlow@gmail.com)
 *         <br>
 *         Copyright (c) Cedar Software LLC
 *         <br><br>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br><br>
 *         <a href="http://www.apache.org/licenses/LICENSE-2.0">License</a>
 *         <br><br>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.*
 */
public class JsonIo {
    /**
     * Convert the passed in Java source object to JSON.
     * @param srcObject Java instance to convert to JSON format.
     * @param writeOptions Feature options settings to control the JSON output.  Can be null,
     *                     in which case, default settings will be used.
     * @return String of JSON that represents the srcObject in JSON format.
     * @throws JsonIoException A runtime exception thrown if any errors happen during serialization
     */
    public static String toJson(Object srcObject, WriteOptions writeOptions) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonWriter writer = new JsonWriter(stream, writeOptions);
            writer.write(srcObject);
            writer.close();
            return stream.toString();
        } catch (Exception e) {
            throw new JsonIoException("Unable to convert object to JSON", e);
        }
    }

    /**
     * Convert the passed in Java source object to JSON.  If you want a copy of the JSON that was written to the
     * OutputStream, you can wrap the output stream before calling this method, like this:<br/>
     * <br/>
     * <code>ByteArrayOutputStream baos = new ByteArrayOutputStream(originalOutputStream);<br/>
     * JsonIo.toJson(baos, source, writeOptions);<br/>
     * baos.flush();<br/>
     * String json = new String(baos.toByteArray(), StandardCharsets.UTF_8);<br/>
     * </code><br/>
     * @param out OutputStream destination for the JSON output.
     * @param source Root Java object to begin creating the JSON.
     * @param writeOptions Feature options settings to control the JSON output.  Can be null,
     *                     in which case, default settings will be used.
     * @throws JsonIoException A runtime exception thrown if any errors happen during serialization
     */
    public static void toJson(OutputStream out, Object source, WriteOptions writeOptions) {
        try {
            JsonWriter writer = new JsonWriter(out, writeOptions);
            writer.write(source);
            writer.close();
        } catch (Exception e) {
            throw new JsonIoException("Unable to convert object and send in JSON format to OutputStream.", e);
        }
    }

    /**
     * Convert the passed in JSON to Java Objects.
     * @param json String containing JSON content.
     * @param readOptions Feature options settings to control the JSON processing.  Can be null,
     *                     in which case, default settings will be used.
     * @param rootType Class of the root type of object that will be returned. Can be null, in which
     *                 case a best-guess will be made for the Class type of the return object.  If it
     *                 has an @type meta-property that will be used, otherwise the JSON types { ... }
     *                 will return a Map, [...] will return Object[] or Collection, and the primtive
     *                 types will be returned (String, long, Double, boolean, or null).
     * @return rootType Java instance that represents the Java equivalent of the passed in JSON string.
     * @throws JsonIoException A runtime exception thrown if any errors happen during serialization
     */
    public static <T> T toObjects(String json, ReadOptions readOptions, Class<T> rootType) {
        if (json == null) {
            return null;
        }
        return toObjects(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), readOptions, rootType);
    }

    /**
     * Convert the passed in JSON to Java Objects.
     * @param in InputStream bringing JSON content.
     * @param readOptions Feature options settings to control the JSON processing.  Can be null,
     *                     in which case, default settings will be used.
     * @param rootType Class of the root type of object that will be returned. Can be null, in which
     *                 case a best-guess will be made for the Class type of the return object.  If it
     *                 has an @type meta-property that will be used, otherwise the JSON types { ... }
     *                 will return a Map, [...] will return Object[] or Collection, and the primtive
     *                 types will be returned (String, long, double, boolean, or null).
     * @return rootType Java instance that represents the Java equivalent of the JSON input.
     * @throws JsonIoException A runtime exception thrown if any errors happen during serialization
     */
    public static <T> T toObjects(InputStream in, ReadOptions readOptions, Class<T> rootType) {
        try (JsonReader jr = new JsonReader(in, readOptions)) {
            return (T) jr.readObject(rootType);
        }
    }

    /**
     * Note that the return type will match one of these JSON types: JsonObject, JsonArray, or JsonPrimitive, all
     * of which implement JsonValue.
     * @param json        json string
     * @param readOptions options to use when reading. Can be null, in which case the defaults will be used.
     * @return JsonValue graph, containing JsonObjects, JsonArrays, and/or JsonPrimitives.
     */
    public static JsonObject toJsonValues(String json, ReadOptions readOptions) {
        if (json == null) {
            // TODO: return JsonPrimitive representing null
            return null;
        }
        return toJsonValues(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), readOptions);
    }

    /**
     * Note that the return type will match one of these JSON types: JsonObject, JsonArray, or JsonPrimitive, all
     * of which implement JsonValue.
     * @param inputStream bytes representing UTF-8 string
     * @param readOptions options to use when reading.  Can be null, in which case the defaults will be used.
     * @return JsonValue graph, containing JsonObjects, JsonArrays, and/or JsonPrimitives.
     */
    public static JsonObject toJsonValues(InputStream inputStream, ReadOptions readOptions) {
        Convention.throwIfNull(inputStream, "inputStream cannot be null");

        if (readOptions == null) {
            readOptions = new ReadOptionsBuilder().returnAsMaps().build();
        } else {
            readOptions.ensureUsingMaps();
        }

        try (JsonReader jr = new JsonReader(inputStream, readOptions)) {
            return jr.readObject(JsonObject.class);
        }
    }

    /**
     * Format the passed in JSON into multi-line, indented format, commonly used in JSON online editors.
     * @param readOptions ReadOptions to control the feature options. Can be null to take the defaults.
     * @param writeOptions WriteOptions to control the feature options. Can be null to take the defaults.
     * @param json String JSON content.
     * @return String JSON formatted in human readable, standard multi-line, indented format.
     */
    public static String formatJson(String json, ReadOptions readOptions, WriteOptions writeOptions) {
        if (writeOptions.isBuilt())
        {
            writeOptions = new WriteOptions(writeOptions).prettyPrint(true);
        }
        Object object = JsonReader.toObjects(json, readOptions.ensureUsingMaps(), null);
        return JsonWriter.toJson(object, writeOptions.prettyPrint(true));
    }

    /**
     * Format the passed in JSON into multi-line, indented format, commonly used in JSON online editors.
     * @param json String JSON content.
     * @return String JSON formatted in human readable, standard multi-line, indented format.
     */
    public static String formatJson(String json) {
        return formatJson(json,
                new ReadOptionsBuilder().returnAsMaps().build(),
                new WriteOptions().prettyPrint(true));
    }

     /**
     * Copy an object graph using JSON.
     * @param source Object root object to copy
     * @param readOptions ReadOptions feature settings. Can be null for default ReadOptions.
     * @param writeOptions WriteOptions feature settings. Can be null for default WriteOptions.
     * @return A new, duplicate instance of the original.
     */
    public static <T> T deepCopy(Object source, ReadOptions readOptions, WriteOptions writeOptions) {
        if (source == null) {
            // They asked to copy null.  The copy of null is null.
            return null;
        }
        String json = toJson(source, writeOptions);
        return (T) toObjects(json, readOptions, source.getClass());
    }
}
