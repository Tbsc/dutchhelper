package tbsc.dutchhelper.util.wikt;

import de.tudarmstadt.ukp.jwktl.api.*;
import de.tudarmstadt.ukp.jwktl.api.util.NLInflection;
import tbsc.dutchhelper.DutchHelperApplication;
import tbsc.dutchhelper.util.Log;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Has various methods for converting raw database data to a user-friendly format.
 *
 * Created on 12/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class DefinitionFormatter {

    private static Log log = new Log(DefinitionFormatter.class);

    /**
     * Gets all Dutch entries inside the page, formats each using {@link #formatEntry(IWiktionaryEntry)}, and
     * returns each entry as a string object, for adding to the definition ListView.
     * @param page Page to format
     * @return List containing each definition as a separate string
     */
    public static List<String> formatPage(IWiktionaryPage page) {
        // filter out non-Dutch entries
        List<IWiktionaryEntry> dutchEntries = WiktionaryHelper.getDutchEntries(page);

        // format Dutch entries using formatEntry and insert into a list
        List<String> definitions = new ArrayList<>();

        // definitions.add(page.getTitle());
        definitions.addAll(dutchEntries.stream().map(DefinitionFormatter::formatEntry).collect(Collectors.toList()));
        log.d("Formatted all entries (%s) in page (%s)", definitions.size(), page.getTitle());

        // only if at least one entry was found, try to see if it has an audio pronunciation
        if (!dutchEntries.isEmpty()) {
            // filter out anything that's not audio
            List<IPronunciation> audioFiles = dutchEntries.stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.getPronunciations() != null)
                    .flatMap(e -> e.getPronunciations().stream())
                    .filter(p -> p.getType() == IPronunciation.PronunciationType.AUDIO)
                    .collect(Collectors.toList());

            if (audioFiles.isEmpty()) {
                // we're left with an empty list, no audio pronunciations
                DutchHelperApplication.currentAudioFileProperty.set("");
                DutchHelperApplication.get().playAudioBtn.setText("No Pronunciation");
                // also prevent clicking
                DutchHelperApplication.get().playAudioBtn.setDisable(true);
                log.d("No pronunciation for %s, disabling playing audio", page.getTitle());
            } else {
                // we have an audio file; update property, which will also update button and allow playing
                for (IPronunciation pron : audioFiles) {
                    // skip Belgian pronunciations
                    if (pron.getText().toLowerCase().contains("belgium") || pron.getNote().toLowerCase().contains("belgium")) {
                        log.d("Skipped Belgian pronunciation of %s (%s)", page.getTitle(), pron.getText());
                        continue;
                    }
                    // uses first non-belgian pronunciation
                    DutchHelperApplication.currentAudioFileProperty.set(pron.getText());
                    log.d("Pronunciation of %s found (%s), updating property", page.getTitle(), pron.getText());
                    break;
                }
            }
        }
        // DutchHelperApplication.get().playAudioBtn.setText("");

        return definitions;
    }

    public static String formatEntry(IWiktionaryEntry entry) {
        StringBuilder result = new StringBuilder();

        result.append(entry.getWord()).append("\n");

        if (entry.getPartOfSpeech() != null) {
            result.append("  PoS: ")
                    .append(entry.getPartOfSpeech().name().replace('_', ' ').toLowerCase())
                    .append("\n");
        }

        if (entry.getGender() != null) {
            result.append("  Article: ").append(DutchArticle.fromGender(entry.getGender()).getText())
                    .append(" (").append(entry.getGender().name().toLowerCase()).append(")\n");
        }

        // TODO: Find way to print plural/diminutive form!
        // when building the database, I didn't think about lines already having a newline
        // so this reverses the duplicate newlines
        log.d(entry.getBodyText().replaceAll("\n\n", "\n"));

        if (entry.getPronunciations() != null) {
            for (IPronunciation p : entry.getPronunciations()) {
                // skip audio pronunciation because the audio filename is useless for the user
                if (p.getType() == IPronunciation.PronunciationType.AUDIO) {
                    continue;
                }
                result.append("  ")
                        .append(p.getType() == IPronunciation.PronunciationType.RHYME
                                ? camelCase(p.getType().name())
                                : p.getType().name())
                        .append(": ").append(p.getText()).append("\n");
            }
        }

        // disabled for now as I don't need etymology
//        if (entry.getWordEtymology() != null) {
//            result.append("  Etymology: ").append(entry.getWordEtymology().getText()).append("\n");
//        }

        // to preserve order, so that glosses are always first
        Map<SenseType, List<String>> senses = new LinkedHashMap<>();
        // pre-adding all types, to prevent it from returning null later on
        for (SenseType type : SenseType.values()) {
            senses.put(type, new ArrayList<>());
        }

        for (IWiktionarySense sense : entry.getSenses()) {
            Map<SenseType, String> senseResult = formatSense(sense);

            senseResult.forEach((key, value) -> {
                List<String> senseValues = senses.get(key);
                senseValues.add(value);
                senses.put(key, senseValues);
            });
        }

        for (Map.Entry<SenseType, List<String>> senseEntry : senses.entrySet()) {
            // skip empty sections
            if (senseEntry.getValue().isEmpty()) {
                continue;
            }

            StringBuilder builder = new StringBuilder();
            // start sub-entries in the next line
            builder.append("\n");
            senseEntry.getValue().forEach(builder::append);
            result.append("  ").append(camelCase(senseEntry.getKey().name())).append(": ").append(builder.toString());
            if (!result.toString().endsWith("\n")) {
                result.append("\n");
            }
        }

        if (!entry.getDutchVerbInflections().isEmpty()) {
            Map<NLInflection, String> infls = entry.getDutchVerbInflections();
            result.append("  Auxiliary Verb: ").append(infls.get(NLInflection.AUXILIARY_VERB)).append("\n");
            result.append("  Inflections: \n")
                    .append("    Present 1st-person: ").append(infls.get(NLInflection.FIRST_PERSON_SINGULAR_PRESENT)).append("\n")
                    .append("    Present 2nd-person: ").append(infls.get(NLInflection.SECOND_PERSON_SINGULAR_PRESENT)).append("\n")
                    .append("    Present 3rd-person: ").append(infls.get(NLInflection.THIRD_PERSON_SINGULAR_PRESENT)).append("\n")
                    .append("    Past 1st-person: ").append(infls.get(NLInflection.FIRST_PERSON_SINGULAR_PAST)).append("\n")
                    .append("    Past 2nd-person: ").append(infls.get(NLInflection.SECOND_PERSON_SINGULAR_PAST)).append("\n")
                    .append("    Past 3rd-person: ").append(infls.get(NLInflection.THIRD_PERSON_SINGULAR_PAST)).append("\n")
                    .append("    Past Plural: ").append(infls.get(NLInflection.PLURAL_PAST)).append("\n")
                    .append("    Imperative Singular: ").append(infls.get(NLInflection.IMPERATIVE_SINGULAR)).append("\n")
                    .append("    Present Participle: ").append(infls.get(NLInflection.PRESENT_PARTICIPLE)).append("\n")
                    .append("    Past Participle: ").append(infls.get(NLInflection.PAST_PARTICIPLE)).append("\n");
        }

        // TODO: Find way to have word forms, through reflection/whatever

        if (entry.getUsageNotes() != null) {
            result.append("  Usage: ").append(WiktionaryHelper.removeFormatting(entry.getUsageNotes().getText()));
        }

        for (IWikiString ws : entry.getReferences()) {
            result.append("  Reference: ").append(ws.getText()).append("\n");
        }

        if (entry.getEntryLink() != null) {
            result.append("  Link: ").append(entry.getEntryLinkType())
                    .append(": ").append(entry.getEntryLink()).append("\n");
        }

        return result.toString();
    }

    public static Map<SenseType, String> formatSense(IWiktionarySense sense) {
        Map<SenseType, String> result = new LinkedHashMap<>();
        String senseIdx = (sense.getIndex() == 0
                ? "-"
                : Integer.toString(sense.getIndex()));

        if (sense.getGloss() != null) {
            result.put(SenseType.GLOSSES, "    " + senseIdx + ". "
                    + WiktionaryHelper.removeFormatting(sense.getGloss().getText())
                    + "\n");
        }

        if (sense.getExamples() != null) {
            StringBuilder examples = new StringBuilder();
            examples.append("    ").append(senseIdx).append(":\n");
            for (IWiktionaryExample ws : sense.getExamples()) {
                // TODO: Look into maybe printing example WITH corresponding gloss
                examples.append("      ").append(WiktionaryHelper.removeFormatting(ws.getText())).append("\n");
            }
            result.put(SenseType.EXAMPLES, examples.toString());
        }

        /*
        if (sense.getQuotations() != null)
            for (IQuotation quotation : sense.getQuotations()) {
                result.append("  QTN [")
                        .append(senseIdx).append("] ")
                        .append(quotation.getSource().getText())
                        .append("\n");
                for (IWikiString ws : quotation.getLines())
                    result.append("    ").append(ws.getText()).append("\n");
            }

        if (sense.getRelations() != null) {
            sense.getRelations().stream()
                    .map(relation -> "  REL [" + senseIdx + "] "
                            + relation.getRelationType() + ": "
                            + relation.getTarget())
                    .sorted()
                    .forEach(result::append);
        }

        if (sense.getTranslations() != null) {
            sense.getTranslations().stream()
                    .map(trans -> "  TRL [" + senseIdx + "] "
                            + formatLanguage(trans.getLanguage()) + ": "
                            + trans.getTranslation())
                    .sorted()
                    .forEach(s -> result.append(s).append("\n"));
        }*/

        return result;
    }

    enum SenseType {
        GLOSSES, EXAMPLES, QUOTATIONS, RELATIONS, TRANSLATIONS
    }

    /**
     * Capitalizes first letter.
     * @param s String to work on
     * @return Same string with first letter capitalized
     */
    private static String camelCase(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

}
