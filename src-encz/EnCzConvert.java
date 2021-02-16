import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;


public class EnCzConvert {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length <= 0) {
			System.out.println("You need to specify input and output files:");
			System.out.println("java /home/user/yourfile /home/user/outfile");
			System.out.println("Or something like that");
			return;
		}
		if (args.length < 2) {
			System.out.println("You have to specify output file");
			//well very lazy to think sory for a copy
			System.out.println("java /home/user/yourfile /home/user/outfile");
			System.out.println("Or something like that");
			return;
		}
		String fileName = args[0];
		String charsLowerCase = "aábcčdďeéěfghiíjklľmnňoópqrřsštťuúůvwxyýzž";
		String charsUpperCase = "AÁBCČDĎEÉĚFGHIÍJKLĽMNŇOÓPQRŘSŠTŤUÚŮVWXYÝZŽ";
		String charsNumbers = "0123456789";
		String otherChars  = "- =/,()'\\.\"?<;!´&:+>[]$^*`%#_~|";
		String otherCharsNoWhiteSpace = "";
		BufferedReader in; 
        try {
            in = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException ex) {
            System.out.println("Input file does not exists - " + fileName);
            return;
        }

        FileOutputStream out = new FileOutputStream(args[1]); 
        StringBuffer lines = new StringBuffer();
        
        String line = in.readLine();
        String correct = charsNumbers + charsLowerCase + charsUpperCase + otherChars;
        String usedChars = "";
        char ch;
        
        	
        while (line != null) {
        	if (line.length() > 0 )	ch = line.charAt(0);
        	else ch = '#';
        	if (Character.isLetter(ch) || Character.isDigit(ch)) {

        		int tabC = 0;
        		int i = 0;
        		while (i < line.length()) {
        			ch = line.charAt(i++);

        			if (ch == '\t') {
        				tabC++;
        				if (tabC > 1) {
        					i = line.length() + 1;
        				} else {
        					lines.append('\t');
        				}
        			} else if (ch == '\n') {
        				if (tabC < 1) {
        					lines.append('\t');
        				}
    					i = line.length() + 1;
        			} else {
        				lines.append(ch);
        				if (correct.indexOf(ch) == -1) {
        					if (usedChars.indexOf(ch) == -1) {
        						usedChars += ch;
        						System.out.println(line);
        					}
        				}
        			}
        		}
        		lines.append('\n');
        	}
    		line = in.readLine();
        }
        String o;
        
        o = "ExcludeChars:" + usedChars +"\n";
        o += "ExcludeCharsTo:" + usedChars + "\n";
        o += "Languages:en,cz\n";
        o += "CharsetLowerCase:" + charsLowerCase + "\nCharsetUpperCase:" + charsUpperCase +"\nNumbers:" + charsNumbers + "\n";
        o += "OtherChars:" + otherChars + "\n";
        o += "OtherCharsNoWhiteSpace:" + otherCharsNoWhiteSpace + "\n";
        o += "KeyMap1: 0	.1	abcáč2	defďéě3	ghií4	jkl5	mnoňó6	pqrsřš7	tuvťúů8	wxyzýž9\n";
        o += "KeyMap2: 0	.1	abcáč2	defďéě3	ghií4	jkl5	mnoňó6	pqrsřš7	tuvťúů8	wxyzýž9\n";
        o += "DATA:\n";
        out.write(o.getBytes("UTF-8"));
        
        out.write(lines.toString().getBytes("UTF-8"));
        
        in.close();
        out.close();
        
	}

}
