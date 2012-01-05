package com.ettrema.media;

import com.bradmcevoy.io.FileUtils;
import com.bradmcevoy.io.StreamUtils;
import com.ettrema.video.FFMPEGConverter;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.ImageFile;
import com.ettrema.web.Thumb;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author brad
 */
public class FFMpegThumbProcessor implements ThumbProcessor {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FFMpegThumbProcessor.class);
	private String ffmpegProcess = "ffmpeg";

	public int generateThumbs(ImageFile imageFile, List<Thumb> thumbs, boolean skipIfExists) throws FileNotFoundException, IOException {
		Folder parent = imageFile.getParent();
		GenContext genContext = new GenContext(imageFile, skipIfExists, parent);

		for (Thumb thumb : thumbs) {
			generate(thumb, genContext);
		}
		return genContext.generated;
	}

	public int generateThumbs(ImageFile imageFile, List<Thumb> thumbs, Rotate rotate, boolean skipIfExists) throws FileNotFoundException, IOException {
		return generateThumbs(imageFile, thumbs, skipIfExists);
	}

	private void generate(Thumb thumbSpec, GenContext genContext) throws FileNotFoundException, IOException {
		Folder parent = genContext.parent;
		Folder thumbsFolder = parent.thumbs(thumbSpec.getSuffix(), true);

		// Ensure we dont do versioning of thumbs
		if (thumbsFolder.isVersioningEnabled() == null) {
			thumbsFolder.setVersioningEnabled(false);
			thumbsFolder.save();
		}

		BaseResource resExisting = thumbsFolder.childRes(genContext.imageFile.getName());
		if (resExisting != null) {
			if (genContext.skipIfExists) {
				return;
			} else {
				resExisting.deletePhysically();
			}
		}

		// get the original image, since we know we're going to generate
		if (genContext.tempImage == null) {
			genContext.tempImage = writeToTempFile(genContext.imageFile);
		}

		FileInputStream fin = null;
		String ext = FileUtils.getExtension(genContext.imageFile.getName());
		ByteArrayOutputStream out = new ByteArrayOutputStream(10000);
		try {
			fin = new FileInputStream(genContext.tempImage);
			FFMPEGConverter c = new FFMPEGConverter(ffmpegProcess, fin, ext);
			c.generateThumb(thumbSpec.getHeight(), thumbSpec.getWidth(), out, "jpeg");
		} finally {
			StreamUtils.close(fin);
		}

		ImageFile thumb = new ImageFile("image/jpeg", thumbsFolder, genContext.imageFile.getName());
		log.debug("create thumb: " + thumb.getHref());
		thumb.save();
		InputStream in = new ByteArrayInputStream(out.toByteArray());
		thumb.setContent(in);
		thumb.save();
	}

	private File writeToTempFile(ImageFile imageFile) throws IOException {
		File temp = File.createTempFile("clyde-FFMpegThumbProcessor", null);
		FileOutputStream fout = null;
		InputStream in = null;
		try {
			in = imageFile.getInputStream();
			StreamUtils.readTo(in, fout);
			return temp;
		} finally {
			StreamUtils.close(fout);
			StreamUtils.close(in);
		}
	}

	private class GenContext {

		final ImageFile imageFile;
		final boolean skipIfExists;
		final Folder parent;
		File tempImage;
		int generated;

		public GenContext(ImageFile imageFile, boolean skipIfExists, Folder parent) {
			this.imageFile = imageFile;
			this.skipIfExists = skipIfExists;
			this.parent = parent;
		}
	}
}
