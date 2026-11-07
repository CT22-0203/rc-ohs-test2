package com.rocs.rc.ohs;

import java.util.*;

public class DemoViolations {
    public void test (String a) {
        int unused = 5;
        try {
            Integer.parseInt(a);
        } catch (Exception e) {
        }
    }
}