package gr.uom.java.jdeodorant.metrics.reusability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.uom.java.ast.MethodObject;
import gr.uom.java.ast.Access;
import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.SystemObject;

// Implementation for QMOOD reusability metric 
public class ReusabilityV1 {

	public static double calculate(SystemObject system) {
		return calculateMessaging(system);
	}

	private static double calculateMessaging(SystemObject system) {

		Map<String, Integer> publicMethodsMap = new HashMap<String, Integer>();

		Set<ClassObject> classes = system.getClassObjects();

		for (ClassObject classObject : classes) {
			int publicMethodsCount = countPublicMessages(classObject);
			publicMethodsMap.put(classObject.getName(), publicMethodsCount);

		}
        double nbClasses = classes.size();
        double totalPublicMethods = 0.0;
        for(String key : publicMethodsMap.keySet()) {
        	totalPublicMethods += publicMethodsMap.get(key);
        	System.out.println( key + "  " +  publicMethodsMap.get(key));
        }
        
		return totalPublicMethods/nbClasses;
	}

	private static  int countPublicMessages(ClassObject classObject) {
		int result = 0;
		List<MethodObject> methods = classObject.getMethodList();
		for (int i = 0; i < methods.size() - 1; i++) {
			MethodObject method = methods.get(i);
			if (method.getAccess() == Access.PUBLIC)
				result++;
		}
		return result;
	}

	private static double calculateCoupling(SystemObject system) {
		return 0.0;
	}

}
