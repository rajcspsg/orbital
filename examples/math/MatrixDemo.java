

import orbital.math.*;

public class MatrixDemo {
	public static void main(String arg[]) throws Exception {
		double ms[][] = {
			{2,1,0,-2},
			{1,2,4,1},
			{-2,1,2,-2},
			{-3,0,1,-4}};
		double vs[] = {
			1, 2, 1, 2
		};
		double us[] = {
			2, 1, 0, -3
		};
		Matrix M = Values.valueOf(ms);
		Vector v = Values.valueOf(vs);
		Vector u = Values.valueOf(us);
		System.out.println(M + "*" + v + "=" + M.multiply(v));
		System.out.println(u + "*" + v + "=" + u.multiply(v));
		System.out.println(v + "*" + 2 + "=" + v.multiply(Values.valueOf(2)));
		System.out.println("M^-1=" + M.inverse());
		System.out.println("Type examination Matrix N to multiply with");
		String n = "";
		while (true) {
			int ch = System.in.read();
			if (ch == -1 || ch == 0x1b)
				break;
			n += (char) ch;
		} 
		Matrix N = (Matrix) Values.valueOf(n);
		System.out.println("||N||=" + N.norm());
		System.out.println("|N|=" + N.det());
		System.out.println("Tr N=" + N.trace());
		System.out.print(M + "\n*\n" + N);
		System.out.println("=" + M.multiply(N));
		System.out.println("N^-1=" + N.inverse());
	} 
}