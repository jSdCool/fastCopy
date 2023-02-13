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
				String[] newDir=(Main.destination+"/"+toCopy.get(0)).split("\\\\|/");
				String destDir="";
				for(int i=0;i<newDir.length-1;i++) {//get the path to the current file 
					destDir+=newDir[i]+"/";
				}
				copy(0,newDir,destDir,null);
				Main.completed++;//increase the number of of coppies completed
				toCopy.remove(0);


			}else {
				if(endReaddy)//if there are not more files that need to be coppied then kill the thread
					return;
			}
			if(working&&toCopy.size()==0)
				working=false;
		}

	}
	private void copy(int times,String newDir[],String destDir,IOException ie) {
		if(times>=50) {
			Main.errorMessages.add("faied to copy file: "+toCopy.get(0));
			StackTraceElement[] eelements = ie.getStackTrace();
			String sstack="";
			for(int eele=0;eele<eelements.length;eele++){
				sstack+=eelements[eele].toString()+"\n";
			}//save the stactrace for later use
			Main.stackTraces.add(toCopy.get(0)+"\n"+ie.toString()+"\n"+sstack);
			return;
		}
		try {


			new File(destDir).mkdirs();//make the parent folder if it dosen't exist
			File dest=new File(Main.destination+"/"+toCopy.get(0));
			if(dest.exists()) {//if the file already exists in the new location then delete the current version
				dest.delete();
			}
			java.nio.file.Files.copy(new File(Main.source+"/"+toCopy.get(0)).toPath(),dest.toPath());//copy the file `
		} catch (IOException e) {//if it fails
			Main.errors++;//Increase the number of errors that have been encounter
			//System.exit(-1);
			//compile the stacktrace into a string 
			copy(times+1,newDir,destDir,ie);//try to copy the file again
			//save that string into an array list for later use

		}
	}
}
