/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.medialibrary.commons.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageHelper {

	public static InputStream getScaledInputStream(String sourceFilePath,
			int destWidth, int destHeight) throws IOException {
		//TODO: either remove or implement this method
		File picFile = new File(sourceFilePath);
		if (!picFile.exists()) {
			return null;
		}
		
		//don't use this for now as it doesn't seem to have a lot of impact 
		//to improve memory usage and the quality of the images is greatly reduced
		return new FileInputStream(picFile);
		
//		// algo source: http://www.rgagnon.com/javadetails/java-0243.html
//
//		// only scale jpg images bigger than 200KB in size
//		if (picFile.length() > 200 * 1024
//				&& (sourceFilePath.toLowerCase().endsWith("jpg") || sourceFilePath
//						.toLowerCase().endsWith("jpg"))) {
//			try {
//				InputStream imageStream = new BufferedInputStream(
//						new FileInputStream(sourceFilePath));
//				Image image = (Image) ImageIO.read(imageStream);
//
//				int thumbWidth = destWidth;
//				int thumbHeight = destHeight;
//
//				// Make sure the aspect ratio is maintained, so the image is not
//				// skewed
//				double thumbRatio = (double) thumbWidth / (double) thumbHeight;
//				int imageWidth = image.getWidth(null);
//				int imageHeight = image.getHeight(null);
//				double imageRatio = (double) imageWidth / (double) imageHeight;
//				if (thumbRatio < imageRatio) {
//					thumbHeight = (int) (thumbWidth / imageRatio);
//				} else {
//					thumbWidth = (int) (thumbHeight * imageRatio);
//				}
//
//				// Draw the scaled image
//				BufferedImage thumbImage = new BufferedImage(thumbWidth,
//						thumbHeight, BufferedImage.TYPE_INT_RGB);
//				Graphics2D graphics2D = thumbImage.createGraphics();
//				graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
//						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//				graphics2D
//						.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
//
//				// Write the scaled image to the outputstream
//				ByteArrayOutputStream out = new ByteArrayOutputStream();
//				ByteArrayInputStream res = null;
//
//				try {
//					JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//					JPEGEncodeParam param = encoder
//							.getDefaultJPEGEncodeParam(thumbImage);
//					int quality = 90; // Use between 1 and 100, with 100 being
//					// highest quality
//					quality = Math.max(0, Math.min(quality, 100));
//					param.setQuality((float) quality / 100.0f, false);
//					encoder.setJPEGEncodeParam(param);
//					encoder.encode(thumbImage);
//					ImageIO.write(thumbImage, "JPG", out);
//
//					res = new ByteArrayInputStream(out.toByteArray());
//				} finally {
//					out.close();
//				}
//
//				return res;
//			} catch (IOException ex) {
//				log.error("Failed to scale image " + sourceFilePath, ex);
//
//				// return a stream to the original file if the scale failed
//				return new FileInputStream(picFile);
//			}
//		} else {
//			return new FileInputStream(picFile);
//		}
	}
}
