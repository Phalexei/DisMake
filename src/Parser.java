import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Parser
 *
 * Parser for a makefile
 */

public class Parser {
	private String fileName;
	private List<CatRoot> theliste;
	
	public Parser(String fileName) {
		String tempcible;
		List<CatRoot> dep = null;
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
			while (myLine != null) {
				tempcommand = "";
				dep = null;
				if (!myLine.isEmpty()) {
				// to verifiy if there's something to do
					
					String words[] = myLine.split(":");
					tempcible = words[0];

					
					String[] thewords;
					if (words.length > 1) {
					// if there some dependances
						thewords = words[1].split(" ");
						dep = new ArrayList<CatRoot>();
						for (String s : thewords) {
							dep.add(new CatRoot(s));
						}

					}
					thewords = null;
					try {
						myLine = bufRead.readLine();
						//read the command associated with the cible
						tempcommand = myLine;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// add the things to the main list
					// for now dep has just a name, it's incomplete
					theliste.add(new CatRoot(new String(tempcommand),
							new String(tempcible), dep));
				}

				// next line
				myLine = bufRead.readLine();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		//TODO
		// we have to link the dep with the real cible daclarated later
		//so we have to check all the dep of all cible and set the real dep
		// from the real "theliste"
	}

	@Override
	public String toString() {
		String sortie = "";
		for (CatRoot c : theliste) {

			sortie = sortie + c.toString();
		}
		return sortie;

	}

}
