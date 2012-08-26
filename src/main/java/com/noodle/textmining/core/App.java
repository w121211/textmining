package com.noodle.textmining.core;

import java.util.HashMap;
import java.util.Map;

public class App {
	
	public static void main(String[] args) {
		
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("A", 1.0/3);
		map.put("B", 1.0/3);
		map.put("C", 1.0/3);
		
		int iteration = 100;
		while (--iteration >= 0) {
			double a = map.get("A") * 0.1 + map.get("B") * 0.1 + map.get("C") * 0.2;
			double b = map.get("A") * 0.45 + map.get("B") * 0.9;
			double c = map.get("A") * 0.45 + map.get("C") * 0.8;
			map.put("A", a);
			map.put("B", b);
			map.put("C", c);
			System.out.println(map);
		}
		System.out.println(map);
		
	}
	
}