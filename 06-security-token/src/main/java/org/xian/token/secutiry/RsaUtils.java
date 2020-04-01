package org.xian.token.secutiry;


import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 私钥与公钥
 *
 * @author xian
 */
public class RsaUtils {

    /**
     * PrivateKey * 生成秘钥 > openssl genrsa -out rsa_private_key.pem 2048
     * 转换成PKCS8格式 > openssl pkcs8 -topk8 -inform * PEM -in rsa_private_key.pem -outform PEM -nocrypt
     * 在终端输出结果，去掉“-----BEGIN PRIVATE KEY-----” * “-----END PRIVATE KEY-----”
     *
     * @return PrivateKey
     */
    public static PrivateKey getPrivateKey() {
        try {
            String privateKeyStr = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCtMiN7Vitzs8FM" +
                    "SHoaxIiIvj3wrlce5ckNO+ZiXWox8QiDmpD08m51CSGrJsrDSzWebVH7ICcyjvSz" +
                    "RuRCrHzmyE4dX3isaXdz0uqDhABHTRUxcHGFopEv4ChbFp5y0eCQB5NMcw0x5erA" +
                    "efDaggLRJwvw1puyjVmSGmA2AQZvXSzH4ygZr6CtKbzrRpee8JsH18pcp/RGvzho" +
                    "/Rme6AT8AxXWdnNOCnyFN20QlCyx+i1wL+NY98qa0Wi8b72uuN0H0P9tGZUO7xTZ" +
                    "le6tI3s57bk1SAELRdAxxIxFMf4UcKsQO4WOyamtfGOJtDvwgxARtTkcGaDmm4W0" +
                    "fq5RoJ71AgMBAAECggEAInCd7aCs800DWw/ud/Qs0kRCPOHuIQ7AdAk9PsIko9my" +
                    "P7k0qgCrRkqSj14fY8ZI20AR1UYHhV43+o5WRYvXKm0aW2zoqiAuyLFgQNa213bU" +
                    "iAg8Cr7Q8Bw66iINT/e8F+fpuoIGVa3KiIu3ATxpIv4IudNzEoZZEVx9VT/O78Xb" +
                    "ol9mkW7xt+rpdFJDj77gEEYK7FJnAFRRnREyF+xv4VjqRSLAsidFxVla6JvUSsEW" +
                    "ENsnbHEKBvP7Mn6bbRNV++G7cml9jNByeAz8VhIef6zEOWWvVJC67XMwMPtWLtTX" +
                    "837CkmWM7LFQMTksNhMwNmfZdaFLYMj8xPf2vAHR4QKBgQDYzDRigN+yVJ1z8Cly" +
                    "EkTeKymK1a+SefnVxXJCUwo0Q056B/PmJ+LodmbrpKHHXae/V3SfvF2GIRgYSK0j" +
                    "X4rUwflr5yWxBHflPAQpUNNsyYjPhJd8VkUU3/p5vbY0xMxCT9GZvdqzkp4So+S5" +
                    "PueUjAVK3UJPfJs2ou2wX3vQ3wKBgQDMg48qeM1azYj1O/HGbuvb/qmgwfRjUVd/" +
                    "hwrvogOxVa8xxILASLqEQH3Wt2eI35weObWZ5VrO7Ry/vtfb7SQ6k6NCCYx8Wqb5" +
                    "Y28chQubDfQAWylai9P0FkQRgSDP3m8AZXLCdmllUwGURBMi2Je+mDra9yA3cXVv" +
                    "UymrrXUmqwKBgQDYBuNMJE7DTD15AZ7K5BlrqGbEyRkiRBkHQrAQFt7cJRVV1Abd" +
                    "GHGqYhE8TdhG6LP2nha/9NrchK2VxRWBYfrc9kyrkLCmK2WtLpiW8ss6Y7fH0L+C" +
                    "ThSYMHdOSBP/kPpYOWEKaXw746VPvbB0RpJMGxpK9x7URb4EO2XNkde20QKBgQCZ" +
                    "x4bcWc+YKpqERBnNqtRBLOFh1tWr5JsVcna/r6R1MMe2dVwXt9g/37p/vJLmcbWT" +
                    "aLNrz/ETTWnjqF3pqgwm26LT4hg33XZWafU/ja8nXTuPFP0ob6HllOSEAQOqKkhv" +
                    "FNlQ7fbcUKkMUo/NEO4xS4hd45re9bpnBjJ8O8auVwKBgQDC88ledelQdXvk+h+V" +
                    "imGpA/M/iKNMlZh31yTKCFWquAhcbxoxeidSZJivSddHba7XNBEuQjsuee3LzNnL" +
                    "p1ZPikRyDWzmMSaclB4+fgvFJvtR1gGDZbOP6fTCL5YmcF7gC36o+oMTBJHK6MFX" +
                    "mkhVZXbOx8/ikNP34wBdih2I9A==";
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * PublicKey 根据 秘钥 生成public key > openssl rsa -in rsa_private_key.pem -out rsa_public_key.pem -pubout
     *
     * @return PublicKey
     */
    public static PublicKey getPublicKey() {
        PublicKey publicKey = null;
        try {
            String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArTIje1Yrc7PBTEh6GsSI" +
                    "iL498K5XHuXJDTvmYl1qMfEIg5qQ9PJudQkhqybKw0s1nm1R+yAnMo70s0bkQqx8" +
                    "5shOHV94rGl3c9Lqg4QAR00VMXBxhaKRL+AoWxaectHgkAeTTHMNMeXqwHnw2oIC" +
                    "0ScL8Nabso1ZkhpgNgEGb10sx+MoGa+grSm860aXnvCbB9fKXKf0Rr84aP0ZnugE" +
                    "/AMV1nZzTgp8hTdtEJQssfotcC/jWPfKmtFovG+9rrjdB9D/bRmVDu8U2ZXurSN7" +
                    "Oe25NUgBC0XQMcSMRTH+FHCrEDuFjsmprXxjibQ78IMQEbU5HBmg5puFtH6uUaCe" +
                    "9QIDAQAB";
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyStr));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }
}

