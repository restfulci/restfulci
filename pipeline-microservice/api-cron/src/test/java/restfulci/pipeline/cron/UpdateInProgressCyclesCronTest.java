package restfulci.pipeline.cron;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UpdateInProgressCyclesCronTest {
	
	@SpyBean UpdateInProgressCyclesCron cron;

	@Test
	public void testCallUpdateInProgressCycles() {
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			verify(cron, atLeast(4)).updateInProgressCycles();
		});
	}
}
