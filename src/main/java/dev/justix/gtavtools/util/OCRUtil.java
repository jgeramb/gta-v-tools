package dev.justix.gtavtools.util;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;

public class OCRUtil {

    private static final Tesseract tesseract;

    static {
        tesseract = new Tesseract();

        try {
            tesseract.setDatapath(new File(OCRUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath() + "\\classes\\tessdata");
            tesseract.setLanguage("deu");
        } catch (URISyntaxException ex) {
            System.err.println("Could not find Tesseract training data");
        }
    }

    public static String ocr(BufferedImage image) {
        try {
            return tesseract.doOCR(image).strip();
        } catch (TesseractException ignore) {
            return "";
        }
    }

}
