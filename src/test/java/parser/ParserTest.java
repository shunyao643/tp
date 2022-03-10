package parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ParserTest {

    Parser parser;

    @BeforeEach
    void setup() {
        parser = new Parser();
    }

    @Test
    void splitCommandTerm_validCommand_success() throws IncompleteCommandException {
        ArrayList<String> expectedResult = new ArrayList<>(
                Arrays.asList("add", "n/ITEM_NAME sn/SERIAL_NUMBER t/TYPE c/COST pf/PURCHASED_FROM pd/PURCHASED_DATE")
        );
        ArrayList<String> actualResult = parser.splitCommandTerm(
                "add n/ITEM_NAME sn/SERIAL_NUMBER t/TYPE c/COST pf/PURCHASED_FROM pd/PURCHASED_DATE");
        assertEquals(expectedResult, actualResult);
        assertEquals(expectedResult.get(0), actualResult.get(0));
        assertEquals(expectedResult.get(1), actualResult.get(1));
    }

    @Test
    void splitCommandTerm_incompleteCommand_exceptionThrown(){
        ArrayList<String> unexpectedResult = new ArrayList<>(
                Arrays.asList("add","n/ITEM_NAMEsn/SERIAL_NUMBERt/TYPEc/COSTpf/PURCHASED_FROMpd/PURCHASED_DATE")
        );
        try {
            ArrayList<String> actualResult = parser.splitCommandTerm(
                    "addn/ITEM_NAMEsn/SERIAL_NUMBERt/TYPEc/COSTpf/PURCHASED_FROMpd/PURCHASED_DATE");
            assertEquals(unexpectedResult, actualResult);
            fail();
        } catch (IncompleteCommandException e){
            assertEquals("Could not find space delimiter between command and arguments!", e.getMessage());
        }
    }

}