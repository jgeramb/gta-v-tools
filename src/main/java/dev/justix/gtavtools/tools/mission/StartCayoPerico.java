package dev.justix.gtavtools.tools.mission;

import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.OCRUtil;
import dev.justix.gtavtools.util.SystemUtil;

public class StartCayoPerico extends Tool {

    private boolean waitingForOtherPlayers, waitingForCutDefinition;

    public StartCayoPerico(Logger logger) {
        super(logger, Category.MISSION, "Start Cayo Perico");

        this.relativeData.addRect("1920x1200", "interaction_menu_ceo", 35, 175, 190, 24);
        this.relativeData.addRect("1920x1080", "interaction_menu_ceo", 35, 160, 175, 22);

        this.waitingForOtherPlayers = false;
        this.waitingForCutDefinition = false;
    }

    @Override
    public void execute() {
        if (!(this.waitingForOtherPlayers || this.waitingForCutDefinition)) {
            // Check if already CEO
            SystemUtil.keyPress("M", 25);
            SystemUtil.sleep(250);

            if (OCRUtil.ocr(SystemUtil.screenshot(this.relativeData.getRect("interaction_menu_ceo"))).equals("SecuroServ-CEO")) {
                SystemUtil.keyPress("ESCAPE", 15);
                SystemUtil.sleep(20);
            } else {
                for (int i = 0; i < 2; i++) {
                    SystemUtil.keyPress("DOWN", 15);
                    SystemUtil.sleep(20);
                }

                for (int i = 0; i < 3; i++) {
                    SystemUtil.keyPress("ENTER", 15);
                    SystemUtil.sleep(20);
                }

                SystemUtil.sleep(750);
            }

            // Open planing screen
            SystemUtil.keyPress("E", 15);
            SystemUtil.sleep(6000);

            // Start mission lobby
            for (int i = 0; i < 2; i++) {
                SystemUtil.keyPress("E", 25);
                SystemUtil.sleep(50);
            }

            SystemUtil.keyPress("RIGHT", 25);
            SystemUtil.sleep(30);

            SystemUtil.keyPress("ENTER", 25);
            SystemUtil.sleep(30);

            this.waitingForOtherPlayers = true;
        } else if (this.waitingForOtherPlayers) {
            // Confirm mission settings
            SystemUtil.keyPress("UP", 15);
            SystemUtil.sleep(20);

            SystemUtil.keyPress("ENTER", 15);
            SystemUtil.sleep(550);

            // Start mission
            SystemUtil.keyPress("UP", 15);
            SystemUtil.sleep(100);

            SystemUtil.keyPress("ENTER", 15);
            SystemUtil.sleep(3_000);

            SystemUtil.keyPress("ENTER", 15);
            SystemUtil.sleep(9_500L);

            // Set mission settings

            /* Vehicle: Kosatka */
            for (int i = 0; i < 2; i++) {
                SystemUtil.keyPress("ENTER", 15);
                SystemUtil.sleep(150);
            }

            SystemUtil.keyPress("ESCAPE", 15);
            SystemUtil.sleep(150);

            SystemUtil.keyPress("DOWN", 15);
            SystemUtil.sleep(20);

            /* Infiltration point: Drainage Tunnel */
            SystemUtil.keyPress("ENTER", 15);
            SystemUtil.sleep(150);

            SystemUtil.keyPress("UP", 15);
            SystemUtil.sleep(20);

            SystemUtil.keyPress("ENTER", 15);
            SystemUtil.sleep(20);

            SystemUtil.keyPress("ESCAPE", 15);
            SystemUtil.sleep(150);

            SystemUtil.keyPress("DOWN", 15);
            SystemUtil.sleep(20);

            /* Entry point to compound: Drainage Tunnel */
            for (int i = 0; i < 2; i++) {
                SystemUtil.keyPress("ENTER", 15);
                SystemUtil.sleep(150);
            }

            SystemUtil.keyPress("ESCAPE", 15);
            SystemUtil.sleep(150);

            SystemUtil.keyPress("DOWN", 15);
            SystemUtil.sleep(20);

            /* Infiltration point: Drainage Tunnel */
            SystemUtil.keyPress("ENTER", 15);
            SystemUtil.sleep(150);

            SystemUtil.keyPress("UP", 15);
            SystemUtil.sleep(20);

            SystemUtil.keyPress("ENTER", 15);
            SystemUtil.sleep(20);

            SystemUtil.keyPress("ESCAPE", 15);
            SystemUtil.sleep(150);

            SystemUtil.keyPress("DOWN", 15);
            SystemUtil.sleep(20);

            /* Time: Day */
            for (int i = 0; i < 2; i++) {
                SystemUtil.keyPress("ENTER", 15);
                SystemUtil.sleep(150);
            }

            SystemUtil.keyPress("ESCAPE", 15);
            SystemUtil.sleep(150);

            SystemUtil.keyPress("DOWN", 15);
            SystemUtil.sleep(20);

            /* Weapon arsenal: Aggressor */
            SystemUtil.keyPress("ENTER", 15);
            SystemUtil.sleep(150);

            SystemUtil.keyPress("DOWN", 15);
            SystemUtil.sleep(20);

            for (int i = 0; i < 2; i++) {
                SystemUtil.keyPress("ENTER", 15);
                SystemUtil.sleep(20);
            }

            SystemUtil.keyPress("ESCAPE", 15);
            SystemUtil.sleep(150);

            // Go to cut definition
            for (int i = 0; i < 2; i++) {
                SystemUtil.keyPress("DOWN", 15);
                SystemUtil.sleep(20);
            }

            SystemUtil.keyPress("ENTER", 15);
            SystemUtil.sleep(20);

            this.waitingForOtherPlayers = false;
            this.waitingForCutDefinition = true;
        } else {
            SystemUtil.keyPress("ESCAPE", 15);
            SystemUtil.sleep(150);

            SystemUtil.keyPress("RIGHT", 15);
            SystemUtil.sleep(20);

            for (int i = 0; i < 2; i++) {
                SystemUtil.keyPress("ENTER", 15);
                SystemUtil.sleep(500);
            }

            this.waitingForCutDefinition = false;
        }
    }

    @Override
    public void forceStop() {
        this.waitingForOtherPlayers = false;
        this.waitingForCutDefinition = false;
    }

}
