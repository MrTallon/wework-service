package com.wework.service;


import java.util.List;
import org.springframework.util.CollectionUtils;
import cn.hutool.json.JSONObject;
import com.wework.entity.Agent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.chanjar.weixin.cp.api.WxCpService;

/**
 * 企微工具类
 */
public interface WeworkExternalService {

    /**
     * 目前只有一个企微账号
     */
    String ACCOUNT = "YB";

    /**
     * 获取wxCpService
     */
    WxCpService getWxCpService(String agentCode);

    /**
     * 获取客服列表
     */
    List<Agent> getAgents(String accountCode, Agent.AgentType agentType);

    /**
     * 创建非临时的客户[联系我]
     * 后续若需要创建临时会话的联系我（现只有医疗行业允许创建）
     */
    CreateContactWayResp createContactWay(CreateContactWayReq req);

    /**
     * 创建客户[联系我]方式
     * 注意：此处有50W条的限制
     */
    String createExternalContactWay(WxCpService wxCpService, CreateContactWayReq req);

    /**
     * 获取[联系我]配置二维码
     */
    String getExternalContactWay(WxCpService wxCpService, String configId);

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    class CreateContactWayReq {

        @Builder.Default
        private String accountCode = ACCOUNT;

        private String appId;

        @NotNull(message = "contactType不能为空")
        private ContactType contactType;

        @NotNull(message = "contactScene不能为空")
        private ContactScene contactScene;

        @Size(max = 100, message = "最多支持100个user")
        private List<String> weworkUserIds;

        @Size(max = 10, message = "最多支持10个部门")
        private List<String> weworkDepartIds;

        /**
         * 可选，联系方式的备注信息，用于助记，不超过30个字符
         */
        @Size(max = 30, message = "remark最多30字符")
        private String remark;

        /**
         * 可选，企业自定义的state参数，用于区分不同的添加渠道，在调用“获取外部联系人详情”时会返回该参数值，不超过30个字符
         */
        @Size(max = 30, message = "state最多30字符")
        private String state;

        /**
         * 外部客户添加时是否无需验证，默认为true
         */
        @Builder.Default
        private Boolean skipVerify = true;

        /**
         * 只在contactScene = MINI_PROGRAM 时有效
         * 在小程序中联系时使用的控件样式，详见附表
         * https://work.weixin.qq.com/api/doc/90000/90135/92572#%E9%99%84%E5%BD%95
         */
        @Builder.Default
        private Integer miniProgramStyle = 1;

        private JSONObject ext;

        public void check() {
            if (contactType == ContactType.SINGLE_USER) {
                if (CollectionUtils.isEmpty(weworkUserIds) || weworkUserIds.size() > 1) {
                    System.out.println("联系方式为单人时，weworkUserIds必须不为空且为1人");
                    // throw
                }

                if (!CollectionUtils.isEmpty(weworkDepartIds)) {
                    System.out.println("联系方式为单人时，不能指定weworkDepartIds");
                    // throw
                }
            }

            if (contactType == ContactType.MULTI_USER) {
                if (CollectionUtils.isEmpty(weworkUserIds) && CollectionUtils.isEmpty(weworkDepartIds)) {
                    System.out.println("联系人或联系部门为空");
                    // 异常处理
                }
            }
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    class CreateContactWayResp {

        /**
         * 该联系我的configId
         */
        private String configId;
        /**
         * 联系我二维码，只在contactScene=QR_CODE时有值
         */
        private String qrCodeUrl;
    }
}

/**
 * 微信公众号service信息对象
 */


@Getter
@AllArgsConstructor
enum ContactType {
    // 单人
    SINGLE_USER(1),
    // 多人
    MULTI_USER(2),
    ;

    /**
     * 企业微信要求传递的值
     */
    private Integer weworkTypeValue;
}

@Getter
@AllArgsConstructor
enum ContactScene {
    /**
     * 通过小程序联系
     */
    MINI_PROGRAM(1),

    /**
     * 通过二维码联系
     */
    QR_CODE(2),
    ;

    /**
     * 企业微信要求传递的值
     */
    private Integer weworkSceneValue;
}