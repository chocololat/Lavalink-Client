package lavalink.client.player.track;

public interface TrackInfo {

	String getTitle();
	String getAuthor();
	long getLength();
	String getIdentifier();
	boolean isStream();
	String getUri();
	String getSourceName();
}
