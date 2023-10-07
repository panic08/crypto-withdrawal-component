package com.casino.replenishments.util;
import java.util.Random;
public class RandomNumberGeneratorUtil {
    public static int generateRandomNumber(int min, int max){
        Random random = new Random();

        return random.nextInt((max - min) + 1) + min;
    }
}
