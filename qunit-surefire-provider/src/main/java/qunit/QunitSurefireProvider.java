package qunit;

import kx.c;
import org.apache.commons.io.FileUtils;
import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.*;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
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
    private final File sourceDirectory;

    private final File qBinary;
    private final File testRunnerFile;

    public QunitSurefireProvider(ProviderParameters providerParameters) throws IOException {
        this.providerParameters = providerParameters;
        this.consoleLogger = providerParameters.getConsoleLogger();
        this.testSourceDirectory = providerParameters.getTestRequest().getTestSourceDirectory();
        this.testClassesDirectory = new File(providerParameters.getProviderProperties().getProperty("testClassesDirectory"));
        this.sourceDirectory = new File(providerParameters.getProviderProperties().getProperty("sourceDirectory"));
        this.testRunnerFile = new File(this.testClassesDirectory.getAbsolutePath() + "/" + "testRunner.q");
        qBinary = findQBinary();

        // setup test runner
        File testRunnerSrcFile = new File(providerParameters.getTestClassLoader().getResource("q/testRunner.q").getFile());
        FileUtils.copyFile(testRunnerSrcFile, testRunnerFile);
    }

    @Override
    public Iterator getSuites() {
        throw new UnsupportedOperationException("getSuites not supported by Qunit");
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
        int port = 0;
        Process qProcess = null;
        kx.c testInstance = null;
        try {
            // find a free port
            ServerSocket socket = new ServerSocket(0);
            port = socket.getLocalPort();
            socket.close();
            // start q
            ProcessBuilder processBuilder = new ProcessBuilder(qBinary.getAbsolutePath(),testRunnerFile.getAbsolutePath(),
                    "-p",String.valueOf(port),
                    "-testFile",testSuite.getAbsolutePath(),
                    "-sourceFiles",sourceDirectory.getAbsolutePath()
                    // todo dependencies
            );
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            //consoleLogger.info("starting q on port " + port + EOL);
            qProcess = processBuilder.start();

            // process started - now do stuff
            Thread.sleep(10);

            testInstance = new c("localhost", port);
            Object[] tests = (Object[])testInstance.k(".qunit.initialise[]");
            if (tests.length == 0) {
                consoleLogger.info("No tests found for " + testSuite.getName() + EOL);
            } else {
                for (Object test : tests) {
                    runListener.testStarting(new SimpleReportEntry(testSuite.getName(), test.toString()));
                    long before = System.currentTimeMillis();
                    try {
                        Object result = testInstance.k(".qunit.runTest",test.toString());
                        if (result instanceof char[]) {
                            int duration = (int)(System.currentTimeMillis() - before) / 1000;
                            runListener.testFailed(new SimpleReportEntry(testSuite.getName(), test.toString(), new QunitStackTraceWriter(testSuite, test.toString(), String.valueOf((char[])result)), duration));
                        } else {
                            runListener.testSucceeded(new SimpleReportEntry(testSuite.getName(), test.toString()));
                        }
                    } catch (c.KException e) {
                        int duration = (int)(System.currentTimeMillis() - before) / 1000;
                        runListener.testError(new SimpleReportEntry(testSuite.getName(), test.toString(), new QunitStackTraceWriter(testSuite, test.toString(), e), duration));
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Exception from q process: " + port, e);
        } catch (c.KException e) {
            throw new RuntimeException("Exception from q process: " + port, e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Exception from q process: " + port, e);
        } finally {
            if (testInstance != null) {
                try {
                    testInstance.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (qProcess != null) {
                qProcess.destroy();
            }
            //consoleLogger.info("destroyed q on port " + port + EOL);
        }
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
}
