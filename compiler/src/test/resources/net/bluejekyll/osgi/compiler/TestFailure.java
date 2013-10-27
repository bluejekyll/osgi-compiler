package net.bluejekyll.osgi.compiler;

import net.bluejekyll.osgi.compiler.include.TestLocalImport;
import test.bundle.impl.TestPrivate;

import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: benjaminfry
 * Date: 10/26/13
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestFailure {
    public static String test() {
        PrintStream ps = System.out; // checking for import functionality
        ps.println("Hello World!");
        ps.println(TestLocalImport.THIS_SHOULD_WORK);
        ps.println(TestPrivate.NOT_IMPORTED);

        return "deadbeef";
    }

    @Override
    public String toString() {
        return test();
    }
}
