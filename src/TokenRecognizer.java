/**
 * Created by Saif on 25-03-15.
 */

import javafx.geometry.Pos;
import sun.plugin.dom.html.ns4.NS4DOMObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.*;


public class TokenRecognizer {

    public static final Integer MAX = 100;
    public static final Character Epsilon = 'e';
    public static boolean[][] isFinalState = new boolean[MAX][MAX];
    public static Integer[][][] DFA = new Integer[MAX][MAX][MAX];
    public static Integer[] NState = new Integer[MAX];
    public static boolean ErrorDetected = false;


    private static void DesignDFA(Integer nRegEx, String Postfix){


        Boolean[] Nullable = new Boolean[MAX];
        Set<Integer>[] FirstPos = new Set[MAX];
        Set<Integer>[] LastPos = new Set[MAX];
        Set<Integer>[] FollowPos = new Set[MAX];

        Map<Character, Integer> map = new HashMap<Character, Integer>(); //MAP FOR POSITIONING THE TERMINALS

        List<Character> allOperators = Arrays.asList('|', '?', '+', '*', '.');
        List<Character> unaryOperators = Arrays.asList('*', '?', '+');
        List<Character> binaryOperators = Arrays.asList('|', '.');

        int iC1=0, iC2=0, iC=0;

        Stack < Integer > stack = new  Stack();

        for(int i=0;i<Postfix.length();i++) {

            Character c = Postfix.charAt(i);

            /** Calculationg c1 and c2 */

            if (!allOperators.contains(c))
                stack.push(i);
            else {
                if (unaryOperators.contains(c))
                    iC = stack.pop();
                else if (binaryOperators.contains(c)) {
                    iC2 = stack.pop();
                    iC1 = stack.pop();
                }
                stack.push(i);
            }
            /**  Nullable Calulation */
            if (c == Epsilon) Nullable[i] = true;
            else if (c == '*') Nullable[i] = true;
            else if (c == '+') Nullable[i] = Nullable[iC];
            else if (c == '?') Nullable[i] = true;
            else if (!allOperators.contains(c)) Nullable[i] = false;
            else if (c == '|') Nullable[i] = (Nullable[iC1] || Nullable[iC2]);
            else if (c == '.') Nullable[i] = Nullable[iC1] && Nullable[iC2];

            /** FirstPos and LastPos Calculation */

            FirstPos[i] = new HashSet<Integer>();
            LastPos[i] = new HashSet<Integer>();
            FollowPos[i] = new HashSet<Integer>();

            if (!allOperators.contains(c)) {
                FirstPos[i].add(i);
                LastPos[i].add(i);
            } else if (c == '|') {
                FirstPos[i].addAll(FirstPos[iC1]);
                FirstPos[i].addAll(FirstPos[iC2]);

                LastPos[i].addAll(LastPos[iC2]);
                LastPos[i].addAll(LastPos[iC1]);

            } else if (c == '.') {

                if (Nullable[iC1]) {
                    FirstPos[i].addAll(FirstPos[iC2]);
                    FirstPos[i].addAll((FirstPos[iC1]));
                } else
                    FirstPos[i].addAll(FirstPos[iC1]);

                if (Nullable[iC2]) {
                    LastPos[i].addAll(LastPos[iC2]);
                    LastPos[i].addAll(LastPos[iC1]);
                } else
                    LastPos[i].addAll(LastPos[iC2]);
            } else if (c == '*' || c == '+' || c == '?') {
                FirstPos[i] = FirstPos[iC];
                LastPos[i] = LastPos[iC];
            }

            /** FollowPos Calculation */

            if (c == '.') {

                for (int j : LastPos[iC1]) {
                    //System.out.println(". " + j);
                    FollowPos[j].addAll(FirstPos[iC2]);
                }
            }
            else if (c == '*' || c == '+') {

                for (int j : LastPos[i]) {
                    //System.out.println("* "+j);
                    FollowPos[j].addAll(FirstPos[i]);
                }

            }

        }

/*
        for(int i=0; i<Postfix.length();i++){
            System.out.print(Postfix.charAt(i) + " : ");
            for(int j: FollowPos[i]){
                System.out.print(j + " ");
            }
            System.out.println("");
        }
*/
        /** Making DFA from FollowPos */
        char[] Letters = new char[2];
        Letters[0] = 'a'; Letters[1] = 'b';

        Map <Set<Integer>, Integer> MapOfSetToInt = new HashMap< Set<Integer>, Integer>();
        Map <Integer, Set<Integer>> MapOfIntToSet = new HashMap< Integer, Set<Integer>>();
        Queue<Integer> queue = new LinkedList<Integer>();



        NState[nRegEx] = 1;
        MapOfSetToInt.put(FirstPos[Postfix.length() - 1], NState[nRegEx]);
        MapOfIntToSet.put(NState[nRegEx], FirstPos[Postfix.length() - 1]);
        queue.add(NState[nRegEx]);
        NState[nRegEx]++;

        while(!queue.isEmpty()){
            Set <Integer> set = new HashSet<>();
            Integer Top = queue.remove();
            set.addAll(MapOfIntToSet.get(Top));
            //System.out.print(set + " ");


            for(int i=0; i<2; i++){

                Set <Integer> TranSet= new HashSet<>();
                for(int j : set){
                    if( Letters[i] == Postfix.charAt(j)) {
                        TranSet.addAll(FollowPos[j]);
                    }
                }
                if(TranSet.isEmpty()) continue;

                if(MapOfSetToInt.get(TranSet)==null){

                    DFA[nRegEx][MapOfSetToInt.get(set)][i] = NState[nRegEx];
                    MapOfSetToInt.put(TranSet,NState[nRegEx]);
                    MapOfIntToSet.put(NState[nRegEx],TranSet);
                    queue.add(NState[nRegEx]);
                    NState[nRegEx]++;
                }
                else{

                    DFA[nRegEx][MapOfSetToInt.get(set)][i] = MapOfSetToInt.get(TranSet);
                }

            }


            for(int i: set){
                if(i == Postfix.length()-2) isFinalState[nRegEx][Top] = true;
            }

        }



        /** Printing DFA table*/
/*
        for(int i = 1; i<NState[nRegEx];i++) {
            System.out.println(i + ": " + DFA[nRegEx][i][0] + " " + DFA[nRegEx][i][1]);
        }

        for(int i = 1; i<NState[nRegEx];i++) {
            if(isFinalState[nRegEx][i]==true)
                System.out.println("Final: " + i);
        }
*/


    }

