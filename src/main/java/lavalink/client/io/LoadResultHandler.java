package lavalink.client.io;

import lavalink.client.player.track.AudioPlaylist;
import lavalink.client.player.track.AudioTrack;

import java.util.List;

public interface LoadResultHandler {
	/**
	 * Called when the requested item is a track and it was successfully loaded.
	 *
	 * @param track The loaded track
	 */
	void trackLoaded(AudioTrack track);

	/**
	 * Called when the requested item is a playlist and it was successfully loaded.
	 *
	 * @param playlist The loaded playlist
	 */
	void playlistLoaded(AudioPlaylist playlist);

	/**
	 * Called when the requested item is a search result and it was successfully loaded.
	 *
	 * @param tracks The loaded tracks from the search
	 */
	void searchResultLoaded(List<AudioTrack> tracks);

	/**
	 * Called when there were no items found by the specified identifier.
	 */
	void noMatches();

	/**
	 * Called when loading an item failed with an exception.
	 *
	 * @param exception The exception that was thrown
	 */
	void loadFailed(FriendlyException exception);
}