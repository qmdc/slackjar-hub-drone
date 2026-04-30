package com.slack.slackjarservice.foundation.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HitokotoResponse {
    private String hitokoto;
    private String from;
}
