package qunit;

import org.apache.maven.surefire.report.SafeThrowable;
import org.apache.maven.surefire.report.StackTraceWriter;

/**
 * Created by mpetzsch on 20/04/2014.
 */
public class EmptyStackTraceWriter implements StackTraceWriter {

    private String testSuite;
    private String testFunction;
    private String testMessage;

    public EmptyStackTraceWriter(String testSuite, String testFunction, String testMessage) {
        this.testSuite = testSuite;
        this.testFunction = testFunction;
        this.testMessage = testMessage;
    }

    @Override
    public String writeTraceToString() {
        return testSuite + "#" + testFunction + " : " + testMessage;
    }

    @Override
    public String writeTrimmedTraceToString() {
        return "TestFailed : " + testMessage;
    }

    @Override
    public String smartTrimmedStackTrace() {
        return testSuite + "#" + testFunction + " : " + testMessage;
    }

    @Override
    public SafeThrowable getThrowable() {
        return new SafeThrowable(null);
    }
}
