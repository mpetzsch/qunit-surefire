package qunit;

import org.apache.commons.io.FileUtils;
import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.*;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mpetzsch on 20/04/2014.
 */
public class QunitSurefireProvider extends AbstractProvider {

    private final String EOL = System.getProperty("line.separator");
    private final ProviderParameters providerParameters;
    private final ConsoleLogger consoleLogger;

    private final File testSourceDirectory;
    private final File testClassesDirectory;

    private final File qBinary;
    private final File testRunnerFile;

    public QunitSurefireProvider(ProviderParameters providerParameters) throws IOException {
        this.providerParameters = providerParameters;
        this.consoleLogger = providerParameters.getConsoleLogger();
        this.testSourceDirectory = providerParameters.getTestRequest().getTestSourceDirectory();
        this.testClassesDirectory = new File(providerParameters.getProviderProperties().getProperty("testClassesDirectory"));
        this.testRunnerFile = new File(this.testClassesDirectory.getAbsolutePath() + "/" + "testRunner.q");
        qBinary = findQBinary();

        // setup test runner
        File testRunnerSrcFile = new File(providerParameters.getTestClassLoader().getResource("q/testRunner.q").getFile());
        FileUtils.copyFile(testRunnerSrcFile, testRunnerFile);
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

    private File findQBinary(){
        // find the Q version which this project depends on - should be in target dir
        String qBaseDir = null;
        if (providerParameters.getProviderProperties().containsKey("qBaseDir")) {
            qBaseDir = providerParameters.getProviderProperties().getProperty("qBaseDir");
        } else {
            qBaseDir = System.getenv("QHOME");
            consoleLogger.info("[WARN] Using QHOME - this means the test has an external dependency");
            if (null == qBaseDir) {
                throw new IllegalStateException("Could not find an instance of q to execute.  Is qBaseDir set in plugin configuration?");
            }
        }

        // basedir found - now look for best version to use
        String qPlatform = getQBinaryPlatform();
        String qBinaryArch = getQBinaryArch(qBaseDir, qPlatform);
        File qBinary = new File(qBaseDir + "/" + qPlatform + qBinaryArch + "/q");
        // test for existence and can execute
        if (!qBinary.canExecute()) {
            throw new IllegalArgumentException("Could not find executable Q, tried: " + qBinary.getAbsolutePath());
        }
        consoleLogger.info("Using q @ " + qBinary.getAbsolutePath() + EOL);
        return qBinary;
    }

    private static String getQBinaryPlatform() {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("linux")) {
            return "l";
        } else if (osName.toLowerCase().contains("windows")) {
            return "w";
        } else if (osName.toLowerCase().contains("mac")) {
            return "m";
        } else {
            throw new IllegalArgumentException("Unsupported OS: " + osName);
        }
    }

    private String getQBinaryArch(String qBaseDir, String platformKey) {
        String osArch = System.getProperty("os.arch");
        File defaultBinary = new File(qBaseDir + "/" + platformKey + "64" + "/q");
        defaultBinary.setExecutable(true);
        if (osArch.toLowerCase().contains("64") && defaultBinary.canExecute()) {
            return "64";
        }
        return "32";
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
