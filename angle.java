import com.recognition.software.jdeskew.ImageDeskew;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITessAPI.TessBaseAPI;
import net.sourceforge.tess4j.ITessAPI.TessPageIterator;
import net.sourceforge.tess4j.ITessAPI.TessPageSegMode;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.ImageIOHelper;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class angle {

  public static double findSkewAngle(BufferedImage image) {
    // ImageDeskew skew = new ImageDeskew(image);
    // double angle = skew.getSkewAngle();
    Custom_ImageDeskew skew = new Custom_ImageDeskew(image);

    double angle = skew.getSkewAngle();
    System.out.println("angle is " + (-angle));
  
    return -angle;
  }
   static  ArrayList<ArrayList<Double>> arr = new ArrayList<>();
   public static void printAllAngles (){ 
    for(ArrayList<Double> a :arr){
      for (Double d : a) {
        System.out.print(d+" ");
      }
      System.out.println();
    }
   }
  public static BufferedImage deskew(BufferedImage image) {
     ArrayList <Double> temp =  new ArrayList<>();
    double prevAngle = findSkewAngle(image);
    temp.add(prevAngle);
    BufferedImage rotatedImg = ImageHelper.rotateImage(image, prevAngle);
    
    double newAngle = findSkewAngle(rotatedImg);
    temp.add(newAngle);

    while (!(newAngle > -1 && newAngle < 1) && prevAngle != newAngle) {
      prevAngle = newAngle;
      rotatedImg = ImageHelper.rotateImage(rotatedImg, newAngle);
      newAngle = findSkewAngle(rotatedImg);
      temp.add(newAngle);
    }
    arr.add(temp);
    return rotatedImg;
  }

  // public static BufferedImage rotate_me(BufferedImage bimg, Double angle) {
  // // text garbed
  // double sin = Math.abs(Math.sin(Math.toRadians(angle))),
  // cos = Math.abs(Math.cos(Math.toRadians(angle)));
  // int w = bimg.getWidth();
  // int h = bimg.getHeight();
  // int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h *
  // cos + w * sin);
  // BufferedImage rotated = new BufferedImage(neww, newh, bimg.getType());
  // Graphics2D graphic = rotated.createGraphics();
  // graphic.translate((neww - w) / 2, (newh - h) / 2);
  // graphic.rotate(Math.toRadians(angle), w / 2, h / 2);
  // graphic.drawRenderedImage(bimg, null);
  // graphic.dispose();
  // return rotated;
  // }

  public static BufferedImage checkAndRotate(BufferedImage image, boolean deskew, String lang) {

    TessAPI api = TessAPI.INSTANCE;
    TessBaseAPI handle = api.TessBaseAPICreate();
    IntBuffer orientation = IntBuffer.allocate(1);
    IntBuffer direction = IntBuffer.allocate(1);
    IntBuffer order = IntBuffer.allocate(1);
    FloatBuffer deskew_angle = FloatBuffer.allocate(1);
    ByteBuffer buf = ImageIOHelper.convertImageData(image);
    int bpp = image.getColorModel().getPixelSize();
    System.out.println("bpp " + bpp);
    int bytespp = bpp / 8;
    System.out.println("bytespp " + bytespp);
    int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);
    System.out.println("bytespl " + bytespl);
    api.TessBaseAPIInit3(handle, System.getProperty("tessdata.dir"), lang);
    api.TessBaseAPISetPageSegMode(handle, TessPageSegMode.PSM_AUTO_OSD);
    api.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);
    int success = api.TessBaseAPIRecognize(handle, null);
    System.out.println("success val " + success);
    if (success == 0) {
      TessPageIterator pi = api.TessBaseAPIAnalyseLayout(handle);
      api.TessPageIteratorOrientation(pi, orientation, direction, order, deskew_angle);

      System.out.println("pi " + pi);
      System.out.println("orientation " + orientation);
      System.out.println("direction " + direction);
      System.out.println("order " + order);
      System.out.println("deskew_angle " + deskew_angle);

      if (orientation.get(0) == 1) {
        System.out.println("-90");
        image = ImageHelper.rotateImage(image, -90);
      }
      if (orientation.get(0) == 2) {
        System.out.println("180");
        image = ImageHelper.rotateImage(image, 180);
      }
      if (orientation.get(0) == 3) {
        System.out.println("90");
        image = ImageHelper.rotateImage(image, 90);
      }
      if (deskew) {
        System.out.println("deskew");
        image = ImageHelper.rotateImage(image, -deskew_angle.get(0));
      }
    }
    api.TessBaseAPIDelete(handle);

    return image;
  }

  public static void saveImage(Mat image, String path) {
    Imgcodecs.imwrite(path, image);
  }

  public static void main(String[] args) throws IOException {
    BufferedImage img = ImageIO.read(
        new File("/home/local/ZOHOCORP/bairavi-14586/Downloads/ESRGAN/hacky_test/rot.png"));
    BufferedImage angleCorrected = checkAndRotate(img, true, "eng");
    angleCorrected = deskew(angleCorrected);
    // Utils.saveImage(img,
    // "/home/local/ZOHOCORP/bairavi-14586/Downloads/ESRGAN/hacky_test/out/dumma.png");
    File outputfile = new File("/home/local/ZOHOCORP/bairavi-14586/Downloads/ESRGAN/hacky_test/out/sample.png");
    ImageIO.write(angleCorrected, "png", outputfile);
    System.out.println("completed");
  }
}
