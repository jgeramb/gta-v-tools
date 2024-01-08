package dev.justix.gtavtools.tools.cayoperico;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.OCRUtil;
import dev.justix.gtavtools.util.SystemUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VaultCode extends Tool {

    private static final Pattern CODE_PATTERN = Pattern.compile("[0-9]{2}-[0-9]{2}-[0-9]{2}");

    private boolean cancel;
    private int[] vaultCode;

    public VaultCode(Logger logger) {
        super(logger, Category.CAYO_PERICO, "Vault Code");

        relativeData.addRect("1920x1200", "code", 33, 891, 291, 49);

        relativeData.addRect("1920x1080", "code", 34, 801, 261, 43);

        this.cancel = false;
        this.vaultCode = null;
    }

    @Override
    public void execute() {
        if (this.vaultCode == null) {
            String ocr = OCRUtil.ocr(SystemUtil.screenshot(relativeData.getRect("code")), false);
            Matcher matcher = CODE_PATTERN.matcher(ocr);

            if(!matcher.find()) {
                logger.log(Level.INFO, "Could not read code");
                return;
            }

            String code = matcher.group();

            logger.log(Level.INFO, "Code: " + code);

            String[] codeParts = code.split("-");

            try {
                this.vaultCode = new int[] { Integer.parseInt(codeParts[0]), Integer.parseInt(codeParts[1]), Integer.parseInt(codeParts[2]) };
            } catch (NumberFormatException ignore) {
            }
        } else {
            for (int i = 0; i < 3; i++) {
                int codePart = this.vaultCode[i];
                boolean up = codePart <= 50;

                for (int j = 0; j < (up ? codePart : (100 - codePart)); j++) {
                    if (this.cancel) return;

                    SystemUtil.keyPress(up ? "W" : "S", 4);
                    SystemUtil.sleep(8);
                }

                SystemUtil.sleep(25);
                SystemUtil.mouseClick("LEFT", 25);
                SystemUtil.sleep(75);
            }

            logger.log(Level.INFO, "Vault unlocked");

            this.vaultCode = null;
        }
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

}
