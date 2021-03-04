package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Main {

	public static void main(String[] args) {
		try {
			BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
			StringTokenizer stk = new StringTokenizer(bf.readLine());
			int maxWeight = Integer.parseInt(stk.nextToken());
			stk = new StringTokenizer(bf.readLine());
			int packages = Integer.parseInt(stk.nextToken());

			int numLocs = packages + 1;
			int[] minDists = new int[numLocs];
			int[] weights = new int[numLocs];
			int[][] locations = new int[numLocs][2];
			int[] distsToOffice = new int[numLocs];
			int[] distToNext = new int[numLocs];

			for (int i = 0; i < packages; i++) {
				stk = new StringTokenizer(bf.readLine());
				int x = Integer.parseInt(stk.nextToken());
				int y = Integer.parseInt(stk.nextToken());
				weights[]
			}

		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
