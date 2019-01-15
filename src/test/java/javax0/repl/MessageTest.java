package javax0.repl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    @DisplayName("Test the message issuing and retrieving")
    void testMessage() {
        final var msg = new Message();
        msg.error("Error message");
        msg.warning("Warning message");
        msg.info("Info message");
        Assertions.assertEquals(
                "[ERROR] Error message\n" +
                        "[WARNING] Warning message\n" +
                        "[INFO] Info message\n",
                msg.message());
    }

}
