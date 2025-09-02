package com.xyz.service;

import com.xyz.user.AppUser;

import java.util.List;

/**
 * <p>Package Name: com.xyz.service </p>
 * <p>Description: 令牌服务接口，定义了JWT（JSON Web Token）的生命周期管理操作，包括令牌的签发、解析、吊销和状态检查。这是实现JWT认证流程的核心契约。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public interface TokenService {

    /**
     * 表示一个已签发的令牌。
     *
     * @param accessToken      生成的JWT字符串。
     * @param expiresInSeconds 令牌的有效期，单位为秒。
     */
    record Issued(String accessToken, long expiresInSeconds) {}

    /**
     * 表示从JWT中解析出的元数据。
     *
     * @param jti          JWT的唯一标识符 (JWT ID)。
     * @param issuedAtMs   令牌的签发时间戳（毫秒）。
     * @param expireAtMs   令牌的过期时间戳（毫秒）。
     */
    record Meta(String jti, long issuedAtMs, long expireAtMs) {}

    /**
     * 为指定用户签发一个新的JWT。
     *
     * @param username 用户名，将作为JWT的 'sub' (subject) 声明。
     * @param role     用户的角色，将作为自定义声明存入JWT。
     * @param tenantId 租户ID（可选），可作为自定义声明存入JWT。
     * @return 返回一个包含访问令牌和有效期的 {@link Issued} 对象。
     * @throws Exception 如果令牌签发过程中发生错误。
     */
    Issued issueToken(String username, String role, String tenantId) throws Exception;

    /**
     * 解析JWT字符串，提取其元数据（如jti, iat, exp）。
     * 此方法会验证JWT的签名和时效性。
     * 控制器里在用：parseMeta(jwt).jti()/issuedAtMs()/expireAtMs()
     *
     * @param jwt 待解析的JWT字符串。
     * @return 返回一个包含JWT元数据的 {@link Meta} 对象。
     * @throws Exception 如果JWT无效（例如签名错误、已过期）。
     */
    Meta parseMeta(String jwt) throws Exception;

    /**
     * 吊销一个JWT。
     * 通常的实现方式是将JWT的jti添加到一个黑名单中（如Redis），并设置与原令牌相同的过期时间。
     *
     * @param jti 要吊销的JWT的唯一标识符。
     */
    void revoke(String jti);

    /**
     * 检查一个JWT是否已被吊销。
     *
     * @param jti 要检查的JWT的唯一标识符。
     * @return 如果令牌已被吊销（存在于黑名单中），则返回 true；否则返回 false。
     */
    boolean isRevoked(String jti);

    /**
     * 根据JWT的jti反向查找并获取对应的用户信息。
     * 这通常需要一个缓存机制（如Redis），在签发令牌时将 jti -> AppUser 的映射关系存入。
     *
     * @param jti JWT的唯一标识符。
     * @return 关联的 {@link AppUser} 对象。
     */
    AppUser parseJtiToUser(String jti);

    /**
     * 根据JWT的jti反向查找并获取对应的用户角色信息。
     *
     * @param jti JWT的唯一标识符。
     * @return 关联的用户角色字符串。
     */
    String parseJtiToRole(String jti);
}