package method_analyze;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;


/**
 * @author fwk
 * parse method body from java file and put it to DB
 * 
 */
public class LocalMethodAnalyzer {
	
//	public String getMethodBody(String filepath, String classname, String signature)  {
//		FileInputStream inputStream;
//		try {
//			inputStream = new FileInputStream(filepath);
//			CompilationUnit cUnit;
//			cUnit = JavaParser.parse(inputStream);
//			ClassOrInterfaceDeclaration n = cUnit.getClassByName(classname).get();
//			signature = signature.replace(" ", "");
//			int begin = signature.indexOf("(");
//			int end = signature.indexOf(")");
//			String[] signatures = signature.substring(begin+1, end).split(",");
//			List<MethodDeclaration> ms = n.getMethodsBySignature(signature.substring(0,begin),signatures);
//			try {
//				inputStream.close();
//			} catch (IOException e) {}
//			if(ms.isEmpty()) {
//				return "";
//			}
//
//			return ms.get(0).getBody().get().toString();
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			return "";
//		}
//		
//	}
	
	
	static String path = "";
	static CompilationUnit unit = null;
	private CompilationUnit getCunit(String filepath) {
		if(path.equals(filepath)) {
			return unit;
		}else {
			path = filepath;
			FileInputStream inputStream;
			try {
				inputStream = new FileInputStream(filepath);
				unit = JavaParser.parse(inputStream);
				inputStream.close();
				return unit;
			} catch (IOException e) {
				unit = null;
				return unit;
			}
			
		}
	}
	

	Optional<ClassOrInterfaceDeclaration> Option = null;
	public int[] getMethodLineNumber(String filepath, String classname, String signature) {
		CompilationUnit cUnit = getCunit(filepath);
		if(cUnit == null) {
			return new int[] {0,0};
		}
		Option = cUnit.getClassByName(classname);
		
		if(!Option.isPresent()) {
			System.out.println("no class found");
			return new int[] {0,0};
		}
		ClassOrInterfaceDeclaration n = Option.get();
		signature = signature.replace(" ", "");
		int begin = signature.indexOf("(");
		int end = signature.indexOf(")");
		String[] signatures = signature.substring(begin+1, end).split(",");
		if (signatures[0].equals("")) {
			signatures = new String[] {};
		}
		List<MethodDeclaration> ms = n.getMethodsBySignature(signature.substring(0,begin),signatures);
		if(ms.isEmpty()) {
			System.out.println("no method found");
			return new int[] {0,0};
		}
		
		int b = ms.get(0).getBegin().get().line;
		int e = ms.get(0).getEnd().get().line;
		Option = null;
		return new int[] {b,e};
	}
	
	
}
