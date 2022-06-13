import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
/**the thread class responsible for actually copying the files
 * 
 * @author jSdCool
 *
 */
public class CopyThread extends Thread{
static int numOfinstances=0;
int threadNumber;
	CopyThread(){
		super("copying Thread "+(numOfinstances+1));
		threadNumber=numOfinstances+1;
		numOfinstances++;
	}
ArrayList<String> toCopy=new ArrayList<>();
boolean shouldRun=true,working=false,endReaddy=false;
	public void run() {
		while(shouldRun) {
			Math.random();//prevent this thread from being put to sleep for being "inactive"
			if(toCopy.size()>0) {
				
				try {
					String[] newDir=(Main.destination+"/"+toCopy.get(0)).split("\\\\|/");
					String destDir="";
					for(int i=0;i<newDir.length-1;i++) {//get the path to the current file 
						destDir+=newDir[i]+"/";
					}
					new File(destDir).mkdirs();//make the parent folder if it dosen't exist
					File dest=new File(Main.destination+"/"+toCopy.get(0));
					if(dest.exists()) {//if the file already exists in the new location then delete the current version
						dest.delete();
					}
					java.nio.file.Files.copy(new File(Main.source+"/"+toCopy.get(0)).toPath(),dest.toPath());//copy the file `
				} catch (IOException e) {//if it fails
					e.printStackTrace();//print the stactrace
					System.out.println(toCopy.get(0));
					Main.errors++;//Increase the number of errors that have been encounter
					//System.exit(-1);
					//compile the stacktrace into a string 
					StackTraceElement[] elements = e.getStackTrace();
					 String stack="";
					 for(int ele=0;ele<elements.length;ele++){
					    stack+=elements[ele].toString()+"\n";
					 }
					 //save that string into an array list for later use
					 Main.stackTraces.add(toCopy.get(0)+"\n"+e.toString()+"\n"+stack);
					 try {//try to copy it again
							File dest=new File(Main.destination+"/"+toCopy.get(0));
							if(dest.exists()) {//if it already exists delete it 
								dest.delete();
							}
							java.nio.file.Files.copy(new File(Main.source+"/"+toCopy.get(0)).toPath(),dest.toPath());//copy the file
						} catch (IOException ee) {//if it fails again
							ee.printStackTrace();
							System.out.println(toCopy.get(0));
							Main.errors++;//increase the errors
							//System.exit(-1);
							StackTraceElement[] eelements = ee.getStackTrace();
							 String sstack="";
							 for(int eele=0;eele<eelements.length;eele++){
							    sstack+=eelements[eele].toString()+"\n";
							 }//save the stactrace for later use
							 Main.stackTraces.add(toCopy.get(0)+"\n"+ee.toString()+"\n"+sstack);
						}
				}
				Main.completed++;//increase the number of of coppies completed
				double precent=((int)((Main.completed*0.1/Main.total)*10000))/10.0;//calculate the completion percent 
				if(Main.logLevel>=2)
				System.out.println("(%"+precent+") "+Main.source+"/"+toCopy.get(0)+" >>>> "+Main.destination+"/"+toCopy.get(0));
				toCopy.remove(0);
				
				
			}else {
				if(endReaddy)//if there are not more files that need to be coppied then kill the thread
					return;
			}
			if(working&&toCopy.size()==0)
				working=false;
		}
	
	}
}
