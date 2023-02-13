import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * @author jSdCool
 *
 */
public class Main {
	static ArrayList<String> fileIndex=new ArrayList<>(),stackTraces=new ArrayList<>(),errorMessages=new ArrayList<>();
	static String source,destination;
	static int completed=0,total=0,numOfThreads=4,batchSize=10,logLevel=2,errors=0;
	static ArrayList<CopyThread> threads=new ArrayList<>();
	static boolean printMode80=false;
	public static void main(String[] args) {
		//deal with the args
		if(args.length<2) {//if you have no provided file paths
			if(args.length==1&&args[0].equals("help")) {//if you ran the help commmand
				System.out.println("fastCopy.jar source destination [OPTIONS]\n\nsource: the file path of the folder that is to be coppied\ndestination: the file path where the folder is to be coppied\n\nOPTIONS\nb= set the copy batch size (default = 10)\nt= set the number of threads to use (default = 4)\nl= set the log level between 0 and 3 (default = 2)\npm= set the print mode. options: 80 (default = unknown)");
				return;
			}
			System.out.println("invalid parameters. use fastCopy.jar help for more info");
			return;
		}
		//set the source and destination based on passed in args
		source=args[0];
		destination=args[1];
		for(int i=2;i<args.length;i++) {//load any other args passed in 
			if(args[i].startsWith("b=")) {//batch size
				batchSize=Integer.parseInt(args[i].substring(2,args[i].length()));
			}
			if(args[i].startsWith("t=")) {//number of threads
				numOfThreads=Integer.parseInt(args[i].substring(2,args[i].length()));
			}
			if(args[i].startsWith("l=")) {//log level
				logLevel=Integer.parseInt(args[i].substring(2,args[i].length()));
			}
			if(args[i].startsWith("pm=")) {
				printMode80=args[i].substring(3,args[i].length()).equals("80");
			}
		}
		printBuffer=new String[numOfThreads];
		for(int i=0;i<numOfThreads;i++) {
			printBuffer[i]="";
		}

		long programStart = System.nanoTime();//note the program start time 
		if(logLevel>=1)
			System.out.println("scanning for files\n\n\n\n\n\n\n\n\n\n\n\n\n");
		scanForFiles(source,"");//Discover all the file that need to be copied
		total=fileIndex.size();//note how many file there are
		if(logLevel>=1)
			System.out.println("found "+total+" files\n\n\n\n\n\n\n\n\n\n\n\n");
		for(int i=0;i<numOfThreads;i++) {//create all the requested threads
			threads.add(new CopyThread());
			threads.get(i).start();
		}
		long copyStart = System.nanoTime();//note the time that copying starts 
		while(fileIndex.size()>0) {//while there are still more unassigned files that need to be copied 
			for(int i=0;i<threads.size();i++) {//check all the threads
				if(!threads.get(i).isAlive()) {//restart the thread if it died
					threads.set(i, new CopyThread());
					threads.get(i).start();
					//System.out.println("repalced thread "+i);
				}
				if(!threads.get(i).working) {//if the thread needs more work to do then give it more work
					threads.get(i).toCopy=createNextJob();
					threads.get(i).working=true;
				}
			}
			if(logLevel>=2)
				printStatus();//print out the current progess status
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(int i=0;i<threads.size();i++) {//tell all threads that there will be no more work once they finish
			threads.get(i).endReaddy=true;
		}
		while(threadsRunning()) {//wait for all the threads to finish copying files
			if(logLevel>=2)
				printStatus();
		}
		long programEndTime=System.nanoTime();//note the time at witch the copying finished
		long totalTime=(programEndTime-programStart)/1000000,indexTime=(copyStart-programStart)/1000000,copyTime=(programEndTime-copyStart)/1000000;//calculates the time things took
		System.out.println("\n\ncoppied "+total+" files with "+errors+" errors\ntotal time taken: "+totalTime+"ms index time: "+indexTime+"ms copy time: "+copyTime+"ms");
		String totalError="";
		//save all error that were encountered to a file
		if(stackTraces.size()>0) {
			for(int i=0;i<stackTraces.size();i++) {
				totalError+=stackTraces.get(i)+"\n\n";
			}
			FileWriter wr;
			try {
				wr = new FileWriter("error.txt");
				wr.write(totalError);
				wr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void printStatus() {
		//System.out.println(Cursor.eraseLine()+"======================================");
		/*while(errorMessages.size()>0){
			System.out.print(Cursor.eraseLine());
			System.out.println(errorMessages.get(0));
			errorMessages.remove(0);
		}*/
		for(int i=0;i<threads.size();i++) {
			System.out.print(Cursor.eraseLine());
			if(threads.get(i).toCopy.size()!=0) {
				try {
					formatPrint(threads.get(i).toCopy.get(0));
				}catch(IndexOutOfBoundsException e) {
					formatPrint("idle");
				}
			}else {
				formatPrint("done");
			}
		}
		formatedPrintFlush(true);
	}

	/**Recursively scan folders for files to copy
	 * 
	 * @param parentPath the root path of the folder that is being copied 
	 * @param subPath the path of the current sub folder that is being looked through
	 */
	public static void scanForFiles(String parentPath,String subPath) {
		String[] files=new File(parentPath+"/"+subPath).list();//get a list of all things in the current folder
		for(int i=0;i<files.length;i++) {//loop through all the things in the current folder

			if(new File(parentPath+"/"+subPath+"/"+files[i]).list()!=null) {//check weather the current thing is a folder or a file 
				scanForFiles(parentPath,subPath+"/"+files[i]);//if it is a folder then scan through that folder for more files

			}else {//if it is a file
				//add this file to the to copy index
				if(subPath.equals("")) {
					fileIndex.add(files[i]);
				}else {
					fileIndex.add(subPath+"/"+files[i]);
				}
				if(logLevel>=3)
					formatPrint(fileIndex.get(fileIndex.size()-1));
				formatedPrintFlush(false);
			}
		}

	}

	/**gets a list of files that need to be copied to send to a thread
	 * 
	 * @return an array list of file paths that need to be copied
	 */
	static ArrayList<String> createNextJob(){
		ArrayList<String> batch=new ArrayList<>();
		for(int i=0;i<batchSize&&fileIndex.size()>0;i++) {//use the batch size to determine the number of items to send to each thread
			batch.add(fileIndex.remove(0));
		}

		return batch;

	}

	/**
	 * 
	 * @return weather any thread is sill running 
	 */
	static boolean threadsRunning() {
		for(int i=0;i<threads.size();i++) {
			if(threads.get(i).isAlive())
				return true;
		}
		return false;
	}

	static String[] printBuffer={"","","",""};
	static int prevNumOfPrintLines=0;
	static void formatPrint(String s) {			
		//shift the print buffer
		for(int i=printBuffer.length-1;i>0;i--) {
			printBuffer[i]=printBuffer[i-1];
		}
		printBuffer[0]=s;

	}
	static void formatedPrintFlush(boolean dispPercent) {

		if(printMode80) {//figure out how many line will be printed
			int numLines=0;
			for(int i=0;i<printBuffer.length;i++) {
				numLines+=printBuffer[i].length()/80+1;	
			}
			//erase previous lines that may have text on them and move to the new position
			if(prevNumOfPrintLines>numLines) {
				System.out.print(Cursor.coursorUp(prevNumOfPrintLines+((dispPercent)?1:0)));
				for(int i=0;i<prevNumOfPrintLines-numLines;i++) {
					System.out.print(Cursor.eraseLine()+Cursor.coursordown(1)+"\r");
				}
			}else {
				System.out.print(Cursor.coursorUp(numLines));
			}
			//print the info in the buffer
			for(int i=printBuffer.length-1;i>=0;i--) {
				if(printBuffer[i].length()>80) {
					int leftIndex=0;
					while(leftIndex<printBuffer[i].length()) {
						if(leftIndex+80>=printBuffer[i].length()) {
							System.out.println(printBuffer[i].substring(leftIndex));
						}else {
							System.out.println(printBuffer[i].substring(leftIndex,leftIndex+80));
						}
						leftIndex+=80;
					}
				}else
					System.out.println(Cursor.eraseLine()+printBuffer[i]);
			}
			prevNumOfPrintLines=numLines;
		}else {
			System.out.println(Cursor.eraseLine()+printBuffer[0]);
		}

		//print the percentage complete with a loading boar and the number of errors
		if(dispPercent) {
			double precent=((int)((completed*0.1/total)*10000))/10.0;
			System.out.print(precent+"% [");
			for(int i=0;i<50;i++) {
				if(((i+1)*1.0/50)*100<=precent) {
					System.out.print("=");
				}else {
					System.out.print(" ");
				}
			}
			System.out.print("] "+errorMessages.size()+" errors\r");
		}
	}

}
