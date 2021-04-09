package com.muhlenberg.bot;

import com.muhlenberg.models.*;

public class HelperSource {

    public boolean isPos(double context) {
        if (context > 0 ) {
            return true;
        }
        else { return false;}
    }
    public boolean isNeg(double context) {
        if (context < 0 ) {
            return true;
        }
        else { return false;}
    }

    
    // Other helper methods
}