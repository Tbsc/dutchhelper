package tbsc.dutchhelper.eventhandlers;

import de.tudarmstadt.ukp.jwktl.api.IWiktionaryPage;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import tbsc.dutchhelper.util.wikt.DefinitionFormatter;
import tbsc.dutchhelper.DutchHelperApplication;
import tbsc.dutchhelper.util.Log;
import tbsc.dutchhelper.util.wikt.WiktionaryHelper;

import java.util.List;

/**
 * Handles searches.
 * Executed when the search button is pressed.
 *
 * Created on 12/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class SearchEventHandler implements EventHandler<ActionEvent> {

    private static Log log = new Log(SearchEventHandler.class);

    @Override
    public void handle(ActionEvent event) {
        log.i("Search event handler called, beginning search");
        // clear error label before starting
        DutchHelperApplication.showError("");
        log.d("Cleared error label");

        // remove all previous definitions before continuing
        DutchHelperApplication.get().definitionListView.setItems(FXCollections.emptyObservableList());
        log.d("Cleared definitions ListView");

        // remove any previous audio file
        DutchHelperApplication.currentAudioFileProperty.set("");
        log.d("Cleared audio file property");

        // when a definition doesn't have a pronunciation, it disables the button, and this reverses it
        DutchHelperApplication.get().playAudioBtn.setDisable(false);
        log.d("Re-enabled play-audio button");

        String word = DutchHelperApplication.get().wordField.getText();

        if (word.isEmpty()) {
            log.w("User didn't enter a word; aborting search");
            DutchHelperApplication.showError("no word entered");
            return;
        }

        log.i("User entered " + word);

        IWiktionaryPage page = WiktionaryHelper.get().getPageForWord(word);

        if (page == null) {
            log.w("Page wasn't found, meaning word doesn't exist; aborting search");
            DutchHelperApplication.showError( "word not found");
            return;
        }

        log.d("Word found, looking for Dutch entry(s) and formatting them if found...");

        List<String> definitions = DefinitionFormatter.formatPage(page);

        // it didn't find a Dutch entry, so tell user that word couldn't be found
        if (definitions.isEmpty()) {
            log.d("Formatter returned an empty list");
            log.w("No Dutch entries found; aborting search");
            DutchHelperApplication.showError(word + " isn't dutch");
            return;
        }

        // changed log statement below to also print how many entries were found
        log.d("Dutch entr%s found", definitions.size() == 1 ? "y" : "ies (" + definitions.size() + ")");
        log.d("Formatted and added definitions to the definitions ListView");

        // clear field so if user wants to search for another thing they don't have to delete field on their own
        DutchHelperApplication.get().wordField.setText("");
        log.d("Clearing input field");

        DutchHelperApplication.get().definitionListView.setItems(FXCollections.observableArrayList(definitions));
        // DutchHelperApplication.get().definitionListView.scrollTo(0);

        log.i("Search completed successfully");
    }

}
