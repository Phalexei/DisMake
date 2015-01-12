import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Parser
 *
 * Parser for a makefile
 */

public class Parser {
	private String fileName;

	public Parser(String fileName) {

		String tempcible;
		List<CatRoot> dep = null;
		List<CatRoot> theliste;
		String tempcommand = "";

		this.fileName = fileName;
		FileReader input = null;
		try {
			input = new FileReader(fileName);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedReader bufRead = new BufferedReader(input);
		String myLine = null;
		theliste = new ArrayList<CatRoot>();

		try {
			myLine = bufRead.readLine();
			while ((myLine != null)) {
				tempcommand = "";
				dep = null;
				if (!myLine.isEmpty()) {
					System.out.println("ligne:" + myLine);
					String words[] = myLine.split(":");
					tempcible = words[0];
					// System.out.println("cible:"+words[0]);
					String[] thewords;
					if (words.length > 1) {
						
						thewords = words[1].split(" ");
						dep = new ArrayList<CatRoot>();
						for (String s : thewords) {
							dep.add(new CatRoot(s));
						}
						
						
					}
					thewords = null;
					try {
						myLine = bufRead.readLine();
						System.out.println("ligne:" + myLine);
						tempcommand = myLine;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					theliste.add(new CatRoot(new String(tempcommand), new String(tempcible), dep));
				}

				myLine = bufRead.readLine();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		  for (CatRoot c : theliste) {
		  
			  c.MytoString(); 
		  }
		 

	}

}
