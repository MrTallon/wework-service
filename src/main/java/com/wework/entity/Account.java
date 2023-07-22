package com.wework.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    /**
     * 现在默认只有一个企业微信帐号，所有的操作都是在该默认帐号下进行
     */
    public static final String DEFAULT_WEWORK_ACCOUNT = "YB";

    private Long id;

    /**
     * 企业微信帐号名称
     */
    private String name;

    /**
     * 帐号编号，由我们自己定义用来标识一个企业微信帐号
     */
    private String code;

    /**
     * 企业id，每个企业都拥有唯一的corpid
     */
    private String corpid;

    /**
     * 每个帐号在企业配置的签名token，将根据它来校验企业微信的回调请求
     * XXX:如果agent已配置该值，则使用agent的，否则使用account的配置
     */
    private String signToken;

    /**
     * 每个帐号在配置的加解密encodingaeskey，将根据它来解密回调内容，该值必填，但实际可能用明文请求而不会用到
     * XXX:如果agent已配置该值，则使用agent的，否则使用account的配置
     */
    private String aesKey;

    /**
     * 标识account已开启的功能，例如是否需要同步员工、是否处理外部联系了事件等
     * 使用bit位记录，每一个bit都表示一个功能的开关，详见{@link FeatureType}
     */
    private Long feature;

    /**
     * 描述
     */
    private String description;
}
