package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Main {

    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            StringTokenizer stkn = new StringTokenizer(br.readLine());
            int height = Integer.parseInt(stkn.nextToken());
            int width = Integer.parseInt(stkn.nextToken());

            int[] combs = new int[height * width];
            int[] sizes = {1, 2, 3, 4, 6, 8, 10, 12, 16};

            int[] results = new int[16];

            results[0] = 1;

            int result = 1;

            for (int i = 0; i < height; i++) {
                char[] line = new char[width];
                br.read(line);
                br.read();

                char fst = line[0];
                int len = 1;
                for (int j = 1; j < width; j++) {
                    if(line[j] == '.')
                        continue;

                    if(line[j] != fst){

                        int subresult = 0;
                        if(results[len] != 0) {
                            subresult = results[len];
                        } else {
                            for (int k = sizes.length - 1; k >= 0; k--) {
                                if(sizes[k] < len)
                                    subresult += results[len - sizes[k] - 1];
                                else if(sizes[k] == len)
                                    subresult++;
                            }

                            results[len] = subresult;
                        }

                        result *= subresult;
                        len = 1;
                        fst = line[j];
                    } else {
                        len++;
                    }
                }
            }

            System.out.println(result);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
