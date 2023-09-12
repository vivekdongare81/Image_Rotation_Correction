import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.ImageHelper;
import org.apache.commons.text.similarity.LevenshteinDistance;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.recognition.software.jdeskew.ImageDeskew;

public class New_ExtractWord {

  private Tesseract instance;

  public ExtractWord(int engineMode, int pageMode, String lang) {

    this.instance = new Tesseract();
    this.instance.setOcrEngineMode(engineMode);
    this.instance.setPageSegMode(pageMode);
    this.instance.setLanguage(lang);
    this.instance.setTessVariable("output-preserve-enabled", "1");
    this.instance.setTessVariable("preserve_interword_spaces", "1");
    this.instance.setTessVariable("textord_heavy_nr", "1");
    this.instance.setTessVariable("language_model_ngram_on", "1");
  }

  private Mat draw(List<Rectangle> list, Mat image, Scalar color) {
    for (Rectangle rectangle : list) {

      Rect rect = new Rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

      Imgproc.rectangle(image, rect.br(), rect.tl(), color, 1);
      // Utils.saveImage(image,
      // "/home/local/ZOHOCORP/bairavi-14586/Downloads/dumma.png");
    }
    return image;
  }

  public static Mat bufferedImageToMat(BufferedImage bi) {

    Mat mat = null;

    // if (bi.getType() == BufferedImage.TYPE_INT_RGB) {
    mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
    byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
    // DataBufferByte dd = (DataBufferByte) bi.getRaster().getDataBuffer();
    // byte[] data = dd.getData();
    mat.put(0, 0, data);
    // } else {
    // mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
    // byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
    // mat.put(0, 0, data);
    // }

    return mat;
  }

  public static double checkAOI(Rectangle rectSent, Rectangle rectWord) {

    double sx1 = rectSent.x, sy1 = rectSent.y, swidth = rectSent.width, sheight = rectSent.height;
    double wx1 = rectWord.x, wy1 = rectWord.y, wwidth = rectWord.width, wheight = rectWord.height;
    double sx2 = sx1 + swidth;
    double sy2 = sy1 + sheight;
    double wx2 = wx1 + wwidth;
    double wy2 = wy1 + wheight;

    // calculating area of boxes

    double wordArea = ((wx2 - wx1) * (wy2 - wy1));
    double sentArea = ((sx2 - sx1) * (sy2 - sy1));

    // finding extreme points

    double width = (Math.min(wx2, sx2)) - (Math.max(wx1, sx1));
    double height = (Math.min(wy2, sy2)) - (Math.max(wy1, sy1));

    double area = 0;

    if (width < 0 || height < 0) {
      area = 0;
    } else {
      area = width * height;
    }

    double minAOI = area / (Math.min(wordArea, sentArea));

    return minAOI;
  }

