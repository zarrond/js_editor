import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
public class testJS {	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter("C:\\Users\\TrofimovDM\\JS_projects\\testdir\\samplefile1.txt", "UTF-8");
		String fileContent = "Hello Learner !! Welcome to howtodoinjava.com.";
		writer.println(fileContent);
		writer.close();
	}
}
