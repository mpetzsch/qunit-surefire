package qunit;

import org.apache.maven.surefire.report.SafeThrowable;
import org.apache.maven.surefire.report.StackTraceWriter;

import java.io.File;

/**
 * Created by mpetzsch on 20/04/2014.
 */
public class QunitStackTraceWriter implements StackTraceWriter {

    private File testFile;
    private String testFunction;
    private String testMessage;

    public QunitStackTraceWriter(File testFile, String testFunction, String testMessage) {
        this.testFile = testFile;
        this.testFunction = testFunction;
        this.testMessage = testMessage;
    }

    @Override
    public String writeTraceToString() {
        return "\tat " + testFile.getAbsolutePath() + "\n";
    }

    @Override
    public String writeTrimmedTraceToString() {
        return "FailedTest : " + testMessage + "\n" + writeTraceToString();
    }

    @Override
    public String smartTrimmedStackTrace() {
        return testFile.getName() + "#" + testFunction + " FailedTest " + testMessage;
    }

    @Override
    public SafeThrowable getThrowable() {
        return new SafeThrowable(null);
    }
}
