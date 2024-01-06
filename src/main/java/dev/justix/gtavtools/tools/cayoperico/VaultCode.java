package dev.justix.gtavtools.tools.cayoperico;

import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.OCRUtil;
import dev.justix.gtavtools.util.SystemUtil;

public class VaultCode extends Tool {

    private boolean cancel;
    private int[] vaultCode;

    public VaultCode(Logger logger) {
        super(logger, Category.CAYO_PERICO, "Vault Code");

        this.relativeData.addRect("1920x1200", "code", 181, 917, 81, 22);

        this.relativeData.addRect("1920x1080", "code", 168, 822, 73, 20);

        this.cancel = false;
        this.vaultCode = null;
    }

    @Override
    public void execute() {
        if (this.vaultCode == null) {
            String ocr = OCRUtil.ocr(SystemUtil.screenshot(this.relativeData.getRect("code")), false);

            if (ocr.length() > "00-00-00".length())
                ocr = ocr.substring(0, "00-00-00".length());

            logger.log(Level.INFO, "Scanned code: " + ocr);

            String[] codeParts = ocr.split("-");

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
