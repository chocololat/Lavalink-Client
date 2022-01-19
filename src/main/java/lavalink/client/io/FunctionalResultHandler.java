package lavalink.client.io;

import lavalink.client.player.track.AudioPlaylist;
import lavalink.client.player.track.AudioTrack;

import java.util.List;
import java.util.function.Consumer;

/**
 * Helper class for creating an audio result handler using only methods that can be passed as lambdas.
 */
public class FunctionalResultHandler implements LoadResultHandler {
    private final Consumer<AudioTrack> trackConsumer;
    private final Consumer<AudioPlaylist> playlistConsumer;
    private final Consumer<List<AudioTrack>> searchResultConsumer;
    private final Runnable emptyResultHandler;
    private final Consumer<FriendlyException> exceptionConsumer;

    /**
     * Refer to {@link LoadResultHandler} methods for details on when each method is called.
     *
     * @param trackConsumer Consumer for single track result
     * @param playlistConsumer Consumer for playlist result
     * @param searchResultConsumer Consumer for search result
     * @param emptyResultHandler Empty result handler
     * @param exceptionConsumer Consumer for an exception when loading the item fails
     */
    public FunctionalResultHandler(Consumer<AudioTrack> trackConsumer, Consumer<AudioPlaylist> playlistConsumer, Consumer<List<AudioTrack>> searchResultConsumer, Runnable emptyResultHandler, Consumer<FriendlyException> exceptionConsumer) {
        this.trackConsumer = trackConsumer;
        this.playlistConsumer = playlistConsumer;
        this.searchResultConsumer = searchResultConsumer;
        this.emptyResultHandler = emptyResultHandler;
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        if (trackConsumer != null) {
            trackConsumer.accept(track);
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlistConsumer != null) {
            playlistConsumer.accept(playlist);
        }
    }

    @Override
    public void searchResultLoaded(List<AudioTrack> tracks) {
        if (searchResultConsumer != null) {
            searchResultConsumer.accept(tracks);
        }
    }

    @Override
    public void noMatches() {
        if (emptyResultHandler != null) {
            emptyResultHandler.run();
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        if (exceptionConsumer != null) {
            exceptionConsumer.accept(exception);
        }
    }
}

