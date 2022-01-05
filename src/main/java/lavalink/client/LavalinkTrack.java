package lavalink.client;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class LavalinkTrack implements AudioTrack {

	private static final Logger log = LoggerFactory.getLogger(LavalinkTrack.class);

	private final AudioTrackInfo info;
	private final long position;
	private final AudioSourceManager sourceManager;
	private Object userData;

	public LavalinkTrack(AudioTrackInfo info, long position, AudioSourceManager sourceManager) {
		this.info = info;
		this.position = position;
		this.sourceManager = sourceManager;
	}

	public LavalinkTrack(AudioTrackInfo info, long position, String sourceName) {
		this(info, position, new DummySourceManager(sourceName));
	}

	public static AudioTrack decode(String track) throws IOException {
		MessageInput stream = new MessageInput(new ByteArrayInputStream(Base64.decodeBase64(track)));
		DataInput input = stream.nextMessage();

		int version = (stream.getMessageFlags() & 1) != 0 ? (input.readByte() & 0xFF) : 1;

		String title = input.readUTF();
		String author = input.readUTF();
		long length = input.readLong();
		String identifier = input.readUTF();
		boolean isStream = input.readBoolean();
		String uri = version >= 2 ? DataFormatTools.readNullableText(input) : null;
		String sourceName = input.readUTF();
		long position = input.readLong();
		stream.skipRemainingBytes();
		return new LavalinkTrack(new AudioTrackInfo(title, author, length, identifier, isStream, uri), position, sourceName);
	}

	public static String encode(AudioTrack track) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MessageOutput outputStream = new MessageOutput(baos);
		DataOutput output = outputStream.startMessage();
		output.write(2);

		AudioTrackInfo trackInfo = track.getInfo();
		output.writeUTF(trackInfo.title);
		output.writeUTF(trackInfo.author);
		output.writeLong(trackInfo.length);
		output.writeUTF(trackInfo.identifier);
		output.writeBoolean(trackInfo.isStream);
		DataFormatTools.writeNullableText(output, trackInfo.uri);

		output.writeUTF(track.getSourceManager().getSourceName());
		output.writeLong(track.getPosition());
		outputStream.commitMessage(1);
		return Base64.encodeBase64String(baos.toByteArray());
	}

	@Override
	public AudioTrackInfo getInfo() {
		return this.info;
	}

	@Override
	public String getIdentifier() {
		return this.info.identifier;
	}

	@Override
	public AudioTrackState getState() {
		return null;
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isSeekable() {
		return !this.info.isStream;
	}

	@Override
	public long getPosition() {
		return this.position;
	}

	@Override
	public void setPosition(long position) {
	}

	@Override
	public void setMarker(TrackMarker marker) {
	}

	@Override
	public long getDuration() {
		return this.info.length;
	}

	@Override
	public AudioTrack makeClone() {
		return new LavalinkTrack(this.info, this.position, this.sourceManager);
	}

	@Override
	public AudioSourceManager getSourceManager() {
		return this.sourceManager;
	}

	@Override
	public void setUserData(Object userData) {
		this.userData = userData;
	}

	@Override
	public Object getUserData() {
		return this.userData;
	}

	@Override
	public <T> T getUserData(Class<T> klass) {
		//noinspection unchecked
		return this.userData == null ? null : (T) this.userData;
	}
}
