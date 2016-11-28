package gr.uom.java.jdeodorant.metrics.reusability;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import gr.uom.java.ast.Access;
import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.MethodInvocationObject;
import gr.uom.java.ast.MethodObject;
import gr.uom.java.ast.SystemObject;

//Implementation Type 1-8
public class ReusabilityType18 {
	
	static Map<String, Integer> generalMap = new HashMap<String, Integer>();

	public static double calculate(SystemObject system) {
		List<MethodObject> allMethods = new ArrayList<MethodObject>();
		//List<MethodInvocationObject> allMethodsInvocations = new ArrayList<MethodInvocationObject>();
		List<String> allMethodsNames = new ArrayList<String>();
		Map<String, String> methodClassMap = new HashMap<String, String>();
        Set<String> classesNames = new HashSet<String>();
		int[][] m = null;
		int n = 0;
		
		//class methods
		for (ClassObject classObject : system.getClassObjects()) {
			classesNames.add(classObject.getName());
		}
		
		// all methods
		for (ClassObject classObject : system.getClassObjects()) {
			System.out.println("++++++ Class :"+ classObject.getName());
			
			List<MethodObject> publicMethods = getNonPrivateMethods(classObject);
			for (MethodObject mo : publicMethods) {
				allMethods.add(mo);
				MethodInvocationObject mi = mo.generateMethodInvocation();
				allMethodsNames.add(mi.toString());
				methodClassMap.put(mi.toString(), mi.getOriginClassName());
				// System.out.println(mo);

			
			}
		}
			
		// sort names
		  java.util.Collections.sort(allMethodsNames);
		
		// build methods matrix
		n = allMethods.size();
		m = new int[n][n] ;
		System.out.println("    Total methods :" + n);	
					
			
			for (int i = 0; i < allMethods.size() - 1; i++) {
				MethodObject mo = allMethods.get(i);
				MethodInvocationObject lmio = mo.generateMethodInvocation();

				int i1 = allMethodsNames.indexOf(lmio.toString());

				//System.out.println("     MO :" + mo);
				///System.out.println("     MOMIO :" + mo.generateMethodInvocation());
				//System.out.println("     MOMIO CLASS :" + mo.generateMethodInvocation().getOriginClassName());
				List<MethodInvocationObject> calledMethods = mo.getMethodInvocations();

				for(MethodInvocationObject mio : calledMethods) {
					//System.out.println("        MIO :" + mio);
					int j1 =   allMethodsNames.indexOf(mio.toString());
					if (j1 >=0)
						m[i1][j1]=1;
				}

			}
        initGeneralMap();
			//System.out.println("------ Class :"+ classObject.getName());
	    int[][] t = calculateT(m,allMethodsNames,methodClassMap) ; 

		try {
			saveToFile(m, "my_array.txt");
			saveToFile(t, "t_array.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total Public Methods:"+ allMethods.size());
//		for(String mn: allMethodsNames)
//			{
//				System.out.println(mn);
//				
//			}
		
		try {
			saveToFileMethods(allMethodsNames, "methods.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		for(String name: classesNames)
//		{
//			System.out.println(name);
//			
//		}
//		System.out.println("----------------");
//		for (ClassObject classObject : system.getClassObjects()) {
//			System.out.println(classObject.getName());
//		}
		int[][] t0 = null;
		try {
			t0 = loadFromFile("t_array_before.txt");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[][] transition = calculateTransitionMatrix(t0,t);
		
		return calculateReusabilityImprovementFactor(transition);
	}

	public static List<String> getAllMethods(SystemObject system) {

		List<String> result =  new ArrayList(); 

		return result;
	}

	private static List<MethodObject> getNonPrivateMethods(ClassObject classObject)
	{
		List<MethodObject> result =  new ArrayList<MethodObject>();

		List<MethodObject> methods = classObject.getMethodList();

		for (int i = 0; i < methods.size() - 1; i++) {
			MethodObject method = methods.get(i);
			if (method.getAccess() != Access.PRIVATE)
				result.add(method);
		}

		return result;
	}

	private static void saveToFile(int[][] board, String fileName) throws IOException{

		StringBuilder builder = new StringBuilder();

		for(int i = 0; i < board.length; i++)//for each row
		{
			for(int j = 0; j < board.length; j++)//for each column
			{
				builder.append(board[i][j]+"");//append to the output string
				if(j < board.length - 1)//if this is not the last row element
					builder.append(",");//then add comma (if you don't like commas you can use spaces)
			}
			builder.append("\n");//append new line at the end of the row
		}
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("/Users/kavaler/"+ fileName));
			writer.write(builder.toString());//save the string representation of the board
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		finally {
			if (writer != null)	
				writer.close();
		}
	}

	private static void saveToFileMethods(List<String> names,String fileName) throws IOException{
		StringBuilder builder = new StringBuilder();

		for(String s : names)
		{
			builder.append(s);
			builder.append("\n");
		}

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("/Users/kavaler/"+ fileName));
			writer.write(builder.toString());//save the string representation of the board
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		finally {
			if (writer != null)	
				writer.close();
		}
	}
    private static int[][] calculateT(int[][] m, List<String> names,Map<String, String> methodClassMap  ){
    	
    	int[][] t = new int[m.length][m.length];
    	
    	for(int i = 0; i < m.length; i++)//for each row
		{
			for(int j = 0; j < m.length; j++)//for each column
			{
				if (m[i][j] != 0)
					t[i][j] = getCouplingType(methodClassMap.get(names.get(i)), methodClassMap.get(names.get(i)));
			}
		}
    	
    	return t;
    }
    
    private static int getCouplingType(String class1, String class2){
    	
    	if (isGeneralGeneralCase(class1,class2))
    		if (areClassesRelated(class1,class2))
    			return 1;
    		else
    			return 2;
    	
    	if (isGeneralSpecificCase(class1,class2))
    		if (areClassesRelated(class1,class2))
    			return 3;
    		else
    			return 4;
    	
    	if (isSpecificGeneralCase(class1,class2))
    		if (areClassesRelated(class1,class2))
    			return 5;
    		else
    			return 6;
    	
    	if (isSpecificSpecificCase(class1,class2))
    		if (areClassesRelated(class1,class2))
    			return 7;
    		else
    			return 8;
    	
    	return -1;
    	
    }
    
    private static boolean isGeneralSpecificCase(String class1, String class2){
    	
    	return ((generalMap.get(class1) == 0) &&
       			(generalMap.get(class2) == 1));
    }
    
    private static boolean isGeneralGeneralCase(String class1, String class2){
    	
    	//System.out.println("class1: "+class1);
    	//System.out.println("class2: "+class2);
    	return ((generalMap.get(class1) == 0) &&
       			(generalMap.get(class2) == 0));
    }
   private static boolean isSpecificGeneralCase(String class1, String class2){
    	
	   return ((generalMap.get(class1) == 1) &&
	   			(generalMap.get(class2) == 0));
    }
   
   private static boolean isSpecificSpecificCase(String class1, String class2){
   	
   	return ((generalMap.get(class1) == 1) &&
   			(generalMap.get(class2) == 1));
   }
   
   private static boolean areClassesRelated(String class1, String class2){
 
   	return true;
   }
   
   private static int[][] calculateTransitionMatrix(int[][] couplingMatrixBefore, int[][] couplingMatrixAfter){
	   int [][] transitionMatrix = new int[couplingMatrixAfter.length][couplingMatrixAfter.length];
	   
	   for(int i = 0; i < couplingMatrixAfter.length; i++)//for each row
		{
			for(int j = 0; j < couplingMatrixAfter.length; j++)//for each column
			{
				//related classes
				if ( (couplingMatrixBefore[i][j] == 3 ) &&
						(couplingMatrixAfter[i][j] == 1 ))
					transitionMatrix[i][j] = 1;
					
				if ( (couplingMatrixBefore[i][j] == 1 ) &&
						(couplingMatrixAfter[i][j] == 3 ))
					transitionMatrix[i][j] = -1;
				
				if ( (couplingMatrixBefore[i][j] == 3 ) &&
						(couplingMatrixAfter[i][j] == 7 ))
					transitionMatrix[i][j] = 1;
				
				if ( (couplingMatrixBefore[i][j] == 7 ) &&
						(couplingMatrixAfter[i][j] == 3 ))
					transitionMatrix[i][j] = -1;
				
				if ( (couplingMatrixBefore[i][j] == 1 ) &&
						(couplingMatrixAfter[i][j] == 5 ))
					transitionMatrix[i][j] = -1;
				
				if ( (couplingMatrixBefore[i][j] == 5 ) &&
						(couplingMatrixAfter[i][j] == 1 ))
					transitionMatrix[i][j] = 1;
				
				// unrelated classes
					
				if ( (couplingMatrixBefore[i][j] == 2 ) &&
						(couplingMatrixAfter[i][j] == 4 ))
					transitionMatrix[i][j] = -1;
				
				if ( (couplingMatrixBefore[i][j] == 2 ) &&
						(couplingMatrixAfter[i][j] == 6 ))
					transitionMatrix[i][j] = 1;
				
				if ( (couplingMatrixBefore[i][j] == 6 ) &&
						(couplingMatrixAfter[i][j] == 2 ))
					transitionMatrix[i][j] = -1;
				
				if ( (couplingMatrixBefore[i][j] == 4 ) &&
						(couplingMatrixAfter[i][j] == 8 ))
					transitionMatrix[i][j] = 1;
				
				if ( (couplingMatrixBefore[i][j] == 8 ) &&
						(couplingMatrixAfter[i][j] == 4 ))
					transitionMatrix[i][j] = -1;
				
			}
		}
	   	return transitionMatrix;
   }
   
	private static int calculateReusabilityImprovementFactor(int[][] transitionMatrix) {
		int sum = 0;
		for (int i = 0; i < transitionMatrix.length; i++)// for each row
		{
			for (int j = 0; j < transitionMatrix.length; j++)// for each column
			{
				sum += transitionMatrix[i][j];
			}
		}
		return sum;
	}
	
	private static void initGeneralMap(){
		generalMap.clear();
		
		generalMap.put("org.jfree.chart.StandardChartTheme",1);
		generalMap.put("org.jfree.chart.LegendItemSource",1);
		generalMap.put("org.jfree.chart.ChartFrame",1);
		generalMap.put("org.jfree.chart.Effect3D",0);
		generalMap.put("org.jfree.chart.LegendRenderingOrder",1);
		generalMap.put("org.jfree.chart.ChartColor",0);
		generalMap.put("org.jfree.chart.JFreeChart",1);
		generalMap.put("org.jfree.chart.JFreeChartInfo",1);
		generalMap.put("org.jfree.chart.LegendItem",1);
		generalMap.put("org.jfree.chart.ChartRenderingInfo",1);
		generalMap.put("org.jfree.chart.ChartMouseListener",1);
		generalMap.put("org.jfree.chart.MouseWheelHandler",1);
		generalMap.put("org.jfree.chart.ChartTheme",1);
		generalMap.put("org.jfree.chart.StrokeMap",0);
		generalMap.put("org.jfree.chart.ChartPanel",0);
		generalMap.put("org.jfree.chart.ClipPath",0);
		generalMap.put("org.jfree.chart.PaintMap",0);
		generalMap.put("org.jfree.chart.ChartMouseEvent",0);
		generalMap.put("org.jfree.chart.ChartFactory",0);
		generalMap.put("org.jfree.chart.ChartHints",1);
		generalMap.put("org.jfree.chart.ChartHints.Key",1);
		generalMap.put("org.jfree.chart.ChartUtilities",1);
		generalMap.put("org.jfree.chart.ChartTransferable",1);
		generalMap.put("org.jfree.chart.HashUtilities",1);
		generalMap.put("org.jfree.chart.PolarChartPanel",1);
		generalMap.put("org.jfree.chart.LegendItemCollection",0);
		generalMap.put("org.jfree.chart.DrawableLegendItem",0);
		generalMap.put("org.jfree.chart.annotations.XYPointerAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.XYPolygonAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.XYLineAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.TextAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.CategoryAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.CategoryTextAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.XYTextAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.CategoryLineAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.XYAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.XYBoxAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.XYAnnotationBoundsInfo",0);
		generalMap.put("org.jfree.chart.annotations.XYImageAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.AbstractAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.AbstractXYAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.CategoryPointerAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.XYDataImageAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.XYTitleAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.XYDrawableAnnotation",0);
		generalMap.put("org.jfree.chart.annotations.Annotation",0);
		generalMap.put("org.jfree.chart.annotations.XYShapeAnnotation",0);
		generalMap.put("org.jfree.chart.axis.CategoryAnchor",0);
		generalMap.put("org.jfree.chart.axis.AxisLocation",0);
		generalMap.put("org.jfree.chart.axis.CategoryLabelPosition",0);
		generalMap.put("org.jfree.chart.axis.DateTickUnit",1);
		generalMap.put("org.jfree.chart.axis.ExtendedCategoryAxis",1);
		generalMap.put("org.jfree.chart.axis.LogarithmicAxis",1);
		generalMap.put("org.jfree.chart.axis.TickUnits",1);
		generalMap.put("org.jfree.chart.axis.TickUnitSource",1);
		generalMap.put("org.jfree.chart.axis.PeriodAxisLabelInfo",1);
		generalMap.put("org.jfree.chart.axis.SubCategoryAxis",1);
		generalMap.put("org.jfree.chart.axis.AxisState",1);
		generalMap.put("org.jfree.chart.axis.AxisSpace",0);
		generalMap.put("org.jfree.chart.axis.DateAxis",0);
		generalMap.put("org.jfree.chart.axis.DateAxis.DefaultTimeline",0);
		generalMap.put("org.jfree.chart.axis.CategoryAxis",0);
		generalMap.put("org.jfree.chart.axis.CategoryLabelPositions",0);
		generalMap.put("org.jfree.chart.axis.AxisLabelLocation",0);
		generalMap.put("org.jfree.chart.axis.Axis",0);
		generalMap.put("org.jfree.chart.axis.DateTick",0);
		generalMap.put("org.jfree.chart.axis.MonthDateFormat",0);
		generalMap.put("org.jfree.chart.axis.CategoryAxis3D",0);
		generalMap.put("org.jfree.chart.axis.NumberAxis",0);
		generalMap.put("org.jfree.chart.axis.SymbolAxis",1);
		generalMap.put("org.jfree.chart.axis.LogTick",1);
		generalMap.put("org.jfree.chart.axis.MarkerAxisBand",1);
		generalMap.put("org.jfree.chart.axis.NumberTick",1);
		generalMap.put("org.jfree.chart.axis.NumberTickUnit",1);
		generalMap.put("org.jfree.chart.axis.SegmentedTimeline",1);
		generalMap.put("org.jfree.chart.axis.SegmentedTimeline.Segment",1);
		generalMap.put("org.jfree.chart.axis.SegmentedTimeline.SegmentRange",1);
		generalMap.put("org.jfree.chart.axis.SegmentedTimeline.BaseTimelineSegmentRange",1);
		generalMap.put("org.jfree.chart.axis.Tick",1);
		generalMap.put("org.jfree.chart.axis.ColorBar",0);
		generalMap.put("org.jfree.chart.axis.LogAxis",0);
		generalMap.put("org.jfree.chart.axis.NumberAxis3D",0);
		generalMap.put("org.jfree.chart.axis.CategoryLabelWidthType",0);
		generalMap.put("org.jfree.chart.axis.QuarterDateFormat",0);
		generalMap.put("org.jfree.chart.axis.ModuloAxis",0);
		generalMap.put("org.jfree.chart.axis.CompassFormat",0);
		generalMap.put("org.jfree.chart.axis.Timeline",0);
		generalMap.put("org.jfree.chart.axis.CategoryTick",0);
		generalMap.put("org.jfree.chart.axis.PeriodAxis",0);
		generalMap.put("org.jfree.chart.axis.TickUnit",0);
		generalMap.put("org.jfree.chart.axis.AxisCollection",0);
		generalMap.put("org.jfree.chart.axis.ValueTick",0);
		generalMap.put("org.jfree.chart.axis.DateTickUnitType",0);
		generalMap.put("org.jfree.chart.axis.NumberTickUnitSource",1);
		generalMap.put("org.jfree.chart.axis.StandardTickUnitSource",1);
		generalMap.put("org.jfree.chart.axis.TickType",0);
		generalMap.put("org.jfree.chart.axis.DateTickMarkPosition",0);
		generalMap.put("org.jfree.chart.axis.ValueAxis",0);
		generalMap.put("org.jfree.chart.axis.CyclicNumberAxis",1);
		generalMap.put("org.jfree.chart.axis.CyclicNumberAxis.CycleBoundTick",1);
		generalMap.put("org.jfree.chart.block.Block",0);
		generalMap.put("org.jfree.chart.block.BlockContainer",0);
		generalMap.put("org.jfree.chart.block.GridArrangement",0);
		generalMap.put("org.jfree.chart.block.AbstractBlock",0);
		generalMap.put("org.jfree.chart.block.EntityBlockParams",1);
		generalMap.put("org.jfree.chart.block.RectangleConstraint",1);
		generalMap.put("org.jfree.chart.block.BorderArrangement",1);
		generalMap.put("org.jfree.chart.block.Arrangement",1);
		generalMap.put("org.jfree.chart.block.BlockFrame",0);
		generalMap.put("org.jfree.chart.block.LineBorder",0);
		generalMap.put("org.jfree.chart.block.FlowArrangement",0);
		generalMap.put("org.jfree.chart.block.EmptyBlock",0);
		generalMap.put("org.jfree.chart.block.BlockBorder",0);
		generalMap.put("org.jfree.chart.block.BlockParams",0);
		generalMap.put("org.jfree.chart.block.BlockResult",0);
		generalMap.put("org.jfree.chart.block.ColorBlock",0);
		generalMap.put("org.jfree.chart.block.ColumnArrangement",0);
		generalMap.put("org.jfree.chart.block.LengthConstraintType",0);
		generalMap.put("org.jfree.chart.block.EntityBlockResult",0);
		generalMap.put("org.jfree.chart.block.CenterArrangement",1);
		generalMap.put("org.jfree.chart.block.LabelBlock",0);
		generalMap.put("org.jfree.chart.demo.PieChartDemo1",1);
		generalMap.put("org.jfree.chart.demo.BarChartDemo1",1);
		generalMap.put("org.jfree.chart.demo.TimeSeriesChartDemo1",0);
		generalMap.put("org.jfree.chart.editor.DefaultChartEditorFactory",0);
		generalMap.put("org.jfree.chart.editor.DefaultLogAxisEditor",0);
		generalMap.put("org.jfree.chart.editor.ChartEditor",0);
		generalMap.put("org.jfree.chart.editor.DefaultAxisEditor",0);
		generalMap.put("org.jfree.chart.editor.PaletteSample",0);
		generalMap.put("org.jfree.chart.editor.DefaultColorBarEditor",0);
		generalMap.put("org.jfree.chart.editor.DefaultValueAxisEditor",0);
		generalMap.put("org.jfree.chart.editor.ChartEditorFactory",0);
		generalMap.put("org.jfree.chart.editor.DefaultPlotEditor",0);
		generalMap.put("org.jfree.chart.editor.DefaultTitleEditor",0);
		generalMap.put("org.jfree.chart.editor.ChartEditorManager",0);
		generalMap.put("org.jfree.chart.editor.DefaultChartEditor",0);
		generalMap.put("org.jfree.chart.editor.PaletteChooserPanel",0);
		generalMap.put("org.jfree.chart.editor.DefaultPolarPlotEditor",0);
		generalMap.put("org.jfree.chart.editor.DefaultNumberAxisEditor",0);
		generalMap.put("org.jfree.chart.encoders.ImageEncoderFactory",0);
		generalMap.put("org.jfree.chart.encoders.KeypointPNGEncoderAdapter",0);
		generalMap.put("org.jfree.chart.encoders.SunJPEGEncoderAdapter",0);
		generalMap.put("org.jfree.chart.encoders.ImageFormat",0);
		generalMap.put("org.jfree.chart.encoders.ImageEncoder",0);
		generalMap.put("org.jfree.chart.encoders.EncoderUtil",0);
		generalMap.put("org.jfree.chart.encoders.SunPNGEncoderAdapter",0);
		generalMap.put("org.jfree.chart.entity.TitleEntity",0);
		generalMap.put("org.jfree.chart.entity.PlotEntity",0);
		generalMap.put("org.jfree.chart.entity.TickLabelEntity",0);
		generalMap.put("org.jfree.chart.entity.StandardEntityCollection",0);
		generalMap.put("org.jfree.chart.entity.XYItemEntity",0);
		generalMap.put("org.jfree.chart.entity.ContourEntity",0);
		generalMap.put("org.jfree.chart.entity.ChartEntity",0);
		generalMap.put("org.jfree.chart.entity.JFreeChartEntity",1);
		generalMap.put("org.jfree.chart.entity.PieSectionEntity",1);
		generalMap.put("org.jfree.chart.entity.AxisEntity",1);
		generalMap.put("org.jfree.chart.entity.EntityCollection",1);
		generalMap.put("org.jfree.chart.entity.CategoryItemEntity",1);
		generalMap.put("org.jfree.chart.entity.XYAnnotationEntity",1);
		generalMap.put("org.jfree.chart.entity.LegendItemEntity",1);
		generalMap.put("org.jfree.chart.entity.CategoryLabelEntity",1);
		generalMap.put("org.jfree.chart.event.AxisChangeListener",1);
		generalMap.put("org.jfree.chart.event.AxisChangeEvent",0);
		generalMap.put("org.jfree.chart.event.ChartChangeEvent",0);
		generalMap.put("org.jfree.chart.event.OverlayChangeEvent",0);
		generalMap.put("org.jfree.chart.event.MarkerChangeListener",0);
		generalMap.put("org.jfree.chart.event.MarkerChangeEvent",0);
		generalMap.put("org.jfree.chart.event.RendererChangeListener",0);
		generalMap.put("org.jfree.chart.event.ChartProgressEvent",0);
		generalMap.put("org.jfree.chart.event.OverlayChangeListener",0);
		generalMap.put("org.jfree.chart.event.AnnotationChangeListener",0);
		generalMap.put("org.jfree.chart.event.RendererChangeEvent",0);
		generalMap.put("org.jfree.chart.event.TitleChangeEvent",0);
		generalMap.put("org.jfree.chart.event.PlotChangeEvent",0);
		generalMap.put("org.jfree.chart.event.ChartChangeEventType",0);
		generalMap.put("org.jfree.chart.event.ChartChangeListener",0);
		generalMap.put("org.jfree.chart.event.PlotChangeListener",0);
		generalMap.put("org.jfree.chart.event.TitleChangeListener",0);
		generalMap.put("org.jfree.chart.event.AnnotationChangeEvent",0);
		generalMap.put("org.jfree.chart.event.ChartProgressListener",0);
		generalMap.put("org.jfree.chart.fx.ChartViewerSkin",0);
		generalMap.put("org.jfree.chart.fx.ChartViewer",0);
		generalMap.put("org.jfree.chart.fx.FXGraphics2D",0);
		generalMap.put("org.jfree.chart.fx.ChartCanvas",0);
		generalMap.put("org.jfree.chart.fx.demo.PieChartFXDemo1",0);
		generalMap.put("org.jfree.chart.fx.demo.BarChartFXDemo1",0);
		generalMap.put("org.jfree.chart.fx.demo.TimeSeriesChartFXDemo1",0);
		generalMap.put("org.jfree.chart.fx.interaction.ZoomHandlerFX",0);
		generalMap.put("org.jfree.chart.fx.interaction.MouseHandlerFX",0);
		generalMap.put("org.jfree.chart.fx.interaction.ChartMouseListenerFX",0);
		generalMap.put("org.jfree.chart.fx.interaction.DispatchHandlerFX",0);
		generalMap.put("org.jfree.chart.fx.interaction.PanHandlerFX",0);
		generalMap.put("org.jfree.chart.fx.interaction.ChartMouseEventFX",0);
		generalMap.put("org.jfree.chart.fx.interaction.TooltipHandlerFX",0);
		generalMap.put("org.jfree.chart.fx.interaction.AnchorHandlerFX",0);
		generalMap.put("org.jfree.chart.fx.interaction.ScrollHandlerFX",0);
		generalMap.put("org.jfree.chart.fx.interaction.AbstractMouseHandlerFX",0);
		generalMap.put("org.jfree.chart.imagemap.URLTagFragmentGenerator",0);
		generalMap.put("org.jfree.chart.imagemap.ToolTipTagFragmentGenerator",0);
		generalMap.put("org.jfree.chart.imagemap.ImageMapUtilities",0);
		generalMap.put("org.jfree.chart.imagemap.DynamicDriveToolTipTagFragmentGenerator",0);
		generalMap.put("org.jfree.chart.imagemap.StandardURLTagFragmentGenerator",0);
		generalMap.put("org.jfree.chart.imagemap.OverLIBToolTipTagFragmentGenerator",0);
		generalMap.put("org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator",0);
		generalMap.put("org.jfree.chart.labels.XYItemLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.CustomXYToolTipGenerator",0);
		generalMap.put("org.jfree.chart.labels.StandardXYZToolTipGenerator",0);
		generalMap.put("org.jfree.chart.labels.StandardCategoryToolTipGenerator",0);
		generalMap.put("org.jfree.chart.labels.CategorySeriesLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.PieSectionLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.CategoryItemLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.XYToolTipGenerator",0);
		generalMap.put("org.jfree.chart.labels.BubbleXYItemLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.HighLowItemLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.MultipleXYSeriesLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.StandardXYSeriesLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.ContourToolTipGenerator",0);
		generalMap.put("org.jfree.chart.labels.IntervalCategoryToolTipGenerator",0);
		generalMap.put("org.jfree.chart.labels.IntervalCategoryItemLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.CrosshairLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.StandardCategorySeriesLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.SymbolicXYItemLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator",0);
		generalMap.put("org.jfree.chart.labels.StandardPieToolTipGenerator",0);
		generalMap.put("org.jfree.chart.labels.StandardXYItemLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.IntervalXYItemLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.BoxAndWhiskerXYToolTipGenerator",0);
		generalMap.put("org.jfree.chart.labels.AbstractXYItemLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.StandardCategoryItemLabelGenerator",0);
		generalMap.put("org.jfree.chart.labels.ItemLabelPosition",0);
		generalMap.put("org.jfree.chart.labels.PieToolTipGenerator",0);
		generalMap.put("org.jfree.chart.labels.StandardPieSectionLabelGenerator",1);
		generalMap.put("org.jfree.chart.labels.ItemLabelAnchor",1);
		generalMap.put("org.jfree.chart.labels.CategoryToolTipGenerator",1);
		generalMap.put("org.jfree.chart.labels.StandardCrosshairLabelGenerator",1);
		generalMap.put("org.jfree.chart.labels.XYSeriesLabelGenerator",1);
		generalMap.put("org.jfree.chart.labels.AbstractCategoryItemLabelGenerator",1);
		generalMap.put("org.jfree.chart.labels.StandardContourToolTipGenerator",1);
		generalMap.put("org.jfree.chart.labels.XYZToolTipGenerator",1);
		generalMap.put("org.jfree.chart.labels.AbstractPieItemLabelGenerator",1);
		generalMap.put("org.jfree.chart.labels.StandardXYToolTipGenerator",0);
		generalMap.put("org.jfree.chart.needle.ArrowNeedle",0);
		generalMap.put("org.jfree.chart.needle.LineNeedle",0);
		generalMap.put("org.jfree.chart.needle.MiddlePinNeedle",0);
		generalMap.put("org.jfree.chart.needle.PlumNeedle",0);
		generalMap.put("org.jfree.chart.needle.PointerNeedle",0);
		generalMap.put("org.jfree.chart.needle.ShipNeedle",0);
		generalMap.put("org.jfree.chart.needle.WindNeedle",0);
		generalMap.put("org.jfree.chart.needle.LongNeedle",0);
		generalMap.put("org.jfree.chart.needle.MeterNeedle",0);
		generalMap.put("org.jfree.chart.needle.PinNeedle",0);
		generalMap.put("org.jfree.chart.panel.Overlay",0);
		generalMap.put("org.jfree.chart.panel.AbstractOverlay",0);
		generalMap.put("org.jfree.chart.panel.CrosshairOverlay",0);
		generalMap.put("org.jfree.chart.plot.PiePlot3D",0);
		generalMap.put("org.jfree.chart.plot.PieLabelRecord",0);
		generalMap.put("org.jfree.chart.plot.MeterInterval",0);
		generalMap.put("org.jfree.chart.plot.AbstractPieLabelDistributor",0);
		generalMap.put("org.jfree.chart.plot.Plot",0);
		generalMap.put("org.jfree.chart.plot.ColorPalette",0);
		generalMap.put("org.jfree.chart.plot.CategoryCrosshairState",0);
		generalMap.put("org.jfree.chart.plot.FastScatterPlot",0);
		generalMap.put("org.jfree.chart.plot.MeterPlot",0);
		generalMap.put("org.jfree.chart.plot.Pannable",0);
		generalMap.put("org.jfree.chart.plot.XYPlot",0);
		generalMap.put("org.jfree.chart.plot.ValueMarker",0);
		generalMap.put("org.jfree.chart.plot.CategoryPlot",0);
		generalMap.put("org.jfree.chart.plot.DatasetRenderingOrder",0);
		generalMap.put("org.jfree.chart.plot.Marker",0);
		generalMap.put("org.jfree.chart.plot.CombinedRangeCategoryPlot",0);
		generalMap.put("org.jfree.chart.plot.IntervalMarker",0);
		generalMap.put("org.jfree.chart.plot.Zoomable",0);
		generalMap.put("org.jfree.chart.plot.ContourPlot",0);
		generalMap.put("org.jfree.chart.plot.CompassPlot",0);
		generalMap.put("org.jfree.chart.plot.PolarPlot",0);
		generalMap.put("org.jfree.chart.plot.SeriesRenderingOrder",0);
		generalMap.put("org.jfree.chart.plot.PlotOrientation",0);
		generalMap.put("org.jfree.chart.plot.JThermometer",0);
		generalMap.put("org.jfree.chart.plot.ContourPlotUtilities",0);
		generalMap.put("org.jfree.chart.plot.PiePlotState",0);
		generalMap.put("org.jfree.chart.plot.MultiplePiePlot",0);
		generalMap.put("org.jfree.chart.plot.ValueAxisPlot",0);
		generalMap.put("org.jfree.chart.plot.PieLabelDistributor",0);
		generalMap.put("org.jfree.chart.plot.RainbowPalette",0);
		generalMap.put("org.jfree.chart.plot.ContourValuePlot",0);
		generalMap.put("org.jfree.chart.plot.DefaultDrawingSupplier",0);
		generalMap.put("org.jfree.chart.plot.Crosshair",0);
		generalMap.put("org.jfree.chart.plot.CenterTextMode",0);
		generalMap.put("org.jfree.chart.plot.PieLabelLinkStyle",0);
		generalMap.put("org.jfree.chart.plot.WaferMapPlot",0);
		generalMap.put("org.jfree.chart.plot.CombinedRangeXYPlot",0);
		generalMap.put("org.jfree.chart.plot.XYCrosshairState",0);
		generalMap.put("org.jfree.chart.plot.ThermometerPlot",0);
		generalMap.put("org.jfree.chart.plot.SpiderWebPlot",0);
		generalMap.put("org.jfree.chart.plot.DrawingSupplier",0);
		generalMap.put("org.jfree.chart.plot.CategoryMarker",0);
		generalMap.put("org.jfree.chart.plot.PlotState",0);
		generalMap.put("org.jfree.chart.plot.PiePlot",0);
		generalMap.put("org.jfree.chart.plot.PlotUtilities",0);
		generalMap.put("org.jfree.chart.plot.CombinedDomainXYPlot",0);
		generalMap.put("org.jfree.chart.plot.GreyPalette",0);
		generalMap.put("org.jfree.chart.plot.CombinedDomainCategoryPlot",0);
		generalMap.put("org.jfree.chart.plot.RingPlot",0);
		generalMap.put("org.jfree.chart.plot.PolarAxisLocation",0);
		generalMap.put("org.jfree.chart.plot.DialShape",0);
		generalMap.put("org.jfree.chart.plot.PlotRenderingInfo",0);
		generalMap.put("org.jfree.chart.plot.CrosshairState",0);
		generalMap.put("org.jfree.chart.plot.dial.DialPointer",0);
		generalMap.put("org.jfree.chart.plot.dial.DialPointer.Pin",0);
		generalMap.put("org.jfree.chart.plot.dial.DialPointer.Pointer",0);
		generalMap.put("org.jfree.chart.plot.dial.StandardDialRange",0);
		generalMap.put("org.jfree.chart.plot.dial.AbstractDialLayer",0);
		generalMap.put("org.jfree.chart.plot.dial.DialLayer",0);
		generalMap.put("org.jfree.chart.plot.dial.DialValueIndicator",0);
		generalMap.put("org.jfree.chart.plot.dial.ArcDialFrame",0);
		generalMap.put("org.jfree.chart.plot.dial.DialScale",0);
		generalMap.put("org.jfree.chart.plot.dial.DialFrame",0);
		generalMap.put("org.jfree.chart.plot.dial.DialCap",0);
		generalMap.put("org.jfree.chart.plot.dial.DialPlot",0);
		generalMap.put("org.jfree.chart.plot.dial.DialTextAnnotation",0);
		generalMap.put("org.jfree.chart.plot.dial.DialLayerChangeEvent",0);
		generalMap.put("org.jfree.chart.plot.dial.StandardDialFrame",0);
		generalMap.put("org.jfree.chart.plot.dial.DialBackground",0);
		generalMap.put("org.jfree.chart.plot.dial.DialLayerChangeListener",0);
		generalMap.put("org.jfree.chart.plot.dial.StandardDialScale",0);
		generalMap.put("org.jfree.chart.renderer.AbstractRenderer",1);
		generalMap.put("org.jfree.chart.renderer.Outlier",1);
		generalMap.put("org.jfree.chart.renderer.OutlierList",1);
		generalMap.put("org.jfree.chart.renderer.RendererState",1);
		generalMap.put("org.jfree.chart.renderer.PaintScale",1);
		generalMap.put("org.jfree.chart.renderer.WaferMapRenderer",1);
		generalMap.put("org.jfree.chart.renderer.RendererUtilities",1);
		generalMap.put("org.jfree.chart.renderer.DefaultPolarItemRenderer",1);
		generalMap.put("org.jfree.chart.renderer.LookupPaintScale",1);
		generalMap.put("org.jfree.chart.renderer.LookupPaintScale.PaintItem",1);
		generalMap.put("org.jfree.chart.renderer.OutlierListCollection",1);
		generalMap.put("org.jfree.chart.renderer.AreaRendererEndType",0);
		generalMap.put("org.jfree.chart.renderer.PolarItemRenderer",0);
		generalMap.put("org.jfree.chart.renderer.NotOutlierException",0);
		generalMap.put("org.jfree.chart.renderer.GrayPaintScale",0);
		generalMap.put("org.jfree.chart.renderer.category.BarRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.BoxAndWhiskerRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.CategoryItemRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.LevelRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.GroupedStackedBarRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.LineRenderer3D",0);
		generalMap.put("org.jfree.chart.renderer.category.MinMaxCategoryRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.WaterfallBarRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.CategoryStepRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.CategoryStepRenderer.State",0);
		generalMap.put("org.jfree.chart.renderer.category.GanttRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.DefaultCategoryItemRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.IntervalBarRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.LayeredBarRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.StandardBarPainter",0);
		generalMap.put("org.jfree.chart.renderer.category.StackedBarRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.StatisticalBarRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.GradientBarPainter",0);
		generalMap.put("org.jfree.chart.renderer.category.BarRenderer3D",0);
		generalMap.put("org.jfree.chart.renderer.category.AreaRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.AbstractCategoryItemRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.StackedBarRenderer3D",0);
		generalMap.put("org.jfree.chart.renderer.category.LineAndShapeRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.StackedAreaRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.CategoryItemRendererState",0);
		generalMap.put("org.jfree.chart.renderer.category.ScatterRenderer",0);
		generalMap.put("org.jfree.chart.renderer.category.BarPainter",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYStepRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYBarRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYBarRenderer.XYBarRendererState",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYErrorRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.GradientXYBarPainter",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYBlockRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.ClusteredXYBarRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.StandardXYItemRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.StandardXYItemRenderer.State",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYBoxAndWhiskerRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.StackedXYAreaRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.StackedXYAreaRenderer.StackedXYAreaRendererState",0);
		generalMap.put("org.jfree.chart.renderer.xy.YIntervalRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.CyclicXYItemRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.CyclicXYItemRenderer.OverwriteDataSet",0);
		generalMap.put("org.jfree.chart.renderer.xy.DefaultXYItemRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.StackedXYAreaRenderer2",0);
		generalMap.put("org.jfree.chart.renderer.xy.SamplingXYLineRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.SamplingXYLineRenderer.State",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYBarPainter",0);
		generalMap.put("org.jfree.chart.renderer.xy.CandlestickRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.StackedXYBarRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.StandardXYBarPainter",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYStepAreaRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYSplineRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYSplineRenderer.FillType",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYSplineRenderer.XYSplineState",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYLineAndShapeRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYLineAndShapeRenderer.State",0);
		generalMap.put("org.jfree.chart.renderer.xy.VectorRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYAreaRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYAreaRenderer.XYAreaRendererState",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYItemRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYDotRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.WindItemRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYLine3DRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.AbstractXYItemRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYShapeRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.HighLowRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYDifferenceRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYAreaRenderer2",0);
		generalMap.put("org.jfree.chart.renderer.xy.DeviationRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.DeviationRenderer.State",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYBubbleRenderer",0);
		generalMap.put("org.jfree.chart.renderer.xy.XYItemRendererState",0);
		generalMap.put("org.jfree.chart.resources.JFreeChartResources",0);
		generalMap.put("org.jfree.chart.servlet.ChartDeleter",0);
		generalMap.put("org.jfree.chart.servlet.DisplayChart",0);
		generalMap.put("org.jfree.chart.servlet.ServletUtilities",0);
		generalMap.put("org.jfree.chart.title.ImageTitle",0);
		generalMap.put("org.jfree.chart.title.TextTitle",0);
		generalMap.put("org.jfree.chart.title.DateTitle",0);
		generalMap.put("org.jfree.chart.title.LegendItemBlockContainer",0);
		generalMap.put("org.jfree.chart.title.ShortTextTitle",0);
		generalMap.put("org.jfree.chart.title.LegendTitle",0);
		generalMap.put("org.jfree.chart.title.CompositeTitle",0);
		generalMap.put("org.jfree.chart.title.Title",0);
		generalMap.put("org.jfree.chart.title.LegendGraphic",0);
		generalMap.put("org.jfree.chart.title.PaintScaleLegend",0);
		generalMap.put("org.jfree.chart.urls.StandardXYZURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.XYURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.PieURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.StandardXYURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.StandardPieURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.CustomCategoryURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.CategoryURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.StandardCategoryURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.XYZURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.CustomPieURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.CustomXYURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.TimeSeriesURLGenerator",0);
		generalMap.put("org.jfree.chart.urls.URLUtilities",0);
		generalMap.put("org.jfree.chart.util.ParamChecks",0);
		generalMap.put("org.jfree.chart.util.LineUtilities",0);
		generalMap.put("org.jfree.chart.util.ShadowGenerator",0);
		generalMap.put("org.jfree.chart.util.DefaultShadowGenerator",0);
		generalMap.put("org.jfree.chart.util.XYCoordinateType",0);
		generalMap.put("org.jfree.chart.util.HexNumberFormat",0);
		generalMap.put("org.jfree.chart.util.RelativeDateFormat",0);
		generalMap.put("org.jfree.chart.util.TextUtils",0);
		generalMap.put("org.jfree.chart.util.CloneUtils",0);
		generalMap.put("org.jfree.chart.util.HMSNumberFormat",0);
		generalMap.put("org.jfree.chart.util.DirectionalGradientPaintTransformer",0);
		generalMap.put("org.jfree.chart.util.AttrStringUtils",0);
		generalMap.put("org.jfree.chart.util.ExportUtils",0);
		generalMap.put("org.jfree.chart.util.PaintAlpha",0);
		generalMap.put("org.jfree.chart.util.ResourceBundleWrapper",0);
		generalMap.put("org.jfree.chart.util.LogFormat",0);
		generalMap.put("org.jfree.data.ComparableObjectSeries",0);
		generalMap.put("org.jfree.data.KeyedObject",0);
		generalMap.put("org.jfree.data.Values",0);
		generalMap.put("org.jfree.data.DefaultKeyedValue",0);
		generalMap.put("org.jfree.data.KeyedObjects2D",0);
		generalMap.put("org.jfree.data.RangeType",0);
		generalMap.put("org.jfree.data.KeyedValue",0);
		generalMap.put("org.jfree.data.KeyedValues2D",0);
		generalMap.put("org.jfree.data.Range",0);
		generalMap.put("org.jfree.data.Values2D",0);
		generalMap.put("org.jfree.data.ComparableObjectItem",0);
		generalMap.put("org.jfree.data.DefaultKeyedValues2D",0);
		generalMap.put("org.jfree.data.DataUtilities",0);
		generalMap.put("org.jfree.data.KeyedObjects",0);
		generalMap.put("org.jfree.data.KeyedValueComparatorType",0);
		generalMap.put("org.jfree.data.DomainOrder",0);
		generalMap.put("org.jfree.data.KeyedValues",0);
		generalMap.put("org.jfree.data.RangeInfo",0);
		generalMap.put("org.jfree.data.UnknownKeyException",0);
		generalMap.put("org.jfree.data.DefaultKeyedValues",0);
		generalMap.put("org.jfree.data.KeyToGroupMap",0);
		generalMap.put("org.jfree.data.KeyedValueComparator",0);
		generalMap.put("org.jfree.data.Value",0);
		generalMap.put("org.jfree.data.DomainInfo",0);
		generalMap.put("org.jfree.data.category.CategoryRangeInfo",0);
		generalMap.put("org.jfree.data.category.SlidingCategoryDataset",0);
		generalMap.put("org.jfree.data.category.CategoryDataset",0);
		generalMap.put("org.jfree.data.category.DefaultIntervalCategoryDataset",0);
		generalMap.put("org.jfree.data.category.DefaultCategoryDataset",0);
		generalMap.put("org.jfree.data.category.IntervalCategoryDataset",0);
		generalMap.put("org.jfree.data.category.CategoryToPieDataset",0);
		generalMap.put("org.jfree.data.contour.ContourDataset",0);
		generalMap.put("org.jfree.data.contour.NonGridContourDataset",0);
		generalMap.put("org.jfree.data.contour.DefaultContourDataset",0);
		generalMap.put("org.jfree.data.function.Function2D",0);
		generalMap.put("org.jfree.data.function.PowerFunction2D",0);
		generalMap.put("org.jfree.data.function.PolynomialFunction2D",0);
		generalMap.put("org.jfree.data.function.LineFunction2D",0);
		generalMap.put("org.jfree.data.function.NormalDistributionFunction2D",0);
		generalMap.put("org.jfree.data.gantt.GanttCategoryDataset",0);
		generalMap.put("org.jfree.data.gantt.TaskSeriesCollection",0);
		generalMap.put("org.jfree.data.gantt.XYTaskDataset",0);
		generalMap.put("org.jfree.data.gantt.Task",0);
		generalMap.put("org.jfree.data.gantt.SlidingGanttCategoryDataset",0);
		generalMap.put("org.jfree.data.gantt.TaskSeries",0);
		generalMap.put("org.jfree.data.general.Dataset",0);
		generalMap.put("org.jfree.data.general.HeatMapDataset",0);
		generalMap.put("org.jfree.data.general.KeyedValueDataset",0);
		generalMap.put("org.jfree.data.general.DatasetChangeEvent",0);
		generalMap.put("org.jfree.data.general.DefaultKeyedValueDataset",0);
		generalMap.put("org.jfree.data.general.SeriesException",0);
		generalMap.put("org.jfree.data.general.DatasetUtilities",0);
		generalMap.put("org.jfree.data.general.KeyedValues2DDataset",0);
		generalMap.put("org.jfree.data.general.DefaultPieDataset",0);
		generalMap.put("org.jfree.data.general.PieDataset",0);
		generalMap.put("org.jfree.data.general.SubSeriesDataset",0);
		generalMap.put("org.jfree.data.general.AbstractDataset",0);
		generalMap.put("org.jfree.data.general.KeyedValuesDataset",0);
		generalMap.put("org.jfree.data.general.DefaultHeatMapDataset",0);
		generalMap.put("org.jfree.data.general.DefaultKeyedValues2DDataset",0);
		generalMap.put("org.jfree.data.general.SeriesChangeEvent",0);
		generalMap.put("org.jfree.data.general.WaferMapDataset",0);
		generalMap.put("org.jfree.data.general.DefaultKeyedValuesDataset",0);
		generalMap.put("org.jfree.data.general.ValueDataset",0);
		generalMap.put("org.jfree.data.general.CombinedDataset",0);
		generalMap.put("org.jfree.data.general.CombinedDataset.DatasetInfo",0);
		generalMap.put("org.jfree.data.general.SeriesChangeListener",0);
		generalMap.put("org.jfree.data.general.Series",0);
		generalMap.put("org.jfree.data.general.AbstractSeriesDataset",0);
		generalMap.put("org.jfree.data.general.CombinationDataset",0);
		generalMap.put("org.jfree.data.general.DefaultValueDataset",0);
		generalMap.put("org.jfree.data.general.DatasetGroup",0);
		generalMap.put("org.jfree.data.general.SeriesDataset",0);
		generalMap.put("org.jfree.data.general.HeatMapUtilities",0);
		generalMap.put("org.jfree.data.general.DatasetChangeListener",0);
		generalMap.put("org.jfree.data.io.CSV",0);
		generalMap.put("org.jfree.data.jdbc.JDBCPieDataset",0);
		generalMap.put("org.jfree.data.jdbc.JDBCCategoryDataset",0);
		generalMap.put("org.jfree.data.jdbc.JDBCXYDataset",0);
		generalMap.put("org.jfree.data.resources.DataPackageResources_fr",0);
		generalMap.put("org.jfree.data.resources.DataPackageResources",0);
		generalMap.put("org.jfree.data.resources.DataPackageResources_es",0);
		generalMap.put("org.jfree.data.resources.DataPackageResources_de",0);
		generalMap.put("org.jfree.data.resources.DataPackageResources_pl",0);
		generalMap.put("org.jfree.data.resources.DataPackageResources_ru",0);
		generalMap.put("org.jfree.data.statistics.MultiValueCategoryDataset",0);
		generalMap.put("org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset",0);
		generalMap.put("org.jfree.data.statistics.HistogramDataset",0);
		generalMap.put("org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset",0);
		generalMap.put("org.jfree.data.statistics.DefaultStatisticalCategoryDataset",0);
		generalMap.put("org.jfree.data.statistics.SimpleHistogramBin",0);
		generalMap.put("org.jfree.data.statistics.HistogramType",0);
		generalMap.put("org.jfree.data.statistics.StatisticalCategoryDataset",0);
		generalMap.put("org.jfree.data.statistics.DefaultMultiValueCategoryDataset",0);
		generalMap.put("org.jfree.data.statistics.HistogramBin",0);
		generalMap.put("org.jfree.data.statistics.SimpleHistogramDataset",0);
		generalMap.put("org.jfree.data.statistics.BoxAndWhiskerItem",0);
		generalMap.put("org.jfree.data.statistics.Regression",0);
		generalMap.put("org.jfree.data.statistics.BoxAndWhiskerCalculator",0);
		generalMap.put("org.jfree.data.statistics.Statistics",0);
		generalMap.put("org.jfree.data.statistics.MeanAndStandardDeviation",0);
		generalMap.put("org.jfree.data.statistics.BoxAndWhiskerCategoryDataset",0);
		generalMap.put("org.jfree.data.statistics.BoxAndWhiskerXYDataset",0);
		generalMap.put("org.jfree.data.time.Day",0);
		generalMap.put("org.jfree.data.time.TimeSeriesCollection",0);
		generalMap.put("org.jfree.data.time.MovingAverage",0);
		generalMap.put("org.jfree.data.time.Month",0);
		generalMap.put("org.jfree.data.time.TimePeriodValuesCollection",0);
		generalMap.put("org.jfree.data.time.TimeSeriesDataItem",0);
		generalMap.put("org.jfree.data.time.FixedMillisecond",0);
		generalMap.put("org.jfree.data.time.TimeSeries",0);
		generalMap.put("org.jfree.data.time.Week",0);
		generalMap.put("org.jfree.data.time.Millisecond",0);
		generalMap.put("org.jfree.data.time.SimpleTimePeriod",0);
		generalMap.put("org.jfree.data.time.DateRange",0);
		generalMap.put("org.jfree.data.time.RegularTimePeriod",0);
		generalMap.put("org.jfree.data.time.Second",0);
		generalMap.put("org.jfree.data.time.TimePeriodAnchor",0);
		generalMap.put("org.jfree.data.time.TimeSeriesTableModel",0);
		generalMap.put("org.jfree.data.time.TimePeriodValues",0);
		generalMap.put("org.jfree.data.time.TimeTableXYDataset",0);
		generalMap.put("org.jfree.data.time.TimePeriod",0);
		generalMap.put("org.jfree.data.time.Year",0);
		generalMap.put("org.jfree.data.time.Minute",0);
		generalMap.put("org.jfree.data.time.DynamicTimeSeriesCollection",0);
		generalMap.put("org.jfree.data.time.DynamicTimeSeriesCollection.ValueSequence",0);
		generalMap.put("org.jfree.data.time.TimePeriodValue",0);
		generalMap.put("org.jfree.data.time.Quarter",0);
		generalMap.put("org.jfree.data.time.Hour",0);
		generalMap.put("org.jfree.data.time.TimePeriodFormatException",0);
		generalMap.put("org.jfree.data.time.ohlc.OHLC",0);
		generalMap.put("org.jfree.data.time.ohlc.OHLCSeries",0);
		generalMap.put("org.jfree.data.time.ohlc.OHLCItem",0);
		generalMap.put("org.jfree.data.time.ohlc.OHLCSeriesCollection",0);
		generalMap.put("org.jfree.data.xml.CategorySeriesHandler",0);
		generalMap.put("org.jfree.data.xml.DatasetReader",0);
		generalMap.put("org.jfree.data.xml.RootHandler",0);
		generalMap.put("org.jfree.data.xml.DatasetTags",0);
		generalMap.put("org.jfree.data.xml.CategoryDatasetHandler",0);
		generalMap.put("org.jfree.data.xml.ItemHandler",0);
		generalMap.put("org.jfree.data.xml.KeyHandler",0);
		generalMap.put("org.jfree.data.xml.ValueHandler",0);
		generalMap.put("org.jfree.data.xml.PieDatasetHandler",0);
		generalMap.put("org.jfree.data.xy.XYDomainInfo",0);
		generalMap.put("org.jfree.data.xy.XYSeriesCollection",0);
		generalMap.put("org.jfree.data.xy.YisSymbolic",0);
		generalMap.put("org.jfree.data.xy.DefaultXYDataset",0);
		generalMap.put("org.jfree.data.xy.DefaultWindDataset",0);
		generalMap.put("org.jfree.data.xy.WindDataItem",0);
		generalMap.put("org.jfree.data.xy.XYBarDataset",0);
		generalMap.put("org.jfree.data.xy.XIntervalSeriesCollection",0);
		generalMap.put("org.jfree.data.xy.XYDataset",0);
		generalMap.put("org.jfree.data.xy.XIntervalSeries",0);
		generalMap.put("org.jfree.data.xy.XYIntervalSeries",0);
		generalMap.put("org.jfree.data.xy.XYIntervalSeriesCollection",0);
		generalMap.put("org.jfree.data.xy.OHLCDataItem",0);
		generalMap.put("org.jfree.data.xy.DefaultOHLCDataset",0);
		generalMap.put("org.jfree.data.xy.XYInterval",0);
		generalMap.put("org.jfree.data.xy.DefaultIntervalXYDataset",0);
		generalMap.put("org.jfree.data.xy.NormalizedMatrixSeries",0);
		generalMap.put("org.jfree.data.xy.MatrixSeriesCollection",0);
		generalMap.put("org.jfree.data.xy.YIntervalSeriesCollection",0);
		generalMap.put("org.jfree.data.xy.XYDatasetTableModel",0);
		generalMap.put("org.jfree.data.xy.XYIntervalDataItem",0);
		generalMap.put("org.jfree.data.xy.XYSeries",0);
		generalMap.put("org.jfree.data.xy.VectorSeriesCollection",0);
		generalMap.put("org.jfree.data.xy.XYCoordinate",0);
		generalMap.put("org.jfree.data.xy.VectorDataItem",0);
		generalMap.put("org.jfree.data.xy.DefaultHighLowDataset",0);
		generalMap.put("org.jfree.data.xy.MatrixSeries",0);
		generalMap.put("org.jfree.data.xy.AbstractXYZDataset",0);
		generalMap.put("org.jfree.data.xy.AbstractXYDataset",0);
		generalMap.put("org.jfree.data.xy.TableXYDataset",0);
		generalMap.put("org.jfree.data.xy.YInterval",0);
		generalMap.put("org.jfree.data.xy.IntervalXYDelegate",0);
		generalMap.put("org.jfree.data.xy.XIntervalDataItem",0);
		generalMap.put("org.jfree.data.xy.WindDataset",0);
		generalMap.put("org.jfree.data.xy.XYDataItem",0);
		generalMap.put("org.jfree.data.xy.YWithXInterval",0);
		generalMap.put("org.jfree.data.xy.IntervalXYDataset",0);
		generalMap.put("org.jfree.data.xy.XisSymbolic",0);
		generalMap.put("org.jfree.data.xy.YIntervalSeries",0);
		generalMap.put("org.jfree.data.xy.XYZDataset",0);
		generalMap.put("org.jfree.data.xy.YIntervalDataItem",0);
		generalMap.put("org.jfree.data.xy.CategoryTableXYDataset",0);
		generalMap.put("org.jfree.data.xy.OHLCDataset",0);
		generalMap.put("org.jfree.data.xy.AbstractIntervalXYDataset",0);
		generalMap.put("org.jfree.data.xy.Vector",0);
		generalMap.put("org.jfree.data.xy.VectorSeries",0);
		generalMap.put("org.jfree.data.xy.VectorXYDataset",0);
		generalMap.put("org.jfree.data.xy.XYRangeInfo",0);
		generalMap.put("org.jfree.data.xy.DefaultTableXYDataset",0);
		generalMap.put("org.jfree.data.xy.DefaultXYZDataset",0);
		generalMap.put("org.jfree.data.xy.IntervalXYZDataset",0);
		generalMap.put("org.jfree.chart.fx.demo.CrosshairOverlayFXDemo1",1);
		generalMap.put("org.jfree.chart.labels.IntervalXYToolTipGenerator",1);
		generalMap.put("org.jfree.data.json.JSONUtils",1);
		generalMap.put("org.jfree.data.json.impl.JSONValue",1);
		generalMap.put("org.jfree.data.json.impl.JSONArray",1);
		generalMap.put("org.jfree.data.json.impl.JSONObject",1);
		generalMap.put("org.jfree.data.json.impl.JSONStreamAware",1);
	}
	
	private static int[][] loadFromFile(String fileName) throws NumberFormatException, IOException{
		
		
		int[][] t1 = new int[7185][7185];
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("/Users/kavaler/"+fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = "";
		int row = 0;
		while((line = reader.readLine()) != null)
		{
		   String[] cols = line.split(","); //note that if you have used space as separator you have to split on " "
		   int col = 0;
		   for(String  c : cols)
		   {
		      t1[row][col] = Integer.parseInt(c);
		      col++;
		   }
		   row++;
		}
		reader.close();
		return t1;
	}
}


