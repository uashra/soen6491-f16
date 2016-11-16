package gr.uom.java.jdeodorant.metrics.reusability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.uom.java.ast.MethodObject;
import gr.uom.java.ast.Access;
import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.FieldInstructionObject;
import gr.uom.java.ast.SystemObject;

// Implementation for QMOOD reusability metric 
public class ReusabilityQMOOD {

	public static double calculate(SystemObject system) {
		return  -0.25*calculateCoupling(system) +
			     0.25*calculateCohesion(system) +
			     0.5*calculateMessaging(system) + 
			     0.5*calculateDesignSize(system);
	}

	private static double calculateDesignSize(SystemObject system) {
		return system.getClassObjects().size();
	}

	private static double calculateCohesion(SystemObject system) {
		Map<String, Double> cohesionMap = new HashMap<String, Double>();
		Set<ClassObject> classes = system.getClassObjects();
		
		for (ClassObject classObject : classes) {
			double classCohesion = calculateCohesion(classObject);
			cohesionMap.put(classObject.getName(), classCohesion);

		}
		
		double sumCohesion = 0.0;
		for(String key : cohesionMap.keySet()) {
			sumCohesion += cohesionMap.get(key);
        	System.out.println( key + "  " +  cohesionMap.get(key));
        }
		return sumCohesion/classes.size();
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
	
	private static double calculateCohesion(ClassObject classObject){
		List<MethodObject> methods = classObject.getMethodList();
		Set<String> allParameters = new HashSet<String>();
		
		for (int i = 0; i < methods.size() - 1; i++) {
			List<FieldInstructionObject> methodParameters = methods.get(i).getFieldInstructions();
			for (FieldInstructionObject param : methodParameters)
				allParameters.add(param.getType().toString());
			
		}
		
		if (allParameters.size() == 0)
			return 0;
		
		double sumIntersection = 0.0;
		
		for (int i = 0; i < methods.size() - 1; i++) {
			List<FieldInstructionObject> methodParameters = methods.get(i).getFieldInstructions();
			for (FieldInstructionObject param : methodParameters)
				if (allParameters.contains(param.getType().toString()))
						sumIntersection++;
			
		}		
		
		return sumIntersection/(methods.size() + allParameters.size());
	}

}
