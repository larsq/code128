package larsq.barcode;

import com.google.common.collect.ImmutableMap;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Dimension;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.MissingResourceException;

import static java.util.Objects.requireNonNull;

/**
 * Test support class. Takes an encoded string, generates a barcode using LibreBarcode128 TTF-font,
 * save it as an image and then makes an attempt to read the barcode and return the result.
 * <p>
 * This is made to make better test for the code-128 encoder
 */
public class TextDecoderSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextDecoderSupport.class);
    private static final String FONT_ASSET = "/LibreBarcode128-Regular.ttf";
    private final Font font;
    private final DOMImplementation implementation = GenericDOMImplementation.getDOMImplementation();
    private final PNGTranscoder transcoder = new PNGTranscoder();

    TextDecoderSupport(float fontSize) throws IOException, FontFormatException {
        font = Font.createFont(Font.TRUETYPE_FONT, requireNonNull(readFontAsset())).deriveFont(fontSize);
    }

    private static InputStream readFontAsset() {
        InputStream in = TextDecoderSupport.class.getResourceAsStream(FONT_ASSET);

        if (in == null) {
            LOGGER.error("Could not load font at {}. The font must be downloaded and installed separately", FONT_ASSET);
            throw new UncheckedIOException(new FileNotFoundException(FONT_ASSET));
        }

        return TextDecoderSupport.class.getResourceAsStream(FONT_ASSET);
    }

    public BarcodeResult readBarcode(FileInputStream input) throws IOException, NotFoundException {
        BufferedImage image = ImageIO.read(input);
        BufferedImageLuminanceSource bufferedImageLuminanceSource = new BufferedImageLuminanceSource(image);

        MultiFormatReader reader = new MultiFormatReader();
        Result result = reader.decode(new BinaryBitmap(new HybridBinarizer(bufferedImageLuminanceSource)));

        return new BarcodeResult(result.getBarcodeFormat().name(), result.getText());
    }

    public void transcodeToPng(Dimension dimension, InputStream svgFile, OutputStream pngFile) throws TranscoderException {
        TranscoderInput in = new TranscoderInput(svgFile);
        TranscoderOutput out = new TranscoderOutput(pngFile);

        transcoder.setTranscodingHints(ImmutableMap.of(SVGAbstractTranscoder.KEY_HEIGHT, (float) dimension.height, SVGAbstractTranscoder.KEY_WIDTH, (float) dimension.width));
        transcoder.transcode(in, out);
    }

    public Dimension renderBarcode(String text, OutputStreamWriter out) throws SVGGraphics2DIOException {
        Graphics2D g = null;

        try {
            Document document = implementation.createDocument("http://www.w3.org/2000/svg", "svg", null);

            SVGGraphics2D svg = new SVGGraphics2D(document);
            GlyphVector glyphVector = font.createGlyphVector(svg.getFontRenderContext(), text);
            g = (Graphics2D) svg.create();

            g.drawLine(1, 1, 1, 1);
            g.setStroke(new BasicStroke(1));
            g.setPaint(Color.BLACK);
            g.setBackground(Color.WHITE);

            int w = (int) Math.ceil(glyphVector.getVisualBounds().getWidth());
            int h = (int) Math.ceil(glyphVector.getVisualBounds().getHeight());

            g.clearRect(0, 0, w, h);
            g.drawGlyphVector(glyphVector, 0, (float) glyphVector.getVisualBounds().getHeight());

            svg.stream(out, true);

            return new Dimension(w, h);
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }


    public static void main(String[] args) {
        try {
            TextDecoderSupport svg = new TextDecoderSupport(80f);
            BarcodeResult barcodeResult = svg.extracted("ÌHello World!WÎ");

            System.out.println(barcodeResult);
        } catch (IOException | FontFormatException | NotFoundException | TranscoderException e) {
            e.printStackTrace();
        }
    }

    public BarcodeResult extracted(String text) throws IOException, FontFormatException, TranscoderException, NotFoundException {
        File svgFileName = Files.createTempFile("svg-", ".svg").toFile();
        File pngFileName = Files.createTempFile("png-", ".png").toFile();

        Dimension dimension = null;

        try (FileOutputStream out = new FileOutputStream(svgFileName)) {
            dimension = renderBarcode(text, new OutputStreamWriter(out));
        }

        try (FileInputStream in = new FileInputStream(svgFileName); FileOutputStream out = new FileOutputStream(pngFileName)) {
            transcodeToPng(dimension, in, out);
        }

        try (FileInputStream in = new FileInputStream(pngFileName)) {
            return readBarcode(in);
        }
    }

    static class BarcodeResult {
        public final String format;
        public final String decodedText;

        BarcodeResult(String format, String decodedText) {
            this.format = format;
            this.decodedText = decodedText;
        }

        @Override
        public String toString() {
            return "BarcodeResult{" +
                    "encoding='" + format + '\'' +
                    ", decodedText='" + decodedText + '\'' +
                    '}';
        }
    }
}