    public static String  isAcceped(int nRegEx, String string){
        int currentState = 1;

        String PossibleToken = new String();
        String LargestPossibleToken = new  String();


        //System.out.println(string);
        for(int i=0; i<string.length();i++){

            //System.out.println(currentState);
            PossibleToken+=string.charAt(i);
            if(string.charAt(i)=='a') {
                if(DFA[nRegEx][currentState][0]==null ){
                    break;
                }
                currentState = DFA[nRegEx][currentState][0];

            }

            else if(string.charAt(i)=='b') {
                if(DFA[nRegEx][currentState][1]==null ){
                    break;
                }
                currentState = DFA[nRegEx][currentState][1];
            }
            else {
                ErrorDetected = true;
                return LargestPossibleToken;
            }
            if(isFinalState[nRegEx][currentState]==true) LargestPossibleToken = PossibleToken;

        }

       // System.out.println(LargestPossibleToken);
        return LargestPossibleToken;
    }


    public static void main(String[] args){


        String[] RegEx = new String[MAX];
        String[] string = new String[MAX];

        int nRegEx=0;


            FileReader FR;
            BufferedReader BR;
        try {
                FR = new FileReader("H:\\Intellij workspace\\src\\input.l");
                BR = new BufferedReader(FR);


            while( (RegEx[nRegEx] = BR.readLine())!=null ){
                nRegEx++;
            }
            BR.close();
            FR.close();
        }catch(Exception e) {System.out.println(e);}



        RegExConverter Postfix = new RegExConverter();
        for( int i=0; i<nRegEx; i++) {

            String str = Postfix.infixToPostfix(RegEx[i]);
            str = str.replace("?", "e|");
            str += "$.";
            System.out.println("Postfix of Token " + (i+1) + ": " + RegEx[i] + " is " + str);
            DesignDFA(i, str);
        }

        System.out.println("Now Give Input String: ");
        int  nString=1;
        Scanner scanIn = new Scanner(System.in);
        while(scanIn.hasNext()) {
                    string[0] = scanIn.nextLine();
                    int NotMatched = 0;
                    for (int i = 0; i < nString; i++) {

                        int start = 0;

                        while(start < string[i].length()) {
                            //System.out.println("hete");
                            int fin = 0;
                            int FinalMax = -1;
                            String LargestStr = "";
                            String LargestMatch = new String();
                            int TokenNum = 0;
                            //System.out.print(start + " ");
                            for (int k = 0; k < nRegEx; k++) {

                                String Temp = isAcceped(k, string[i].substring(start));
                               // System.out.println(k + " " + start + " " + Temp);

                                if(ErrorDetected==true) {
                                    System.out.println("An Error Detected: " + string[i].substring(start) + " is Unrecognized!");
                                    start++;
                                }
                                if (LargestMatch.length() < Temp.length()) LargestMatch = Temp;

                                if(FinalMax<LargestMatch.length()) {
                                    FinalMax = LargestMatch.length();
                                    fin = start+FinalMax;
                                    LargestStr = Temp;
                                    TokenNum = k;

                                }
                            }
                            if(ErrorDetected==true){
                                ErrorDetected = false;
                                break;
                            }
                            if(!LargestStr.equals(""))
                            System.out.println(LargestStr + " Token "  + (TokenNum+1));
                            start = fin;
                        }

                    }
        }
        scanIn.close();

    }

}
