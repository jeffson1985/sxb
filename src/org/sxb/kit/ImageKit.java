package org.sxb.kit;

import net.coobird.thumbnailator.Thumbnails;

/**
 * 重新设计开源组件 未完成
 * 
 * @author Sun
 *
 */
public class ImageKit extends Thumbnails {

	ImageKit() {}
	
	
	public static void main(String args){
		/**
		
	//Create a thumbnail with rotation and a watermark	
		ImageKit.of(new File("original.jpg"))
	    .size(160, 160)
	    .rotate(90)
	    .watermark(Positions.BOTTOM_RIGHT, ImageIO.read(new File("watermark.png")), 0.5f)
	    .outputQuality(0.8)
	    .toFile(new File("image-with-watermark.jpg"));
		
		
	//Create a thumbnail and write to an OutputStream	
		OutputStream os = ...;

		ImageKit.of("large-picture.jpg")
		        .size(200, 200)
		        .outputFormat("png")
		        .toOutputStream(os);
//		Creating fixed-size thumbnails	
		BufferedImage originalImage = ImageIO.read(new File("original.png"));

		BufferedImage thumbnail = ImageKit.of(originalImage)
		        .size(200, 200)
		        .asBufferedImage();
//		Scaling an image by a given factor	
		BufferedImage originalImage = ImageIO.read(new File("original.png"));

		BufferedImage thumbnail = ImageKit.of(originalImage)
		        .scale(0.25)
		        .asBufferedImage();
//		Rotating an image when creating a thumbnail	
		BufferedImage originalImage = ImageIO.read(new File("original.jpg"));

		BufferedImage thumbnail = ImageKit.of(originalImage)
		        .size(200, 200)
		        .rotate(90)
		        .asBufferedImage();
	//Creating a thumbnail with a watermark	
		BufferedImage originalImage = ImageIO.read(new File("original.jpg"));
		BufferedImage watermarkImage = ImageIO.read(new File("watermark.png"));

		BufferedImage thumbnail = ImageKit.of(originalImage)
		        .size(200, 200)
		        .watermark(Positions.BOTTOM_RIGHT, watermarkImage, 0.5f)
		        .asBufferedImage();
	//Writing thumbnails to a specific directory
		File destinationDir = new File("path/to/output");

		ImageKit.of("apple.jpg", "banana.jpg", "cherry.jpg")
		        .size(200, 200)
		        .toFiles(destinationDir, Rename.PREFIX_DOT_THUMBNAIL);
		
		File destinationDir = new File("path/to/output");
	//preserve the original filename while writing to a specified directory
		ImageKit.of("apple.jpg", "banana.jpg", "cherry.jpg")
		        .size(200, 200)
		        .toFiles(destinationDir, Rename.NO_CHANGE);
*/
	}
	
}
