/**
 * Created by Goncalo on 11/04/2017.
 */
public class MorseCodeDecoder {
    public static String decode(String morseCode) {
        String[] alpha = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
                "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8",
                "9", "0", "!"};
        String[] morse = { ".-", "-...", "-.-.", "-..", ".", "..-.", "--.",
                "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.",
                "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-",
                "-.--", "--..", ".----", "..---", "...--", "....-", ".....",
                "-....", "--...", "---..", "----.", "-----", "-.-.--"};
        String build = "";
        String change = morseCode.trim();
        String[] words = change.split("   ");
        for (String word : words) {
            for(String letter : word.split(" ")){
                for(int x=0;x<morse.length;x++){
                    if(letter.equals(morse[x]))
                        build=build+alpha[x];
                }
            }
            build+=" ";
        }
        return build.trim().toUpperCase();
    }

    public static void main(String[] args){
        System.out.println(decode("... --- ..."));
    }

}
