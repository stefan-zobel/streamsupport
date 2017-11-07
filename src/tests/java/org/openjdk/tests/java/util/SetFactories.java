/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.tests.java.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java8.util.Iterators;
import java8.util.Sets;
import java8.util.Sets2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/*
 * @test
 * @bug 8048330
 * @summary Test convenience static factory methods on Set.
 * @run testng SetFactories
 */

public class SetFactories {

    static final int NUM_STRINGS = 20; // should be larger than the largest fixed-arg overload
    static final String[] stringArray;
    static {
        String[] sa = new String[NUM_STRINGS];
        for (int i = 0; i < NUM_STRINGS; i++) {
            sa[i] = String.valueOf((char)('a' + i));
        }
        stringArray = sa;
    }

    static Object[] a(Set<String> act, Set<String> exp) {
        return new Object[] { act, exp };
    }

    static Set<String> hashSetOf(String... args) {
        return new HashSet<>(Arrays.asList(args));
    }

    @DataProvider(name="empty")
    public Iterator<Object[]> empty() {
        return Collections.singletonList(
            // actual, expected
            a(Sets.of(), Collections.emptySet())
        ).iterator();
    }

    @DataProvider(name="nonempty")
    public Iterator<Object[]> nonempty() {
        return Arrays.asList(
            // actual, expected
            a(   Sets2.of("a"),
              hashSetOf("a")),
            a(   Sets2.of("a", "b"),
              hashSetOf("a", "b")),
            a(   Sets2.of("a", "b", "c"),
              hashSetOf("a", "b", "c")),
            a(   Sets2.of("a", "b", "c", "d"),
              hashSetOf("a", "b", "c", "d")),
            a(   Sets2.of("a", "b", "c", "d", "e"),
              hashSetOf("a", "b", "c", "d", "e")),
            a(   Sets2.of("a", "b", "c", "d", "e", "f"),
              hashSetOf("a", "b", "c", "d", "e", "f")),
            a(   Sets2.of("a", "b", "c", "d", "e", "f", "g"),
              hashSetOf("a", "b", "c", "d", "e", "f", "g")),
            a(   Sets2.of("a", "b", "c", "d", "e", "f", "g", "h"),
              hashSetOf("a", "b", "c", "d", "e", "f", "g", "h")),
            a(   Sets2.of("a", "b", "c", "d", "e", "f", "g", "h", "i"),
              hashSetOf("a", "b", "c", "d", "e", "f", "g", "h", "i")),
            a(   Sets2.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"),
              hashSetOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")),
            a(   Sets2.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"),
                 Sets2.of("j", "i", "h", "g", "f", "e", "d", "c", "b", "a")),
            a(   Sets2.of(stringArray),
              hashSetOf(stringArray)),
            a(   Sets.of("a"),
              hashSetOf("a")),
            a(   Sets.of("a", "b"),
              hashSetOf("a", "b")),
            a(   Sets.of("a", "b", "c"),
              hashSetOf("a", "b", "c")),
            a(   Sets.of("a", "b", "c", "d"),
              hashSetOf("a", "b", "c", "d")),
            a(   Sets.of("a", "b", "c", "d", "e"),
              hashSetOf("a", "b", "c", "d", "e")),
            a(   Sets.of("a", "b", "c", "d", "e", "f"),
              hashSetOf("a", "b", "c", "d", "e", "f")),
            a(   Sets.of("a", "b", "c", "d", "e", "f", "g"),
              hashSetOf("a", "b", "c", "d", "e", "f", "g")),
            a(   Sets.of("a", "b", "c", "d", "e", "f", "g", "h"),
              hashSetOf("a", "b", "c", "d", "e", "f", "g", "h")),
            a(   Sets.of("a", "b", "c", "d", "e", "f", "g", "h", "i"),
              hashSetOf("a", "b", "c", "d", "e", "f", "g", "h", "i")),
            a(   Sets.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"),
              hashSetOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")),
            a(   Sets.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"),
                 Sets.of("j", "i", "h", "g", "f", "e", "d", "c", "b", "a")),
            a(   Sets.of(stringArray),
              hashSetOf(stringArray))
        ).iterator();
    }

    @DataProvider(name="all")
    public Iterator<Object[]> all() {
        List<Object[]> all = new ArrayList<>();
        Iterators.forEachRemaining(empty(), all::add);
        Iterators.forEachRemaining(nonempty(), all::add);
        return all.iterator();
    }

    @Test(dataProvider="all", expectedExceptions=UnsupportedOperationException.class)
    public void cannotAdd(Set<String> act, Set<String> exp) {
        act.add("x");
    }

    @Test(dataProvider="nonempty", expectedExceptions=UnsupportedOperationException.class)
    public void cannotRemove(Set<String> act, Set<String> exp) {
        act.remove(act.iterator().next());
    }

    @Test(dataProvider="all")
    public void contentsMatch(Set<String> act, Set<String> exp) {
        assertEquals(act, exp);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed2_2() {
        @SuppressWarnings("unused")
        Set<String> set = Sets2.of("a", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed2() {
        @SuppressWarnings("unused")
        Set<String> set = Sets.of("a", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed3_2() {
        @SuppressWarnings("unused")
        Set<String> set = Sets2.of("a", "b", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed3() {
        @SuppressWarnings("unused")
        Set<String> set = Sets.of("a", "b", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed4_2() {
        @SuppressWarnings("unused")
        Set<String> set = Sets2.of("a", "b", "c", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed4() {
        @SuppressWarnings("unused")
        Set<String> set = Sets.of("a", "b", "c", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed5_2() {
        @SuppressWarnings("unused")
        Set<String> set = Sets2.of("a", "b", "c", "d", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed5() {
        @SuppressWarnings("unused")
        Set<String> set = Sets.of("a", "b", "c", "d", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed6_2() {
        @SuppressWarnings("unused")
        Set<String> set = Sets2.of("a", "b", "c", "d", "e", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed6() {
        @SuppressWarnings("unused")
        Set<String> set = Sets.of("a", "b", "c", "d", "e", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed7_2() {
        @SuppressWarnings("unused")
        Set<String> set = Sets2.of("a", "b", "c", "d", "e", "f", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed7() {
        @SuppressWarnings("unused")
        Set<String> set = Sets.of("a", "b", "c", "d", "e", "f", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed8_2() {
        @SuppressWarnings("unused")
        Set<String> set = Sets2.of("a", "b", "c", "d", "e", "f", "g", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed8() {
        @SuppressWarnings("unused")
        Set<String> set = Sets.of("a", "b", "c", "d", "e", "f", "g", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed9_2() {
        @SuppressWarnings("unused")
        Set<String> set = Sets2.of("a", "b", "c", "d", "e", "f", "g", "h", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed9() {
        @SuppressWarnings("unused")
        Set<String> set = Sets.of("a", "b", "c", "d", "e", "f", "g", "h", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed10_2() {
        @SuppressWarnings("unused")
        Set<String> set = Sets2.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowed10() {
        @SuppressWarnings("unused")
        Set<String> set = Sets.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "a");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowedN_2() {
        String[] array = stringArray.clone();
        array[0] = array[1];
        @SuppressWarnings("unused")
        Set<String> set = Sets2.of(array);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void dupsDisallowedN() {
        String[] array = stringArray.clone();
        array[0] = array[1];
        @SuppressWarnings("unused")
        Set<String> set = Sets.of(array);
    }

    @Test(dataProvider="all")
    public void hashCodeEqual(Set<String> act, Set<String> exp) {
        assertEquals(act.hashCode(), exp.hashCode());
    }

    @Test(dataProvider="all")
    public void containsAll(Set<String> act, Set<String> exp) {
        assertTrue(act.containsAll(exp));
        assertTrue(exp.containsAll(act));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed1_2() {
        Sets2.of((String) null); // force one-arg overload
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed1() {
        Sets.of((String) null); // force one-arg overload
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed2a_2() {
        Sets2.of("a", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed2a() {
        Sets.of("a", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed2b_2() {
        Sets2.of(null, "b");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed2b() {
        Sets.of(null, "b");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed3_2() {
        Sets2.of("a", "b", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed3() {
        Sets.of("a", "b", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed4_2() {
        Sets2.of("a", "b", "c", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed4() {
        Sets.of("a", "b", "c", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed5_2() {
        Sets2.of("a", "b", "c", "d", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed5() {
        Sets.of("a", "b", "c", "d", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed6_2() {
        Sets2.of("a", "b", "c", "d", "e", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed6() {
        Sets.of("a", "b", "c", "d", "e", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed7_2() {
        Sets2.of("a", "b", "c", "d", "e", "f", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed7() {
        Sets.of("a", "b", "c", "d", "e", "f", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed8_2() {
        Sets2.of("a", "b", "c", "d", "e", "f", "g", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed8() {
        Sets.of("a", "b", "c", "d", "e", "f", "g", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed9_2() {
        Sets2.of("a", "b", "c", "d", "e", "f", "g", "h", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed9() {
        Sets.of("a", "b", "c", "d", "e", "f", "g", "h", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed10_2() {
        Sets2.of("a", "b", "c", "d", "e", "f", "g", "h", "i", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowed10() {
        Sets.of("a", "b", "c", "d", "e", "f", "g", "h", "i", null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowedN2() {
        String[] array = stringArray.clone();
        array[0] = null;
        Sets2.of(array);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullDisallowedN() {
        String[] array = stringArray.clone();
        array[0] = null;
        Sets.of(array);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullArrayDisallowed2() {
        Sets2.of((Object[])null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void nullArrayDisallowed() {
        Sets.of((Object[])null);
    }

    @Test(dataProvider="all")
    public void serialEquality(Set<String> act, Set<String> exp) {
        // assume that act.equals(exp) tested elsewhere
        Set<String> copy = serialClone(act);
        assertEquals(act, copy);
        assertEquals(copy, exp);
    }

    @SuppressWarnings("unchecked")
    static <T> T serialClone(T obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    Set<Integer> genSet() {
        return new HashSet<>(Arrays.asList(1, 2, 3));
    }

    @Test
    public void copyOfResultsEqual2() {
        Set<Integer> orig = genSet();
        Set<Integer> copy = Sets2.copyOf(orig);

        assertEquals(orig, copy);
        assertEquals(copy, orig);
    }

    @Test
    public void copyOfResultsEqual() {
        Set<Integer> orig = genSet();
        Set<Integer> copy = Sets.copyOf(orig);

        assertEquals(orig, copy);
        assertEquals(copy, orig);
    }

    @Test
    public void copyOfModifiedUnequal2() {
        Set<Integer> orig = genSet();
        Set<Integer> copy = Sets2.copyOf(orig);
        orig.add(4);

        assertNotEquals(orig, copy);
        assertNotEquals(copy, orig);
    }

    @Test
    public void copyOfModifiedUnequal() {
        Set<Integer> orig = genSet();
        Set<Integer> copy = Sets.copyOf(orig);
        orig.add(4);

        assertNotEquals(orig, copy);
        assertNotEquals(copy, orig);
    }

    @Test
    public void copyOfIdentity2() {
        Set<Integer> orig = genSet();
        Set<Integer> copy1 = Sets2.copyOf(orig);
        Set<Integer> copy2 = Sets2.copyOf(copy1);

        assertNotSame(orig, copy1);
        assertSame(copy1, copy2);
    }

    @Test
    public void copyOfIdentity() {
        Set<Integer> orig = genSet();
        Set<Integer> copy1 = Sets.copyOf(orig);
        Set<Integer> copy2 = Sets.copyOf(copy1);

        assertNotSame(orig, copy1);
        assertSame(copy1, copy2);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void copyOfRejectsNullCollection2() {
        @SuppressWarnings("unused")
        Set<Integer> set = Sets2.copyOf(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void copyOfRejectsNullCollection() {
        @SuppressWarnings("unused")
        Set<Integer> set = Sets.copyOf(null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void copyOfRejectsNullElements2() {
        @SuppressWarnings("unused")
        Set<Integer> set = Sets2.copyOf(Arrays.asList(1, null, 3));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void copyOfRejectsNullElements() {
        @SuppressWarnings("unused")
        Set<Integer> set = Sets.copyOf(Arrays.asList(1, null, 3));
    }

    @Test
    public void copyOfAcceptsDuplicates2() {
        Set<Integer> set = Sets2.copyOf(Arrays.asList(1, 1, 2, 3, 3, 3));
        assertEquals(set, Sets2.of(1, 2, 3));
    }

    @Test
    public void copyOfAcceptsDuplicates() {
        Set<Integer> set = Sets.copyOf(Arrays.asList(1, 1, 2, 3, 3, 3));
        assertEquals(set, Sets.of(1, 2, 3));
    }
}
