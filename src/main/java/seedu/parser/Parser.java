package seedu.parser;

import java.util.ArrayList;

import seedu.command.*;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse user input
 * Referenced https://github.com/se-edu/addressbook-level2/blob/master/src/seedu/addressbook/parser/Parser.java
 */
public class Parser {

    /**
     * Format to parse command by breaking it up into two segments: command and arguments: these can then be separately
     * passed into arguments.
     */
    public static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)\\s+(?<arguments>.+)");
    public static final Pattern ADD_COMMAND_FORMAT = Pattern.compile(
            "n\\/(?<itemName>.+)" + "\\s+" +
                    "sn\\/(?<serialNumber>.+)" + "\\s+" +
                    "t\\/(?<equipmentType>.+)" + "\\s+" +
                    "c\\/(?<cost>.+)" + "\\s+" +
                    "pf\\/(?<purchasedFrom>.+)" + "\\s+" +
                    "pd\\/(?<purchasedDate>.+)"
    );
    public static final int PREPAREADD_REQUIRED_ARGUMENT_COUNT = 6;
    public static final Pattern VIEW_COMMAND_FORMAT = Pattern.compile("n/(?<itemName>.+)");
    public static final Pattern DELETE_COMMAND_FORMAT = Pattern.compile("s/(?<itemName>.+)");
    // ARGUMENT_FORMAT extracts first n-1 tags
    public static final Pattern ARGUMENT_FORMAT = Pattern.compile(
            "((?:sn|n|t|c|pf|pd)\\/[\\w\\s\\-]+?)\\s+(?=sn|n|t|c|pf|pd)"
    );
    // ARGUMENT_TRAILING_FORMAT extracts last tag
    public static final Pattern ARGUMENT_TRAILING_FORMAT = Pattern.compile(
            "(?<!\\w)(?:sn|n|t|c|pf|pd)\\/([\\w\\s\\-]+)"
    );
    public static final String MESSAGE_INCOMPLETE_COMMAND_MISSING_DELIMITER =
            "Please split your command into arguments with each argument seperated by spaces!";
    public static final String INCORRECT_COMMAND_FORMAT = "Incorrect Command format!";

    public Command parseCommand(String userInput) {
        ArrayList<String> commandAndArgument;
        ArrayList<String> args;
        try {
            commandAndArgument = splitCommandTerm(userInput);
        } catch (IncompleteCommandException e) {
            return new IncorrectCommand(MESSAGE_INCOMPLETE_COMMAND_MISSING_DELIMITER);
        }

        switch (commandAndArgument.get(0)) {
        case AddCommand.COMMAND_WORD:
            try {
                args = prepareAdd(commandAndArgument.get(1));
                return new AddCommand(args);
            } catch (IncompleteCommandException e) {
                return new IncorrectCommand(AddCommand.COMMAND_WORD + AddCommand.COMMAND_DESCRIPTION);
            }
        case CheckCommand.COMMAND_WORD:
            try {
                args = prepareView(commandAndArgument.get(1));
                return new CheckCommand(args);
            } catch (IncompleteCommandException e) {
                return new IncorrectCommand(CheckCommand.COMMAND_WORD + CheckCommand.COMMAND_DESCRIPTION);
            }
        case DeleteCommand.COMMAND_WORD:
            try {
                args = prepareDelete(commandAndArgument.get(1));
                return new DeleteCommand(args);
            } catch (IncompleteCommandException e) {
                return new IncorrectCommand(DeleteCommand.COMMAND_WORD + DeleteCommand.COMMAND_DESCRIPTION);
            }
        case UpdateCommand.COMMAND_WORD:
            try {
                args = extractArguments(commandAndArgument.get(1));
                return prepareUpdate(args);
            } catch (IncompleteCommandException e) {
                return new IncorrectCommand(UpdateCommand.COMMAND_WORD + UpdateCommand.COMMAND_DESCRIPTION);
            }
        default:
            return new IncorrectCommand(INCORRECT_COMMAND_FORMAT);
        }

    }

    /**
     * Create UpdateCommand class containing all arguments required to update a given item
     *
     * Should multiple arguments specifying the same argument parameter (e.g. 'c/1000' and 'c/2000') is given,
     * the previous arguments passed in will be overwritten by the most recent parameter ('c/2000' in example).
     *
     * @param args ArrayList of arguments for an Update Command
     * @return Command object
     */
    protected Command prepareUpdate(ArrayList<String> args) throws IncompleteCommandException {
        UpdateCommand updateCommand = new UpdateCommand();
        for (String s : args) {
            int delimiterPos = s.indexOf('/');
            // the case where delimiterPos = -1 is impossible as
            // ARGUMENT_FORMAT and ARGUMENT_TRAILING_FORMAT regex requires a '/'
            assert delimiterPos != -1: "Each args will need to include minimally a '/' to split arg and value upon";
            String argType = s.substring(0, delimiterPos);
            String argValue = s.substring(delimiterPos + 1);
            switch (argType) {
            case "n":
                updateCommand.setUpdateName(argValue);
                break;
            case "pd":
                updateCommand.setPurchaseDate(argValue);
                break;
            case "t":
                updateCommand.setType(argValue);
                break;
            case "pf":
                updateCommand.setPurchaseFrom(argValue);
                break;
            case "c":
                updateCommand.setCost(argValue);
                break;
            case "sn":
                updateCommand.setSerialNumber(argValue);
                break;
            default:
                System.out.println("`" + argValue + "` not updated for type " + argType + ": Unrecognised Tag");
            }
        }
        if (updateCommand.getSerialNumber() == null)
            return new IncorrectCommand("Serial Number is required to update an item!");
        return updateCommand;
    }

    /**
     * Splits main arguments into split tags with each substring
     *
     * @param args String to be split into substrings
     * @return ArrayList of two elements
     * @throws IncompleteCommandException if no parameters found
     */
    protected ArrayList<String> extractArguments(String args) throws IncompleteCommandException {
        int lastIndex = 0;
        ArrayList<String> splitArguments = new ArrayList<>();
        try {
            Matcher matcher = ARGUMENT_FORMAT.matcher(args.trim());
            while (matcher.find()) {
                splitArguments.add(matcher.group());
                lastIndex = matcher.end();
            }
            matcher.usePattern(ARGUMENT_TRAILING_FORMAT);
            matcher.find(lastIndex);
            splitArguments.add(matcher.group());
        } catch (IllegalStateException e) {
            throw new IncompleteCommandException("No parameters found!");
        }
        return splitArguments;
    }


    /**
     * Prepare arguments for AddCommand by splitting up the arguments into different parts
     * <p>
     * Index:
     * 0. <code> equipmentName </code>: String of equipment name
     * 1. <code> serialNumber </code>: String of unique serial number
     * 2. <code> type </code>: String representation of enumerated class
     * 3. <code> cost </code>: String representation of double value, "$" optional but "," delimiter forbidden
     * 4. <code> purchasedFrom </code>: String of vendor name, suggest adhering to one consistent naming scheme
     * 5. <code> purchasedDate </code>: String representation for now, possibility for future support
     *
     * @param args String to be split into substrings
     * @return ArrayList of arguments
     * @throws IncompleteCommandException if no match found
     */
    protected ArrayList<String> prepareAdd(String args) throws IncompleteCommandException {
        final Matcher matcher = ADD_COMMAND_FORMAT.matcher(args.trim());
        // validate arg string format
        int matchCount = matcher.groupCount();
        if (!matcher.matches() || matchCount != PREPAREADD_REQUIRED_ARGUMENT_COUNT) {
            throw new IncompleteCommandException("Add command values are incomplete or missing!");
        }
        ArrayList<String> results = new ArrayList<>();
        for (int i = 1; i <= matchCount; i++) {
            results.add(matcher.group(i));
        }
        return results;
    }

    /**
     * Prepare argument for CheckCommand by removing the preceding "n/" prefix
     *
     * @param args String to be split into substrings
     * @return ArrayList of one element (assumes rest of string is item name)
     * @throws IncompleteCommandException if no match found
     */
    protected ArrayList<String> prepareView(String args) throws IncompleteCommandException {
        final Matcher matcher = VIEW_COMMAND_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            throw new IncompleteCommandException("View command values are incomplete or missing!");
        }
        return new ArrayList<>(Collections.singleton(matcher.group()));
    }

    /**
     * Prepare argument for DeleteCommand by removing the preceding "s/" prefix
     *
     * @param args String to be split into substrings
     * @return ArrayList of one element (assumes rest of string is serial number)
     * @throws IncompleteCommandException if no match found
     */
    protected ArrayList<String> prepareDelete(String args) throws IncompleteCommandException {
        final Matcher matcher = DELETE_COMMAND_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            throw new IncompleteCommandException("Delete command values are incomplete or missing!");
        }
        return new ArrayList<>(Collections.singleton(matcher.group()));
    }


    /**
     * Break down a command into the command term to be parsed and the remainder of the arguments.
     * Assumes command term and remainder arguments are delimited by minimally one space.
     *
     * @param userInput String to be split into substrings
     * @return ArrayList of String, first element being the command term and the second element being arguments
     * @throws IncompleteCommandException if no space is found
     */
    public ArrayList<String> splitCommandTerm(String userInput) throws IncompleteCommandException {
        ArrayList<String> resultArrayList = new ArrayList<>();
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        // guard against no match
        if (!matcher.matches()) {
            throw new IncompleteCommandException("Could not find space delimiter between command and arguments!");
        }
        resultArrayList.add(matcher.group("commandWord"));
        resultArrayList.add(matcher.group("arguments"));
        return resultArrayList;
    }


}