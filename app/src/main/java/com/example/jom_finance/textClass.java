package com.example.jom_finance;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.algorithmia.APIException;
import com.algorithmia.AlgorithmException;
import com.algorithmia.Algorithmia;
import com.algorithmia.AlgorithmiaClient;
import com.algorithmia.algo.AlgoResponse;
import com.algorithmia.algo.Algorithm;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class textClass {
    private static final String TAG = "TEXTCLASS";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String textClass(String inputVoice) throws APIException, AlgorithmException, JSONException {

        String moneyName = "";
        double moneyAmount = 0;
        AlgorithmiaClient client = Algorithmia.client("simM1WqQdPsI8nRUsh1Z+n7kMWU1");
        //String input = "teh ice extra ice for 10 ringgit";
        String input = inputVoice;
        Algorithm algo = client.algo("StanfordNLP/Java2NER/0.1.1");
        algo.setTimeout(300L, java.util.concurrent.TimeUnit.SECONDS); //optional
        AlgoResponse result = algo.pipe(input);

        List<String> stringArray = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(result.asJsonString().split(","));
        for (int i = 0; i < jsonArray.length(); i++) {
            stringArray.add(jsonArray.getString(i).replaceAll("[\\[\\]\"\"]","").trim());
        }
        if(stringArray.contains("MONEY")){
            int moneyIndex = stringArray.lastIndexOf("MONEY");
            if(isNumeric(stringArray.get(moneyIndex-1))){
               moneyAmount = Double.parseDouble(stringArray.get(moneyIndex-1));
            }else if(isNumeric(stringArray.get(moneyIndex-3))){
                moneyAmount = Double.parseDouble(stringArray.get(moneyIndex-3));
            }
            while(stringArray.contains("MONEY")){
                moneyIndex = stringArray.lastIndexOf("MONEY");
                stringArray.remove(moneyIndex-1);
            }

        }else{
            if(stringArray.contains("ringgit") || stringArray.contains("Ringgit")){
                Log.d(TAG,"Contain Ringgit");
                if(stringArray.contains("ringgit")){
                    int moneyIndex = stringArray.lastIndexOf("ringgit");
                    moneyAmount = Double.parseDouble(stringArray.get(moneyIndex-2));
                    int counter = 0;
                    while(counter < 4){
                        stringArray.remove(moneyIndex-counter);
                        counter++;
                    }
                }
                else if(stringArray.contains("Ringgit")){
                    int moneyIndex = stringArray.lastIndexOf("Ringgit");
                    moneyAmount = Double.parseDouble(stringArray.get(moneyIndex-2));
                    int counter = 0;
                    while(counter < 4){
                        stringArray.remove(moneyIndex-counter);
                        counter++;
                    }
                }
            }else{
                return "false";
            }
        }
        if(stringArray.contains("PERSON") || stringArray.contains("ORGANIZATION")){
            if(stringArray.contains("PERSON")){
                while(stringArray.contains("PERSON")){
                    if(moneyName == ""){
                        moneyName += stringArray.get((stringArray.indexOf("PERSON"))-1);
                    }else {
                        moneyName = moneyName +" " + stringArray.get((stringArray.indexOf("PERSON"))-1);
                    }
                    stringArray.remove(stringArray.indexOf("PERSON")-1);
                    stringArray.remove(stringArray.indexOf("PERSON"));
                    Log.d(TAG,"moneyName : " + moneyName);
                }

            }else if(stringArray.contains("ORGANIZATION")){
                while(stringArray.contains("ORGANIZATION")){
                    if(moneyName == ""){
                        moneyName += stringArray.get((stringArray.indexOf("ORGANIZATION"))-1);
                    }else {
                        moneyName = moneyName +" " + stringArray.get((stringArray.indexOf("ORGANIZATION"))-1);
                    }
                    stringArray.remove(stringArray.indexOf("ORGANIZATION")-1);
                    stringArray.remove(stringArray.indexOf("ORGANIZATION"));
                    Log.d(TAG,"moneyName : " + moneyName);
                }
            }
        }else {
            if(stringArray.size()!=0){
                int counter = stringArray.size() / 2;
                for(int i=0;i<counter;i++){
                    stringArray.remove(i+1);
                }
                String listString = String.join(" ", stringArray);
                Log.d(TAG,"listString : " + listString);
                algo = client.algo("demo/text_processing_demo/0.1.0");
                algo.setTimeout(300L, java.util.concurrent.TimeUnit.SECONDS); //optional
                result = algo.pipe(listString);

                ArrayList<String> stringArray2 = new ArrayList<String>();
                JSONArray jsonArray2 = new JSONArray(result.asJsonString().split(","));
                for (int i = 0; i < jsonArray2.length(); i++) {
                    stringArray2.add(jsonArray2.getString(i).replaceAll("[\\[\\]]",""));
                }
                String listString2 = String.join(" ", stringArray2);
                moneyName = listString2;
            }
        }
        return moneyName +"," + moneyAmount;
    }

    public static boolean isNumeric(String string) {
        int intValue;

        try {
            intValue = Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Input String cannot be parsed to Integer.");
        }
        return false;
    }
}
