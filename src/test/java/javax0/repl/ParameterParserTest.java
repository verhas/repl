package javax0.repl;

import javax0.repl.ParameterParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

class ParameterParserTest {

    @Test
    @DisplayName("test a command line parsing with parameters and values")
    void testCommand() {
        final var sut = ParameterParser.parse("key1=value1 key2=value2 value3 value3 key0=wuff",
                Set.of("key1a", "key2", "key0"));
        Assertions.assertEquals("value1", sut.get("key1a"));
        Assertions.assertEquals("value2", sut.get("key2"));
        Assertions.assertEquals("wuff", sut.get("key0"));
        Assertions.assertEquals("value3", sut.get(0));
        Assertions.assertEquals("value3", sut.get(1));
    }

    @Test
    @DisplayName("test a command line containing extra key")
    void testExtraKey() {
        final var e = Assertions.assertThrows(IllegalArgumentException.class, () ->
                ParameterParser.parse("key1=value1 key2=value2 value3 value3 key3=wuff",
                        Set.of("key1a", "key2", "key0")));
        Assertions.assertTrue(e.getMessage().contains("key3 is not an allowed parameter"),e.getMessage());
    }

    @Test
    @DisplayName("ambiguous key")
    void testAmbiKey() {
        final var e = Assertions.assertThrows(IllegalArgumentException.class, () ->
                ParameterParser.parse("key=value1 key2=value2 value3 value3 key3=wuff",
                        Set.of("key1a", "key2", "key0")));
        Assertions.assertTrue(e.getMessage().contains("Parameter key is ambiguous."),e.getMessage());
    }
}
