package com.ettrema.media;

import com.ettrema.context.Context;
import com.ettrema.grid.Processable;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsCommon;
import com.ettrema.vfs.VfsSession;
import com.ettrema.video.FlashService;
import com.ettrema.web.VideoFile;
import java.io.Serializable;
import java.util.UUID;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class StreamingVideoProcessable extends VfsCommon implements Processable, Serializable {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( StreamingVideoProcessable.class );
	private static final long serialVersionUID = 1L;
	private final String sourceName;
	private final UUID id;

	public StreamingVideoProcessable(String sourceName, UUID id) {
		this.sourceName = sourceName;
		this.id = id;
	}

	@Override
	public void doProcess(Context context) {
		log.debug("processing: " + sourceName);
		VfsSession vfs = context.get(VfsSession.class);
		NameNode nn = vfs.get(id);
		if (nn == null) {
			log.warn("Couldnt find node: " + id);
			return;
		}
		DataNode data = nn.getData();
		if (data == null) {
			log.warn("node was found but datanode was null: name node id: " + id);
		} else if (data instanceof VideoFile) {
			VideoFile file = (VideoFile) data;
			FlashService gen = _(FlashService.class);
			try {
				gen.generateStreamingVideo(file);
				commit();
			} catch (Exception e) {
				log.warn("Exception generating streaming video: " + file.getHref(), e);
				rollback();
			}
		} else {
			log.warn("Not an instanceof video file: " + data.getClass());
		}
	}

	@Override
	public void pleaseImplementSerializable() {
	}
	
}
