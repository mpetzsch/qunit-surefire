package qunit;

import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

/**
 * Created by mpetzsch on 20/04/2014.
 */
public class QunitSurefireProvider extends AbstractProvider {


    private final ProviderParameters providerParameters;

    public QunitSurefireProvider(ProviderParameters providerParameters) {
        this.providerParameters = providerParameters;
    }

    @Override
    public Iterator getSuites() {

        return null;
    }

    @Override
    public RunResult invoke(Object o) throws TestSetFailedException, ReporterException, InvocationTargetException {
        return null;
    }
}
