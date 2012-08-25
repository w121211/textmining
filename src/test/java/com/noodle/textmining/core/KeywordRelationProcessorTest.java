package com.noodle.textmining.core;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import junit.framework.TestCase;

public class KeywordRelationProcessorTest extends TestCase {
	
	String[] docs = {
		"中鋁台股上半年大虧 巴克萊看淡",
		"MSCI季度調整後 台股先蹲後跳",
		"惡化嚴重！ 西班牙呆帳飆歷史新高 台股",
		"原定裁員5千 夏普擬增為1萬",
	};
	
	String[] terms = {
		"巴克萊",
		"台股",
		"夏普"
	};
	
	KeywordRelationProcessor tester;
	
	public KeywordRelationProcessorTest() throws FileNotFoundException, IOException {
		tester = new KeywordRelationProcessor();
	}
	
	@Test
	public void testGetRelationalMatrix() {
		double[][] ans = {
				
		};
		double[][] matrix = tester.getRelationMatrix(docs, terms); 
		tester.out(System.out, matrix, terms, terms);
//		assertEquals(ans, tester.getRelationMatrix(docs, terms));
	}
}
