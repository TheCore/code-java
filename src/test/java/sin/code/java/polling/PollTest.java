package sin.code.java.polling;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import sin.code.java.polling.Poll.PollBuilder;

public class PollTest {

    private Logger log = Logger.getGlobal();
    private int dummyVal = 1;

    @Test
    public void booleanSequentialPollTest() {
        List<Boolean> pollResult = PollBuilder.<Boolean>poll(PollingType.SEQUENTIAL)
            .forEvery(Duration.ofMillis(200))
            .atMost(Duration.ofSeconds(5))
            .until(incrementCntr(3), pollSuccessful(), "Cntr less than " + 3)
            .prepare().start();
        log.info(pollResult.toString());

        dummyVal = 1; // Reset dummy value.
        pollResult = PollBuilder.<Boolean>poll(PollingType.SEQUENTIAL)
            .forEvery(Duration.ofMillis(200))
            .atMost(Duration.ofSeconds(5))
            .until(incrementCntr(3), pollSuccessful(), "Cntr less than " + 3)
            .until(incrementCntr(5), pollSuccessful(), "Cntr less than " + 5)
            .prepare().start();
        log.info(pollResult.toString());

        dummyVal = 1; // Reset dummy value.
        pollResult = PollBuilder.<Boolean>poll(PollingType.SEQUENTIAL)
            .forEvery(Duration.ofMillis(200))
            .atMost(Duration.ofSeconds(5))
            .until(incrementCntr(3), pollSuccessful(), "Cntr less than " + 3)
            .until(incrementCntr(5), pollSuccessful(), "Cntr less than " + 5)
            .until(incrementCntr(1000), pollSuccessful(), "Cntr less than " + 1000)
            .prepare().start();
        log.info(pollResult.toString());

    }

    private Callable<Boolean> incrementCntr(int limit) {
        return () -> {
            log.info("DummyVal: " + Integer.toString(dummyVal));
            return dummyVal++ > limit;
        };
    }

    private Matcher<Boolean> pollSuccessful() {
        return Matchers.equalTo(Boolean.TRUE);
    }
}
