package com.playlet.internal.utils;


import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * 带随机后缀的绝对唯一邀请码生成器
 */
public class RandomSuffixInviteCodeUtil {
    
    private static final char[] BASE_CHARS = {
        '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R',
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };
    
    private static final int BASE = BASE_CHARS.length;
    private static final SecureRandom RANDOM = new SecureRandom();
    
    // 用于存储已生成的邀请码，避免冲突
    private static final Set<String> GENERATED_CODES = new HashSet<>();
    
    /**
     * 为同一ID生成不同邀请码
     * @param id 基础ID
     * @param randomBits 随机位数（建议2-4位）
     * @param totalLength 总长度
     * @return 唯一邀请码
     */
    public static String generateUniqueCode(long id, int randomBits, int totalLength) {
        if (randomBits < 1 || randomBits >= totalLength) {
            throw new IllegalArgumentException("随机位数必须大于0且小于总长度");
        }
        
        // 1. 将ID转换为固定部分
        String idPart = idToFixedPart(id, totalLength - randomBits);
        
        String code;
        int attempts = 0;
        int maxAttempts = 100; // 防止死循环
        
        do {
            // 2. 生成随机后缀
            String randomPart = generateRandomPart(randomBits);
            
            // 3. 组合
            code = idPart + randomPart;
            
            attempts++;
            if (attempts >= maxAttempts) {
                throw new RuntimeException("生成唯一邀请码失败，尝试次数过多");
            }
            
        } while (GENERATED_CODES.contains(code)); // 检查是否重复
        
        GENERATED_CODES.add(code);
        return code;
    }
    
    /**
     * ID转换为固定部分
     */
    private static String idToFixedPart(long id, int length) {
        StringBuilder part = new StringBuilder();
        
        // 转换为BASE进制
        long tempId = id;
        while (tempId > 0) {
            int remainder = (int) (tempId % BASE);
            part.insert(0, BASE_CHARS[remainder]);
            tempId /= BASE;
        }
        
        // 处理id为0的情况
        if (part.length() == 0) {
            part.append(BASE_CHARS[0]);
        }
        
        // 补全长度
        while (part.length() < length) {
            part.insert(0, BASE_CHARS[0]);
        }
        
        // 截断超长部分
        if (part.length() > length) {
            return part.substring(part.length() - length);
        }
        
        return part.toString();
    }
    
    /**
     * 生成随机部分
     */
    private static String generateRandomPart(int length) {
        StringBuilder part = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(BASE);
            part.append(BASE_CHARS[index]);
        }
        return part.toString();
    }
    
    /**
     * 清除已生成的记录（定期调用）
     */
    public static void clearGeneratedCodes() {
        GENERATED_CODES.clear();
    }
    
    /**
     * 批量生成唯一邀请码（同一ID多次调用）
     */
    public static String[] generateMultipleCodesForSameId(long id, int count, 
                                                         int randomBits, int totalLength) {
        String[] codes = new String[count];
        for (int i = 0; i < count; i++) {
            codes[i] = generateUniqueCode(id, randomBits, totalLength);
        }
        return codes;
    }
    
    public static void main(String[] args) {
    	System.out.println(RandomSuffixInviteCodeUtil.generateUniqueCode(1, 4, 6));
	}
}