/*
		MetaDB: A Distributed Metadata Collection Tool
		Copyright 2011, Lafayette College, Eric Luhrs, Haruki Yamaguchi, Long Ho.

		This file is part of MetaDB.

    MetaDB is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MetaDB is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MetaDB.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.lafayette.metadb.model.imagemgt;

import edu.lafayette.metadb.model.commonops.MetaDbHelper;
import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import java.io.*;
import java.util.ArrayList;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.OpImage;

import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.Sanselan;

import com.sun.media.jai.codec.SeekableStream;

/**
 * Class to handle the file-level creation for derivatives and annotations using ImageMagick.
 *
 * @author Long L. Ho
 * @author Miguel Haruki Yamaguchi
 * @version 1.0 
 * 
 */
public class ImageGenerator
{

	/**
	 * Generates a resized image based on input/output filenames, and max width/max height.
	 * @param inputFileName The input file path.
	 * @param outputFileName The output file path.
	 * @param maxWidth The max width of the resized image.
	 * @param maxHeight The max height of the resized image.
	 * @return true if the resized image was successfully output, false otherwise.
	 */

	public static boolean generateDerivative(String inputFileName, String outputFileName, int maxWidth, int maxHeight)
	{	
		try
		{
			//Get the image as a RenderedOp handle-able by JAI
			FileInputStream is=new FileInputStream(inputFileName);
			SeekableStream s = SeekableStream.wrapInputStream(is, true);
			RenderedOp image = JAI.create("stream", s);
			((OpImage)image.getRendering()).setTileCache(null);

			double scale = 1.0;

			if(maxWidth==0||maxHeight==0) //If either of the width/height are 0, set scale to 100% (UI issue)
				scale=1.0;
			
			//If the image is too small to be scaled to given maxWidth/maxHeight, 
			//just leave it as it is.
			else if(image.getWidth()<=maxWidth || image.getHeight()<=maxHeight)
				scale=1.0;
			
			else //image is large enough
			{
				//Calculate the appropriate scale factor from the maxWidth and maxHeight.
				double xScale = (double)maxWidth / (double)image.getWidth();
				double yScale = (double)maxHeight / (double)image.getHeight();
				//Use the smallest scale to make sure the result fits within specified dimensions
				scale=(xScale < yScale) ? xScale : yScale;
			}
			//MetaDbHelper.note("Configuring parameter block...");
			ParameterBlock pb = new ParameterBlock();
			pb.addSource(image); // The source image
			pb.add(scale);          // The xScale
			pb.add(scale);          // The yScale
			pb.add(0.0F);           
			pb.add(0.0F);          
			pb.add(image);

			//Resize the img.
			RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			RenderedOp resizedImage = JAI.create("SubsampleAverage", pb, qualityHints);
			MetaDbHelper.note("Writing derivative to disk...");
			//Output to file.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JAI.create("encode", resizedImage, baos, "JPEG", null);
			FileOutputStream fileOut=new FileOutputStream(outputFileName);
			fileOut.write(baos.toByteArray());
			is.close();
			s.close();
			fileOut.close();
			baos.close();
			MetaDbHelper.note("Wrote image to disk");
			return true;
		}
		catch(FileNotFoundException e)
		{
			MetaDbHelper.logEvent(e);
		}
		catch(Exception e)
		{
			MetaDbHelper.logEvent(e);
		}
		return false;
	}

