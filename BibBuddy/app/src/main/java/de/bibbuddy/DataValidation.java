package de.bibbuddy;

public class DataValidation {

    //checks for the type of the input (String or Integer)
    public boolean isInputString(Object input){
        if (input instanceof String) {
            return true;
        } else {
            System.out.println("Input object is not String.");
            return false;
        }
    }

    public boolean isInputInteger(Object input){
        if (input instanceof Integer) {
            return true;
        } else {
            System.out.println("Input object is not Integer.");
            return false;
        }
    }

    //checks if the input string is null, empty or both
    public boolean isStringNotNull(String input){
        if(input != null){
            return true;
        } else {
            System.out.println("Input string is null.");
            return false;
        }
    }

    public boolean isStringNotEmpty(String input){
        if(!input.isEmpty()){
            return true;
        } else {
            System.out.println("Input string is empty.");
            return false;
        }
    }

    public boolean isStringNotNullNotEmpty(String input) {
        if (input != null && !input.isEmpty()){
            return true;
        } else {
            System.out.println("Input string is either null or empty.");
            return false;
        }
    }

    //checks if the input integer is equal to null
    public boolean isIntegerNull(int input) {
        if (input == 0){
            return true;
        } else {
            System.out.println("Integer is 0.");
            return false;
        }
    }
}
