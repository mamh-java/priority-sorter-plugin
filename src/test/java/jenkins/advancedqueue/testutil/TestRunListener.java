package jenkins.advancedqueue.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;

@Extension
public class TestRunListener extends RunListener<Run> {

    private static final Logger LOGGER = Logger.getLogger(TestRunListener.class.getName());

    private static List<ItemInfo> actual;
    private static ExpectedItem[] expected;

    public static void init(ExpectedItem... expected) {
        TestRunListener.expected = expected;
        actual = new ArrayList<>(expected.length);
    }

    @Override
    public void onStarted(Run r, TaskListener listener) {
        LOGGER.info("ON STARTED: " + r.getParent().getName());
        try {
            ItemInfo item = QueueItemCache.get().getItem(r.getParent().getName());
            if (actual == null) {
                // Init was not called, initialize
                TestRunListener.expected = null;
                actual = new ArrayList<>();
            }
            actual.add(item);
        } catch (Throwable e) {
            LOGGER.log(Level.INFO, "###########", e);
        }
    }

    public static void assertStartedItems() {
        assertEquals(expected.length, actual.size(), "Wrong number of started items");
        for (int i = 0; i < actual.size(); i++) {
            LOGGER.info("Validating Build " + i);
            assertTrue(
                    actual.get(i).getJobName().matches(expected[i].getJobName()),
                    "Job mismatch at position [" + i + "] expected <" + expected[i].getJobName() + "> was <"
                            + actual.get(i).getJobName() + ">");
            assertEquals(
                    expected[i].getPriority(),
                    actual.get(i).getPriority(),
                    "Priority mismatch at position [" + i + "]");
        }
    }
}
