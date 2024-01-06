package dev.justix.gtavtools.tools.casino;

import dev.justix.gtavtools.gui.components.settings.BooleanSetting;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;

import static dev.justix.gtavtools.util.SystemUtil.keyPress;
import static dev.justix.gtavtools.util.SystemUtil.sleep;

public class StartMission extends Tool {

    private boolean waitingForOtherPlayers, waitingForCutDefinition;

    public StartMission(Logger logger) {
        super(logger, Category.CASINO, "Start Mission");

        addSetting(new BooleanSetting(this, "Silent & Sneaky", true));
        addSetting(new BooleanSetting(this, "The Big Con", false));

        this.waitingForOtherPlayers = false;
        this.waitingForCutDefinition = false;
    }

    @Override
    public void execute() {
        if (!(this.waitingForOtherPlayers || this.waitingForCutDefinition)) {
            InterfaceNavigationUtil.openPlaningScreen();

            keyPress("ENTER", 15);
            sleep(15);

            this.waitingForOtherPlayers = true;
        } else if (this.waitingForOtherPlayers) {
            InterfaceNavigationUtil.startMission();

            // Set mission settings

            boolean silentAndSneaky = booleanValue("Silent & Sneaky", true),
                    theBigCon = booleanValue("The Big Con", false);

            // Entry point
            keyPress("ENTER", 10);
            sleep(75);

            if(silentAndSneaky) {
                /* Staff lobby */
                keyPress("RIGHT", 10);
                sleep(15);
            } else if(booleanValue("The Big Con", false)) {
                /* Security tunnel */
                for (int i = 0; i < 4; i++) {
                    keyPress("LEFT", 10);
                    sleep(15);
                }
            } else if(booleanValue("Aggressive", false)) {
                /* Sewers */
                for (int i = 0; i < 2; i++) {
                    keyPress("LEFT", 10);
                    sleep(15);
                }
            }

            goToNextOption();

            // Exit point

            if(silentAndSneaky || theBigCon) {
                /* Staff lobby */
                keyPress("RIGHT", 10);
                sleep(15);
            } else {
                /* Rooftop (north-west) */
                for (int i = 0; i < 4; i++) {
                    keyPress("RIGHT", 10);
                    sleep(15);
                }
            }

            goToNextOption();

            /* Buyer: Far */
            keyPress("LEFT", 10);
            sleep(15);

            keyPress("ENTER", 10);
            sleep(75);

            // Navigate to cut definition
            keyPress("RIGHT", 10);
            sleep(15);

            keyPress("UP", 10);
            sleep(15);

            this.waitingForOtherPlayers = false;
            this.waitingForCutDefinition = true;
        } else {
            keyPress("DOWN", 10);
            sleep(15);

            for (int i = 0; i < 2; i++) {
                keyPress("ENTER", 10);
                sleep(500);
            }

            this.waitingForCutDefinition = false;
        }
    }

    private void goToNextOption() {
        keyPress("ENTER", 10);
        sleep(75);

        keyPress("DOWN", 10);
        sleep(15);

        keyPress("ENTER", 10);
        sleep(75);
    }

}
