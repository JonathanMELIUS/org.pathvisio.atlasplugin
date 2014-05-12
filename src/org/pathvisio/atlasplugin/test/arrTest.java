package org.pathvisio.atlasplugin.test;

import java.util.ArrayList;
import java.util.Arrays;

public class arrTest {

	public static void main(String[]args)  {
		String[] tt = new String[3];
		ArrayList<String> test = new ArrayList<String>(Arrays.asList(tt));
		for (String ss : test){
			
			System.out.println(ss);
		}
		System.out.println(tt);
	}
}
