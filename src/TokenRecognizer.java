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
    public static final char EPSILON = 'ε';    //defining EPSILON as 'ε'
    public static boolean[][] isFinalState = new boolean[MAX][MAX]; //chechikg for final state
    public static Integer[][][] DFA = new Integer[MAX][MAX][MAX]; //DFA table
    public static Integer[] nState = new Integer[MAX]; //
    public static boolean errorDetected = false; //Error detection for any unknown characters
    public static final char CHARACTERS[] = {   'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                                                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                                                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                                                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                                                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
                                            }; //character set for rules and string

    private static void DesignDFA(Integer nRegEx, String postfix){

    /**
        Arrays and sets for storing nullable, firstPos, lastPos, followPos
    */
        Boolean[] nullable = new Boolean[MAX];
        Set<Integer>[] firstPos = new Set[MAX];
        Set<Integer>[] lastPos = new Set[MAX];
        Set<Integer>[] followPos = new Set[MAX];

        Map<Character, Integer> map = new HashMap<Character, Integer>(); //MAP FOR POSITIONING THE TERMINALS
    /**
        Defining all types of operator that we will use, and distinguish which of them are unary and which them are binary
    */
        List<Character> allOperators = Arrays.asList('|', '?', '+', '*', '.');
        List<Character> unaryOperators = Arrays.asList('*', '?', '+');
        List<Character> binaryOperators = Arrays.asList('|', '.');

        int iC1=0, iC2=0, iC=0;

        Stack < Integer > stack = new  Stack();

        for(int i=0;i<postfix.length();i++) {

            Character c = postfix.charAt(i);

            /** Calculating c1 and c2 */

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
            /**  nullable Calulation */
            if (c == EPSILON) nullable[i] = true;
            else if (c == '*') nullable[i] = true;
            else if (c == '+') nullable[i] = nullable[iC];
            else if (c == '?') nullable[i] = true;
            else if (!allOperators.contains(c)) nullable[i] = false;
            else if (c == '|') nullable[i] = (nullable[iC1] || nullable[iC2]);
            else if (c == '.') nullable[i] = nullable[iC1] && nullable[iC2];

            /** firstPos and lastPos Calculation */

            firstPos[i] = new HashSet<Integer>();
            lastPos[i] = new HashSet<Integer>();
            followPos[i] = new HashSet<Integer>();

            if (!allOperators.contains(c)) {
                firstPos[i].add(i);
                lastPos[i].add(i);
            } else if (c == '|') {
                firstPos[i].addAll(firstPos[iC1]);
                firstPos[i].addAll(firstPos[iC2]);

                lastPos[i].addAll(lastPos[iC2]);
                lastPos[i].addAll(lastPos[iC1]);

            } else if (c == '.') {

                if (nullable[iC1]) {
                    firstPos[i].addAll(firstPos[iC2]);
                    firstPos[i].addAll((firstPos[iC1]));
                } else
                    firstPos[i].addAll(firstPos[iC1]);

                if (nullable[iC2]) {
                    lastPos[i].addAll(lastPos[iC2]);
                    lastPos[i].addAll(lastPos[iC1]);
                } else
                    lastPos[i].addAll(lastPos[iC2]);
            } else if (c == '*' || c == '+' || c == '?') {
                firstPos[i] = firstPos[iC];
                lastPos[i] = lastPos[iC];
            }

            /** followPos Calculation */

            if (c == '.') {

                for (int j : lastPos[iC1]) {
                    //System.out.println(". " + j);
                    followPos[j].addAll(firstPos[iC2]);
                }
            }
            else if (c == '*' || c == '+') {

                for (int j : lastPos[i]) {
                    //System.out.println("* "+j);
                    followPos[j].addAll(firstPos[i]);
                }

            }

        }

        /*
        for(int i=0; i<Postfix.length();i++){
            System.out.print(Postfix.charAt(i) + " : ");
            for(int j: followPos[i]){
                System.out.print(j + " ");
            }
            System.out.println("");
        }
        */
        /** Making DFA from followPos */

        Map <Set<Integer>, Integer> mapOfSetToInt = new HashMap< Set<Integer>, Integer>();
        Map <Integer, Set<Integer>> mapOfIntToSet = new HashMap< Integer, Set<Integer>>();
        Queue<Integer> queue = new LinkedList<Integer>();



        nState[nRegEx] = 1;
        mapOfSetToInt.put(firstPos[postfix.length() - 1], nState[nRegEx]);
        mapOfIntToSet.put(nState[nRegEx], firstPos[postfix.length() - 1]);
        queue.add(nState[nRegEx]);
        nState[nRegEx]++;

        while(!queue.isEmpty()){
            Set <Integer> set = new HashSet<>();
            Integer top = queue.remove();
            set.addAll(mapOfIntToSet.get(top));
            //System.out.print(set + " ");


            for(int i=0; i<CHARACTERS.length; i++){

                Set <Integer> tranSet= new HashSet<>();
                for(int j : set){
                    if( CHARACTERS[i] == postfix.charAt(j)) {
                        tranSet.addAll(followPos[j]);
                    }
                }
                if(tranSet.isEmpty()) continue;

                if(mapOfSetToInt.get(tranSet)==null){

                    DFA[nRegEx][mapOfSetToInt.get(set)][i] = nState[nRegEx];
                    mapOfSetToInt.put(tranSet,nState[nRegEx]);
                    mapOfIntToSet.put(nState[nRegEx],tranSet);
                    queue.add(nState[nRegEx]);
                    nState[nRegEx]++;
                }
                else{

                    DFA[nRegEx][mapOfSetToInt.get(set)][i] = mapOfSetToInt.get(tranSet);
                }

            }

            /** Finding the state is Final or not*/
            for(int i: set){
                if(i == postfix.length()-2) isFinalState[nRegEx][top] = true;
            }

        }



        /** Printing DFA table*/
        /*
        for(int i = 1; i<nState[nRegEx];i++) {
            System.out.println(i + ": " + DFA[nRegEx][i][0] + " " + DFA[nRegEx][i][1]);
        }

        for(int i = 1; i<nState[nRegEx];i++) {
            if(isFinalState[nRegEx][i]==true)
                System.out.println("Final: " + i);
        }
        */
    }

    /** Funcion for any subsequence is accepted by the DFA or not */
    public static String  isAcceped(int nRegEx, String string){
        int currentState = 1;

        String possibleToken = new String();
        String largestPossibleToken = new  String();


        //System.out.println(string);

        boolean foundEndPoint = false;

        for(int i=0; i<string.length();i++){

            //System.out.println(currentState);
            possibleToken+=string.charAt(i);
            boolean letterFound = false;

            for(int j=0; j<CHARACTERS.length; j++) {

                if (string.charAt(i) == CHARACTERS[j]) {
                    if (DFA[nRegEx][currentState][j] == null) {
                        foundEndPoint = true;
                        break;
                    }
                    currentState = DFA[nRegEx][currentState][j];
                    letterFound = true;
                    break;
                }
            }

            if(foundEndPoint)
                break;

            if(!letterFound) {
                errorDetected = true;
                return largestPossibleToken;
            }
            if(isFinalState[nRegEx][currentState]==true) largestPossibleToken = possibleToken;

        }

        //System.out.println(largestPossibleToken);
        return largestPossibleToken;
    }


    /*
    *Returns true if there exist a left square bracket from the position pos
    * */
    public static boolean findLeftSquare(String regex, int pos){
        for(int i=pos; i>=0; i--){
            if(regex.charAt(i)==']') return false;
            if(regex.charAt(i)=='[') return true;
        }
        return false;
    }
    /*
    *Returns true if there exist a right square bracket from the position pos
    * */
    public static boolean findRightSquare(String regex, int pos){
        for(int i=pos; i<regex.length(); i++){
            if(regex.charAt(i)=='[') return false;
            if(regex.charAt(i)==']') return true;
        }
        return false;
    }

    /*
    *Processes Ranges i.e. [A-EXYZ] will be converted to (A|B|C|D|E|X|Y|Z)
    * */
    public static String processRange(String regex){
        String temp;
        for(int i=0; i<regex.length(); i++){
            if(regex.charAt(i)=='-'){
                if(i>1 && i<regex.length()-1 && (int)regex.charAt(i-1)<(int)regex.charAt(i+1) && findLeftSquare(regex, i) && findRightSquare(regex, i)){
                    temp = "";
                    for(int j=(int)regex.charAt(i-1); j<=(int)regex.charAt(i+1); j++) {
                        temp += (char)j;
                    }
                    regex = regex.substring(0, i-1) + temp + regex.substring(i+2, regex.length());
                }else{
                    return null;
                }
            }
        }

        boolean started = false;
        String ret = "";
        for(int i=0; i<regex.length(); i++){

            if(regex.charAt(i)=='['){
                ret+='(';
                started = true;
            }else if(started==true && regex.charAt(i)!=']' && regex.charAt(i)!='['){
                ret+=regex.charAt(i);
                if(i+1<regex.length() && regex.charAt(i+1)!=']')
                    ret+='|';
            }else if(started==true && regex.charAt(i)==']'){
                ret+=')';
                started = false;
            }else
                ret+=regex.charAt(i);
        }
        System.out.println(ret);
        return ret;
    }

    /** Main driver function */
    public static void main(String[] args){


        String[] regEx = new String[MAX];
        String[] string = new String[MAX];

        int nRegEx=0;   /** Number of regular expressions that will be read from input.l file*/


            FileReader FR;
            BufferedReader BR;
        try {
                FR = new FileReader("/home/sayef/dev/GithubProjects/LexicalAnalyzer/src/input.l"); /** Change the directory for your path/to/input.l */
                BR = new BufferedReader(FR);


            while( (regEx[nRegEx] = BR.readLine())!=null ){
                try {
                    regEx[nRegEx] = processRange(regEx[nRegEx]);
                }catch (Exception e){
                    System.out.println("Format not recognized!");
                }
                nRegEx++;
            }
            BR.close();
            FR.close();
        }catch(Exception e) {System.out.println(e);}



        RegExConverter Postfix = new RegExConverter();      /** Object of RegExConverter class */
        for( int i=0; i<nRegEx; i++) {

            String str = Postfix.infixToPostfix(regEx[i]); /** converted string of infix to postfix */
            str = str.replace("?", EPSILON+"|");
            str += "$.";
            System.out.println("Postfix of Token " + (i+1) + ": " + regEx[i] + " is " + str);
            DesignDFA(i, str);
        }

        System.out.println("Now Give Input String: ");
        int  nString=1;
        Scanner scanIn = new Scanner(System.in);
        while(scanIn.hasNext()) {
                    string[0] = scanIn.nextLine();      /** Now take input from console to match with regular expression*/
                    int notMatched = 0;
                    for (int i = 0; i < nString; i++) {

                        int start = 0;

                        while(start < string[i].length()) {
                            //System.out.println("here");
                            int fin = 0;
                            int finalMax = -1;
                            String largestStr = "";
                            String largestMatch = new String();
                            int tokenNum = 0;
                            //System.out.print(start + " ");
                            for (int k = 0; k < nRegEx; k++) {     /** We are trying to match with every DFA*/

                                String temp = isAcceped(k, string[i].substring(start));

                                if(errorDetected==true) {
                                    System.out.println("An Error Detected: " + string[i].substring(start) + " is Unrecognized!");
                                    start++;
                                }
                                if (largestMatch.length() < temp.length()) largestMatch = temp; /** We want largest match */

                                /** When we get the largest match then we move our string position to start + FinalMax*/
                                if(finalMax<largestMatch.length()) {
                                    finalMax = largestMatch.length();
                                    fin = start+finalMax;
                                    largestStr = temp;
                                    tokenNum = k;

                                }
                            }
                            if(errorDetected==true){
                                errorDetected = false;
                                break;
                            }
                            if(!largestStr.equals(""))
                            System.out.println(largestStr + " Token "  + (tokenNum+1));
                            start = fin;
                            if(start == fin && start!=string[i].length()){
                                System.out.println("No match found!");
                                break;
                            }
                        }
                    }
        }
        scanIn.close();

    }

}
