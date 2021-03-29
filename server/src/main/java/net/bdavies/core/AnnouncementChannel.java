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

package net.bdavies.core;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.bdavies.db.model.Model;
import net.bdavies.db.model.fields.PrimaryField;
import net.bdavies.db.model.fields.Property;
import net.bdavies.db.model.fields.Unique;
import net.bdavies.db.model.serialization.UseSerializationObject;
import net.bdavies.db.model.serialization.util.GuildSerializationObject;
import net.bdavies.db.model.serialization.util.TextChannelSerializationObject;

/**
 * @author ben.davies99@outlook.com (Ben Davies)
 * @since 1.2.7
 */
@Slf4j
@Getter
@Setter
@Data
@EqualsAndHashCode(callSuper = false)
public class AnnouncementChannel extends Model
{
    @Property
    @PrimaryField
    @Setter(AccessLevel.PRIVATE)
    private int id;

    @Property
    @Unique
    @UseSerializationObject(GuildSerializationObject.class)
    private Guild guild;

    @Property
    @Unique
    @UseSerializationObject(TextChannelSerializationObject.class)
    private TextChannel channel;
}
