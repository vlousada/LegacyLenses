package com.vlousada.legacylenses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Legacy {
    private static final int DEFAULT_FLENGTH_STEP = 1;
    private static final int DEFAULT_FSTOP_STEP = 1;
    private static final char RANGE_SPLIT = '-';
    private static final String ELEMENT_SPLIT = ",";
    private static final char STEP_SPLIT = '/';

    private static String[] FULL_F_STOPS =  {"0.5","0.7","1.0","1.4","2.0","2.8","4.0","5.6","8.0","11","16","22","32","45","64"};
    private static String[] HALF_F_STOPS =  {"0.5","0.6","0.7","0.8","1.0","1.2","1.4","1.7","2.0","2.4","2.8","3.3","4.0","4.8","5.6","6.7","8.0","9.5","11","13","16","19","22","27","32","38","45","54","64"};
    private static String[] THIRD_F_STOPS = {"0.5","0.6","0.7","0.8","0.9","1.0","1.1","1.3","1.4","1.6","1.8","2.0","2.2","2.5","2.8","3.2","3.5","4.0","4.5","5.0","5.6","6.3","7.1","8.0","9.0","10","11","13","14","16","18","20","22","25","29","32","36","40","45","51","57","64"};

    String name;
    HashSet<Integer> focalLengths = new HashSet<Integer>();
    ArrayList<Integer> focalLengthsList = new ArrayList<Integer>();
    HashSet<Integer> apertures = new HashSet<Integer>();   //f-stops multiplied by 10 and round to force INTEGERS
    ArrayList<Integer> aperturesList = new ArrayList<Integer>();

    public boolean isValid() {
        return valid;
    }

    public String getErrorReason() {
        return errorReason;
    }

    boolean valid = true;
    String errorReason = null;


    public static Legacy  getLensFromProfile (String lensName, String lensFocal, String lensApertures) {
        Legacy lens = new Legacy();

        //process lens focal and apertures
        lens.name=lensName;
        lens.parseFocalLengths(lensFocal);
        lens.parseApertures(lensApertures);

        return lens;
    }



    private void parseFocalLengths(String focalLengthList) {
        String[] elements = focalLengthList.split(ELEMENT_SPLIT);
        for (String element:elements) {
            if (element.indexOf(RANGE_SPLIT)>0) {
                if (!parseFocalLengthRange(element)) {
                    // Failed parsing. So abort.
                    valid=false;
                    return;
                }
            } else { // Not a range. So need to be a single element
                try {
                    focalLengths.add(Integer.parseInt(element)) ;
                } catch (Exception e) {
                    valid=false;
                    errorReason="Cannot convert to single entry: "+element;
                    return;
                }
            }
        }
        if (valid) { //Now lets order the results
            focalLengthsList.addAll(focalLengths);
            Collections.sort(focalLengthsList);
        }
    }


    private boolean parseFocalLengthRange(String range) {
        int step=DEFAULT_FLENGTH_STEP;

        String rangeTmp = range;
        int stepPosi = range.indexOf(STEP_SPLIT);
        if (stepPosi>=0) {
            rangeTmp=range.substring(0,stepPosi);
            try {
                step = Integer.parseInt(range.substring(stepPosi+1));
                if (step<=0) {
                    errorReason="Not a positive integer step: "+range;
                    return false;
                }
            } catch (Exception e) {
                errorReason="Cannot convert Step section to Number: "+range;
                return false;
            }
        }

        int start;
        int end;
        try {
            start=Integer.parseInt(rangeTmp.substring(0,rangeTmp.indexOf(RANGE_SPLIT)));
        } catch (Exception e) {
            errorReason="Cannot convert initial part of Range to Number: "+range;
            return false;
        }
        try {
            end=Integer.parseInt(rangeTmp.substring(rangeTmp.indexOf(RANGE_SPLIT)+1));
        } catch (Exception e) {
            errorReason="Cannot convert initial part of Range to Number: "+range;
            return false;
        }
        if (start<0 || end <= start) {
            errorReason="Invalid range: "+range;
            return false;
        }
        if ((end-start) / step >500) {
            errorReason="Too many Focal Length steps: "+(end-start) / step;
            return false;
        }
        int i;
        for (i= start;i<=end;i+=step) {
            focalLengths.add(i);
        }
        if (i!=end) {
            focalLengths.add(end);
        }
        return true;
    }


    private void parseApertures(String apertureList) {
        String friendlyList = apertureList.replaceAll("[^,\\d./-]+", ""); //friendly format
        String[] elements = friendlyList.split(ELEMENT_SPLIT);
        for (String element:elements) {
            if (element.indexOf(RANGE_SPLIT)>0) {
                if (!parseAperturesRange(element)) {
                    // Failed parsing. So abort.
                    valid=false;
                    return;
                }
            } else { // Not a range. So need to be a single element
                try {
                    apertures.add((int) Math.round(10 * Double.parseDouble(element))) ; // Multiplied by 10
                } catch (Exception e) {
                    valid=false;
                    errorReason="Cannot convert to single entry: "+element;
                    return;
                }
            }
        }
        if (valid) { //Now lets order the results
            aperturesList.addAll(apertures);
            Collections.sort(aperturesList);
        }
    }


    private boolean parseAperturesRange(String range) {
        int step=DEFAULT_FSTOP_STEP;

        String rangeTmp = range;
        int stepPosi = range.indexOf(STEP_SPLIT);
        if (stepPosi>=0) {
            rangeTmp=range.substring(0,stepPosi);
            try {
                step = Integer.parseInt(range.substring(stepPosi+1));
                if ((step<0) && (step>3)) {
                    errorReason="Invalid step in: "+range;
                    return false;
                }
            } catch (Exception e) {
                errorReason="Cannot convert Step section to Number: "+range;
                return false;
            }
        }
        int start = 5;
        int end = 640;
        try {
            start = (int) Math.round(10 * Double.parseDouble(rangeTmp.substring(0,rangeTmp.indexOf(RANGE_SPLIT)))); // Multiplied by 10
        } catch (Exception e) {
            errorReason="Cannot convert initial part of Range to Number: "+range;
            return false;
        }
        try {
            end=(int) Math.round(10 * Double.parseDouble((rangeTmp.substring(rangeTmp.indexOf(RANGE_SPLIT)+1)))); // Multiplied by 10
        } catch (Exception e) {
            errorReason="Cannot convert initial part of Range to Number: "+range;
            return false;
        }
        if (start<5 || end >640 || end <= start) {  // max Aperture = F0.5 e min Aperture = F64
            errorReason="Invalid range: "+range;
            return false;
        }

        apertures.add(start);
        apertures.add(end);

        int i;
        // Create an ArrayList of STOPS according to the FSTOP_STEP
        if (step==1) {//full f-stops
            for (String item:FULL_F_STOPS) {
                int tmpItem = (int) Math.round(10 * Double.parseDouble(item));
                if ((tmpItem >= start) && (tmpItem <= end)) {
                    apertures.add(tmpItem);
                }
            }
        }
        if (step==2) {//half f-stops
            for (String item:HALF_F_STOPS) {
                int tmpItem = (int) Math.round(10 * Double.parseDouble(item));
                if ((tmpItem >= start) && (tmpItem <= end)) {
                    apertures.add(tmpItem);
                }
            }
        }
        if (step==3) {  //Third f-stops
            for (String item:THIRD_F_STOPS) {
                int tmpItem = (int) Math.round(10 * Double.parseDouble(item));
                if ((tmpItem >= start) && (tmpItem <= end)) {
                    apertures.add(tmpItem);
                }
            }
        }

        return true;
    }
}
