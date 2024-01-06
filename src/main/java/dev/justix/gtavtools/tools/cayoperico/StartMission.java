package dev.justix.gtavtools.tools.cayoperico;

import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;

import static dev.justix.gtavtools.util.SystemUtil.keyPress;
import static dev.justix.gtavtools.util.SystemUtil.sleep;

public class StartMission extends Tool {

    private boolean waitingForOtherPlayers, waitingForCutDefinition;

    public StartMission(Logger logger) {
        super(logger, Category.CAYO_PERICO, "Start Mission");

        this.waitingForOtherPlayers = false;
        this.waitingForCutDefinition = false;
    }

    @Override
    public void execute() {
        if (!(this.waitingForOtherPlayers || this.waitingForCutDefinition)) {
            InterfaceNavigationUtil.openPlaningScreen();

            // Start mission lobby
            for (int i = 0; i < 2; i++) {
                keyPress("E", 10);
                sleep(50);
            }

            keyPress("RIGHT", 10);
            sleep(15);

            keyPress("ENTER", 10);
            sleep(15);

            this.waitingForOtherPlayers = true;
        } else if (this.waitingForOtherPlayers) {
            InterfaceNavigationUtil.startMission();

            // Set mission settings

            /* Vehicle: Kosatka */
            for (int i = 0; i < 2; i++) {
                keyPress("ENTER", 10);
                sleep(15);
            }

            goToNextOption();

            /* Infiltration point: Drainage Tunnel */
            keyPress("UP", 10);
            sleep(15);

            keyPress("ENTER", 10);
            sleep(15);

            goToNextOption();

            /* Entry point to compound: Drainage Tunnel */
            keyPress("ENTER", 10);
            sleep(15);

            goToNextOption();

            /* Infiltration point: Drainage Tunnel */
            keyPress("UP", 10);
            sleep(15);

            keyPress("ENTER", 10);
            sleep(15);

            goToNextOption();

            /* Time: Day */
            keyPress("ENTER", 10);
            sleep(15);

            goToNextOption();

            /* Weapon arsenal: Aggressor */
            keyPress("DOWN", 10);
            sleep(15);

            for (int i = 0; i < 2; i++) {
                keyPress("ENTER", 10);
                sleep(15);
            }

            keyPress("ESCAPE", 10);
            sleep(75);

            // Go to cut definition
            for (int i = 0; i < 2; i++) {
                keyPress("DOWN", 10);
                sleep(15);
            }

            keyPress("ENTER", 10);
            sleep(15);

            this.waitingForOtherPlayers = false;
            this.waitingForCutDefinition = true;
        } else {
            keyPress("ESCAPE", 10);
            sleep(75);

            keyPress("RIGHT", 10);
            sleep(15);

            for (int i = 0; i < 2; i++) {
                keyPress("ENTER", 10);
                sleep(500);
            }

            this.waitingForCutDefinition = false;
        }
    }

    @Override
    public void forceStop() {
        this.waitingForOtherPlayers = false;
        this.waitingForCutDefinition = false;
    }

    private void goToNextOption() {
        keyPress("ESCAPE", 10);
        sleep(75);

        keyPress("DOWN", 10);
        sleep(15);

        keyPress("ENTER", 10);
        sleep(75);
    }

}
