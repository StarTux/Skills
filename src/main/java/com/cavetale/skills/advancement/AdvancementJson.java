package com.cavetale.skills.advancement;

import lombok.Data;

@Data
final class AdvancementJson {
    protected DisplayJson display = new DisplayJson();
    protected CriteriaJson criteria = new CriteriaJson();
    protected String parent;
}
