package helpers;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class ASTHelper {

	public static ASTParser createAndSetupParser(String unitName, String sourceCode, String sourcePath) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setBindingsRecovery(true);
		
		Map<String, String> options = JavaCore.getOptions();
		parser.setCompilerOptions(options);
		
		parser.setUnitName(unitName);
		
		String[] sources = new String[] { sourcePath };
		
		parser.setEnvironment(null, sources, new String[] { "UTF-8" }, true);
		parser.setSource(sourceCode.toCharArray());
		
		return parser;
	}
	
	public static String getUniqueMethodIdentifier(IMethodBinding binding) {
		StringBuilder builder = new StringBuilder(binding.getName());
		if (binding.getParameterTypes().length == 0) {
			return builder.toString();
		}
		
		for (ITypeBinding current : binding.getParameterTypes()) {
			builder.append(current.getName());
		}
		
		return builder.toString();
	}
}
