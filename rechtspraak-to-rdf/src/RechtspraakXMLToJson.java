import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.LinkedList;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.leibnizcenter.rechtspraak_importer.model.CouchDoc;
import org.leibnizcenter.rechtspraak.CouchInterface;
import org.leibnizcenter.rechtspraak.enricher.Enrich;
import org.leibnizcenter.xml.NotImplemented;
import org.xml.sax.SAXException;

import generated.OpenRechtspraak;

public class RechtspraakXMLToJson {

	public static void main(String[] args) {
		// Command-line options: either ecli or input and output
		Options options = new Options();
		
		OptionGroup inputGroup = new OptionGroup();
		Option input = new Option("i", "input", true, "input file path. This can be an xml file, or a directory.");
		inputGroup.addOption(input);
		
		inputGroup.setRequired(true);
		options.addOptionGroup(inputGroup);
		
		Option output = new Option("o", "output", true, "output file");
		options.addOption(output);

		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

		try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

		
		//Read the options and call the right functions
		//Input XML path
		
		LinkedList<File> inputFiles = new LinkedList<File>();
		LinkedList<String> outputFilenames = new LinkedList<String>();
		
		File inputFile = new File(cmd.getOptionValue('i'));
		if(inputFile.exists()){
			if(inputFile.isFile()){
				inputFiles.add(inputFile);
			}
			else if(inputFile.isDirectory()){
				//Take all xml files in the directory
				String[] filenames = inputFile.list(new SuffixFileFilter(".xml"));
				for(String file : filenames){
					inputFiles.add(new File(inputFile.getPath(), file));
				}
			}
		}
		else {
			//TODO wildcards doesn't work
			String pattern = inputFile.getName();
			FileFilter fileFilter = new WildcardFileFilter("pattern");
			File parentFile = inputFile.getParentFile();
			File[] files = parentFile.listFiles(fileFilter);
			for(File file : files){
				inputFiles.add(file);
			}
			
		}
		

		if(cmd.hasOption('o')){
			String outputPathValue = cmd.getOptionValue('o');
			File file = new File(outputPathValue);
			if(file.isDirectory()){
				for(File inputFilename : inputFiles){
					String filename = inputFilename.getName().replaceAll(".xml$", ".json");
					String outputPath = new File(outputPathValue, filename).getPath();
					outputFilenames.add(outputPath);
				}
			}
			else {
				String outputPath = cmd.getOptionValue('o');
				outputFilenames.add(outputPath);
			}
		}

		if(inputFiles.size()>outputFilenames.size()){
			System.out.println("Not enough output paths specified");
			System.exit(1);
            return;
		}
		
		//Do the actual conversion
		try {
			for(int i=0; i<inputFiles.size(); i++){
				convertToJson(inputFiles.get(i), outputFilenames.get(i));
			}
		} catch (JAXBException | IOException | IllegalAccessException | InstantiationException | ClassNotFoundException | TransformerException | URISyntaxException | SAXException | ParserConfigurationException | NotImplemented e) {
			System.out.println(e.getMessage());
			System.exit(1);
            return;
		}
		

	}
	
	public static void convertToJson(File inputFile, String outputPath) throws JAXBException, IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, TransformerException, URISyntaxException, SAXException, ParserConfigurationException, NotImplemented{
		byte[] encodedXML = Files.readAllBytes(inputFile.toPath());
		String strXml = new String(encodedXML, "UTF-8");
		//System.out.println(strXml);
		//DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Enrich enricher = new Enrich();
        OpenRechtspraak doc = CouchInterface.parseXml(strXml);
        CouchDoc couchDoc = new CouchDoc(doc, strXml);
		String json = CouchInterface.toJson(couchDoc);
		//Files.write(Paths.get(outputPath), json.getBytes());
		System.out.println(json);
		System.out.println("Wrote JSON file to "+outputPath);
	}

}
