import org.testng.annotations.Test;

import java.util.UUID;

@Test
public class UUIDStatisticTest {

    private static final int TIMES = 34000;

    @Test
    public void test() {
        testFor(10);
    }

    private void testFor(int i) {
        UUID sample = null;
        int passed = 0, notPassed = 0;
        for (int j = 0; j < TIMES; j++) {
            final UUID uuid = UUID.randomUUID();
            if(uuid.hashCode() % i == 0) {
                sample = uuid;
                passed++;
            } else {
                notPassed++;
            }
        }
        System.out.println("Passed for integer: " + i + ": " + passed + " " + notPassed + " " + (notPassed == 0? "100%" : (100. * passed / notPassed) + "%"));
        System.out.println("Sample: " + sample);
    }

}
