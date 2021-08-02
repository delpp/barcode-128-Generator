package barcode128Generator.model;

public class Text {
	public static String removeFreeSpaces(String sentence){
		byte leftSpaces = 0;
		byte rightSpaces = 0;
		char charFromSentence;
		
		for (int i = 0; i < sentence.length(); i++){
			charFromSentence = sentence.charAt(i);	
			if (charFromSentence == ' ') leftSpaces++;
				else break;
		}
		
		if (leftSpaces < sentence.length())
			for (int i = sentence.length()-1; i >= 0; i--){
				charFromSentence = sentence.charAt(i);		
				if (charFromSentence == ' ') rightSpaces++;
					else break;
			}	
		
		return sentence.substring(leftSpaces, sentence.length()-rightSpaces);
	}
}
