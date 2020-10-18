package sin.code.java.polling;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.Matcher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class Poll<T> {
    private List<Callable<T>> checkers;
    private List<Matcher<T>> conditions;
    private List<String> failMessages;
    private PollingType type;
    private Duration timeout;
    private Duration interval;

    private Logger log = Logger.getGlobal();

    private Poll() {}

    public List<T> start() {
        assert checkers.size() == conditions.size();
        log.info("Poll.start()");
        switch (type) {
        case SEQUENTIAL:
            return pollSequentially();
        case PARALLEL:
            return pollInParallel();
        }
        return Collections.emptyList();
    }

    private List<T> pollInParallel() {
        // TODO
        return Collections.emptyList();
	}

	private List<T> pollSequentially() {
        log.info("Poll.pollSequentially()");
        List<T> pollingResults = new LinkedList<>();
        Duration timeLeft = timeout;
        for (int i = 0; i < checkers.size(); i++) {
            LocalDateTime awaitStart = LocalDateTime.now();
            LocalDateTime awaitStop = awaitStart;
            T result = null;
            try {
                result = Awaitility.await()
                   .atMost(timeLeft)
                   .pollInterval(interval)
                   .until(checkers.get(i), conditions.get(i));
                awaitStop = LocalDateTime.now();
                log.info("Poll.pollSequentially() result: " + result.toString());
            } catch(ConditionTimeoutException e) {
                log.info(failMessages.get(i));
            }
            pollingResults.add(result);
            timeLeft = timeLeft.minus(Duration.between(awaitStart, awaitStop));
        }
        return pollingResults;
	}

	public static class PollBuilder<U> {
        private List<Callable<U>> checkers;
        private List<Matcher<U>> conditions;
        private List<String> failMessages;
        private PollingType type;
        private Duration timeout = Duration.ofSeconds(1);
        private Duration interval = Duration.ofSeconds(1);

        private PollBuilder(PollingType pollingType) {
            this.checkers = new LinkedList<>();
            this.conditions = new LinkedList<>();
            this.failMessages = new LinkedList<>();
            this.type = pollingType;
        }

        public static <U> PollBuilder<U> poll(PollingType pollingType) {
            return new PollBuilder<>(pollingType);
        }

        public PollBuilder<U> until(
                Callable<U> check,
                Matcher<U> matches,
                String failMsg) {
            this.checkers.add(check);
            this.conditions.add(matches);
            this.failMessages.add(failMsg);
            return this;
        }

        public PollBuilder<U> atMost(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public PollBuilder<U> forEvery(Duration interval) {
            this.interval = interval;
            return this;
        }

        public Poll<U> prepare() {
            Poll<U> poll = new Poll<>();
            poll.checkers = checkers;
            poll.conditions = conditions;
            poll.failMessages = failMessages;
            poll.type = type;
            poll.timeout = timeout;
            poll.interval = interval;
            return poll;
        }
    }
}
