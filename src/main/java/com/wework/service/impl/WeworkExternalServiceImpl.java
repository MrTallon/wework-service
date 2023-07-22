package com.wework.service.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wework.entity.Account;
import com.wework.entity.Agent;
import com.wework.entity.WxCpServiceInfo;
import com.wework.service.WeworkExternalService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;

@Service
@Slf4j
public class WeworkExternalServiceImpl implements WeworkExternalService {

    private static final String CREATE_EXTERNAL_CONTACT_WAY = "/cgi-bin/externalcontact/add_contact_way";

    private static final String GET_EXTERNAL_CONTACT_WAY = "/cgi-bin/externalcontact/get_contact_way";

    private static final String DEFAULT_CONFIG = "a05e714484231a490bba60e62789695f";
    private static final String DEFAULT_QR = "";

    @Override
    public WxCpService getWxCpService(String accountCode) {
        List<Agent> agents = getAgents(accountCode, Agent.AgentType.BUILTIN_EXTERNAL);
        if (agents.isEmpty()) {
            log.warn("account下没有外部联系人agent，accountCode：{}", accountCode);
            return null;
        }
        if (agents.size() > 1) {
            log.warn("一个企业微信下肯定只有一个员工通讯录应用");
            return null;
        }

        var agentCode = agents.get(0).getCode();

        Optional<WxCpServiceInfo> wxCpServiceInfoOpt = getWxCpServiceInfo(agentCode);
        return wxCpServiceInfoOpt.map(WxCpServiceInfo::getWxCpService).orElse(null);
    }

    @Override
    public List<Agent> getAgents(String accountCode, Agent.AgentType agentType) {
        return List.of();
    }

    @Override
    public CreateContactWayResp createContactWay(CreateContactWayReq req) {
        req.check();
        var wxCpService = getWxCpService(req.getAccountCode());
        if (ObjectUtil.isEmpty(wxCpService)) {
            return CreateContactWayResp.builder().configId(DEFAULT_CONFIG).qrCodeUrl(DEFAULT_QR).build();
        }
        try {
            // 创建并查询详情得到二维码链接
            var configId = createExternalContactWay(wxCpService, req);
            var qrCodeUrl = getExternalContactWay(wxCpService, configId);

            // TODO 落库
            return CreateContactWayResp.builder()
                .configId(configId)
                .qrCodeUrl(qrCodeUrl)
                .build();
        } catch (Exception e) {
            // 生产环境可能会出现以下三种异常，总之都需要落库处理
            // 1. 创建联系我失败
            // 2. 创建成功但是保存失败
            // 3. 其他未捕获的异常
            log.error(e.toString());
        }
        return null;
    }

    @Override
    public String createExternalContactWay(WxCpService wxCpService, CreateContactWayReq req) {
        String url = wxCpService.getWxCpConfigStorage().getApiUrl(CREATE_EXTERNAL_CONTACT_WAY);
        try {
            String responseContent = wxCpService.post(url, JSONUtil.toJsonStr(req));
            return JSONUtil.parseObj(responseContent).getStr("config_id");
        } catch (Exception e) {
            // 异常处理
            log.error(e.toString());
        }
        return null;
    }

    @Override
    public String getExternalContactWay(WxCpService wxCpService, String configId) {
        try {
            String url = wxCpService.getWxCpConfigStorage().getApiUrl(GET_EXTERNAL_CONTACT_WAY);
            String responseContent = wxCpService.post(url, new JSONObject().putOpt("config_id", configId).toString());
            return JSONUtil.parseObj(responseContent).getStr("qr_code");
        } catch (Exception e) {
            // 异常处理
            log.error(e.toString());
        }
        return null;
    }

    /**
     * 账号信息基本不会更改，只有新增，所以可以考虑使用缓存
     * 此处只做展示，不做缓存处理
     */
    private Optional<WxCpServiceInfo> getWxCpServiceInfo(String agentCode) {

        // TODO 一般是落库查询，此处直接mock
        Agent agent = Agent.builder().code(agentCode).build();
        Account account = Account.builder().corpid(agent.getCorpid()).build();

        agent.setAccount(account);
        WxCpServiceInfo wxCpServiceInfo = new WxCpServiceInfo();
        BeanUtils.copyProperties(agent, wxCpServiceInfo);
        wxCpServiceInfo.setWxCpService(buildWxCpService(agent));
        return Optional.of(wxCpServiceInfo);
    }

    private WxCpService buildWxCpService(Agent agent) {
        WxCpService wxCpService = new WxCpServiceImpl();
        // 注意：微信accessToken的缓存和避免多节点相互覆盖的问题
        var config = new WxCpDefaultConfigImpl();
        config.setCorpId(agent.getCorpid());
        config.setAgentId(agent.getAgentid());
        config.setCorpSecret(agent.getSecret());
        config.setToken(agent.getSignToken());
        config.setAesKey(agent.getAesKey());
        wxCpService.setWxCpConfigStorage(config);
        return wxCpService;
    }
}
