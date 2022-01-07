package lavalink.client.player.track;

import java.io.*;
import java.util.Base64;

public interface Track {

	int TRACK_INFO_VERSIONED = 1;
	int TRACK_INFO_VERSION = 2;

	static void writeNullableText(DataOutput output, String text) throws IOException {
		output.writeBoolean(text != null);

		if (text != null) {
			output.writeUTF(text);
		}
	}

	static String readNullableText(DataInput input) throws IOException {
		boolean exists = input.readBoolean();
		return exists ? input.readUTF() : null;
	}

	static TrackInfo decode(String track) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(track));
		MessageInput stream = new MessageInput(bais);
		DataInput input = stream.nextMessage();
		if (input == null) {
			return null;
		}

		input.readByte();

		TrackInfo trackInfo = new DefaultTrackInfo(
				input.readUTF(),
				input.readUTF(),
				input.readLong(),
				input.readUTF(),
				input.readBoolean(),
				readNullableText(input),
				input.readUTF()
		);

		stream.skipRemainingBytes();
		return trackInfo;
	}

	static String encode(TrackInfo trackInfo) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MessageOutput stream = new MessageOutput(baos);
		DataOutput output = stream.startMessage();
		output.write(TRACK_INFO_VERSION);

		output.writeUTF(trackInfo.getTitle());
		output.writeUTF(trackInfo.getAuthor());
		output.writeLong(trackInfo.getLength());
		output.writeUTF(trackInfo.getIdentifier());
		output.writeBoolean(trackInfo.isStream());
		writeNullableText(output, trackInfo.getUri());
		output.writeUTF(trackInfo.getSourceName());

		stream.commitMessage(TRACK_INFO_VERSIONED);

		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	String getTrack();

	TrackInfo getInfo();

	void setUserData(Object userData);

	Object getUserData();

	<T> T getUserData(Class<T> klass);
}
