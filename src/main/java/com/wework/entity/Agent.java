package com.wework.entity;

import cn.hutool.json.JSONObject;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Agent {
    /**
     * 默认企业微信外部联系人应用code，现在只有一个企业微信帐号，即只有这一个"外部联系人应用"
     */
    public static final String DEFAULT_EXTERNAL_CONTACT_AGENT_CODE = "yb_external";

    /**
     * 默认企业微信通讯录应用code，现在只有一个企业微信帐号，即只有这一个"通讯录"
     */
    public static final String DEFAULT_EMPLOYEE_AGENT_CODE = "yb_user";

    private Integer id;

    /**
     * 所属企业的corpid
     */
    private String corpid;

    /**
     * 企业微信应用名称
     */
    private String name;

    private AgentType type;

    /**
     * app编号，由我们自己定义用来标识一个企业app
     */
    private String code;

    /**
     * 管理后台->应用与小程序->应用，点进某个应用，即可看到agentid
     */
    private Integer agentid;

    /**
     * 每一个应用的独立访问密钥，为了保证数据的安全，secret务必不能泄漏
     */
    private String secret;

    /**
     * 每个应用回调使用的验签token
     */
    @Getter(AccessLevel.NONE)
    private String signToken;

    /**
     * 每个应用加解密回调内容时的key
     */
    @Getter(AccessLevel.NONE)
    private String aesKey;

    /**
     * 描述
     */
    private String description;

    /**
     * 扩展字段
     */
    private JSONObject ext;


    /**
     * 该应用关联的帐号，注意必须通过AccountAgentService的接口获取到的才会填充该值
     */
    private Account account;

    public String getSignToken() {
        if (account == null) {
            return signToken;
        }
        // 如果agent没有配置token，则使用account的
        return Strings.isNullOrEmpty(signToken) ? account.getSignToken() : signToken;
    }

    public String getAesKey() {
        if (account == null) {
            return aesKey;
        }
        // 如果agent没有配置aesKey，则使用account的
        return Strings.isNullOrEmpty(aesKey) ? account.getAesKey() : aesKey;
    }

    public enum AgentType {
        // 企业微信内置应用: 外部联系人应用
        BUILTIN_EXTERNAL,

        // 企业微信内置应用: 员工通讯录
        BUILTIN_EMPLOYEE,

        // 其他内置应用
        BUILTIN,

        // 自建应用，如robot
        CUSTOM,

        // 关联的小程序在企业微信也是作为一种应用
        MINI_APP,
        ;
    }
}
