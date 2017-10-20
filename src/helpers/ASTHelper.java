package helpers;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

public class ASTHelper {

	/**
	 * Create and setup an ASTParser for visitation. Visits the specified
	 * unit name (typically a class name), and builds the tree using the source
	 * in sourcePath.
	 * 
	 * @param unitName		The compilation unit name, typically a class name
	 * @param sourceCode	The source code in the specified unit
	 * @param sourcePath	Path to the project root, so we can build an AST with resolved
	 * 						type bindings.
	 * @return an ASTParser set to parse the contents of a project
	 */
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
	
	/**
	 * Get a unique identifier for a method binding.
	 * The identifier is built by concatenating the following,
	 * in order: top level class name, method name, parameter types
	 * 
	 * @param binding	The method binding, resolved from a MethodDeclaration
	 * 					or MethodInvocation that has been visited.
	 * @param fileName	The class containing the node we used to resolve binding.
	 * 					We use this to determine if the declaring class is top-level
	 * 					or not.
	 * @return	A String representing a unique identifier for this method within the
	 * 			current project.
	 */
	public static String getUniqueMethodIdentifier(IMethodBinding binding, String fileName) {
		ITypeBinding declaringClass = binding.getDeclaringClass();
		if (fileName != null) {
			// if a filename is specified, check that the class matches
			// the file name.
			ITypeBinding topLevelClass = declaringClass;
			
			// this could be an inner class, so get the top level class
			while (!topLevelClass.isTopLevel() ) {
				topLevelClass = declaringClass.getDeclaringClass();
			}
			
			fileName = fileName.replaceFirst("[.][^.]+$", "");
			if (!topLevelClass.getName().equals(fileName)) {
				// this file name is not the same as the top level class
				// this class was never meant to be compiled, so ignore this method
				return null;
			}
		}
		
		StringBuilder builder = new StringBuilder(declaringClass.getName())
					.append("," + binding.getName());
		if (binding.getParameterTypes().length == 0) {
			return builder.toString();
		}
		
		for (ITypeBinding current : binding.getParameterTypes()) {
			builder.append("," + current.getName());
		}
		
		return builder.toString();
	}
	
	/**
	 * Check if the method belonging to the specified IMethodBinding
	 * is public or not.
	 * @param binding	The binding, resolved from a MethodInvocation or MethodDeclaration
	 * 					that was visited.
	 * @return true if it is public, false otherwise
	 */
	public static boolean methodIsPublic(IMethodBinding binding) {
		return binding != null && (binding.getModifiers() & Modifier.PRIVATE) != 0;
	}
}
