package dev.justix.gtavtools.util.ocr;

import dev.justix.gtavtools.util.FileUtil;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;

public class OCRUtil {

    private static Tesseract tesseract = null;

    static {
        final File outputDir = new File("tessdata");

        if(FileUtil.copyFromJar("/tessdata", outputDir.toPath())) {
            tesseract = new Tesseract();
            tesseract.setDatapath(outputDir.getAbsolutePath());
            tesseract.setLanguage("eng");
        } else
            System.err.println("Could not copy Tesseract model");
    }

    public static String ocr(BufferedImage image, Symbols... symbols) {
        if(tesseract == null)
            throw new RuntimeException("Tesseract not initialized");

        final StringBuilder whitelist = new StringBuilder();

        for(Symbols symbol : symbols)
            whitelist.append(symbol);

        tesseract.setVariable("tessedit_char_whitelist", whitelist.toString());

        try {
            return tesseract.doOCR(image).strip();
        } catch (TesseractException ignore) {
            return "";
        }
    }

}
