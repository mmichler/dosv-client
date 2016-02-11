/*
 * DoSV-Client
 * Copyright (C) 2016  Humboldt-Universität zu Berlin
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see <http://www.gnu.org/licenses/>.
 */

package de.hu_berlin.dosv;

import java.util.HashMap;

// TODO document
public class DosvData extends HashMap<String, Object> {
    public DosvData(Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (i % 2 == 0) { // key
                if (!(args[i] instanceof String)) {
                    throw new IllegalArgumentException("key_not_string");
                }
            } else { // value
                put((String) args[i - 1], args[i]);
            }
        }
    }

    /**
     * @see HashMap#put(Object, Object)
     */
    @Override
    public String put(String key, Object value) {
        return (String) super.put(key, value);
    }
}
