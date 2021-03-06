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

package uk.co.bjdavies.config;

import com.google.gson.Gson;

import java.io.Reader;

/**
 * BabbleBot, open-source Discord Bot
 * Author: Ben Davies
 * Class Name: ConfigParser.java
 * Compiled Class Name: ConfigParser.class
 * Date Created: 30/01/2018
 */

public class ConfigParser {
    /**
     * This is the config variable that can be used after parsing through a getter.
     */
    private final Config config;

    /**
     * This is where the config will get parsed.
     *
     * @param json - The inputted file / string.
     */
    public ConfigParser(String json) {
        config = new Gson().fromJson(json, Config.class);
    }

    /**
     * This is where the config will get parsed.
     *
     * @param reader - The reader from which you wish to read the json from.
     */
    public ConfigParser(Reader reader) {
        config = new Gson().fromJson(reader, Config.class);
    }


    /**
     * This will return the Config.
     *
     * @return Config
     */
    public Config getConfig() {
        return config;
    }
}
