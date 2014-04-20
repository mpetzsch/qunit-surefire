package qunit;

import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

/**
 * Created by mpetzsch on 20/04/2014.
 */
public class QunitSurefireProvider extends AbstractProvider {


    private final ProviderParameters providerParameters;
    private final File testSourceDirectory;

    public QunitSurefireProvider(ProviderParameters providerParameters) {
        this.providerParameters = providerParameters;
        this.testSourceDirectory = providerParameters.getTestRequest().getTestSourceDirectory();
    }

    @Override
    public Iterator getSuites() {
        providerParameters.getConsoleLogger().info("getSuites: TestSource:" + testSourceDirectory);
        return null;
    }

    @Override
    public RunResult invoke(Object o) throws TestSetFailedException, ReporterException, InvocationTargetException {
        providerParameters.getConsoleLogger().info("invoke: TestSource:" + testSourceDirectory);
        return null;
    }

}
