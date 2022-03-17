package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.rl.qlearning.aimdroid.ActivityInsulatedMultiLevelExploration;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEAimDroid {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting AimDroid...");
        MATE mate = new MATE();

        ActivityInsulatedMultiLevelExploration activityInsulatedMultiLevelExploration
                = new ActivityInsulatedMultiLevelExploration(false, Properties.MAX_NUMBER_EVENTS());
        mate.testApp(activityInsulatedMultiLevelExploration);
    }

}
