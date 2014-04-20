package qunit;

import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.*;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mpetzsch on 20/04/2014.
 */
public class QunitSurefireProvider extends AbstractProvider {


    private final ProviderParameters providerParameters;
    private final ConsoleLogger consoleLogger;

    private final File testSourceDirectory;

    public QunitSurefireProvider(ProviderParameters providerParameters) {
        this.providerParameters = providerParameters;
        this.consoleLogger = providerParameters.getConsoleLogger();
        this.testSourceDirectory = providerParameters.getTestRequest().getTestSourceDirectory();
    }

    @Override
    public Iterator getSuites() {
        throw new UnsupportedOperationException("getSuites not supported by Qunit");
        /*consoleLogger.info("getSuites: TestSource:" + testSourceDirectory);
        //return null;*/
    }

    @Override
    public RunResult invoke(Object forkTestSet) throws TestSetFailedException, ReporterException, InvocationTargetException {
        ReporterFactory reporterFactory = providerParameters.getReporterFactory();
        RunListener runListener = reporterFactory.createReporter();

        // for each test suite (q file) found
        for (File testSuite : getTestSuites()) {
            ReportEntry sampleSuite = new SimpleReportEntry(testSuite.getAbsolutePath(), testSuite.getName());
            runListener.testSetStarting(sampleSuite);
            runTestSuite(testSuite, runListener);
            runListener.testSetCompleted(sampleSuite);
        }
        return reporterFactory.close();
    }

    private List<File> getTestSuites() {
        if (testSourceDirectory.isDirectory()) {
            File[] files = testSourceDirectory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("test.*\\.q");
                }
            });
            return Arrays.asList(files);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private void runTestSuite(File testSuite, RunListener runListener) {
        // find the Q version which this project depends on - should be in target dir

        // run it with a qunit boot script and an argument for the test suite to run

        // get tests

        // execute each test (setup / teardown?)

        // any need for multiple process on ports ability here?

        // stop process

        runListener.testStarting(new SimpleReportEntry(testSuite.getAbsolutePath(), "test_firstTest"));
        runListener.testSucceeded(new SimpleReportEntry(testSuite.getAbsolutePath(), "test_firstTest"));

        //runListener.testStarting(new SimpleReportEntry(testSuite.getAbsolutePath(), "test_secondTest"));
        //runListener.testFailed(new SimpleReportEntry(testSuite.getAbsolutePath(), "test_secondTest", new QunitStackTraceWriter(testSuite, "test_secondTest", "does not match"), 0));
    }

    public RunResult sampleInvoke(Object forkTestSet) throws TestSetFailedException, ReporterException, InvocationTargetException {
        consoleLogger.info("invoke: TestSource:" + testSourceDirectory + "\n");

        ReporterFactory reporterFactory = providerParameters.getReporterFactory();
        RunListener runListener = reporterFactory.createReporter();
        ReportEntry sampleSuite = new SimpleReportEntry("sampleSuite", "sampleSuite");
        runListener.testSetStarting(sampleSuite);
        runListener.testStarting(new SimpleReportEntry("test_sample.q", "test_sample"));
        runListener.testSucceeded(new SimpleReportEntry("test_sample.q", "test_sample"));
        runListener.testStarting(new SimpleReportEntry("test_sample1.q", "test_sample"));
        runListener.testFailed(new SimpleReportEntry("test_sample1.q", "test_sample", new LegacyPojoStackTraceWriter("aa1", "bbb1", new FailedTest("bob does not match")), 0));
        runListener.testStarting(new SimpleReportEntry("test_sample1.q", "test_sample"));
        runListener.testFailed(new SimpleReportEntry("test_sample1.q", "test_sample", new LegacyPojoStackTraceWriter("aa2","bbb2",new FailedTest("not true")), 0));
        runListener.testStarting(new SimpleReportEntry("test_sample1.q", "test_sample"));
        runListener.testFailed(new SimpleReportEntry("test_sample1.q", "test_sample", new LegacyPojoStackTraceWriter("aa3","bbb3",new FailedTest("feck")), 0));
        runListener.testSetCompleted(sampleSuite);
        RunResult rr =  reporterFactory.close();
        return rr;
    }
}
