import java.io.*;
import java.util.ArrayList;
public class ExternalSort {

	// readers and writers needed for the files
	private static FileReader _input; 
	private static BufferedReader _bufRead;
	private static FileReader _input2; 
	private static BufferedReader _bufRead2;
	private static FileWriter _output; 
	private static BufferedWriter _bufWrite;

	private static String identifierRegex = "[a-zA-Z]+\\w*";
	
	// the main() function
	public static void main(String[] args) 
	{
		String relation = "test"; // the code takes care of the .CSV
		String attribute = "B";
        externalSort(relation, attribute);
        System.out.println("The relation was sorted");

    }
	
	

	// returns an array of ints given an array of strings that should be ints
	private static Integer[] getIntsFromStringArray(String[] row)
	{
		return getIntsFromStringArray(row, false);
	}
	private static Integer[] getIntsFromStringArray(String[] row, boolean nullIsZero)
	{
		Integer [] rowAsInts = new Integer[row.length];
		for(int i=0; i<row.length; i++)
		{
			try
			{
				if (row[i].length()==0 & nullIsZero)
					row[i] = "0";
				rowAsInts[i]=Integer.parseInt(row[i]);	
			}
			catch (Exception ex)
			{
				 
				System.out.println("The was an error in the file. '" + row[i] +"' is not an integer." );
				ex.printStackTrace();
				System.exit(-1);
			}
		}
		return rowAsInts;
	}
	
		
	private static void externalSort(String relation, String attribute)
	{
		try
		{
			FileReader intialRelationInput = new FileReader(relation + ".csv"); 
			BufferedReader initRelationReader = new BufferedReader(intialRelationInput);
			String [] header = initRelationReader.readLine().split(",");
			String [] row = header;
			int indexToCompare = getIndexForColumn(header,attribute);
			ArrayList<Integer[]> tenKRows = new ArrayList<Integer[]>();
						
			int numFiles = 0;
			while (row!=null)
			{
				// get 10k rows
				for(int i=0; i<10000; i++)
				{
					String line = initRelationReader.readLine();
					if (line==null) 
					{
						row = null;
						break;
					}
					row = line.split(",");
					tenKRows.add(getIntsFromStringArray(row));
				}
				// sort the rows
				tenKRows = mergeSort(tenKRows, indexToCompare);
				
				// write to disk
				FileWriter fw = new FileWriter(relation + "_chunk" + numFiles + ".csv");
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(flattenArray(header,",")+"\n");
				for(int i=0; i<tenKRows.size(); i++)
				{
					bw.append(flattenArray(tenKRows.get(i),",")+"\n");
				}
				bw.close();
				numFiles++;
				tenKRows.clear();
			}
			
			mergeFiles(relation, numFiles, indexToCompare);
			
			
			initRelationReader.close();
			intialRelationInput.close();
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		
		
	}
	
	private static void mergeFiles(String relation, int numFiles, int compareIndex)
	{
		try
		{
			ArrayList<FileReader> mergefr = new ArrayList<FileReader>();
			ArrayList<BufferedReader> mergefbr = new ArrayList<BufferedReader>();
			ArrayList<Integer[]> filerows = new ArrayList<Integer[]>(); 
			FileWriter fw = new FileWriter(relation + "_sorted.csv");
			BufferedWriter bw = new BufferedWriter(fw);
			String [] header;
			
			boolean someFileStillHasRows = false;
			
			for (int i=0; i<numFiles; i++)
			{
				mergefr.add(new FileReader(relation+"_chunk"+i+".csv"));
				mergefbr.add(new BufferedReader(mergefr.get(i)));
				// get each one past the header
				header = mergefbr.get(i).readLine().split(",");
								
				if (i==0) bw.write(flattenArray(header,",")+"\n");
				
				// get the first row
				String line = mergefbr.get(i).readLine();
				if (line != null)
				{
					filerows.add(getIntsFromStringArray(line.split(",")));
					someFileStillHasRows = true;
				}
				else 
				{
					filerows.add(null);
				}
					
			}
			
			
			Integer[] row;
			while (someFileStillHasRows)
			{
				Integer min;
				int minIndex;
				
				row = filerows.get(0);
				if (row!=null) {
					min = row[compareIndex];
					minIndex = 0;
				}
				else {
					min = null;
					minIndex = -1;
				}
				
				// check which one is min
				for(int i=1; i<filerows.size(); i++)
				{
					row = filerows.get(i);
					if (min!=null) {
						
						if(row!=null && row[compareIndex] < min)
						{
							minIndex = i;
							min = filerows.get(i)[compareIndex];
						}
					}
					else
					{
						if(row!=null)
						{
							min = row[compareIndex];
							minIndex = i;
						}
					}
				}
				
				if (minIndex < 0) {
					someFileStillHasRows=false;
				}
				else
				{
					// write to the sorted file
					bw.append(flattenArray(filerows.get(minIndex),",")+"\n");
					
					// get another row from the file that had the min
					String line = mergefbr.get(minIndex).readLine();
					if (line != null)
					{
						filerows.set(minIndex,getIntsFromStringArray(line.split(",")));
					}
					else 
					{
						filerows.set(minIndex,null);
					}
				}								
				// check if one still has rows
				for(int i=0; i<filerows.size(); i++)
				{
					
					someFileStillHasRows = false;
					if(filerows.get(i)!=null) 
					{
						if (minIndex < 0) 
						{
							System.out.println("mindex lt 0 and found row not null" + flattenArray(filerows.get(i)," "));
							System.exit(-1);
						}
						someFileStillHasRows = true;
						break;
					}
				}
				
				// check the actual files one more time
				if (!someFileStillHasRows)
				{
					
					//write the last one not covered above
					for(int i=0; i<filerows.size(); i++)
					{
						if (filerows.get(i) == null)
						{
							String line = mergefbr.get(i).readLine();
							if (line!=null) 
							{
								
								someFileStillHasRows=true;
								filerows.set(i,getIntsFromStringArray(line.split(",")));
							}
						}
								
					}
				}
					
			}
			
			
			
			// close all the files
			bw.close();
			fw.close();
			for(int i=0; i<mergefbr.size(); i++)
				mergefbr.get(i).close();
			for(int i=0; i<mergefr.size(); i++)
				mergefr.get(i).close();
			
			
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	// sort an arrayList of arrays based on the ith column
	private static ArrayList<Integer[]> mergeSort(ArrayList<Integer[]> arr, int index)
	{
		ArrayList<Integer[]> left = new ArrayList<Integer[]>();
		ArrayList<Integer[]> right = new ArrayList<Integer[]>();
		if(arr.size()<=1)
			return arr;
		else
		{
			int middle = arr.size()/2;
			for (int i = 0; i<middle; i++)
				left.add(arr.get(i));
			for (int j = middle; j<arr.size(); j++)
				right.add(arr.get(j));
			left = mergeSort(left, index);
			right = mergeSort(right, index);
			return merge(left, right, index);
			
		}
		
	}
	
	// merge the the results for mergeSort back together 
	private static ArrayList<Integer[]> merge(ArrayList<Integer[]> left, ArrayList<Integer[]> right, int index)
	{
		ArrayList<Integer[]> result = new ArrayList<Integer[]>();
		while (left.size() > 0 && right.size() > 0)
		{
			if(left.get(0)[index] <= right.get(0)[index])
			{
				result.add(left.get(0));
				left.remove(0);
			}
			else
			{
				result.add(right.get(0));
				right.remove(0);
			}
		}
		if (left.size()>0) 
		{
			for(int i=0; i<left.size(); i++)
				result.add(left.get(i));
		}
		if (right.size()>0) 
		{
			for(int i=0; i<right.size(); i++)
				result.add(right.get(i));
		}
		return result;
	}
	
	
	
	
	
	private static void verifyHeader(String[] header)
	{
		for(int i=0; i<header.length; i++)
			if (!header[i].matches(identifierRegex))
			{
				System.out.println("One or more header columns is not a valid identifier.");
				System.out.println("The first problem encountered was: " + header[i]);
				//System.exit(-1);
			}
	}
	
	
	
	// getRow initializes the appropriate reader if it is not yet initialized, 
	// and reads the next line from the appropriate file.
	private static String[] getRow(String relation, int whichReader, String extension)
	{
		String line = "";
		FileReader f;
		BufferedReader b;
		
		String[] lineAsArray = null;
		try
		{
			if (whichReader==1)
			{
				if (_input == null)
				{
					_input = new FileReader(relation + extension);
					_bufRead = new BufferedReader(_input);
					//System.out.println(relation + " is null");
				}
				f=_input;
				b=_bufRead;
			}
			else
			{
				if (_input2 == null)
				{
					_input2 = new FileReader(relation + extension);
					_bufRead2 = new BufferedReader(_input2);
					//System.out.println(relation + " is null");
				}
				f=_input2;
				b=_bufRead2;
			}
			
			
			line = b.readLine();
			if (line!=null)
			{
				lineAsArray=line.split(",");
				lineAsArray=mergeQuotedCells(lineAsArray);
				
			}
			
		}
		catch (Exception ex)
		{
			System.out.println("A file with the name " + relation + extension + " could not be found.\n");
			ex.printStackTrace();
			System.exit(-1);
		}
						
		return lineAsArray;	
	}
	
	// just an alias to use for reading input for all left side relations
	public static String[] getRow(String relation)
	{
		return getRow(relation, 1, ".csv");
	}
	
	public static String[] getRow(String relation, String extension)
	{
		return getRow(relation, 1, extension);
	}
	public static String[] getRowInDifRelation(String relation, String extension)
	{
		return getRow(relation, 2, extension);
	}
	
	// just an alias to use for reading input for all right side relations
	public static String[] getRowInDifRelation(String relation)
	{
		return getRow(relation, 2, ".csv");
	}
	
	private static String[] mergeQuotedCells(String[] lineAsArray)
	{
		// check to see if a cell begins with a ", then merge it until we find one that ends in a "
		String[] mergingQuotedCells = new String[lineAsArray.length];
		for (int i=0; i<lineAsArray.length; i++)
		{
			String mergedCell = lineAsArray[i];
			if (lineAsArray[i].startsWith("\""))
			{
				
				int j=i+1;
				while (!lineAsArray[j].endsWith("\""))
				{
					mergedCell += "," + lineAsArray[j];
					j++;
				}
				mergedCell += "," + lineAsArray[j];
				i=j;
				//System.out.println(mergedCell);
			}
			mergingQuotedCells[i] = mergedCell;
		}
		
		lineAsArray=mergingQuotedCells.clone();
		int nonNullCnt = 0;
		for (int i=0; i<lineAsArray.length; i++)
		{
			if (lineAsArray[i]!=null)
				nonNullCnt++;
		}
		
		String[] nonNullLine = new String[nonNullCnt];
		int cnt = 0;
		for (int i=0; i<lineAsArray.length; i++)
		{
			if (lineAsArray[i]!=null)
			{
				// replace the first quote
				if(lineAsArray[i].startsWith("\""))
					lineAsArray[i]=lineAsArray[i].replaceFirst("\"","");
				// replace the last quote
				if(lineAsArray[i].endsWith("\""))
					lineAsArray[i]=lineAsArray[i].substring(0,lineAsArray[i].length()-1);
						
				nonNullLine[cnt] = lineAsArray[i];
				cnt++;
			}
		}
		return nonNullLine;
	}
	
	// putRow initializes an output file (if it has not yet been done) and prints a row to it
	public static void putRow(String relation, String row)
	{
		putRow(relation, row, ".csv");
	}
	public static void putRow(String relation, String row, String extension)
	{
			 
		try
		{
			if (_output == null)
			{
				_output = new FileWriter(relation + extension);
				_bufWrite = new BufferedWriter(_output);
				_bufWrite.write(row);
			}
			else
				_bufWrite.append(row);
			//System.out.println(row);
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
				
			
	}
	
	
	
	// just a utility function to turn arrays into strings with spaces between each element 
	private static String flattenArray(String[] arr, String delimiter)
	{
		String result = "";
		for (int i=0; i<arr.length; i++)
			result+=arr[i] + delimiter;
		
		if (result.endsWith(","))
			result=result.substring(0,result.length()-1);
		
		return result.trim();
	}
	
	//	 just a utility function to turn arrays into strings with spaces between each element 
	private static String flattenArray(Integer[] arr, String delimiter)
	{
		String result = "";
		for (int i=0; i<arr.length; i++)
			result+=arr[i] + delimiter;
		
		if (result.endsWith(","))
			result=result.substring(0,result.length()-1);
		
		return result.trim();
	}
	
	// closes the output and input files, since we don't do that for ease of programming
	protected void finalize() throws Throwable
	{
		try
		{
			_input.close();
			_bufRead.close();
			_input2.close();
			_bufRead2.close();
			_output.close();
			_bufWrite.close();
		} 
		finally 
		{
			super.finalize();
		}
	}
	
	
	
	// getIndexForColumn basically just finds a value in an array and returns the index where it 
	// found it.  If it doesn't find it, it returns -1.
	private static int getIndexForColumn(String[] arr, String val)
	{
		//find index to look at
		int index=-1;
		for (int i=0; i<arr.length; i++)
		{
			if (arr[i].equals(val))
			{
					index = i;
					break;
			}
			
		}
		return index;
	}
}
