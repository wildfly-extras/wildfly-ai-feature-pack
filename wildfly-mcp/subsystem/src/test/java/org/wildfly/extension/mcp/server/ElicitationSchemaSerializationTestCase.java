/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import java.util.List;
import org.junit.Test;
import org.wildfly.extension.mcp.injection.elicitation.BooleanSchema;
import org.wildfly.extension.mcp.injection.elicitation.EnumSchema;
import org.wildfly.extension.mcp.injection.elicitation.IntegerSchema;
import org.wildfly.extension.mcp.injection.elicitation.NumberSchema;
import org.wildfly.extension.mcp.injection.elicitation.StringSchema;

public class ElicitationSchemaSerializationTestCase {

    // ==================== BooleanSchema ====================

    @Test
    public void testBooleanSchemaMinimal() {
        JsonObject json = new BooleanSchema().asJson().build();
        assertEquals("boolean", json.getString("type"));
        assertEquals(1, json.size());
    }

    @Test
    public void testBooleanSchemaRequired() {
        BooleanSchema schema = new BooleanSchema(true);
        assertTrue(schema.required());
        JsonObject json = schema.asJson().build();
        assertEquals("boolean", json.getString("type"));
    }

    // ==================== StringSchema ====================

    @Test
    public void testStringSchemaMinimal() {
        JsonObject json = new StringSchema().asJson().build();
        assertEquals("string", json.getString("type"));
        assertEquals(1, json.size());
    }

    @Test
    public void testStringSchemaAllFields() {
        StringSchema schema = new StringSchema("My Title", "A description", 2, 100, "email", true, "default@example.com");
        JsonObject json = schema.asJson().build();
        assertEquals("string", json.getString("type"));
        assertEquals("My Title", json.getString("title"));
        assertEquals("A description", json.getString("description"));
        assertEquals(2, json.getInt("minLength"));
        assertEquals(100, json.getInt("maxLength"));
        assertEquals("email", json.getString("format"));
        assertEquals("default@example.com", json.getString("default"));
    }

    @Test
    public void testStringSchemaRequiredOnly() {
        StringSchema schema = new StringSchema(true);
        assertTrue(schema.required());
        JsonObject json = schema.asJson().build();
        assertEquals("string", json.getString("type"));
        assertFalse(json.containsKey("title"));
        assertFalse(json.containsKey("description"));
        assertFalse(json.containsKey("minLength"));
        assertFalse(json.containsKey("maxLength"));
        assertFalse(json.containsKey("format"));
        assertFalse(json.containsKey("default"));
    }

    @Test
    public void testStringSchemaRequiredWithTitleAndDescription() {
        StringSchema schema = new StringSchema(true, "Title", "Desc");
        JsonObject json = schema.asJson().build();
        assertEquals("Title", json.getString("title"));
        assertEquals("Desc", json.getString("description"));
        assertFalse(json.containsKey("format"));
    }

    @Test
    public void testStringSchemaOptionalFieldsAbsent() {
        StringSchema schema = new StringSchema(null, null, null, null, null, false, null);
        JsonObject json = schema.asJson().build();
        assertEquals("string", json.getString("type"));
        assertEquals(1, json.size());
    }

    // ==================== NumberSchema ====================

    @Test
    public void testNumberSchemaMinimal() {
        JsonObject json = new NumberSchema().asJson().build();
        assertEquals("number", json.getString("type"));
        assertEquals(1, json.size());
    }

    @Test
    public void testNumberSchemaWithBounds() {
        NumberSchema schema = new NumberSchema(true, 0.5, 99.9);
        JsonObject json = schema.asJson().build();
        assertEquals("number", json.getString("type"));
        assertEquals(0.5, json.getJsonNumber("minimum").doubleValue(), 0.001);
        assertEquals(99.9, json.getJsonNumber("maximum").doubleValue(), 0.001);
        assertTrue(schema.required());
    }

    @Test
    public void testNumberSchemaOnlyMin() {
        NumberSchema schema = new NumberSchema(false, 1.0, null);
        JsonObject json = schema.asJson().build();
        assertEquals("number", json.getString("type"));
        assertTrue(json.containsKey("minimum"));
        assertFalse(json.containsKey("maximum"));
    }

    // ==================== IntegerSchema ====================

    @Test
    public void testIntegerSchemaMinimal() {
        JsonObject json = new IntegerSchema().asJson().build();
        assertEquals("integer", json.getString("type"));
        assertEquals(1, json.size());
    }

    @Test
    public void testIntegerSchemaWithBounds() {
        IntegerSchema schema = new IntegerSchema(true, 1, 10);
        JsonObject json = schema.asJson().build();
        assertEquals("integer", json.getString("type"));
        assertEquals(1, json.getInt("minimum"));
        assertEquals(10, json.getInt("maximum"));
    }

    @Test
    public void testIntegerSchemaOnlyMax() {
        IntegerSchema schema = new IntegerSchema(false, null, 100);
        JsonObject json = schema.asJson().build();
        assertFalse(json.containsKey("minimum"));
        assertEquals(100, json.getInt("maximum"));
    }

    // ==================== EnumSchema ====================

    @Test
    public void testEnumSchemaWithoutNames() {
        EnumSchema schema = new EnumSchema(true, List.of("A", "B", "C"));
        JsonObject json = schema.asJson().build();
        assertEquals("string", json.getString("type"));
        JsonArray arr = json.getJsonArray("enum");
        assertNotNull(arr);
        assertEquals(3, arr.size());
        assertEquals("A", arr.getString(0));
        assertEquals("B", arr.getString(1));
        assertEquals("C", arr.getString(2));
        assertFalse(json.containsKey("enumNames"));
        assertTrue(schema.required());
    }

    @Test
    public void testEnumSchemaWithNames() {
        EnumSchema schema = new EnumSchema(false, List.of("en", "fr"), List.of("English", "French"));
        JsonObject json = schema.asJson().build();
        assertEquals("string", json.getString("type"));
        JsonArray enumArr = json.getJsonArray("enum");
        assertEquals(2, enumArr.size());
        assertEquals("en", enumArr.getString(0));
        JsonArray namesArr = json.getJsonArray("enumNames");
        assertNotNull(namesArr);
        assertEquals(2, namesArr.size());
        assertEquals("English", namesArr.getString(0));
        assertEquals("French", namesArr.getString(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumSchemaEmptyValuesThrows() {
        new EnumSchema(false, List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumSchemaMismatchedNamesThrows() {
        new EnumSchema(false, List.of("a", "b"), List.of("Only One"));
    }
}
