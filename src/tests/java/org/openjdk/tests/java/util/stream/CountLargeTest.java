/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * @test
 * @summary Tests counting of streams containing Integer.MAX_VALUE + 1 elements
 * @bug 8031187 8067969
 */
package org.openjdk.tests.java.util.stream;

import java8.util.stream.LongStreams;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
public class CountLargeTest {

    // is this Android? (defaults to false)
    private static final boolean IS_ANDROID = isAndroid();

    private static final long EXPECTED_LARGE_COUNT = 1L + Integer.MAX_VALUE;

    // streamsupport #211: "ART performance regression in CountLargeTest"
    // Use a smaller count for unknown sized streams on Android (this
    // test has become a real performance hog on recent ART!).
    // See https://sourceforge.net/p/streamsupport/tickets/211/
    private static final long UNKNOWN_SIZED_EXPECTED_LARGE_COUNT = !IS_ANDROID ? EXPECTED_LARGE_COUNT : 131072;  

    public void testRefLarge() {
        // Test known sized stream
        {
            long count = LongStreams.range(0, EXPECTED_LARGE_COUNT)
                    .mapToObj(e -> null).count();
            assertEquals(count, EXPECTED_LARGE_COUNT);
        }
        // Test unknown sized stream
        {
            long count = LongStreams.range(0, UNKNOWN_SIZED_EXPECTED_LARGE_COUNT)
                    .mapToObj(e -> null).filter(e -> true).count();
            assertEquals(count, UNKNOWN_SIZED_EXPECTED_LARGE_COUNT);
        }
    }

    public void testIntLarge() {
        // Test known sized stream
        {
            long count = LongStreams.range(0, EXPECTED_LARGE_COUNT)
                    .mapToInt(e -> 0).count();
            assertEquals(count, EXPECTED_LARGE_COUNT);
        }
        // Test unknown sized stream
        {
            long count = LongStreams.range(0, UNKNOWN_SIZED_EXPECTED_LARGE_COUNT)
                    .mapToInt(e -> 0).filter(e -> true).count();
            assertEquals(count, UNKNOWN_SIZED_EXPECTED_LARGE_COUNT);
        }
    }

    public void testLongLarge() {
        // Test known sized stream
        {
            long count = LongStreams.range(0, EXPECTED_LARGE_COUNT)
                    .count();
            assertEquals(count, EXPECTED_LARGE_COUNT);
        }
        // Test unknown sized stream
        {
            long count = LongStreams.range(0, UNKNOWN_SIZED_EXPECTED_LARGE_COUNT)
                    .filter(e -> true).count();
            assertEquals(count, UNKNOWN_SIZED_EXPECTED_LARGE_COUNT);
        }
    }

    public void testDoubleLarge() {
        // Test known sized stream
        {
            long count = LongStreams.range(0, EXPECTED_LARGE_COUNT)
                    .mapToDouble(e -> 0.0).count();
            assertEquals(count, EXPECTED_LARGE_COUNT);
        }
        // Test unknown sized stream
        {
            long count = LongStreams.range(0, UNKNOWN_SIZED_EXPECTED_LARGE_COUNT)
                    .mapToDouble(e -> 0.0).filter(e -> true).count();
            assertEquals(count, UNKNOWN_SIZED_EXPECTED_LARGE_COUNT);
        }
    }

    /**
     * Are we running on Android?
     * @return {@code true} if yes, otherwise {@code false}.
     */
    private static boolean isAndroid() {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("android.util.DisplayMetrics", false,
                    CountLargeTest.class.getClassLoader());
        } catch (Throwable notPresent) {
            // ignore
        }
        return clazz != null;
    }
}
