package tbsc.dutchhelper.util.wikt;

import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryPage;
import de.tudarmstadt.ukp.jwktl.api.util.ILanguage;
import de.tudarmstadt.ukp.jwktl.api.util.Language;
import de.tudarmstadt.ukp.jwktl.api.util.TemplateParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import tbsc.dutchhelper.Constants;
import tbsc.dutchhelper.util.Log;
import tbsc.dutchhelper.util.wikt.handlers.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Some parts taken directly from v1, but this time I'm trying to follow MVC more closely, and that means
 * not changing the GUI directly from here (controller) but rather letting the model do it, like it's supposed to be.
 *
 * This time, I also want to spread parts even more, like having here only methods that help you interact
 * with the database, but they only give you the data, and for formatting it you need to use something different.
 *
 * Created on 12/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public final class WiktionaryHelper {

    private static IWiktionaryEdition wikt;
    private static Log log = new Log(WiktionaryHelper.class);

    /**
     * Loads the Wiktionary database into {@link #wikt}.
     * THIS ALSO ADDS A SHUTDOWN HOOK THAT CLOSES THE DATABASE!
     */
    public static void load() {
        log.i("Loading database...");

        long beforeTime = System.currentTimeMillis();
        wikt = JWKTL.openEdition(new File(Constants.DATABASE_PATH));
        long afterTime = System.currentTimeMillis();

        long duration = afterTime - beforeTime;
        log.i("Database loaded in %d seconds!", TimeUnit.MILLISECONDS.toSeconds(duration));

        // Shutdown hook to close database when execution ends
        Runtime.getRuntime().addShutdownHook(new Thread(WiktionaryHelper::close));
    }

    /**
     * Returns an instance of the database, loading it if it's not loaded.
     * @return database instance
     */
    public static IWiktionaryEdition get() {
        if (wikt == null) {
            log.w("Attempted to access database without first loading it");
            log.w("This is a bug!");
            load();
        }
        // now it must be loaded, so return it
        return wikt;
    }

    /**
     * Closes the database safely. MUST be called on shutdown!
     */
    public static void close() {
        log.i("Closing database...");
        wikt.close();
        log.i("Database closed");
    }

    /**
     * Tries to return all Dutch entries in the given page, or null if no Dutch entry was found.
     * @param page Page to parse
     * @return Dutch entries in page
     */
    public static List<IWiktionaryEntry> getDutchEntries(IWiktionaryPage page) {
        return page.getEntries()
                .stream()
                .filter(e -> e.getWordLanguage() == getNLD())
                .collect(Collectors.toList());
    }

    /**
     * Used to filter entry languages.
     * @return Returns a database-usable instance of Dutch
     */
    public static ILanguage getNLD() {
        return Language.get("nld");
    }

    /**
     * Attempts to strip out Wiktionary formatting, such as {{hello}} or [[thing]].
     * @param s String to de-format
     * @return De-formatted string
     */
    public static String removeFormatting(String s) {
        String result = s;

        // handles {{non-gloss definition}} (part of definition that shouldn't be inside parentheses)
        // test with die
        result = TemplateParser.parse(result, new NumParamBaseTemplateHandler("non-gloss definition"));

        // handles {{gloss}} (definition inside parentheses)
        // test with slaan
        result = TemplateParser.parse(result, new NumParamBaseTemplateHandler("(", ")", 0, "gloss"));

        // handles {{nl-noun form of}} (automated noun forms)
        // test with any plural/diminutive noun
        result = TemplateParser.parse(result, new NLNounTemplateHandler());

        // handles {{nl-verb form of}} (automated verb inflections)
        // test with any non-base verb form
        result = TemplateParser.parse(result, new NLVerbTemplateHandler());

        // handles {{nl-adj form of}} (automated adjective inflections)
        // test with any adjective
        result = TemplateParser.parse(result, new NLAdjTemplateHandler());

        // handles {{l}} (doesn't care about language, just replaces with the text)
        // test with motorrijwiel
        result = TemplateParser.parse(result, new NumParamBaseTemplateHandler(1, "link", "l"));

        // handles {{ant}}
        // test with groot
        result = TemplateParser.parse(result, new ListBaseTemplateHandler("Antonyms: ", 1, "ant", "antonyms"));

        // handles {{syn}}
        // test with groot
        result = TemplateParser.parse(result, new ListBaseTemplateHandler("Synonyms: ", 1, "syn", "synonyms"));

        // handles {{lb}}, {{lbl}} and {{label}}
        // test with verlopen
        result = TemplateParser.parse(result, new ListBaseTemplateHandler("(", ")", 1, "lb", "lbl", "label"));

        // handles {{mention}} and {{m}}
        // test with noemen
        result = TemplateParser.parse(result, new NumParamBaseTemplateHandler(1, "mention", "m"));

        // handles {{ux}}, {{eg}} and {{usex}} (example)
        // test with geloven
        result = TemplateParser.parse(result, new ExampleTemplateHandler());

        // handles {{qualifier}}, {{q}}, {{i}} and {{qual}}
        // test with slaaf
        result = TemplateParser.parse(result, new ListBaseTemplateHandler("(", ")", 0, "qualifier", "q", "i", "qual"));

        // handles [[thing]] (wiki links, should NOT be migrated to use ITemplateHandler)
        // test with verboden
        result = result.replaceAll("\\[\\[((?:[^|\\]]+?\\|)*)([^|\\]]+?)]]", "$2");

        // remove quotes used for bold or italics
        // should NOT be migrated to use ITemplateHandler (it's not a template)
        result = result.replaceAll("'''?", "");

        return result;
    }

    /* old version of removeFormatting, uses regex instead of ITemplateHandler
    public static String removeFormatting(String s) {
        String result = s;

        // replace links with just their text
        result = result.replaceAll("\\{\\{non-gloss definition\\|(.+?)}}", "$1");
        result = result.replaceAll("\\{\\{gloss\\|(.+?)}}", "($1)");

        // replace plural form template with the plain text version that would show on the site
        result = result.replaceAll("\\{\\{nl-noun form of\\|pl\\|(.+?)}}", "plural form of $1");
        result = result.replaceAll("\\{\\{nl-noun form of\\|dim\\|(.+?)}}", "diminutive of $1");
        result = result.replaceAll("\\{\\{nl-noun form of\\|acc\\|(.+?)}}", "(archaic) accusative form of $1");
        result = result.replaceAll("\\{\\{nl-noun form of\\|gen\\|(.+?)}}", "(archaic) genitive form of $1");
        result = result.replaceAll("\\{\\{nl-noun form of\\|dat\\|(.+?)}}", "(archaic) dative form of $1");

        // for some reason Mediawiki decided to have verb forms as a template instead of a normal link
        // so I have to waste my time because they didn't think about users parsing their sites when they designed them
        // this finds any of those {{nl-verb form of|blah|infinitive}} templates and puts in what would show in the site
        result = handleVerbInflections(result);

        // replace [[thing]] wiki links with just thing
        // copied from WikiString because it's too complex for me...
        // this should NOT be ported to us ITemplateHandler because it uses [[thing]] and not {{}}
        // meaning, it's not a template, it's a link
        result = result.replaceAll("\\[\\[((?:[^|\\]]+?\\|)*)([^|\\]]+?)]]", "$2");

        // handle {{l}} (doesn't care about language, just replaces with the text)
        result = result.replaceAll("\\{\\{l\\|[a-zA-Z]{2,3}\\|(.+?)}}", "$1");

        // handle {{ant}}
        Pattern antonymPattern = Pattern.compile("(\\{\\{ant\\|(.+?)}})");
        Matcher antonymMatch = antonymPattern.matcher(result);
        if (antonymMatch.find()) {
            // takes all antonyms, and adds them with an "Antonyms: " prefix, and the antonyms are comma separated
            result = StringUtils.replace(result, antonymMatch.group(1),
                    "Antonyms: " + String.join(", ", antonymMatch.group(2).split("\\|")));
        }

        // handle {{syn}}
        Pattern synonymPattern = Pattern.compile("(\\{\\{syn\\|(.+?)}})");
        Matcher synonymMatch = synonymPattern.matcher(result);
        if (synonymMatch.find()) {
            // takes all synonyms, and adds them with an "Synonyms: " prefix, and the synonyms are comma separated
            result = StringUtils.replace(result, synonymMatch.group(1),
                    "Synonyms: " + String.join(", ", synonymMatch.group(2).split("\\|")));
        }

        // handle {{lb}} (also {{label}} and {{lbl}} as they're equivalent)
        Pattern labelPattern = Pattern.compile("(\\{\\{(?:lb|lbl|label)\\|[a-zA-Z]{2,3}\\|(.+?)}})");
        Matcher labelMatch = labelPattern.matcher(result);
        if (labelMatch.find()) {
            result = StringUtils.replace(result, labelMatch.group(1),
                    "(" + String.join(", ", labelMatch.group(2).split("\\|")) + ")");
        }

        // handle {{ux}}
        Pattern uxPattern = Pattern.compile("\\{\\{ux\\|}}");

        // remove quotes used for bold or italics
        // should NOT be migrated to use ITemplateHandler (it's not a template)
        result = result.replaceAll("'''?", "");

        return result;
    }
    */

    /* old way of handling verb {{nl-verb form of}}, new way in NLVerbTemplateHandler
    private static String handleVerbInflections(String s) {
        // pattern for verb forms
        // there's a group of the entire thing for me to be able to replace the template with the output
        Pattern pattern = Pattern.compile("(\\{\\{nl-verb form of\\|(.+?)}})");
        Matcher match = pattern.matcher(s);
        if (match.find()) {
            // remove "{{nl-verb form of|" from the start and "}}" from the end
            // essentially take only parameters and infinitive - which I decided call opts
            String opts = match.group(2);

            // opts is just everything mashed up; this is each parameter on its own
            List<String> optList = Arrays.asList(opts.split("\\|"));

            // opts typically have a value; split by "=" (equals) and store in a LinkedHashMap (to preserve order)
            // order is important because the infinitive is always last, and that's how I know which one is it
            // it doesn't ALWAYS have a value; therefore checking if there is one is important!
            Map<String, String> optMap = new LinkedHashMap<>();
            optList.stream().map(o -> StringUtils.split(o, "="))
                    .forEach(o -> optMap.put(o[0], o.length > 1 ? o[1] : ""));

            // there can be more than one value, and they're separated with a + (plus).
            // once again, using a LinkedHashMap to preserve order
            Map<String, List<String>> optSplitMap = new LinkedHashMap<>();
            optMap.forEach((key, value) -> optSplitMap.put(key, Arrays.asList(value.split("\\+"))));

            // now I have parsed everything I need to convert to plaintext
            // note: when appending stuff you should almost always put a space at the end

            StringBuilder result = new StringBuilder();

            if (optSplitMap.containsKey("p")) {
                char[] persons = optSplitMap.get("p").get(0).toCharArray();
                for (int i = 0; i < persons.length; i++) {
                    char personChar = persons[i];
                    if (personChar == '1') {
                        result.append("first-");
                    } else if (personChar == '2') {
                        result.append("second-");
                    } else if (personChar == '3') {
                        result.append("third-");
                    }
                    // only add "and" if it's not the last person
                    if (i < persons.length - 1) {
                        result.append(" and ");
                    }
                }
                result.append("person ");
            }

            if (optSplitMap.containsKey("n")) {
                if (optSplitMap.get("n").contains("sg")) {
                    result.append("singular ");
                } else if (optSplitMap.get("n").contains("pl")) {
                    result.append("plural ");
                }
            }

            if (optSplitMap.containsKey("t")) {
                if (optSplitMap.get("t").contains("pres")) {
                    result.append("present ");
                } else if (optSplitMap.get("t").contains("past")) {
                    result.append("past ");
                }
            }

            if (optSplitMap.containsKey("m")) {
                List<String> moods = optSplitMap.get("m");
                for (int i = 0; i < moods.size(); i++) {
                    String mood = moods.get(i);
                    switch (mood) {
                        case "imp":
                            // if it's imperative, it's archaic and that should be noted at the start
                            String tmp = result.toString();
                            result = new StringBuilder("(archaic) " + tmp);
                            result.append("imperative ");
                            break;
                        case "subj":
                            result.append("subjunctive ");
                            break;
                        case "ptc":
                            result.append("participle ");
                            break;
                        case "ind":
                            result.append("indicative ");
                            break;
                    }

                    // only add "and" if it's not the last mood
                    if (i < moods.size() - 1) {
                        result.append("and ");
                    }
                }
            }

            // getting the last element in a map is so dumb
            result.append("of ")
                    .append(new ArrayList<>(optSplitMap.keySet()).get(optSplitMap.size() - 1));

            return StringUtils.replace(s, match.group(1), result.toString());
        }

        // the match branch wasn't taken, no change should be done
        return s;
    }
    */

    /**
     * Returns a URL to the actual audio file from the given filename.
     * This isn't such a simple task, as HTML parsing is needed to get the URL from the audio file Wiki page.
     * @param filename Which audio file to find
     * @return Direct URL to audio file of the given filename, or an empty string if couldn't find
     */
    public static String getAudioURL(String filename) throws IOException {
        long startTime = System.currentTimeMillis();
        // All page URLs consist of a constant url and the filename
        String wikiPageUrl = "https://en.wiktionary.org/wiki/File:" + filename;

        // using jsoup, parse the link to the actual audio file from the wiki page
        Document document = Jsoup.parse(new URL(wikiPageUrl), 3000);

        // selector of the audio player element
        // all 3 selectors work, and they're all similar in performance
        // I chose to select using the internal class because that is likely to work between changes
        // Elements elements = document.select("body div[id=content] div[id=bodyContent] div[id=mw-content-text] div[class=fullMedia] a");
        // Elements elements = document.select("div[class=fullMedia] a");
        Elements elements = document.select(".internal");
        long endTime = System.currentTimeMillis();
        log.d("Fetching Audio URL took %sms", endTime - startTime);

        // retrieve audiofile URL from player element
        String audioUrl = elements.attr("href");
        return audioUrl.isEmpty() ? "" : "https:" + audioUrl;
    }

}
