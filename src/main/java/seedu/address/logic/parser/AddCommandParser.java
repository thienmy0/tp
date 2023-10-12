package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_REQUIRED_COMMAND_NOT_FOUND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Email;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Patient;
import seedu.address.model.patient.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    public static final Prefix[] RELEVANT_PREFIXES = new Prefix[]{PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
        PREFIX_ADDRESS, PREFIX_TAG};
    public static final Prefix[] REQUIRED_PREFIXES = new Prefix[]{PREFIX_NAME};

    public static final Prefix[] OPTIONAL_PREFIXES = new Prefix[]{PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
        PREFIX_TAG};


    /**
     * Gets prefixes in argMultimap and returns a Prefix array containing all present prefixes
     *
     * @param argMultimap Argument multimap which contains prefix to value mapping
     * @return Prefix array containing present prefixes
     */
    public static Prefix[] getPrefixesPresent(ArgumentMultimap argMultimap) {
        ArrayList<Prefix> prefixesPresent;
        prefixesPresent = new ArrayList<>(List.copyOf(Arrays.asList(REQUIRED_PREFIXES))); // add required
        // prefixes to prefixes present.

        // go through other optional prefixes, check which contains value, add to list
        for (Prefix p : OPTIONAL_PREFIXES) {
            if (argMultimap.getValue(p).isPresent()) {
                prefixesPresent.add(p);
            }
        }
        return prefixesPresent.toArray(new Prefix[]{});
    }

    /**
     * Takes in a list of prefixes and creates patients with its corresponding values in argMultimap, if
     * optional field values are missing, fill it with default instead.
     *
     * @param argMultimap Argument multimap which contains prefix to value mapping
     * @param prefixes    List of prefixes present
     * @return Patient object
     * @throws ParseException if the user input does not conform the expected format
     */
    public static Patient createPatientFromPrefixes(ArgumentMultimap argMultimap, Prefix[] prefixes)
        throws ParseException {
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = new Phone("12345678");
        Email email = new Email("default_email@gmail.com");
        Address address = new Address("Address not added");
        Set<Tag> tagList = new HashSet<>();
        for (Prefix p : prefixes) {
            switch (p.getPrefix()) {
            case "p/":
                phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
                break;
            case "e/":
                email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
                break;
            case "a/":
                address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
                break;
            case "t/":
                tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
                break;
            default:
            }
        }
        return new Patient(name, phone, email, address, tagList);
    }

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, RELEVANT_PREFIXES);

        // check if any prefixes present
        if (!areRelevantPrefixesPresent(argMultimap, RELEVANT_PREFIXES) || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        // check if required prefixes are present and not empty
        if (!areRequiredPrefixesPresent(argMultimap, REQUIRED_PREFIXES)) {
            throw new ParseException(
                String.format(MESSAGE_REQUIRED_COMMAND_NOT_FOUND_FORMAT, Arrays.toString(REQUIRED_PREFIXES)));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS);
        Prefix[] prefixesPresent = getPrefixesPresent(argMultimap);

        Patient patient = createPatientFromPrefixes(argMultimap, prefixesPresent);
        return new AddCommand(patient);
    }

    /**
     * Returns true if none of the compulsory prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean areRequiredPrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... requiredPrefixes) {
        return Stream.of(requiredPrefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

    /**
     * Returns true if any of the relevant prefixes contains a value
     * {@code ArgumentMultimap}.
     */
    private static boolean areRelevantPrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... requiredPrefixes) {
        return Stream.of(requiredPrefixes).anyMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
