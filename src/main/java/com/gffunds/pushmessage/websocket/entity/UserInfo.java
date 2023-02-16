package com.gffunds.pushmessage.websocket.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author hezhc
 * @date 2023/2/14 15:56
 * @description 用户信息
 */
@Data
@Accessors(chain = true)
public class UserInfo {
    /** OA ID */
    private String userId;
    private String username;
    /** 用户昵称 */
    private String nickname;
    private String empid;
    /** sessionId */
    private String sessionId;
    /** 岗位编码 */
    private String posNum;
    /** 岗位名称 */
    private String posName;
    /** 组织编码 */
    private String orgNum;
    /** 组织名称 */
    private String orgName;
    /** 是否管理员 */
    private boolean admin;
    /** session超时时间 (minute)*/
    private Integer sessionExpireTime;
    /** 密码 */
    private String password;
    /** 移动电话 */
    private String mobile;
    /** 传真号码 */
    private String faxno;
    /** 邮箱 */
    private String email;
}

