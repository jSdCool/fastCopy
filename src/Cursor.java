
public class Cursor {
	public static String coursorUp(int n) {
		return "\033["+n+"A";
	}
	public static String coursordown(int n) {
		return "\033["+n+"B";
	}
	public static String coursorLeft(int n) {
		return "\033["+n+"D";
	}
	public static String coursorRight(int n) {
		return "\033["+n+"C";
	}
	public static String coursorPos(int x,int y) {
		return "\033["+x+";"+y+"H";
	}
	
	public static String cls() {
		return "\033[2J";
	}
	public static String eraseLine() {
		return "\033[K";
	}
}
