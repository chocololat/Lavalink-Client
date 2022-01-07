package lavalink.client.player.track;

public interface AudioTrackInfo {

	String getTitle();
	String getAuthor();
	long getLength();
	String getIdentifier();
	boolean isStream();
	String getUri();
	String getSourceName();
}
