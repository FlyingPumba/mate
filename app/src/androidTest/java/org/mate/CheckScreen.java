package org.mate;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.Widget;

import java.util.List;


/**
 * Created by marceloeler on 29/06/17.
 */
@RunWith(AndroidJUnit4.class)
public class CheckScreen {

    @Test
    public void useAppContext() throws Exception {

        MATE.log("start checking screen");
        MATE mate = new MATE();
        //run mate for timeout minutes
        mate.testApp("checkScreen");


    }

}
