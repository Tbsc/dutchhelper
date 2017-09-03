package tbsc.dutchhelper.eventhandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import tbsc.dutchhelper.DutchHelperApplication;
import tbsc.dutchhelper.util.AudioPlayer;
import tbsc.dutchhelper.util.Log;
import tbsc.dutchhelper.util.wikt.WiktionaryHelper;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * Handles audio playback.
 * Executed when the play audio button is pressed, or the ENTER key is pressed inside the word field.
 *
 * Created on 12/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class PlayAudioEventHandler implements EventHandler<ActionEvent> {

    private static Log log = new Log(PlayAudioEventHandler.class);
    
    @Override
    public void handle(ActionEvent event) {
        // using a separate thread because this way UI methods are executed when called and not delayed
        new Thread(() -> {
            log.i("Play audio event handler called");

            // prevent pressing button while playing
            DutchHelperApplication.get().playAudioBtn.setDisable(true);
            log.d("Disabled play-audio button");

            // remove previous error message
            DutchHelperApplication.showError("");
            log.d("Cleared error label");

            try {
                if (DutchHelperApplication.currentAudioFileProperty.get().isEmpty()) {
                    log.w("User attempted to play audio without searching for a word first");
                    DutchHelperApplication.showError("no word searched");
                    return;
                }
                log.d("There is a selected word (%s), can start trying to fetch its audio file URL",
                        DutchHelperApplication.currentAudioFileProperty.get());

                String audioURL = WiktionaryHelper.getAudioURL(DutchHelperApplication.currentAudioFileProperty.get());

                // getAudioURL never returns null so this is safe
                if (audioURL.isEmpty()) {
                    log.e("Fetch method returned no value; audio file URL wasn't found");
                    DutchHelperApplication.showError("error fetching");
                    return;
                }
                log.d("Fetched audio file URL: %s", audioURL);

                log.d("Everything was successful up to now, start playing audio from URL");
                AudioPlayer.playURL(audioURL);
                log.d("Audio playback done");
                log.d("Playing audio completed successfully");
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                log.d("Failure playing audio file %s, error: %s",
                        DutchHelperApplication.currentAudioFileProperty.get(), e.getMessage());
                DutchHelperApplication.showError("error playing audio", e.getMessage());
            } finally {
                // finished playing, let user press button again
                // it's in a finally block so even if an error happens it will let you play again
                DutchHelperApplication.get().playAudioBtn.setDisable(false);
                log.d("Re-enabled play-audio button");
            }
        }).start();
    }

}
