package org.openjdk.other.tests.optional;
/*
 * Copyright (c) 2013, 2017, Oracle and/or its affiliates. All rights reserved.
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

/* @test
 * @summary Basic functional test of OptionalInt
 * @author Mike Duigou
 * @run testng BasicInt
 */

import java.util.NoSuchElementException;

import java8.lang.Integers;
import java8.util.OptionalInt;
import java8.util.function.IntConsumer;
import java8.util.function.IntSupplier;
import java8.util.function.Supplier;
import java8.util.stream.IntStream;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class BasicInt {

    @Test(groups = "unit")
    public void testEmpty() {
        OptionalInt empty = OptionalInt.empty();
        OptionalInt present = OptionalInt.of(1);

        // empty
        assertTrue(empty.equals(empty));
        assertTrue(empty.equals(OptionalInt.empty()));
        assertTrue(!empty.equals(present));
        assertTrue(0 == empty.hashCode());
        assertTrue(!empty.toString().isEmpty());
        assertTrue(!empty.isPresent());
        empty.ifPresent(new IntConsumer() {
            @Override
            public void accept(int v) {
                fail();
            }
        });
        assertEquals(2, empty.orElse(2));
        assertEquals(2, empty.orElseGet(new IntSupplier() {
            @Override
            public int getAsInt() {
                return 2;
            }
        }));
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void testEmptyGet() {
        OptionalInt empty = OptionalInt.empty();

        int got = empty.getAsInt();
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void testEmptyOrElseGetNull() {
        OptionalInt empty = OptionalInt.empty();

        int got = empty.orElseGet(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void testEmptyOrElseThrowNull() throws Throwable {
        OptionalInt empty = OptionalInt.empty();

        int got = empty.orElseThrow(null);
    }

    @Test(expectedExceptions=ObscureException.class)
    public void testEmptyOrElseThrow() throws Exception {
        OptionalInt empty = OptionalInt.empty();
        int got = empty.orElseThrow(new Supplier<ObscureException>() {
            @Override
            public ObscureException get() {
                return new ObscureException();
            }
        });
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void testEmptyOrElseThrowNoArg() throws Exception {
        OptionalInt empty = OptionalInt.empty();

        int got = empty.orElseThrow();
    }

    @Test(groups = "unit")
    public void testPresent() {
        OptionalInt empty = OptionalInt.empty();
        OptionalInt present = OptionalInt.of(1);

        // present
        assertTrue(present.equals(present));
        assertFalse(present.equals(OptionalInt.of(0)));
        assertTrue(present.equals(OptionalInt.of(1)));
        assertFalse(present.equals(empty));
        assertTrue(Integers.hashCode(1) == present.hashCode());
        assertFalse(present.toString().isEmpty());
        assertTrue(-1 != present.toString().indexOf(Integer.toString(present.getAsInt()).toString()));
        assertTrue(-1 != present.toString().indexOf(Integer.toString(present.orElseThrow()).toString()));
        assertEquals(1, present.getAsInt());
        assertEquals(1, present.orElseThrow());
        try {
            present.ifPresent(new IntConsumer() {
                @Override
                public void accept(int v) {
                    throw new ObscureException();
                }
            });
            fail();
        } catch (ObscureException expected) {

        }
        assertEquals(1, present.orElse(2));
        assertEquals(1, present.orElseGet(null));
        assertEquals(1, present.orElseGet(new IntSupplier() {
            @Override
            public int getAsInt() {
                return 2;
            }
        }));
        assertEquals(1, present.orElseGet(new IntSupplier() {
            @Override
            public int getAsInt() {
                return 3;
            }
        }));
        assertEquals(1, present.<RuntimeException>orElseThrow(null));
        assertEquals(1, present.<RuntimeException>orElseThrow(new Supplier<RuntimeException>() {
            @Override
            public RuntimeException get() {
                return new ObscureException();
            }
        }));
    }

    @Test(groups = "unit")
    public void testStream() {
        {
            IntStream s = OptionalInt.empty().stream();
            assertFalse(s.isParallel());

            int[] es = s.toArray();
            assertEquals(es.length, 0);
        }

        {
            IntStream s = OptionalInt.of(42).stream();
            assertFalse(s.isParallel());

            int[] es = OptionalInt.of(42).stream().toArray();
            assertEquals(es.length, 1);
            assertEquals(es[0], 42);
        }
    }

    private static class ObscureException extends RuntimeException {

    }
}
