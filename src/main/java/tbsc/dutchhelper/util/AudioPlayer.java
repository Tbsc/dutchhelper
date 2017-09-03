package tbsc.dutchhelper.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Helper class for playing audio files, either from a local file or from a URL.
 * Copied directly from DutchHelper-v1.
 *
 * Created on 07/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class AudioPlayer {

    public static void playURL(String url) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        play(new URL(url));
    }

    public static void playFile(String file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        play(new File(file));
    }

    public static void play(URL url) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        play(AudioSystem.getAudioInputStream(url));
    }

    public static void play(File file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        play(AudioSystem.getAudioInputStream(file));
    }

    private static void play(AudioInputStream in) throws IOException, LineUnavailableException {
        final AudioFormat outFormat = getOutFormat(in.getFormat());
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, outFormat);

        try (final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            if (line != null) {
                line.open(outFormat);
                line.start();
                AudioInputStream inputMystream = AudioSystem.getAudioInputStream(outFormat, in);
                stream(inputMystream, line);
                line.drain();
                line.stop();
            }
        }
    }

    private static AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();
        final float rate = inFormat.getSampleRate();
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
    }

    private static void stream(AudioInputStream in, SourceDataLine line) throws IOException {
        final byte[] buffer = new byte[65536];
        for (int n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
            line.write(buffer, 0, n);
        }
    }

}
