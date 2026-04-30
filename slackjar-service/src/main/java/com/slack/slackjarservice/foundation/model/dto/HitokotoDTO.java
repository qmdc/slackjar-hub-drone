package com.slack.slackjarservice.foundation.model.dto;

import lombok.Data;

/**
 * @author zhn
 */
@Data
public class HitokotoDTO {

    private String id;
    private String uuid;
    private String hitokoto;
    private String type;
    private String from;
    private String from_who;
    private String creator;
    private Integer creator_uid;
    private Integer reviewer;
    private String commit_from;
    private String created_at;
    private Integer length;

}