  public List<Word> findText(File image, int level) {
    try {
      return findText(ImageIO.read(image), level);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public List<Word> findText(BufferedImage image, int level) {
    return instance.getWords(image, level);
  }

  public static JSONArray doOCR() throws Exception {
    JSONArray responseArray = new JSONArray();

    return responseArray;
  }

  public static void saveImage(Mat image, String path) {
    Imgcodecs.imwrite(path, image);
  }

  public String stripExtension(final String filePath) {
    return filePath != null && filePath.lastIndexOf(".") > 0
        ? filePath.substring(0, filePath.lastIndexOf("."))
        : filePath;
  }

  public static BufferedImage resizeAndCropImage(BufferedImage originalImage, int newWidth, int newHeight) {
    // Calculate the starting point (top-left corner) for cropping
    int x = (originalImage.getWidth() - newWidth) / 2;
    int y = (originalImage.getHeight() - newHeight) / 2;

    // Create a cropped image with the desired dimensions
    BufferedImage croppedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = croppedImage.createGraphics();

    // Draw the center portion of the original image onto the cropped image
    g.drawImage(originalImage, 0, 0, newWidth, newHeight, x, y, x + newWidth, y + newHeight, null);
    g.dispose();

    return croppedImage;
  }

  public static int calculateLevenshteinDistance(String text1, String text2) {
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    return levenshteinDistance.apply(text1, text2);
  }

  public static void main(String[] args) throws IOException {
    // String cvsoPath = args[0];
    // String tessDataPath = args[1];
    // String imgPath = args[2];
    String cvsoPath = "/home/vivek-18890/Python_setup/opencv-4.8.0/build/lib/libopencv_java480.so";
    String tessDataPath = "/home/vivek-18890/Python_setup/libs/tessdata/tessdata_best-main/";
    String imgPath = "/home/vivek-18890/Downloads/hacky_test/National73cropped.png";

    System.out.println("loading cv.so file...");
    System.load(cvsoPath);
    System.out.println("loaded cv.so");
    Scalar color = new Scalar(0, 0, 0);

    if (imgPath.endsWith(".jpg") || imgPath.endsWith(".png") || imgPath.endsWith(".jpeg")) {
      System.out.println("performing sentence level ocr in image");
      System.out.println("input image path read: " + imgPath);
      // Mat matrix = Imgcodecs.imread(imgPath);
      // Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGRA2BGR);
      // Imgcodecs.imwrite("/tmp/out.png", matrix);
      ExtractWord i2t = new ExtractWord(1, 4, "eng");
      String extStrip = i2t.stripExtension(imgPath);
      File inpuImg = new File(imgPath);
      String parentDir = inpuImg.getParent();
      parentDir = parentDir + File.separator + "annotated";
      File parentDirectory = new File(parentDir);

      System.out.println("parent directory " + parentDir);
      BufferedImage img2Deskew = ImageIO.read(inpuImg);

      BufferedImage deskewkedImg = angle.deskew(img2Deskew);

      File f = new File("/home/vivek-18890/Downloads/hacky_test/MyFile.png");
      ImageIO.write(deskewkedImg, "PNG", f);

      System.out.println("done");

      BufferedImage image = ImageIO.read(f);
      BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      result.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);

      Mat matrix = bufferedImageToMat(result);
      //
      parentDirectory.mkdirs();
      //
      // saveImage(matrix, parentDir + File.separator + "deskewd.jpg");
      //
      String original_Text = "Nature also has a way of reminding us of the fragility of life We may be reminded of the fragility of life when we witness a storm ravage a forest when we witness a wildfire take away everything in its path or when we see the effects of global warming on our planet In these moments were reminded of the importance of taking care of our planet and of appreciating the beauty of nature";

      List<Word> words = ocrOnImage(i2t, tessDataPath, result, matrix, color, parentDir, extStrip);
      String words_str = "";
      int confidence = 0;
      for (Word w : words) {
        words_str += w.getText() + " ";
        confidence += w.getConfidence();
      }
      BufferedImage rotatedImage = ImageHelper.rotateImage(result, 180);
      Mat matrix_rot = bufferedImageToMat(rotatedImage);

      List<Word> rotated_words = ocrOnImage(i2t, tessDataPath, rotatedImage, matrix_rot, color, parentDir, extStrip);

      String rotated_words_str = "";
      int confidence_rot = 0;
      for (Word w : rotated_words) {
        rotated_words_str += w.getText() + " ";
        confidence_rot += w.getConfidence();
      }

      int levenshteinDistance = calculateLevenshteinDistance(original_Text, words_str.trim());
      int levenshteinDistance_rotated = calculateLevenshteinDistance(original_Text, rotated_words_str.trim());

      System.out.println(original_Text);
      System.out.println(words_str);
      System.out.println(rotated_words_str);
      // System.out.println(levenshteinDistance + " " + levenshteinDistance_rotated);
      System.out.println(confidence + " " + confidence_rot);
      // if (levenshteinDistance >= levenshteinDistance_rotated) {
      if (confidence <= confidence_rot) {
        System.out.println("Image is flipped.");
        result = rotatedImage;

      }
      File output_f = new File("/home/vivek-18890/Downloads/hacky_test/deskewd.png");
      ImageIO.write(result, "png", output_f);


    } else {
      System.out.println("invalid image path given");
    }

  }

  public static List<Word> ocrOnImage(ExtractWord i2t, String tessDataPath, BufferedImage result, Mat matrix,
      Scalar color, String parentDir, String extStrip) {
    i2t.instance.setDatapath(tessDataPath);
    List<Word> lines = null;
    try {
      try {

        lines = i2t.findText(result, TessPageIteratorLevel.RIL_TEXTLINE);
        System.out.println();
        System.out.println("lines :" + lines);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    List<Rectangle> listSent = new ArrayList<Rectangle>();
    for (Word line : lines) {
      String text = line.getText().trim();
      if (!text.isEmpty()) {
        Rectangle rect = line.getBoundingBox();
        listSent.add(rect);
      }
      Mat img = i2t.draw(listSent, matrix, color);

      saveImage(img, parentDir + File.separator + "sent_annotated.jpg");
    }

    List<Word> words = new ArrayList<Word>();
    words = i2t.findText(result, TessPageIteratorLevel.RIL_WORD);
    System.out.println();
    // System.out.println("words :" + words);
    System.out.println("words ");

    for (Word w : words) {
      System.out.println(w);
    }
    JSONArray respo = new JSONArray();
    for (Word line : lines) {

      Rectangle rectSent = line.getBoundingBox();
      for (Word eachWord : words) {

        JSONObject wordsItems = new JSONObject();
        JSONObject wordsJson = new JSONObject();
        Rectangle rectWord = eachWord.getBoundingBox();

        double aoi = checkAOI(rectSent, rectWord);

        if (aoi >= 0.5) {
          rectWord.height = Math.max(rectWord.height, rectSent.height);
          rectWord.y = rectSent.y;
          wordsItems.put("conf", eachWord.getConfidence());
          wordsItems.put("x", eachWord.getBoundingBox().x);
          wordsItems.put("y", eachWord.getBoundingBox().y);
          wordsItems.put("height", eachWord.getBoundingBox().height);
          wordsItems.put("width", eachWord.getBoundingBox().width);

          wordsJson.put(eachWord.getText(), wordsItems);
          respo.put(wordsJson);
        }
      }
    }
    List<Rectangle> listWord = new ArrayList<Rectangle>();
    for (Word word : words) {
      String text = word.getText().trim();
      if (!text.isEmpty()) {

        java.awt.Rectangle rect = word.getBoundingBox();
        listWord.add(rect);
      }
      Mat img = i2t.draw(listWord, matrix, new Scalar(255, 255, 0));
      saveImage(img, parentDir + File.separator + "word-annotated.jpg");
    }

    System.out.println();
    System.out.println("saving detected coord to " + extStrip + ".json");
    System.out.println();
    System.out.println("Completed finding ocr for given image");

    for (Object jj : respo) {
      System.out.println(jj.toString());
    }

    try {
      FileWriter output = new FileWriter(extStrip + ".json");

      output.write(respo.toString());

      // Closes the writer
      output.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return words;
  }

}
