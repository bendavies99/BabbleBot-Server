/*
 * MIT License
 *
 * Copyright (c) 2020 Ben Davies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package net.bdavies.db.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.bdavies.db.model.hooks.IPropertyUpdateHook;
import net.bdavies.db.model.serialization.ISQLObjectDeserializer;
import net.bdavies.db.model.serialization.ISQLObjectSerializer;

import java.lang.reflect.Field;

/**
 * Edit me
 *
 * @author me@bdavies (Ben Davies)
 * @since 1.0.0
 */
@SuppressWarnings("ClassWithoutLogger")
@AllArgsConstructor
@Getter
@ToString
public class ModelProperty implements IModelProperty {
    private final Class<?> type;
    private final Field field;
    private final String name;
    private final boolean isProtected;
    private final boolean isPrimary;
    private final boolean increments;
    private final Class<? extends ISQLObjectSerializer<Model, Object>> serializer;
    private final Class<? extends ISQLObjectDeserializer<Model, Object>> deSerializer;
    private final Class<? extends IPropertyUpdateHook<Model, Object>> onUpdate;
    @Setter
    private String previousValue;
    private final boolean isRelational;
    private final Relationship relationship;
}
