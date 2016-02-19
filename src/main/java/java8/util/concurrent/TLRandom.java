/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java8.util.concurrent;

import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A random number generator isolated to the current thread.
 *
 * @since 1.7
 * @author Doug Lea
 */
/*package*/ final class TLRandom {

    /** Generates per-thread initialization/probe field */
    private static final AtomicInteger probeGenerator = new AtomicInteger();

    /**
     * The next seed for default constructors.
     */
    private static final AtomicLong seeder = new AtomicLong(initialSeed());

    private static long initialSeed() {
        if (java.security.AccessController.doPrivileged(
                new PrivilegedAction<Boolean>() {
                    @Override
                    public Boolean run() {
                        return Boolean.getBoolean("java.util.secureRandomSeed");
                    }
                })) {
            byte[] seedBytes = java.security.SecureRandom.getSeed(8);
            long s = (long) seedBytes[0] & 0xffL;
            for (int i = 1; i < 8; ++i) {
                s = (s << 8) | ((long) seedBytes[i] & 0xffL);
            }
            return s;
        }
        return (mix64(System.currentTimeMillis()) ^
                mix64(System.nanoTime()));
    }

    /**
     * The seed increment.
     */
    private static final long GAMMA = 0x9e3779b97f4a7c15L;

    /**
     * The increment for generating probe values.
     */
    private static final int PROBE_INCREMENT = 0x9e3779b9;

    /**
     * The increment of seeder per new instance.
     */
    private static final long SEEDER_INCREMENT = 0xbb67ae8584caa73bL;

    static long mix64(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return z ^ (z >>> 33);
    }

    static int mix32(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        return (int) (((z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L) >>> 32);
    }

    private TLRandom() {
    }

    /**
     * Initialize Thread fields for the current thread.  Called only
     * when Thread.threadLocalRandomProbe is zero, indicating that a
     * thread local seed value needs to be generated. Note that even
     * though the initialization is purely thread-local, we need to
     * rely on (static) atomic generators to initialize the values.
     */
    static final void localInit() {
        int p = probeGenerator.addAndGet(PROBE_INCREMENT);
        int probe = (p == 0) ? 1 : p; // skip 0
        long seed = mix64(seeder.getAndAdd(SEEDER_INCREMENT));
        setThreadLocalRandomSeed(seed);
        setThreadLocalRandomProbe(probe);
    }

    static final long nextSeed() {
        long r; // read and update per-thread seed
        setThreadLocalRandomSeed(r = getThreadLocalRandomSeed() + GAMMA);
        return r;
    }

    // Within-package utilities

    /*
     * Descriptions of the usages of the methods below can be found in
     * the classes that use them. Briefly, a thread's "probe" value is
     * a non-zero hash code that (probably) does not collide with
     * other existing threads with respect to any power of two
     * collision space. When it does collide, it is pseudo-randomly
     * adjusted (using a Marsaglia XorShift). The nextSecondarySeed
     * method is used in the same contexts as ThreadLocalRandom, but
     * only for transient usages such as random adaptive spin/block
     * sequences for which a cheap RNG suffices and for which it could
     * in principle disrupt user-visible statistical properties of the
     * main ThreadLocalRandom if we were to use it.
     */

    /**
     * Returns the probe value for the current thread without forcing
     * initialization. Note that invoking ThreadLocalRandom.current()
     * can be used to force initialization on zero return.
     */
    static final int getProbe() {
        return getThreadLocalRandomProbe();
    }

    /**
     * Pseudo-randomly advances and records the given probe value for the
     * given thread.
     */
    static final int advanceProbe(int probe) {
        probe ^= probe << 13;   // xorshift
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        setThreadLocalRandomProbe(probe);
        return probe;
    }

    /**
     * Returns the pseudo-randomly initialized or updated secondary seed.
     */
    static final int nextSecondarySeed() {
        int r;
        if ((r = getThreadLocalRandomSecondarySeed()) != 0) {
            r ^= r << 13;   // xorshift
            r ^= r >>> 17;
            r ^= r << 5;
        }
        else if ((r = mix32(seeder.getAndAdd(SEEDER_INCREMENT))) == 0) {
            r = 1; // avoid zero
        }
        setThreadLocalRandomSecondarySeed(r);
        return r;
    }

    private static final class SeedsHolder {
        long threadSeed;
        int threadProbe;
        int threadSecondarySeed;
    }

    private static final ThreadLocal<SeedsHolder> localSeeds = new ThreadLocal<SeedsHolder>() {
        @Override
        protected SeedsHolder initialValue() {
            return new SeedsHolder();
        }
    };

    // package-private for access from ThreadLocalRandom
    static long getThreadLocalRandomSeed() {
        return localSeeds.get().threadSeed;
    }

    private static void setThreadLocalRandomSeed(long seed) {
        localSeeds.get().threadSeed = seed;
    }

    // package-private for access from ThreadLocalRandom
    static int getThreadLocalRandomProbe() {
        return localSeeds.get().threadProbe;
    }

    private static void setThreadLocalRandomProbe(int probe) {
        localSeeds.get().threadProbe = probe;
    }

    private static int getThreadLocalRandomSecondarySeed() {
        return localSeeds.get().threadSecondarySeed;
    }

    private static void setThreadLocalRandomSecondarySeed(int secondary) {
        localSeeds.get().threadSecondarySeed = secondary;
    }

    private static void setUncontendedToTrue(Integer isUncontended) {
        U.putInt(isUncontended, VALUE_OFF, 1); // true
    }

    // only called via reflection from Striped64 
    private static int getInitializedProbe(Integer uncontended) {
        int p = getThreadLocalRandomProbe();
        if (p == 0) {
            localInit();
            p = getThreadLocalRandomProbe();
            setUncontendedToTrue(uncontended);
        }
        return p;
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U = UnsafeAccess.unsafe;
    private static final long VALUE_OFF;
    static {
        try {
            VALUE_OFF = U.objectFieldOffset(Integer.class
                    .getDeclaredField("value"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
