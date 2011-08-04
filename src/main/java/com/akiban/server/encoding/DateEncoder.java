/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.server.encoding;

import com.akiban.ais.model.Type;
import com.akiban.server.rowdata.FieldDef;
import com.akiban.server.Quote;
import com.akiban.server.rowdata.RowData;
import com.akiban.util.AkibanAppender;

/**
 * Encoder for working with dates when stored as a 3 byte int using
 * the encoding of DD + MM x 32 + YYYY x 512. This is how MySQL stores the
 * SQL DATE type.
 * See: http://dev.mysql.com/doc/refman/5.5/en/storage-requirements.html
 */
public final class DateEncoder extends LongEncoderBase {
    DateEncoder() {
    }

    @Override
    public long encodeFromObject(Object obj) {
        final int value;
        if(obj == null) {
            value = 0;
        } else if(obj instanceof String) {
            // YYYY-MM-DD
            final String values[] = ((String)obj).split("-");
            int y = 0, m = 0, d = 0;
            switch(values.length) {
                case 3: d = Integer.parseInt(values[2]); // fall
                case 2: m = Integer.parseInt(values[1]); // fall
                case 1: y = Integer.parseInt(values[0]); break;
                default:
                    throw new IllegalArgumentException("Invalid date string");
            }
            value = d + m*32 + y*512;
        } else if(obj instanceof Number) {
            value = ((Number)obj).intValue();
        } else {
            throw new IllegalArgumentException("Requires String or Number");
        }
        return value;
    }

    @Override
    public String decodeToString(long value) {
        final long year = value / 512;
        final long month = (value / 32) % 16;
        final long day = value % 32;
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    @Override
    public boolean validate(Type type) {
        return type.fixedSize() && (type.maxSizeBytes() == 3);
    }

    @Override
    public void toString(FieldDef fieldDef, RowData rowData, AkibanAppender sb, Quote quote) {
        toStringQuoted(fieldDef, rowData, sb, quote);
    }
}