	/**
	 * Annotates a freshly-generated image with background color + text of a specified color. 
	 * @param inputFileName The file to annotate.
	 * @param annotationMode The annotation mode; 0=none, 1=band, 2=brand.
	 * @param brandText The text of the annotation. 
	 * @param bgColor The background color for the annotation text.
	 * @param fgColor The foreground (font) color for the annotation text.
	 * @return true if the annotation succeeded, false otherwise.
	 */
	public static boolean processAnnotation(String inputFileName, int annotationMode, String brandText, 
			String bgColor, String fgColor)
	{
		boolean processed = false;
		if(annotationMode==0) //The annotation mode is turned off. This shouldn't happen due to filtering. 
			return false;

		else
		{
			try
			{		
				//MetaDbHelper.note("Annotation: file="+inputFileName);
				//MetaDbHelper.note("Annotation: brand text="+brandText);
				//MetaDbHelper.note("Annotation: color: BG="+bgColor+", FG="+fgColor);
				
				int imageWidth=0;
				int imageHeight=0;
				File image=new File(inputFileName);
				//Make sure the file exists on the filesystem.
				if(!image.exists()||image==null)
				{
					//MetaDbHelper.note("Image "+image.getName()+" inexistent");
					return false;
				}

				//Attempt to retrieve metadata
				ImageInfo info=Sanselan.getImageInfo(image);
				{
					if(info!=null)
					{
						imageWidth=info.getWidth();
						imageHeight=info.getHeight();
					}
				}
				//Parse error; set to dummy defaults.
				if(imageWidth==0)
					imageWidth=2000;
				if(imageHeight==0)
					imageHeight=1800;
				
				int fontSize=0;		
				if(imageWidth>=500&&imageWidth<=1000)
					fontSize=14;
				
				else if(imageWidth>=1000 && imageWidth<=1400)
					fontSize=24;
				
				else if(imageWidth>=1400 && imageWidth<=1800)
					fontSize=48;
				
				else if(imageWidth>=1800 && imageWidth<=2200)
					fontSize=72;
				
				else if(imageWidth>=2200 && imageWidth<=3000)
					fontSize=85;
				
				else if(imageWidth>=3000)
					fontSize=100;
				//End naive font size calculation
				
				//MetaDbHelper.note("Annotation: image width= "+imageWidth+", height="+imageHeight);
				//MetaDbHelper.note("Annotation: image fontsize="+fontSize);

				if (annotationMode==1) //Banding.
				{					
					//Build the command to annotate the image.
					ArrayList<String> command=new ArrayList<String>();
					command.add("convert");
					command.add("-quality");
					command.add(""+100); //maximum quality
					command.add("-font");
					command.add("Bitstream-Charter-Regular");
					command.add("-background"); 
					command.add(bgColor);
					command.add("-fill");
					command.add(fgColor); //set the background/foreground colors 
					command.add("-pointsize"); 
					command.add(""+fontSize); //specify font size
					command.add("-gravity"); 
					command.add("center"); //set the text to centered
					command.add("-size"); //set the width of the bounding box
					command.add(""+imageWidth+"x"); 
					command.add("caption:"+brandText); //set the actual text
					command.add("+size"); 
					command.add(inputFileName); //fit to the dimension of input file
					command.add("+swap"); //swap the band with the original to have it under image
					command.add("-gravity"); 
					command.add("center");
					command.add("-append"); //append the band to the image.
					command.add(inputFileName);
					
					//MetaDbHelper.note("Running ImageMagick command: "+command);
					//Execute the conversion.
					Runtime.getRuntime().exec((String[])command.toArray(new String[1]));
					processed = true;
				}

				else if(annotationMode==2) //Branding.
				{
					//Build the command to annotate the image.
					ArrayList<String> command=new ArrayList<String>();

					/*
					 * convert -background none -fill "#ffffff96" -gravity center -size 800x -pointsize 45 
					 * -font Bitstream-Charter-Regular caption:"Testing the functionality of this thing. Testing." 
					 * lc-spcol-chiafeng-postcards-0002-800.jpg +swap -gravity center -composite 
					 * lc-spcol-chiafeng-postcards-0001-800t.jpg

					 */
					command.add("convert");
					command.add("-quality");
					command.add(""+100);
					command.add("-background");
					command.add("none");
					command.add("-fill");
					command.add(fgColor+"96"); //alpha
					command.add("-gravity");
					command.add("center");
					command.add("-size");
					command.add(""+(imageWidth-(imageWidth/10))+"x");
					command.add("-pointsize");
					command.add(""+fontSize);
					command.add("-font");
					command.add("Bitstream-Charter-Regular");
					command.add("caption:"+brandText);
					command.add(inputFileName); // Input
					command.add("+swap");
					command.add("-gravity");
					command.add("center");
					command.add("-composite");
					command.add(inputFileName); // Output  file name is same
					 
					//MetaDbHelper.note("Running ImageMagick command: "+command);
					//Execute the conversion.
					Runtime.getRuntime().exec((String[])command.toArray(new String[1]));
					processed = true;
				}
				else //Invalid option for annotation mode.
				{
					processed=false;
				}
			}
			catch(Exception e)
			{
				MetaDbHelper.logEvent(e);
			}
			return processed;
		}
	}

}