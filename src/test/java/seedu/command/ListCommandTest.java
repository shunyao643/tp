package seedu.command;

import org.junit.jupiter.api.Test;
import seedu.equipment.DuplicateSerialNumber;
import seedu.equipment.EquipmentManager;
import seedu.equipment.EquipmentType;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ListCommandTest {
    ListCommand listCommand;

    @Test
    void execute_specifiedType_success() {
        listCommand = new ListCommand(new ArrayList<>(
                Arrays.asList("SPEAKER")
        ));
        listCommand.setEquipmentManager(new EquipmentManager());
        EquipmentManager equipmentManager = listCommand.equipmentManager;
        try {
            equipmentManager.addEquipment("Speaker B",
                    "S1404115ASF",
                    EquipmentType.valueOf("SPEAKER"),
                    1000,
                    "Loud Technologies",
                    "2022-02-23");
        } catch (DuplicateSerialNumber e) {
            fail();
        }

        CommandResult actualResult = listCommand.execute();
        CommandResult expectedResult = new CommandResult("TOTAL QUANTITY OF SPEAKER: 1"  + System.lineSeparator(), equipmentManager.listEquipment());

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void execute_noSpecifiedType_success() {
        listCommand = new ListCommand();

        CommandResult actualResult = listCommand.execute();
        CommandResult expectedResult = new CommandResult("TOTAL QUANTITY OF EQUIPMENT: 1" + System.lineSeparator());

        assertEquals(expectedResult, actualResult);
    }
}