package util;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Sam on 11/10/2014.
 */
public class ImageTagger {
    public static void changeExifMetadata(File jpegImageFile, File dst,double lat,double lon)
            throws IOException, ImageReadException, ImageWriteException
    {
        OutputStream os = null;
        try
        {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata)
            {
                // note that exif might be null if no Exif metadata is found.
                TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif)
                {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet)
                outputSet = new TiffOutputSet();

            {
                // Example of how to add a field/tag to the output set.
                //
                // Note that you should first remove the field/tag if it already
                // exists in this directory, or you may end up with duplicate
                // tags. See above.
                //
                // Certain fields/tags are expected in certain Exif directories;
                // Others can occur in more than one directory (and often have a
                // different meaning in different directories).
                //
                // TagInfo constants often contain a description of what
                // directories are associated with a given tag.
                //
                // see
                // org.apache.sanselan.formats.tiff.constants.AllTagConstants
                //
                TiffOutputField aperture = TiffOutputField.create(
                        TiffConstants.EXIF_TAG_APERTURE_VALUE,
                        outputSet.byteOrder, new Double(0.3));
                TiffOutputDirectory exifDirectory = outputSet
                        .getOrCreateExifDirectory();
                // make sure to remove old value if present (this method will
                // not fail if the tag does not exist).
                exifDirectory.removeField(TiffConstants.EXIF_TAG_APERTURE_VALUE);
                exifDirectory.add(aperture);
            }

            {
                // Example of how to add/update GPS info to output set.
                double longitude = lon;
                double latitude = lat;
                outputSet.setGPSInDegrees(longitude, latitude);
            }

            // printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);

            //os = new FileOutputStream(dst);
            os = new BufferedOutputStream(os);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);

            os.close();
            os = null;
        } finally
        {
            if (os != null)
                try
                {
                    os.close();
                } catch (IOException e)
                {

                }
        }
    }
}
