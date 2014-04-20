package qunit;

/**
 * Created by mpetzsch on 20/04/2014.
 */
public class FailedTest extends Throwable {
    public FailedTest(String message) {
        super(message);
    }
}
