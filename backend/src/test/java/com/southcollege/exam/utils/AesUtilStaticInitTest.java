package com.southcollege.exam.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 测试AesUtil的静态初始化问题
 * 模拟Spring容器初始化过程
 */
class AesUtilStaticInitTest {

    @BeforeEach
    void setUp() {
        // 在每个测试前重置静态变量，模拟全新的类加载
        try {
            java.lang.reflect.Field field = AesUtil.class.getDeclaredField("staticSecretKey");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testEncryptBeforeInitShouldUseDefaultKey() {
        // 模拟在@PostConstruct之前调用静态方法
        // 此时staticSecretKey应该为null，但代码中有默认值
        
        // 重置静态变量为null，模拟未初始化状态
        try {
            java.lang.reflect.Field field = AesUtil.class.getDeclaredField("staticSecretKey");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 此时secretKey字段应该已经通过@Value注入了默认值
        // 但由于static方法无法访问实例字段，我们需要检查实际行为
        
        // 实际上，如果staticSecretKey为null，createAes()会抛出NPE
        // 但我们看到代码中有默认值处理，让我们看看实际会发生什么
        
        String plainText = "test";
        try {
            String encrypted = AesUtil.encrypt(plainText);
            // 如果能成功加密，说明有默认值被使用了
            assertNotNull(encrypted);
            String decrypted = AesUtil.decrypt(encrypted);
            assertEquals(plainText, decrypted);
        } catch (NullPointerException e) {
            // 如果出现NPE，说明静态变量未正确初始化
            fail("AesUtil.encrypt() threw NPE, indicating staticSecretKey was not initialized: " + e.getMessage());
        }
    }

    @Test
    void testStaticSecretKeyIsNullBeforeInit() {
        // 验证在初始化前staticSecretKey确实为null
        try {
            java.lang.reflect.Field field = AesUtil.class.getDeclaredField("staticSecretKey");
            field.setAccessible(true);
            Object value = field.get(null);
            assertNull(value, "staticSecretKey should be null before @PostConstruct");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testAfterManualInitStaticSecretKeyIsSet() throws Exception {
        // 手动初始化后验证staticSecretKey被设置
        AesUtil aesUtil = new AesUtil();
        
        // 设置secretKey（模拟@Value注入）
        java.lang.reflect.Field secretKeyField = AesUtil.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(aesUtil, "test-key-12345678"); // 16字节
        
        // 调用init方法
        java.lang.reflect.Method initMethod = AesUtil.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(aesUtil);
        
        // 检查静态变量
        java.lang.reflect.Field staticField = AesUtil.class.getDeclaredField("staticSecretKey");
        staticField.setAccessible(true);
        Object staticValue = staticField.get(null);
        assertEquals("test-key-12345678", staticValue);
    }
}