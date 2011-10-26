package com.ettrema.web.image;

import com.ettrema.web.image.ImageService.ExifData;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author brad
 */
public interface IImageService {

	Dimensions getDimenions(InputStream in, String name);

	ExifData getExifData(InputStream in, String name);

	String getFormat(String name);

	Dimensions getImageDimensions(File in);

	/**
	 * Return scaled image.
	 * Pre-conditions: (source != null) && (width > 0) && (height > 0)
	 *
	 * @param source the image source
	 * @param width the new image's width
	 * @param height the new image's height
	 * @return the new image scaled
	 */
	BufferedImage getScaleImage(BufferedImage source, int width, int height);

	/**
	 * Return scaled image.
	 * Pre-conditions: (source != null) && (xscale > 0) && (yscale > 0)
	 *
	 * @param source the image source
	 * @param xscale the percentage of the source image's width
	 * @param yscale the percentage of the source image's height
	 * @return the new image scaled
	 */
	BufferedImage getScaleImage(BufferedImage source, double xscale, double yscale);

	/**
	 * Convenience method that returns a scaled instance of the
	 * provided {@code BufferedImage}.
	 *
	 * @param img the original image to be scaled
	 * @param targetWidth the desired width of the scaled instance,
	 * in pixels
	 * @param targetHeight the desired height of the scaled instance,
	 * in pixels
	 * @param hint one of the rendering hints that corresponds to
	 * {@code RenderingHints.KEY_INTERPOLATION} (e.g.
	 * {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
	 * {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
	 * {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	 * @param higherQuality if true, this method will use a multi-step
	 * scaling technique that provides higher quality than the usual
	 * one-step technique (only useful in downscaling cases, where
	 * {@code targetWidth} or {@code targetHeight} is
	 * smaller than the original dimensions, and generally only when
	 * the {@code BILINEAR} hint is specified)
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight);

	BufferedImage read(InputStream is, String type) throws FileNotFoundException, IOException;

	BufferedImage rotateLeft(BufferedImage image);

	BufferedImage rotateRight(BufferedImage image);

	boolean scaleProportionallyFromHeight(File input, File output, int height) throws IOException;

	boolean scaleProportionallyWithMax(File input, File output, int maxHeight, int maxWidth) throws IOException;

	boolean scaleProportionallyWithMax(InputStream in, OutputStream out, int maxHeight, int maxWidth, String format) throws IOException;

	boolean scaleProportionallyWithMax(BufferedImage image, OutputStream out, int maxHeight, int maxWidth, String format) throws IOException;

	void write(BufferedImage input, OutputStream out) throws IOException;

	void write(BufferedImage image, OutputStream out, String format);
	
}
