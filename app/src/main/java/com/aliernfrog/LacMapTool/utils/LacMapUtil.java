package com.aliernfrog.LacMapTool.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class LacMapUtil {
    public static String[] fixMap(String[] content) {
        String[] updatedContent = new String[0];
        for (int i = 0; i < content.length; i++) {
            String line = content[i];
            String[] strArr = line.split(":");
            ArrayList<String> arr = new ArrayList<>(Arrays.asList(strArr));
            if (line.contains("Editor")) {
                switch (arr.get(0)) {
                    case "Trigger_Box_Editor":
                        arr.add("3.0,3.0,3.0");
                        break;
                    case "Panorama_Object_Editor":
                        arr.add("50.0,50.0,50.0");
                        break;
                    case "Container_Open_Editor":
                        arr.add("1.1,1.1,1.0");
                        break;
                    case "Metal_Railing_Editor":
                        arr.add("0.4,0.4,0.4");
                        break;
                    case "Platform_Green_Editor":
                        arr.add("50.0,1.0,50.0");
                        break;
                    case "Road_Guard_Editor":
                    case "Stair_Editor":
                        arr.add("1.2,1.2,1.2");
                        break;
                    case "Soccer_Ball_Editor":
                        arr.add("100.0,100.0,100.0");
                        break;
                    case "Soccer_Map_Editor":
                        arr.add("1.3,1.3,1.3");
                        break;
                    case "Square_Plank_Editor":
                        arr.add("0.7,0.7,0.7");
                        break;
                    case "StreetLight_Editor":
                        arr.add("100.1,100.1,100.1");
                        break;
                    case "Tree_Desert_Editor":
                    case "Tree_Spruce_Editor":
                        arr.add("0.5,0.5,0.5");
                        break;
                    case "Tube_Racing_Curved_Editor":
                    case "Tube_Racing_Editor":
                        arr.add("2.0,2.0,2.0");
                        break;
                    default:
                        arr.add("1.0,1.0,1.0");
                }
            }
            if (line.contains("Editor")) {
                if (line.contains("Block_1by1_Editor")) {
                    arr.set(0, "Block_Scalable_Editor");
                }
                if (line.contains("Block_3by6_Editor")) {
                    arr.set(0, "Block_Scalable_Editor");
                    arr.set(3, "3.0,6.0,1.0");
                }
                if (line.contains("Sofa_Chunk_Red_Editor")) {
                    arr.set(0, "Sofa_Chunk_Editor");
                    arr.add("color{1.00,0.00,0.00}");
                }
                String full = "";
                for (int a = 0; a < arr.size(); a++) {
                    String str = arr.get(a);
                    full = full+str+":";
                }
                updatedContent[i] = full;
            }
        }
        return updatedContent;
    }
}
