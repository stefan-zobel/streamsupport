package java8.util;

import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.testng.annotations.Test;

import java8.util.DualPivotQuicksort;

public class Ticket66Test {

    public static void main(String[] args) {
        int[] a = new int[287];

        for (int i = 0; i < a.length; i++) {
            a[i] = -((i % 143) + 1);
        }

        System.out.println(Arrays.toString(a));

        DualPivotQuicksort.sort(a, 1, 0, a.length);

        System.out.println(Arrays.toString(a));
    }

    @Test
    public static void test() {
        Exception ex = null;
        try {
            main(new String[]{});
        } catch (Exception t) {
            ex = t;
        }
        String msg = "";
        if (ex != null) {
            msg = ex.getMessage();
        }
        assertNull(ex, "Test FAILED with " + msg);
    }
}
