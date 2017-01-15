/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.openjdk.tests.tck;

import java.util.Collection;

/** Allows tests to work with different Collection implementations. */
public interface CollectionImplementation {
// CVS rev. 1.1
    /** Returns the Collection class. */
    public Class<?> klazz();
    /** Returns an empty collection. */
    public Collection emptyCollection();
    public Object makeElement(int i);
    public boolean isConcurrent();
    public boolean permitsNulls();
}
